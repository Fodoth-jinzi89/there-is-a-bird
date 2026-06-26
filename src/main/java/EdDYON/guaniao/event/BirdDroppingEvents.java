package EdDYON.guaniao.event;

import EdDYON.guaniao.content.bird.budgerigar.BudgerigarEntity;
import EdDYON.guaniao.content.bird.columbid.AbstractColumbidEntity;
import EdDYON.guaniao.content.bird.crow.CrowEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.sparrow.SparrowEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingProjectileEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingPrankHandler;
import EdDYON.guaniao.content.dropping.BirdDroppingSplatEntity;
import EdDYON.guaniao.content.dropping.BirdDroppingVariant;
import EdDYON.guaniao.registry.GuaniaoEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "guaniao")
public final class BirdDroppingEvents {
    private static final String TAG_COOLDOWN = "GuaniaoDroppingCooldown";
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int RETRY_MIN_TICKS = 600;
    private static final int RETRY_MAX_TICKS = 1200;
    private static final int MIN_EXISTING_AGE_TICKS = 200;

    private BirdDroppingEvents() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !isBird(entity) || entity.tickCount < MIN_EXISTING_AGE_TICKS) {
            return;
        }
        if ((entity.tickCount + entity.getId()) % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        CompoundTag data = entity.getPersistentData();
        if (!data.contains(TAG_COOLDOWN)) {
            data.putInt(TAG_COOLDOWN, nextNaturalCooldown(entity));
            return;
        }

        int cooldown = Math.max(0, data.getInt(TAG_COOLDOWN) - CHECK_INTERVAL_TICKS);
        if (cooldown > 0) {
            data.putInt(TAG_COOLDOWN, cooldown);
            return;
        }

        if (trySpawnDropping(level, entity)) {
            data.putInt(TAG_COOLDOWN, nextNaturalCooldown(entity));
        } else {
            data.putInt(TAG_COOLDOWN, randomBetween(entity.getRandom(), RETRY_MIN_TICKS, RETRY_MAX_TICKS));
        }
    }

    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            BirdDroppingPrankHandler.tickVillagerTradePenalty(villager);
        }
    }

    private static boolean trySpawnDropping(ServerLevel level, LivingEntity bird) {
        if (bird.isDeadOrDying() || bird.isRemoved() || bird.isInWaterOrBubble() || !bird.isAlive()) {
            return false;
        }

        Vec3 spawnPosition = droppingSpawnPosition(bird);
        if (!BirdDroppingSplatEntity.canAddSplatAt(level, spawnPosition)) {
            return false;
        }

        BirdDroppingVariant variant = chooseVariantForBird(bird);
        BirdDroppingProjectileEntity dropping = new BirdDroppingProjectileEntity(level, bird, variant);
        dropping.markNaturalDropping(bird.getUUID());
        Vec3 birdMotion = bird.getDeltaMovement();
        RandomSource random = bird.getRandom();
        double horizontalX = Mth.nextDouble(random, -0.035D, 0.035D);
        double horizontalZ = Mth.nextDouble(random, -0.035D, 0.035D);
        double downward = Mth.nextDouble(random, -0.18D, -0.10D);
        Vec3 motion = new Vec3(
                birdMotion.x * 0.12D + horizontalX,
                Math.min(birdMotion.y * 0.08D, 0.04D) + downward,
                birdMotion.z * 0.12D + horizontalZ
        );

        dropping.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        dropping.setDeltaMovement(motion);
        return level.addFreshEntity(dropping);
    }

    private static Vec3 droppingSpawnPosition(LivingEntity bird) {
        Vec3 look = bird.getLookAngle();
        Vec3 rearOffset = new Vec3(-look.x, 0.0D, -look.z);
        if (rearOffset.lengthSqr() > 1.0E-4D) {
            rearOffset = rearOffset.normalize().scale(Math.max(0.04D, bird.getBbWidth() * 0.24D));
        }
        double y = bird.getY() + Math.max(0.08D, bird.getBbHeight() * 0.28D);
        return new Vec3(bird.getX(), y, bird.getZ()).add(rearOffset);
    }

    private static int nextNaturalCooldown(LivingEntity bird) {
        RandomSource random = bird.getRandom();
        EntityType<?> type = bird.getType();
        if (type == GuaniaoEntityTypes.PIGEON.get()) {
            return randomBetween(random, 4800, 9600);
        }
        if (type == GuaniaoEntityTypes.SPOTTED_DOVE.get()) {
            return randomBetween(random, 5600, 10800);
        }
        if (type == GuaniaoEntityTypes.CROW.get()) {
            return randomBetween(random, 6000, 12000);
        }
        if (type == GuaniaoEntityTypes.SPARROW.get() || type == GuaniaoEntityTypes.BUDGERIGAR.get()) {
            return randomBetween(random, 7200, 14000);
        }
        if (type == GuaniaoEntityTypes.NIGHT_HERON.get()) {
            return randomBetween(random, 7800, 15000);
        }
        return randomBetween(random, 6000, 12000);
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        return minInclusive + random.nextInt(Math.max(1, maxInclusive - minInclusive + 1));
    }

    private static BirdDroppingVariant chooseVariantForBird(LivingEntity bird) {
        RandomSource random = bird.getRandom();
        EntityType<?> type = bird.getType();
        if (type == GuaniaoEntityTypes.SPARROW.get()) {
            return BirdDroppingVariant.ONE;
        }
        if (type == GuaniaoEntityTypes.BUDGERIGAR.get()) {
            return random.nextBoolean() ? BirdDroppingVariant.ONE : BirdDroppingVariant.TWO;
        }
        if (type == GuaniaoEntityTypes.PIGEON.get() || type == GuaniaoEntityTypes.SPOTTED_DOVE.get()) {
            return random.nextBoolean() ? BirdDroppingVariant.TWO : BirdDroppingVariant.THREE;
        }
        if (type == GuaniaoEntityTypes.NIGHT_HERON.get()) {
            return random.nextBoolean() ? BirdDroppingVariant.THREE : BirdDroppingVariant.FOUR;
        }
        if (type == GuaniaoEntityTypes.CROW.get()) {
            int roll = random.nextInt(10);
            if (roll < 2) {
                return BirdDroppingVariant.TWO;
            }
            return roll < 7 ? BirdDroppingVariant.THREE : BirdDroppingVariant.FOUR;
        }
        return BirdDroppingVariant.random(random);
    }

    private static boolean isBird(Entity entity) {
        return entity instanceof NightHeronEntity
                || entity instanceof SparrowEntity
                || entity instanceof BudgerigarEntity
                || entity instanceof AbstractColumbidEntity
                || entity instanceof CrowEntity
                || entity.getType() == GuaniaoEntityTypes.NIGHT_HERON.get()
                || entity.getType() == GuaniaoEntityTypes.SPARROW.get()
                || entity.getType() == GuaniaoEntityTypes.BUDGERIGAR.get()
                || entity.getType() == GuaniaoEntityTypes.SPOTTED_DOVE.get()
                || entity.getType() == GuaniaoEntityTypes.PIGEON.get()
                || entity.getType() == GuaniaoEntityTypes.CROW.get();
    }
}
