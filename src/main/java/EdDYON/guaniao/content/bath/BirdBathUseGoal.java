package EdDYON.guaniao.content.bath;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class BirdBathUseGoal extends Goal {
    private static final int CLAIM_TICKS = 100;
    private static final int CONSUME_WARMUP_TICKS = 8;
    private final PathfinderMob bird;
    private final double speedModifier;
    private final double searchRadius;
    private final int scanChance;
    private final Predicate<BirdBathBlockEntity> bathPredicate;
    private final BooleanSupplier canStart;
    private final Consumer<BirdBathBlockEntity> onApproach;
    private final BiConsumer<BirdBathBlockEntity, BirdBathContentType> onConsume;
    private final BiConsumer<BirdBathBlockEntity, Boolean> onStop;
    private BirdBathBlockEntity targetBath;
    private int repathTicks;
    private int topUseTicks;
    private int totalTicks;
    private boolean consumed;

    public BirdBathUseGoal(PathfinderMob bird, double speedModifier, double searchRadius, int scanChance,
                           Predicate<BirdBathBlockEntity> bathPredicate, BooleanSupplier canStart,
                           Consumer<BirdBathBlockEntity> onApproach,
                           BiConsumer<BirdBathBlockEntity, BirdBathContentType> onConsume,
                           BiConsumer<BirdBathBlockEntity, Boolean> onStop) {
        this.bird = bird;
        this.speedModifier = speedModifier;
        this.searchRadius = searchRadius;
        this.scanChance = Math.max(1, scanChance);
        this.bathPredicate = bathPredicate;
        this.canStart = canStart;
        this.onApproach = onApproach;
        this.onConsume = onConsume;
        this.onStop = onStop;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.bird.level().isClientSide || !this.canStart.getAsBoolean()) {
            return false;
        }
        if (this.bird.getRandom().nextInt(this.scanChance) != 0) {
            return false;
        }
        Optional<BirdBathBlockEntity> found = BirdBathAttraction.findNearbyUsableBath(
                this.bird.level(),
                this.bird.blockPosition(),
                this.searchRadius,
                this::canUseBath
        );
        if (found.isEmpty()) {
            return false;
        }
        BirdBathBlockEntity bath = found.get();
        if (!BirdBathAttraction.tryClaimUse(bath, this.bird, CLAIM_TICKS)) {
            return false;
        }
        this.targetBath = bath;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.consumed
                && this.totalTicks < 220
                && this.targetBath != null
                && this.canUseBath(this.targetBath)
                && (this.targetBath.isOccupiedBy(this.bird.getUUID()) || BirdBathAttraction.tryClaimUse(this.targetBath, this.bird, CLAIM_TICKS));
    }

    @Override
    public void start() {
        this.consumed = false;
        this.repathTicks = 0;
        this.topUseTicks = 0;
        this.totalTicks = 0;
        if (this.targetBath != null) {
            this.onApproach.accept(this.targetBath);
            this.moveToBathEdge();
        }
    }

    @Override
    public void tick() {
        if (this.targetBath == null) {
            return;
        }
        ++this.totalTicks;
        Vec3 usePosition = BirdBathAttraction.topUsePosition(this.targetBath);
        this.bird.getLookControl().setLookAt(usePosition.x, usePosition.y, usePosition.z, 30.0F, 30.0F);
        if (this.isAtTopUsePosition(usePosition)) {
            this.bird.getNavigation().stop();
            ++this.topUseTicks;
            if (this.topUseTicks >= CONSUME_WARMUP_TICKS) {
                this.consumeFromBath();
            }
            return;
        }
        this.topUseTicks = 0;
        if (--this.repathTicks <= 0 || this.bird.getNavigation().isDone()) {
            this.moveToBathEdge();
        }
        if (this.totalTicks % 35 == 0) {
            BirdBathAttraction.tryClaimUse(this.targetBath, this.bird, CLAIM_TICKS);
        }
    }

    @Override
    public void stop() {
        BirdBathBlockEntity bath = this.targetBath;
        if (bath != null) {
            UUID uuid = this.bird.getUUID();
            bath.releaseUse(uuid);
            this.onStop.accept(bath, this.consumed);
        }
        this.targetBath = null;
        this.topUseTicks = 0;
        this.totalTicks = 0;
        this.consumed = false;
    }

    private boolean canUseBath(BirdBathBlockEntity bath) {
        if (bath == null || bath.isRemoved() || !this.bird.isAlive() || bath.getLevel() != this.bird.level()) {
            return false;
        }
        UUID uuid = this.bird.getUUID();
        return this.bathPredicate.test(bath) && (!bath.isOccupied() || bath.isOccupiedBy(uuid));
    }

    private void moveToBathEdge() {
        if (this.targetBath == null) {
            return;
        }
        Vec3 approach = BirdBathAttraction.edgeApproachPosition(this.targetBath, this.bird.position());
        this.bird.getNavigation().moveTo(approach.x, approach.y, approach.z, this.speedModifier);
        this.repathTicks = 18 + this.bird.getRandom().nextInt(16);
    }

    private boolean isAtTopUsePosition(Vec3 usePosition) {
        Vec3 head = this.bird.getEyePosition();
        double horizontalDistanceSqr = head.subtract(usePosition).multiply(1.0D, 0.0D, 1.0D).lengthSqr();
        double verticalDistance = Math.abs(head.y - usePosition.y);
        return horizontalDistanceSqr <= 3.1D && verticalDistance <= 1.9D;
    }

    private void consumeFromBath() {
        if (this.targetBath == null || this.consumed) {
            return;
        }
        BirdBathContentType consumedType = this.targetBath.getContentType();
        if (BirdBathAttraction.consumeServingForBird(this.targetBath)) {
            this.consumed = true;
            this.onConsume.accept(this.targetBath, consumedType);
        }
    }
}
