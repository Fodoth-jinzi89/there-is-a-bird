package keletu.guaniao.content.bird.sparrow;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import keletu.guaniao.content.feed.BreadcrumbPileBlock;
import net.minecraft.core.Direction;
import keletu.guaniao.registry.GuaniaoEntityTypes;
import keletu.guaniao.registry.GuaniaoBlocks;
import keletu.guaniao.registry.GuaniaoItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SparrowEntity extends TamableAnimal implements GeoEntity {
    static final Ingredient TAMING_ITEMS = Ingredient.of((ItemLike[])new ItemLike[]{
            Items.WHEAT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.PITCHER_POD
    });
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation TAIL_ANIMATION = RawAnimation.begin().thenPlay("animation.idle_diff_1").thenLoop("animation.idle");
    private static final RawAnimation PECK_ANIMATION = RawAnimation.begin().thenPlay("animation.idle_diff_2").thenLoop("animation.idle");
    private static final RawAnimation LOOK_AROUND_ANIMATION = RawAnimation.begin().thenPlay("animation.idle_diff_3").thenLoop("animation.idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("animation.walk");
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("animation.fly");
    private static final double WALKING_SPEED_THRESHOLD = 0.0018;
    private static final double SHORT_FLIGHT_SPEED = 0.24;
    private static final double ESCAPE_FLIGHT_SPEED = 0.42;
    private static final float FLIGHT_YAW_TURN_RATE = 22.0f;
    private static final float FLIGHT_PITCH_TURN_RATE = 10.0f;
    private static final int MAX_FAMILIAR_TICKS = 7200;
    private static final int ATTACK_DISTRUST_TICKS = 48000;
    private static final int FULL_SATIATION_TICKS = 2400;
    private static final int MAX_SATIATION_TICKS = 4800;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private IdleAnimationChoice currentIdleAnimation = IdleAnimationChoice.BASE;
    private long nextIdleAnimationSwapTick;
    private int forcedIdleAnimationTicks;
    private int familiarTicks;
    private int calmAroundPlayerTicks;
    private int satiatedTicks;
    private int flightTicks;
    private int flightDuration;
    private int flightCooldown;
    private int blockedFlightTicks;
    private Vec3 flightTarget;
    private double flightSpeed = SHORT_FLIGHT_SPEED;
    private boolean escapeFlight;
    private UUID distrustedPlayer;
    private int distrustTicks;
    private Vec3 pendingScareSource;
    private int pendingScareTicks;
    private BlockPos noticedBreadcrumbPos;
    private int breadcrumbInterestTicks;
    private GuidePreviewAnimation guidePreviewAnimation = GuidePreviewAnimation.NONE;

    public SparrowEntity(EntityType<? extends SparrowEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 16.0f);
        this.satiatedTicks = this.getRandom().nextFloat() < 0.62f ? 0 : 300 + this.getRandom().nextInt(1800);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, SparrowDefinition.MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, SparrowDefinition.WALK_SPEED)
                .add(Attributes.FOLLOW_RANGE, SparrowDefinition.FOLLOW_RANGE);
    }

    public static boolean canSpawn(EntityType<SparrowEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON) || below.is(Blocks.FARMLAND) || below.is(BlockTags.DIRT);
        return validGround && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new SparrowFleePlayerGoal(this));
        this.goalSelector.addGoal(2, (Goal)new FollowOwnerGoal(this, 1.05, 2.0f, 8.0f, true));
        this.goalSelector.addGoal(3, (Goal)new TemptGoal(this, 0.9, TAMING_ITEMS, false));
        this.goalSelector.addGoal(4, (Goal)new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(5, (Goal)new SparrowEatBreadcrumbGoal(this));
        this.goalSelector.addGoal(6, (Goal)new SparrowFlockGoal(this));
        this.goalSelector.addGoal(7, (Goal)new RandomStrollGoal(this, 0.72));
        this.goalSelector.addGoal(8, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, (Goal)new RandomLookAroundGoal((Mob)this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.familiarTicks > 0) {
            --this.familiarTicks;
        }
        if (this.calmAroundPlayerTicks > 0) {
            --this.calmAroundPlayerTicks;
        }
        if (this.satiatedTicks > 0) {
            --this.satiatedTicks;
        }
        if (this.breadcrumbInterestTicks > 0) {
            --this.breadcrumbInterestTicks;
            if (this.breadcrumbInterestTicks <= 0) {
                this.noticedBreadcrumbPos = null;
            }
        }
        if (this.distrustTicks > 0) {
            --this.distrustTicks;
            if (this.distrustTicks <= 0) {
                this.distrustedPlayer = null;
            }
        }
        if (this.flightCooldown > 0) {
            --this.flightCooldown;
        }
        if (this.forcedIdleAnimationTicks > 0) {
            --this.forcedIdleAnimationTicks;
        }
        if (!this.level().isClientSide) {
            this.tickStaleFlightRecovery();
        }
        if (!this.level().isClientSide && this.pendingScareTicks > 0) {
            --this.pendingScareTicks;
            if (this.pendingScareTicks <= 0) {
                this.releasePendingScare();
            }
        }
        if (!this.level().isClientSide && this.isControlledFlightActive()) {
            this.tickControlledFlight();
        }
        if (!this.level().isClientSide && this.onGround() && !this.isInWaterOrBubble()) {
            if (this.getNavigation().isDone() && this.getRandom().nextInt(160) == 0) {
                this.triggerPeck();
            }
            int ambientFlightChance = this.level().isDay() ? (this.isTame() ? 260 : 150) : 520;
            if (!this.isControlledFlightActive() && this.getNavigation().isDone() && this.flightCooldown <= 0 && this.getRandom().nextInt(ambientFlightChance) == 0) {
                this.startAmbientShortFlight();
            }
            if (!this.isControlledFlightActive() && !this.isTame() && this.getRandom().nextInt(700) == 0) {
                this.shortHop();
            }
        }
        if (!this.onGround() || this.isControlledFlightActive()) {
            this.fallDistance = 0.0f;
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (TAMING_ITEMS.test(stack)) {
            if (this.isDistrusted(player)) {
                if (!this.level().isClientSide) {
                    this.startEscapeFlight(player.position());
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            this.familiarTicks = Math.max(this.familiarTicks, 3600);
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!this.isTame() && this.getRandom().nextInt(3) == 0) {
                    this.tame(player);
                    this.getNavigation().stop();
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt) {
            this.familiarTicks = 0;
            this.calmAroundPlayerTicks = 0;
            Entity attacker = damageSource.getEntity();
            if (attacker != null) {
                if (attacker instanceof Player player) {
                    this.rememberDistrustedPlayer(player);
                }
                this.startEscapeFlight(attacker.position());
                this.alertNearbySparrows(attacker);
            }
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("NoGravity", false);
        if (this.distrustedPlayer != null && this.distrustTicks > 0) {
            compoundTag.putUUID("DistrustedPlayer", this.distrustedPlayer);
            compoundTag.putInt("DistrustTicks", this.distrustTicks);
        }
        compoundTag.putInt("SatiatedTicks", this.satiatedTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.hasUUID("DistrustedPlayer")) {
            this.distrustedPlayer = compoundTag.getUUID("DistrustedPlayer");
            this.distrustTicks = compoundTag.getInt("DistrustTicks");
            if (this.distrustTicks <= 0) {
                this.distrustedPlayer = null;
            }
        }
        this.satiatedTicks = Math.max(0, compoundTag.getInt("SatiatedTicks"));
        this.clearSerializedFlightState();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return TAMING_ITEMS.test(stack);
    }

    @Override
    public SparrowEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return GuaniaoEntityTypes.SPARROW.get().create(level);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        BlockState below = level.getBlockState(pos.below());
        float score = super.getWalkTargetValue(pos, level);
        if (below.is(Blocks.FARMLAND)) {
            score += 5.0f;
        }
        if (below.is(BlockTags.DIRT) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
            score += 2.0f;
        }
        return score;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.08f, 1.55f);
    }

    @Override
    public int getAmbientSoundInterval() {
        return this.level().isDay() ? 120 : 260;
    }

    @Override
    public float getSoundVolume() {
        return 0.38f;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    public void setGuidePreviewAnimation(GuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null ? GuidePreviewAnimation.NONE : guidePreviewAnimation;
    }

    public GuidePreviewAnimation getGuidePreviewAnimation() {
        return this.guidePreviewAnimation;
    }

    private void triggerPeck() {
        this.currentIdleAnimation = IdleAnimationChoice.PECK;
        this.forcedIdleAnimationTicks = 34;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.forcedIdleAnimationTicks;
    }

    private void triggerLookAround() {
        this.currentIdleAnimation = IdleAnimationChoice.LOOK_AROUND;
        this.forcedIdleAnimationTicks = 42;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.forcedIdleAnimationTicks;
    }

    private void shortHop() {
        float yaw = this.getYRot() * ((float)Math.PI / 180.0f);
        double side = (this.getRandom().nextDouble() - 0.5) * 0.12;
        this.setDeltaMovement(-Math.sin(yaw) * 0.13 + side, 0.23, Math.cos(yaw) * 0.13 - side);
        this.hasImpulse = true;
    }

    private boolean isControlledFlightActive() {
        return this.flightTarget != null && this.flightTicks > 0;
    }

    private void clearSerializedFlightState() {
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.blockedFlightTicks = 0;
        this.escapeFlight = false;
        if (this.isNoGravity()) {
            this.setNoGravity(false);
        }
        if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
            this.setDeltaMovement(0.0, -0.08, 0.0);
            this.hasImpulse = true;
            this.fallDistance = 0.0f;
        }
    }

    private void tickStaleFlightRecovery() {
        if (!this.isControlledFlightActive() && this.isNoGravity()) {
            this.setNoGravity(false);
            if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
                this.setDeltaMovement(0.0, -0.08, 0.0);
                this.hasImpulse = true;
            }
        }
    }

    private boolean startAmbientShortFlight() {
        Vec3 target = this.findShortFlightTarget(null, false, 6, 16);
        if (target == null) {
            return false;
        }
        return this.startControlledFlight(target, this.randomBetween(34, 64), SHORT_FLIGHT_SPEED + this.getRandom().nextDouble() * 0.06, false);
    }

    private boolean startEscapeFlight(Vec3 threatPosition) {
        Vec3 target = this.findShortFlightTarget(threatPosition, true, 12, 22);
        if (target == null) {
            Vec3 away = this.position().subtract(threatPosition).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() <= 1.0E-4) {
                double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
                away = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            }
            away = away.normalize();
            target = this.position().add(away.scale(12.0 + this.getRandom().nextDouble() * 7.0)).add(0.0, 3.4, 0.0);
        }
        return this.startControlledFlight(target, this.randomBetween(48, 86), ESCAPE_FLIGHT_SPEED, true);
    }

    private boolean startControlledFlight(Vec3 target, int duration, double speed, boolean escapeFlight) {
        if (this.isInWaterOrBubble()) {
            return false;
        }
        this.pendingScareSource = null;
        this.pendingScareTicks = 0;
        this.flightTarget = target;
        this.flightTicks = duration;
        this.flightDuration = duration;
        this.flightSpeed = speed;
        this.escapeFlight = escapeFlight;
        this.blockedFlightTicks = 0;
        this.flightCooldown = escapeFlight ? 26 : 48 + this.getRandom().nextInt(70);
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setOnGround(false);
        Vec3 direction = target.subtract(this.position()).multiply(1.0, 0.0, 1.0);
        if (direction.lengthSqr() <= 1.0E-4) {
            direction = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        direction = direction.normalize();
        this.setDeltaMovement(direction.scale(speed * 0.75).add(0.0, escapeFlight ? 0.48 : 0.28, 0.0));
        this.faceMovement(this.getDeltaMovement());
        this.hasImpulse = true;
        return true;
    }

    private void tickControlledFlight() {
        if (this.flightTarget == null || this.flightTicks <= 0 || this.isInWaterOrBubble()) {
            this.finishControlledFlight(false);
            return;
        }
        this.getNavigation().stop();
        this.setNoGravity(true);
        --this.flightTicks;
        Vec3 toTarget = this.flightTarget.subtract(this.position());
        double distance = toTarget.length();
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        int flightAge = this.flightDuration - this.flightTicks;
        if (distance < 0.55 || (this.onGround() && flightAge > 8 && horizontalDistance < 1.1)) {
            this.finishControlledFlight(true);
            return;
        }
        Vec3 horizontalDirection = new Vec3(toTarget.x, 0.0, toTarget.z);
        if (horizontalDirection.lengthSqr() <= 1.0E-4) {
            horizontalDirection = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
        }
        if (horizontalDirection.lengthSqr() <= 1.0E-4) {
            horizontalDirection = this.getLookAngle().multiply(1.0, 0.0, 1.0);
        }
        horizontalDirection = horizontalDirection.normalize();
        double speed = horizontalDistance < 2.0 ? this.flightSpeed * 0.58 : this.flightSpeed;
        double lift = Mth.clamp(toTarget.y * 0.16, -0.11, 0.16);
        if (flightAge < 8) {
            lift += this.escapeFlight ? 0.24 : 0.11;
        }
        if (horizontalDistance < 1.6) {
            lift = Mth.clamp(toTarget.y * 0.22 - 0.05, -0.14, 0.06);
        }
        Vec3 desired = horizontalDirection.scale(speed).add(0.0, lift, 0.0);
        Vec3 movement = this.getDeltaMovement().scale(0.32).add(desired.scale(0.68));
        if (this.horizontalCollision || (this.verticalCollision && flightAge > 6)) {
            ++this.blockedFlightTicks;
            movement = movement.add(0.0, 0.08, 0.0);
        } else {
            this.blockedFlightTicks = Math.max(0, this.blockedFlightTicks - 1);
        }
        if (this.blockedFlightTicks > 5) {
            Vec3 newTarget = this.findShortFlightTarget(null, false, 3, 8);
            if (newTarget == null) {
                this.finishControlledFlight(false);
                return;
            }
            this.flightTarget = newTarget;
            this.blockedFlightTicks = 0;
        }
        if (this.flightTicks <= 0) {
            this.finishControlledFlight(false);
            return;
        }
        this.setDeltaMovement(movement);
        this.faceMovement(movement);
        this.fallDistance = 0.0f;
        this.hasImpulse = true;
    }

    private void finishControlledFlight(boolean landed) {
        this.flightTarget = null;
        this.flightTicks = 0;
        this.flightDuration = 0;
        this.blockedFlightTicks = 0;
        this.escapeFlight = false;
        this.setNoGravity(false);
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.35, landed ? 0.0 : -0.06, movement.z * 0.35);
        this.flightCooldown = Math.max(this.flightCooldown, 70 + this.getRandom().nextInt(120));
    }

    private Vec3 findShortFlightTarget(Vec3 threatPosition, boolean escape, int minRadius, int maxRadius) {
        BlockPos origin = this.blockPosition();
        Vec3 bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int attempt = 0; attempt < 28; ++attempt) {
            double angle;
            if (escape && threatPosition != null) {
                Vec3 away = this.position().subtract(threatPosition).multiply(1.0, 0.0, 1.0);
                angle = away.lengthSqr() > 1.0E-4 ? Math.atan2(away.z, away.x) + this.randomSigned(0.75) : this.getRandom().nextDouble() * Math.PI * 2.0;
            } else {
                angle = this.getRandom().nextDouble() * Math.PI * 2.0;
            }
            double radius = minRadius + this.getRandom().nextDouble() * (double)(maxRadius - minRadius);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * radius);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * radius);
            int y = origin.getY() + (escape ? this.randomBetween(2, 8) : this.randomBetween(-1, 4));
            BlockPos landing = this.findLandingSurface(new BlockPos(x, y, z), escape ? 9 : 6);
            if (landing == null) {
                continue;
            }
            double score = this.scoreShortFlightLanding(landing, threatPosition, escape);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new Vec3((double)landing.getX() + 0.5, (double)landing.getY() + 0.05, (double)landing.getZ() + 0.5);
            }
        }
        return bestTarget;
    }

    private BlockPos findLandingSurface(BlockPos center, int verticalRange) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int yOffset = verticalRange; yOffset >= -verticalRange; --yOffset) {
            mutable.set(center.getX(), center.getY() + yOffset, center.getZ());
            if (this.isSafeShortFlightLanding(mutable)) {
                return mutable.immutable();
            }
        }
        return null;
    }

    private boolean isSafeShortFlightLanding(BlockPos pos) {
        if (!this.canReadChunk(pos)) {
            return false;
        }
        BlockState below = this.level().getBlockState(pos.below());
        BlockState feet = this.level().getBlockState(pos);
        BlockState head = this.level().getBlockState(pos.above());
        if (!feet.getCollisionShape((BlockGetter)this.level(), pos).isEmpty() || !head.getCollisionShape((BlockGetter)this.level(), pos.above()).isEmpty()) {
            return false;
        }
        if (this.level().getFluidState(pos).is(FluidTags.WATER) || this.level().getFluidState(pos).is(FluidTags.LAVA)) {
            return false;
        }
        if (below.is(Blocks.CACTUS) || below.is(Blocks.MAGMA_BLOCK) || below.isAir()) {
            return false;
        }
        return below.isFaceSturdy((BlockGetter)this.level(), pos.below(), Direction.UP)
                || below.is(Blocks.FARMLAND)
                || below.is(BlockTags.LEAVES)
                || below.is(BlockTags.LOGS);
    }

    private double scoreShortFlightLanding(BlockPos pos, Vec3 threatPosition, boolean escape) {
        BlockState below = this.level().getBlockState(pos.below());
        double score = 0.0;
        if (below.is(Blocks.FARMLAND)) {
            score += 6.0;
        }
        if (below.is(BlockTags.LEAVES) || below.is(BlockTags.LOGS)) {
            score += 11.0;
        }
        if (below.is(BlockTags.ANIMALS_SPAWNABLE_ON) || below.is(BlockTags.DIRT)) {
            score += 2.5;
        }
        score -= Math.abs((double)pos.getY() - this.getY()) * 0.35;
        if (escape && threatPosition != null) {
            score += Math.min(16.0, new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).distanceTo(threatPosition) * 0.6);
        }
        return score;
    }

    private boolean canReadChunk(BlockPos pos) {
        return this.level().hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private void faceMovement(Vec3 movement) {
        double horizontalLength = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        if (horizontalLength <= 1.0E-4) {
            return;
        }
        float targetYaw = (float)(Math.atan2(movement.z, movement.x) * 57.2957763671875) - 90.0f;
        float yaw = Mth.approachDegrees(this.getYRot(), targetYaw, FLIGHT_YAW_TURN_RATE);
        float targetPitch = (float)(-(Math.atan2(movement.y, horizontalLength) * 57.2957763671875));
        float pitch = Mth.clamp(Mth.approachDegrees(this.getXRot(), targetPitch, FLIGHT_PITCH_TURN_RATE), -35.0f, 35.0f);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRot = yaw;
        this.yHeadRotO = yaw;
        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    private int randomBetween(int min, int max) {
        return min + this.getRandom().nextInt(max - min + 1);
    }

    private double randomSigned(double range) {
        return (this.getRandom().nextDouble() * 2.0 - 1.0) * range;
    }

    private boolean isComfortableNear(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }
        if (this.isDistrusted(player)) {
            return false;
        }
        if (this.isTame()) {
            return true;
        }
        if (TAMING_ITEMS.test(player.getMainHandItem()) || TAMING_ITEMS.test(player.getOffhandItem())) {
            return true;
        }
        if (player.getMainHandItem().is(GuaniaoItems.BREADCRUMBS.get()) || player.getOffhandItem().is(GuaniaoItems.BREADCRUMBS.get())) {
            return true;
        }
        if (this.calmAroundPlayerTicks > 0) {
            return true;
        }
        float calmChance = 0.7f + Mth.clamp((float)this.familiarTicks / 3600.0f, 0.0f, 0.3f);
        if (this.getRandom().nextFloat() < calmChance) {
            this.calmAroundPlayerTicks = 80 + this.getRandom().nextInt(100);
            return true;
        }
        return false;
    }

    private boolean shouldAvoidBreadcrumbs() {
        if (this.pendingScareTicks > 0 || this.isControlledFlightActive()) {
            return true;
        }
        Player player = this.level().getNearestPlayer(this, this.hasDistrustMemory() ? 12.0 : 7.0);
        return player != null && !this.isComfortableNear(player);
    }

    private void gainBreadcrumbConfidence() {
        this.familiarTicks = Math.min(MAX_FAMILIAR_TICKS, this.familiarTicks + 160);
        this.calmAroundPlayerTicks = Math.max(this.calmAroundPlayerTicks, 160);
    }

    private boolean isHungry() {
        return this.satiatedTicks <= 0;
    }

    private void restoreBreadcrumbSatiation() {
        this.satiatedTicks = Math.min(MAX_SATIATION_TICKS, this.satiatedTicks + FULL_SATIATION_TICKS);
    }

    private boolean hasBreadcrumbInterest() {
        return this.breadcrumbInterestTicks > 0 && this.noticedBreadcrumbPos != null;
    }

    private void noticeBreadcrumbs(BlockPos pos, Player player) {
        if (player != null && this.isDistrusted(player)) {
            return;
        }
        this.noticedBreadcrumbPos = pos.immutable();
        this.breadcrumbInterestTicks = Math.max(this.breadcrumbInterestTicks, 160 + this.getRandom().nextInt(120));
        this.satiatedTicks = Math.min(this.satiatedTicks, 700);
        this.calmAroundPlayerTicks = Math.max(this.calmAroundPlayerTicks, 60);
    }

    public static void alertNearbyBreadcrumbs(ServerLevel level, BlockPos pos, Player player) {
        Vec3 center = Vec3.atCenterOf(pos);
        List<SparrowEntity> flock = level.getEntitiesOfClass(SparrowEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(20.0));
        for (SparrowEntity sparrow : flock) {
            if (sparrow.isRemoved() || sparrow.isInWaterOrBubble()) {
                continue;
            }
            double distance = sparrow.position().distanceTo(center);
            float chance = distance <= 6.0 ? 1.0f : distance <= 12.0 ? 0.82f : 0.58f;
            if (sparrow.getRandom().nextFloat() > chance) {
                continue;
            }
            sparrow.noticeBreadcrumbs(pos, player);
        }
    }

    private BlockPos findNearbyBreadcrumbs(int horizontalRadius, int verticalRadius) {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int xOffset = -horizontalRadius; xOffset <= horizontalRadius; ++xOffset) {
            for (int zOffset = -horizontalRadius; zOffset <= horizontalRadius; ++zOffset) {
                for (int yOffset = -verticalRadius; yOffset <= verticalRadius; ++yOffset) {
                    BlockPos candidate = origin.offset(xOffset, yOffset, zOffset);
                    if (!this.canReadChunk(candidate)) {
                        continue;
                    }
                    BlockState state = this.level().getBlockState(candidate);
                    if (!state.is(GuaniaoBlocks.BREADCRUMBS.get())) {
                        continue;
                    }
                    double distanceSqr = this.position().distanceToSqr(Vec3.atCenterOf(candidate));
                    int layers = state.getValue(BreadcrumbPileBlock.LAYERS);
                    double crowdedPenalty = this.level().getEntitiesOfClass(SparrowEntity.class, new net.minecraft.world.phys.AABB(candidate).inflate(1.15), other -> other != this && other.onGround()).size() * 1.35;
                    double score = (double)(layers * 6) - distanceSqr * 0.14 - crowdedPenalty;
                    if (bestPos == null || score > bestScore) {
                        bestPos = candidate.immutable();
                        bestScore = score;
                    }
                }
            }
        }
        return bestPos;
    }

    private Vec3 breadcrumbStandPosition(BlockPos pilePos) {
        double angle = ((double)(this.getId() & 7) / 8.0) * Math.PI * 2.0;
        double radius = 0.45 + (double)(this.getId() % 3) * 0.08;
        return new Vec3((double)pilePos.getX() + 0.5 + Math.cos(angle) * radius, (double)pilePos.getY(), (double)pilePos.getZ() + 0.5 + Math.sin(angle) * radius);
    }

    private boolean isDistrusted(Player player) {
        return this.distrustedPlayer != null && this.distrustTicks > 0 && this.distrustedPlayer.equals(player.getUUID());
    }

    private boolean hasDistrustMemory() {
        return this.distrustedPlayer != null && this.distrustTicks > 0;
    }

    private void rememberDistrustedPlayer(Player player) {
        this.distrustedPlayer = player.getUUID();
        this.distrustTicks = ATTACK_DISTRUST_TICKS;
        this.familiarTicks = 0;
        this.calmAroundPlayerTicks = 0;
    }

    private void queueScareReaction(Vec3 sourcePosition, int delayTicks) {
        if (delayTicks <= 0) {
            this.pendingScareSource = sourcePosition;
            this.pendingScareTicks = 1;
            return;
        }
        if (this.pendingScareTicks == 0 || delayTicks < this.pendingScareTicks) {
            this.pendingScareSource = sourcePosition;
            this.pendingScareTicks = delayTicks;
        }
    }

    private void releasePendingScare() {
        Vec3 sourcePosition = this.pendingScareSource;
        this.pendingScareSource = null;
        this.pendingScareTicks = 0;
        if (sourcePosition == null || this.isRemoved() || this.isTame()) {
            return;
        }
        this.getNavigation().stop();
        if (!this.startEscapeFlight(sourcePosition)) {
            Vec3 away = DefaultRandomPos.getPosAway(this, 12, 7, sourcePosition);
            if (away != null) {
                this.getNavigation().moveTo(away.x, away.y, away.z, 1.2);
                if (this.onGround() && this.getRandom().nextBoolean()) {
                    this.shortHop();
                }
            }
        }
    }

    private void alertNearbySparrows(Entity source) {
        Vec3 sourcePosition = source.position();
        Player attacker = source instanceof Player player ? player : null;
        List<SparrowEntity> flock = this.level().getEntitiesOfClass(SparrowEntity.class, this.getBoundingBox().inflate(18.0));
        for (SparrowEntity sparrow : flock) {
            if (sparrow != this && !sparrow.isTame()) {
                sparrow.familiarTicks = 0;
                sparrow.calmAroundPlayerTicks = 0;
                if (attacker != null) {
                    sparrow.rememberDistrustedPlayer(attacker);
                }
                sparrow.getNavigation().stop();
                double distance = sparrow.position().distanceTo(sourcePosition);
                int delay = 2 + Mth.floor(distance * 0.45);
                delay += sparrow.getRandom().nextInt(5);
                if (distance < 3.0) {
                    delay = 1 + sparrow.getRandom().nextInt(3);
                }
                sparrow.queueScareReaction(sourcePosition, Mth.clamp(delay, 1, 16));
            }
        }
    }

    private RawAnimation pickIdleAnimation() {
        if (this.forcedIdleAnimationTicks > 0) {
            return this.currentIdleAnimation.animation;
        }
        if (this.level().getGameTime() >= this.nextIdleAnimationSwapTick) {
            this.currentIdleAnimation = switch (this.getRandom().nextInt(5)) {
                case 0, 1 -> IdleAnimationChoice.BASE;
                case 2 -> IdleAnimationChoice.TAIL;
                case 3 -> IdleAnimationChoice.LOOK_AROUND;
                default -> IdleAnimationChoice.PECK;
            };
            this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.currentIdleAnimation.nextDuration(this.getRandom());
        }
        return this.currentIdleAnimation.animation;
    }

    private <T extends SparrowEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }
        if (this.isControlledFlightActive() || !this.onGround() || this.getDeltaMovement().y > 0.08) {
            return animationState.setAndContinue(FLY_ANIMATION);
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > WALKING_SPEED_THRESHOLD || !this.getNavigation().isDone()) {
            return animationState.setAndContinue(WALK_ANIMATION);
        }
        return animationState.setAndContinue(this.pickIdleAnimation());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "movement", 4, this::movementController)});
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private static final class SparrowFleePlayerGoal extends Goal {
        private final SparrowEntity sparrow;
        private Player player;
        private Vec3 fleeTarget;

        private SparrowFleePlayerGoal(SparrowEntity sparrow) {
            this.sparrow = sparrow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.sparrow.pendingScareTicks > 0) {
                return false;
            }
            this.player = this.sparrow.level().getNearestPlayer(this.sparrow, this.sparrow.hasDistrustMemory() ? 11.0 : 6.5);
            if (this.player == null || this.sparrow.isComfortableNear(this.player)) {
                return false;
            }
            this.fleeTarget = DefaultRandomPos.getPosAway(this.sparrow, this.sparrow.isDistrusted(this.player) ? 14 : 9, this.sparrow.isDistrusted(this.player) ? 7 : 5, this.player.position());
            return this.fleeTarget != null;
        }

        @Override
        public void start() {
            if (!this.sparrow.startEscapeFlight(this.player.position())) {
                this.sparrow.getNavigation().moveTo(this.fleeTarget.x, this.fleeTarget.y, this.fleeTarget.z, 1.18);
                this.sparrow.shortHop();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.sparrow.isControlledFlightActive() || !this.sparrow.getNavigation().isDone();
        }
    }

    private static final class SparrowFlockGoal extends Goal {
        private final SparrowEntity sparrow;
        private Vec3 target;

        private SparrowFlockGoal(SparrowEntity sparrow) {
            this.sparrow = sparrow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.sparrow.isTame() || this.sparrow.getRandom().nextInt(50) != 0) {
                return false;
            }
            List<SparrowEntity> flock = this.sparrow.level().getEntitiesOfClass(SparrowEntity.class, this.sparrow.getBoundingBox().inflate(SparrowDefinition.SOCIAL_RADIUS), other -> other != this.sparrow && !other.isTame());
            if (flock.isEmpty()) {
                return false;
            }
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            for (SparrowEntity other : flock) {
                x += other.getX();
                y += other.getY();
                z += other.getZ();
            }
            Vec3 center = new Vec3(x / (double)flock.size(), y / (double)flock.size(), z / (double)flock.size());
            if (this.sparrow.position().distanceToSqr(center) < 9.0) {
                return false;
            }
            this.target = center;
            return true;
        }

        @Override
        public void start() {
            this.sparrow.getNavigation().moveTo(this.target.x, this.target.y, this.target.z, 0.82);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.sparrow.getNavigation().isDone();
        }
    }

    private static final class SparrowEatBreadcrumbGoal extends Goal {
        private final SparrowEntity sparrow;
        private BlockPos pilePos;
        private Vec3 standPos;
        private int nextPeckTicks;

        private SparrowEatBreadcrumbGoal(SparrowEntity sparrow) {
            this.sparrow = sparrow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if ((!this.sparrow.isHungry() && !this.sparrow.hasBreadcrumbInterest()) || this.sparrow.shouldAvoidBreadcrumbs() || this.sparrow.isInWaterOrBubble()) {
                return false;
            }
            this.pilePos = null;
            if (this.sparrow.hasBreadcrumbInterest() && this.sparrow.noticedBreadcrumbPos != null
                    && this.sparrow.level().getBlockState(this.sparrow.noticedBreadcrumbPos).is(GuaniaoBlocks.BREADCRUMBS.get())) {
                this.pilePos = this.sparrow.noticedBreadcrumbPos.immutable();
            }
            if (this.pilePos == null) {
                this.pilePos = this.sparrow.findNearbyBreadcrumbs(this.sparrow.hasBreadcrumbInterest() ? 20 : 14, 3);
            }
            if (this.pilePos == null) {
                return false;
            }
            this.standPos = this.sparrow.breadcrumbStandPosition(this.pilePos);
            return true;
        }

        @Override
        public void start() {
            this.nextPeckTicks = this.sparrow.randomBetween(8, 14);
            this.moveTowardsPile();
        }

        @Override
        public void tick() {
            if (this.pilePos == null) {
                return;
            }
            BlockState state = this.sparrow.level().getBlockState(this.pilePos);
            if (!state.is(GuaniaoBlocks.BREADCRUMBS.get())) {
                this.stop();
                return;
            }
            if (this.sparrow.shouldAvoidBreadcrumbs()) {
                this.stop();
                return;
            }

            this.sparrow.getLookControl().setLookAt((double)this.pilePos.getX() + 0.5, (double)this.pilePos.getY() + 0.2, (double)this.pilePos.getZ() + 0.5, 20.0f, 20.0f);
            double distanceToPile = this.sparrow.position().distanceToSqr(Vec3.atCenterOf(this.pilePos));
            if (distanceToPile > 3.1) {
                this.moveTowardsPile();
                return;
            }

            this.sparrow.getNavigation().stop();
            if (this.sparrow.isControlledFlightActive()) {
                return;
            }

            if (--this.nextPeckTicks > 0) {
                return;
            }

            if (this.sparrow.getRandom().nextFloat() < 0.28f) {
                this.sparrow.triggerLookAround();
                this.nextPeckTicks = this.sparrow.randomBetween(12, 20);
                return;
            }

            this.sparrow.triggerPeck();
            this.sparrow.gainBreadcrumbConfidence();
            if (state.getBlock() instanceof BreadcrumbPileBlock breadcrumbPileBlock) {
                breadcrumbPileBlock.consumeOneServing(this.sparrow.level(), this.pilePos, state);
                this.sparrow.restoreBreadcrumbSatiation();
                if (!this.sparrow.level().getBlockState(this.pilePos).is(GuaniaoBlocks.BREADCRUMBS.get())) {
                    this.stop();
                    return;
                }
            }
            this.nextPeckTicks = this.sparrow.randomBetween(10, 16);
        }

        private void moveTowardsPile() {
            if (this.standPos == null) {
                this.standPos = this.sparrow.breadcrumbStandPosition(this.pilePos);
            }
            double distanceSqr = this.sparrow.position().distanceToSqr(this.standPos);
            if (distanceSqr > 20.0 && this.sparrow.onGround() && this.sparrow.flightCooldown <= 0 && this.sparrow.getRandom().nextFloat() < 0.72f) {
                this.sparrow.startControlledFlight(new Vec3(this.standPos.x, (double)this.pilePos.getY() + 0.05, this.standPos.z), this.sparrow.randomBetween(26, 42), SHORT_FLIGHT_SPEED + 0.02, false);
                return;
            }
            this.sparrow.getNavigation().moveTo(this.standPos.x, this.standPos.y, this.standPos.z, 0.98);
        }

        @Override
        public boolean canContinueToUse() {
            return this.pilePos != null
                    && this.sparrow.level().getBlockState(this.pilePos).is(GuaniaoBlocks.BREADCRUMBS.get())
                    && !this.sparrow.shouldAvoidBreadcrumbs();
        }

        @Override
        public void stop() {
            this.pilePos = null;
            this.standPos = null;
            this.nextPeckTicks = 0;
            if (this.sparrow.breadcrumbInterestTicks <= 0) {
                this.sparrow.noticedBreadcrumbPos = null;
            }
            this.sparrow.getNavigation().stop();
        }
    }

    private enum IdleAnimationChoice {
        BASE(IDLE_ANIMATION, 55, 115),
        TAIL(TAIL_ANIMATION, 48, 78),
        PECK(PECK_ANIMATION, 34, 58),
        LOOK_AROUND(LOOK_AROUND_ANIMATION, 58, 88);

        private final RawAnimation animation;
        private final int minDuration;
        private final int maxDuration;

        private IdleAnimationChoice(RawAnimation animation, int minDuration, int maxDuration) {
            this.animation = animation;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        private int nextDuration(RandomSource random) {
            return this.minDuration + random.nextInt(this.maxDuration - this.minDuration + 1);
        }
    }

    public enum GuidePreviewAnimation {
        NONE,
        IDLE,
        TAIL,
        PECK,
        LOOK_AROUND,
        WALK,
        FLY;

        private RawAnimation animation() {
            return switch (this) {
                case NONE -> null;
                case IDLE -> IDLE_ANIMATION;
                case TAIL -> TAIL_ANIMATION;
                case PECK -> PECK_ANIMATION;
                case LOOK_AROUND -> LOOK_AROUND_ANIMATION;
                case WALK -> WALK_ANIMATION;
                case FLY -> FLY_ANIMATION;
            };
        }
    }
}
