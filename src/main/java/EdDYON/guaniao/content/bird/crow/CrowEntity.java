package EdDYON.guaniao.content.bird.crow;

import EdDYON.guaniao.content.bath.BirdBathAttraction;
import EdDYON.guaniao.content.bath.BirdBathContentType;
import EdDYON.guaniao.content.bath.BirdBathFeedingAnimatable;
import EdDYON.guaniao.content.bath.BirdBathMountable;
import EdDYON.guaniao.content.bath.BirdBathUseGoal;
import EdDYON.guaniao.content.bird.BirdGroundAnimation;
import EdDYON.guaniao.content.bird.flight.BirdFlightAware;
import EdDYON.guaniao.content.bird.flight.BirdFlightBoids;
import EdDYON.guaniao.content.bird.flight.BirdFlightController;
import EdDYON.guaniao.content.bird.flight.BirdFlightProfile;
import EdDYON.guaniao.content.bird.flight.BirdFlightTargeting;
import EdDYON.guaniao.content.bird.scale.BirdModelScale;
import EdDYON.guaniao.content.bird.scale.BirdModelScaleProfile;
import EdDYON.guaniao.content.bird.scale.ScalableBirdModel;
import EdDYON.guaniao.registry.GuaniaoItems;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
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

public class CrowEntity extends PathfinderMob implements GeoEntity, FlyingAnimal, ScalableBirdModel, BirdFlightAware, BirdBathMountable, BirdBathFeedingAnimatable {
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLYING_ANIMATION_ACTIVE = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final BirdFlightProfile FLIGHT_PROFILE = BirdFlightProfile.CROW;
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation IDLE_DIFF_1_ANIMATION = RawAnimation.begin().thenPlay("animation.idle_diff_1").thenLoop("animation.idle");
    private static final RawAnimation IDLE_DIFF_2_ANIMATION = RawAnimation.begin().thenPlay("animation.idle_diff_2").thenLoop("animation.idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("animation.walk");
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("animation.fly");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private CrowBehaviorState behaviorState = CrowBehaviorState.IDLE;
    private GuidePreviewAnimation guidePreviewAnimation = GuidePreviewAnimation.NONE;
    private RawAnimation currentIdleAnimation = IDLE_ANIMATION;
    private int behaviorStateLockTicks;
    private int eatingTicks;
    private int foodCooldown;
    private int calmTicks;
    private int angerMemoryTicks;
    private int flightTicks;
    private int timeFlying;
    private int flightCooldown;
    private int hoverRetargetTicks;
    private int airborneGraceTicks;
    private int shinyCooldown;
    private int groupAlertCooldown;
    private boolean escapeFlightActive;
    private boolean landingFlight;
    private Vec3 flightTarget;
    private Vec3 frightSource;
    private UUID rememberedPlayerUUID;

    public CrowEntity(EntityType<? extends CrowEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 12, true);
        this.setPathfindingMalus(BlockPathTypes.LEAVES, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 16.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, CrowDefinition.MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, CrowDefinition.WALK_SPEED)
                .add(Attributes.FLYING_SPEED, CrowDefinition.FLYING_SPEED)
                .add(Attributes.FOLLOW_RANGE, CrowDefinition.FOLLOW_RANGE);
    }

    public static boolean canSpawn(EntityType<CrowEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(BlockTags.DIRT)
                || below.is(BlockTags.LEAVES)
                || below.is(BlockTags.LOGS)
                || below.is(Blocks.FARMLAND)
                || below.is(Blocks.HAY_BLOCK)
                || below.getBlock() instanceof FenceBlock
                || below.getBlock() instanceof FenceGateBlock;
        return validGround && level.getRawBrightness(pos, 0) > 7;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CrowFleeGoal(this));
        this.goalSelector.addGoal(2, new CrowEatDroppedFoodGoal(this));
        this.goalSelector.addGoal(3, new BirdBathUseGoal(this, 1.02D, 12.0D, 42,
                BirdBathAttraction::isAttractiveToCrow,
                this::canStartForagingGoal,
                bath -> this.setBehaviorState(CrowBehaviorState.FORAGING),
                this::consumeBirdBathServing,
                (bath, consumed) -> {
                    if (!this.isEating() && this.getBehaviorState() == CrowBehaviorState.FORAGING) {
                        this.setBehaviorState(CrowBehaviorState.IDLE);
                    }
                }));
        this.goalSelector.addGoal(4, new CrowInvestigateShinyGoal(this));
        this.goalSelector.addGoal(5, new CrowWatchPlayerGoal(this));
        this.goalSelector.addGoal(6, new CrowAmbientFlightGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.92D, 0.001F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BEHAVIOR_STATE, CrowBehaviorState.IDLE.ordinal());
        this.entityData.define(MODEL_SCALE, BirdModelScale.DEFAULT_INDIVIDUAL_SCALE);
        this.entityData.define(FLYING_ANIMATION_ACTIVE, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = CrowEntity.decodeBehaviorState(this.entityData.get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CompoundTag compoundTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, compoundTag);
        if (compoundTag == null || !compoundTag.contains(BirdModelScale.NBT_KEY, 5)) {
            this.randomizeModelScale();
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("CrowCalmTicks", this.calmTicks);
        compoundTag.putInt("CrowAngerMemoryTicks", this.angerMemoryTicks);
        if (this.rememberedPlayerUUID != null) {
            compoundTag.putUUID("CrowRememberedPlayer", this.rememberedPlayerUUID);
        }
        BirdModelScale.save(compoundTag, this.getIndividualModelScale(), this.modelScaleProfile());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.calmTicks = compoundTag.getInt("CrowCalmTicks");
        this.angerMemoryTicks = compoundTag.getInt("CrowAngerMemoryTicks");
        if (compoundTag.hasUUID("CrowRememberedPlayer")) {
            this.rememberedPlayerUUID = compoundTag.getUUID("CrowRememberedPlayer");
        }
        if (compoundTag.contains(BirdModelScale.NBT_KEY, 5)) {
            this.setIndividualModelScale(BirdModelScale.load(compoundTag, this.modelScaleProfile()));
        } else {
            this.randomizeModelScale();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.tickCounters();
        this.tickEating();
        this.tickWaterEscape();
        this.tickFlight();
        this.tickBehaviorFallback();
        this.tickGroundMovementFacing();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && isCrowFood(stack)) {
            if (!this.level().isClientSide) {
                ItemStack eaten = stack.copy();
                eaten.setCount(1);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.startEatingFood(eaten, true);
                this.calmTicks = Math.max(this.calmTicks, 2400 + this.getRandom().nextInt(3600));
                if (this.rememberedPlayerUUID != null && this.rememberedPlayerUUID.equals(player.getUUID())) {
                    this.angerMemoryTicks = Math.max(0, this.angerMemoryTicks - 1200);
                    if (this.angerMemoryTicks <= 0) {
                        this.rememberedPlayerUUID = null;
                    }
                }
                this.heal(1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            this.clearEating();
            Entity attacker = source.getEntity();
            Vec3 sourcePos = attacker == null ? this.position() : attacker.position();
            this.rememberAggressor(attacker);
            this.frightenFrom(sourcePos, attacker instanceof Player ? 90 : 60);
            this.alertNearbyCrows(attacker, sourcePos);
        }
        return hurt;
    }

    @Override
    public boolean isFlying() {
        return this.isBirdFlightActive() || (!this.onGround() && this.getDeltaMovement().y > -0.65D);
    }

    @Override
    public BirdFlightProfile birdFlightProfile() {
        return FLIGHT_PROFILE;
    }

    @Override
    public boolean isBirdFlightActive() {
        return this.flightTicks > 0
                || this.landingFlight
                || this.getBehaviorState().isAirborne()
                || (this.isNoGravity() && !this.onGround());
    }

    @Override
    public boolean isBirdLanding() {
        return this.landingFlight;
    }

    @Override
    public boolean isBirdEscaping() {
        return this.escapeFlightActive;
    }

    @Override
    public boolean startBirdBathMountFlight(Vec3 standPosition) {
        if (standPosition == null || this.isFlightInProgress()) {
            return false;
        }
        Vec3 horizontal = standPosition.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() > 1.0E-4D) {
            horizontal = horizontal.normalize().scale(0.30D);
        }
        this.getNavigation().stop();
        this.setNoGravity(false);
        this.setOnGround(false);
        this.setDeltaMovement(horizontal.x, 0.68D, horizontal.z);
        this.airborneGraceTicks = Math.max(this.airborneGraceTicks, 32);
        this.setFlyingAnimationActive(true);
        this.setBehaviorStateFor(CrowBehaviorState.FLYING, 34);
        this.faceFlightDirection(this.getDeltaMovement());
        this.fallDistance = 0.0F;
        this.hasImpulse = true;
        return true;
    }

    @Override
    public void startBirdBathFeedingAnimation(BirdBathContentType contentType, int ticks) {
        this.getNavigation().stop();
        if (contentType.isFood()) {
            this.eatingTicks = Math.max(this.eatingTicks, Math.max(34, ticks));
            this.setBehaviorStateFor(CrowBehaviorState.EATING, this.eatingTicks);
            return;
        }
        this.setBehaviorStateFor(CrowBehaviorState.WATCHING, Math.max(24, ticks / 2));
    }

    @Override
    public BirdModelScaleProfile modelScaleProfile() {
        return BirdModelScaleProfile.CROW;
    }

    @Override
    public float getIndividualModelScale() {
        if (this.entityData == null) {
            return BirdModelScale.DEFAULT_INDIVIDUAL_SCALE;
        }
        return BirdModelScale.sanitize(this.entityData.get(MODEL_SCALE), this.modelScaleProfile());
    }

    @Override
    public void setIndividualModelScale(float scale) {
        if (this.entityData != null) {
            this.entityData.set(MODEL_SCALE, BirdModelScale.sanitize(scale, this.modelScaleProfile()));
        }
    }

    public ResourceLocation getTextureResource() {
        return CrowDefinition.TEXTURE;
    }

    public void setGuidePreviewAnimation(GuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null ? GuidePreviewAnimation.NONE : guidePreviewAnimation;
    }

    public CrowBehaviorState getBehaviorState() {
        if (this.entityData != null) {
            return CrowEntity.decodeBehaviorState(this.entityData.get(BEHAVIOR_STATE));
        }
        return this.behaviorState;
    }

    void setBehaviorState(CrowBehaviorState state) {
        if (state == null) {
            state = CrowBehaviorState.IDLE;
        }
        this.behaviorState = state;
        if (this.entityData != null) {
            this.entityData.set(BEHAVIOR_STATE, state.ordinal());
        }
    }

    void setBehaviorStateFor(CrowBehaviorState state, int ticks) {
        this.setBehaviorState(state);
        this.behaviorStateLockTicks = Math.max(this.behaviorStateLockTicks, ticks);
    }

    boolean canStartForagingGoal() {
        return this.foodCooldown <= 0
                && !this.isEating()
                && !this.isFlightInProgress()
                && !this.getBehaviorState().isEscape();
    }

    boolean isEating() {
        return this.eatingTicks > 0 || this.getBehaviorState() == CrowBehaviorState.EATING;
    }

    boolean isFlightInProgress() {
        return this.flightTicks > 0 || this.landingFlight;
    }

    boolean isActiveTime() {
        long time = this.level().getDayTime() % 24000L;
        return time >= 23000L || time < 12500L;
    }

    void startEatingFood(ItemStack foodStack, boolean trustedFood) {
        this.getNavigation().stop();
        this.eatingTicks = 38 + this.getRandom().nextInt(22);
        this.foodCooldown = trustedFood ? 90 + this.getRandom().nextInt(70) : 120 + this.getRandom().nextInt(80);
        this.setBehaviorStateFor(CrowBehaviorState.EATING, this.eatingTicks);
        this.playSound(SoundEvents.GENERIC_EAT, 0.42F, 0.78F + this.getRandom().nextFloat() * 0.16F);
    }

    void consumeItemEntity(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        ItemStack eaten = stack.copy();
        eaten.setCount(1);
        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        this.startEatingFood(eaten, false);
        this.calmTicks = Math.max(this.calmTicks, 1200 + this.getRandom().nextInt(1800));
    }

    void frightenFrom(Vec3 sourcePos, int ticks) {
        this.frightSource = sourcePos;
        this.setBehaviorStateFor(CrowBehaviorState.FLEEING, Math.min(90, ticks));
        if (this.flightCooldown <= 0 && !this.isFlightInProgress()) {
            this.startEscapeFlight(sourcePos);
        }
    }

    void receiveGroupAlert(Entity attacker, Vec3 sourcePos, int delayRange) {
        if (this.groupAlertCooldown > 0) {
            return;
        }
        this.groupAlertCooldown = 80 + this.getRandom().nextInt(80);
        this.rememberAggressor(attacker);
        this.setBehaviorStateFor(CrowBehaviorState.ALERT, 35 + this.getRandom().nextInt(35));
        if (this.getRandom().nextInt(Math.max(1, delayRange)) == 0 && this.flightCooldown <= 0) {
            this.startEscapeFlight(sourcePos);
        }
    }

    private void consumeBirdBathServing(EdDYON.guaniao.content.bath.BirdBathBlockEntity bath, BirdBathContentType contentType) {
        if (contentType == BirdBathContentType.FISH) {
            this.startEatingFood(new ItemStack(Items.COD), true);
            return;
        }
        if (contentType == BirdBathContentType.MEAT) {
            this.startEatingFood(new ItemStack(Items.CHICKEN), true);
            return;
        }
        if (contentType == BirdBathContentType.BREAD) {
            this.startEatingFood(new ItemStack(Items.BREAD), true);
            return;
        }
        this.eatingTicks = 22 + this.getRandom().nextInt(12);
        this.foodCooldown = 70 + this.getRandom().nextInt(45);
        this.setBehaviorStateFor(CrowBehaviorState.EATING, this.eatingTicks);
        this.playSound(SoundEvents.GENERIC_DRINK, 0.32F, 0.86F + this.getRandom().nextFloat() * 0.16F);
    }

    public static boolean isCrowFood(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.is(GuaniaoItems.BREADCRUMBS.get())
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.MUTTON)
                || stack.is(Items.RABBIT)
                || stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.TROPICAL_FISH)
                || stack.is(Items.PUFFERFISH)
                || stack.is(Items.SWEET_BERRIES)
                || stack.is(Items.GLOW_BERRIES)
                || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.MELON_SEEDS)
                || stack.is(Items.PUMPKIN_SEEDS)
                || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.TORCHFLOWER_SEEDS)
                || stack.is(Items.PITCHER_POD)
                || stack.is(Items.WHEAT)
                || stack.is(Items.BREAD)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.COOKED_RABBIT)
                || stack.is(Items.COOKED_COD)
                || stack.is(Items.COOKED_SALMON)
                || stack.is(Items.APPLE)
                || stack.is(Items.BEETROOT));
    }

    static boolean isLowValueShiny(ItemStack stack) {
        return !stack.isEmpty()
                && !stack.hasCustomHoverName()
                && !stack.hasTag()
                && (stack.is(Items.GOLD_NUGGET)
                || stack.is(Items.IRON_NUGGET)
                || stack.is(Items.COPPER_INGOT)
                || stack.is(Items.AMETHYST_SHARD)
                || stack.is(Items.REDSTONE)
                || stack.is(Items.LAPIS_LAZULI)
                || stack.is(Items.GLOWSTONE_DUST)
                || stack.is(Items.GLASS_BOTTLE)
                || stack.is(Items.CLOCK));
    }

    private void rememberAggressor(Entity attacker) {
        if (attacker instanceof Player player) {
            this.rememberedPlayerUUID = player.getUUID();
            this.angerMemoryTicks = Math.max(this.angerMemoryTicks, 3600 + this.getRandom().nextInt(2401));
        }
    }

    private Player rememberedPlayer() {
        if (this.rememberedPlayerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.rememberedPlayerUUID);
    }

    private void alertNearbyCrows(Entity attacker, Vec3 sourcePos) {
        if (this.groupAlertCooldown > 0) {
            return;
        }
        this.groupAlertCooldown = 120;
        for (CrowEntity crow : this.level().getEntitiesOfClass(CrowEntity.class, this.getBoundingBox().inflate(16.0D))) {
            if (crow != this) {
                crow.receiveGroupAlert(attacker, sourcePos, 3 + crow.getRandom().nextInt(4));
            }
        }
    }

    private void tickCounters() {
        if (this.behaviorStateLockTicks > 0) {
            --this.behaviorStateLockTicks;
        }
        if (this.eatingTicks > 0) {
            --this.eatingTicks;
        }
        if (this.foodCooldown > 0) {
            --this.foodCooldown;
        }
        if (this.calmTicks > 0) {
            --this.calmTicks;
        }
        if (this.angerMemoryTicks > 0) {
            --this.angerMemoryTicks;
            if (this.angerMemoryTicks <= 0) {
                this.rememberedPlayerUUID = null;
            }
        }
        if (this.flightCooldown > 0) {
            --this.flightCooldown;
        }
        if (this.airborneGraceTicks > 0) {
            --this.airborneGraceTicks;
        }
        if (this.shinyCooldown > 0) {
            --this.shinyCooldown;
        }
        if (this.groupAlertCooldown > 0) {
            --this.groupAlertCooldown;
        }
        if (this.airborneGraceTicks <= 0 && !this.isFlightInProgress() && this.onGround()) {
            this.setFlyingAnimationActive(false);
        }
    }

    private void tickEating() {
        if (this.eatingTicks <= 0 && this.getBehaviorState() == CrowBehaviorState.EATING) {
            this.setBehaviorState(CrowBehaviorState.IDLE);
        }
    }

    private void clearEating() {
        this.eatingTicks = 0;
        if (this.getBehaviorState() == CrowBehaviorState.EATING) {
            this.setBehaviorState(CrowBehaviorState.ALERT);
        }
    }

    private void tickWaterEscape() {
        if (this.isInWaterOrBubble() && this.flightCooldown <= 0 && !this.isFlightInProgress()) {
            this.startShortFlight(this.position().add(this.randomHorizontalDirection().scale(5.0D)).add(0.0D, 3.0D, 0.0D), true);
        }
    }

    private void tickFlight() {
        if (this.flightTicks <= 0 && !this.landingFlight) {
            this.timeFlying = 0;
            this.setNoGravity(false);
            if (this.onGround() && this.airborneGraceTicks <= 0) {
                this.setFlyingAnimationActive(false);
            }
            return;
        }
        this.getNavigation().stop();
        this.setNoGravity(true);
        this.setFlyingAnimationActive(true);
        this.fallDistance = 0.0F;
        ++this.timeFlying;
        this.setBehaviorState(this.escapeFlightActive ? CrowBehaviorState.FLEEING : CrowBehaviorState.FLYING);
        if (this.flightTicks > 0) {
            --this.flightTicks;
        }
        if (this.flightTicks <= 0 && !this.landingFlight) {
            this.beginLandingFlight();
        }
        if (this.flightTarget == null) {
            if (this.landingFlight) {
                this.flightTarget = this.findLandingTarget();
                if (this.flightTarget == null) {
                    this.extendCruiseAfterUnsafeLanding();
                    return;
                }
            } else {
                this.retargetAirCruise(this.escapeFlightActive);
            }
        }
        Vec3 toTarget = this.flightTarget.subtract(this.position());
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (this.landingFlight) {
            if (this.onGround()) {
                this.finishFlight();
                return;
            }
            if (this.flightTicks <= 0 && toTarget.lengthSqr() < 0.35D) {
                this.extendCruiseAfterUnsafeLanding();
                return;
            }
        } else if (toTarget.lengthSqr() < 2.2D || --this.hoverRetargetTicks <= 0) {
            this.retargetAirCruise(this.escapeFlightActive);
            toTarget = this.flightTarget.subtract(this.position());
            horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        }
        Vec3 direction = toTarget.lengthSqr() > 1.0E-4D ? toTarget.normalize() : this.randomHorizontalDirection();
        Vec3 horizontalDirection = BirdFlightTargeting.normalizeHorizontal(new Vec3(direction.x, 0.0D, direction.z), this.getDeltaMovement());
        if (!this.landingFlight) {
            Vec3 flockHeading = BirdFlightBoids.sameTypeHeading(this, 14.0D, 2.8D, 0.035D, 0.36D, 0.12D, this.escapeFlightActive ? 0.14D : 0.08D);
            if (flockHeading.lengthSqr() > 1.0E-4D) {
                horizontalDirection = BirdFlightTargeting.normalizeHorizontal(horizontalDirection.add(flockHeading), horizontalDirection);
            }
        }
        double speed = this.escapeFlightActive ? FLIGHT_PROFILE.escapeSpeed() : (this.landingFlight ? FLIGHT_PROFILE.landingSpeed() : FLIGHT_PROFILE.cruiseSpeed());
        if (this.landingFlight) {
            speed = BirdFlightController.decelerateNearLanding(speed, horizontalDistance, 4.0D, 0.45D);
        }
        double vertical = this.landingFlight
                ? Mth.clamp(toTarget.y * 0.11D - 0.035D, -0.13D, 0.055D)
                : Mth.clamp(toTarget.y * 0.11D + Math.sin((this.tickCount + this.getId()) * 0.22D) * 0.018D, -0.08D, 0.16D);
        Vec3 desired = new Vec3(horizontalDirection.x * speed, vertical, horizontalDirection.z * speed);
        Vec3 movement = BirdFlightController.blendMovement(this.getDeltaMovement(), desired, 0.68D);
        if (!this.landingFlight && BirdFlightController.isStalledInAir(this, this.timeFlying, 0.006D)) {
            this.retargetAirCruise(this.escapeFlightActive);
            movement = horizontalDirection.scale(Math.max(speed, 0.22D)).add(0.0D, 0.08D, 0.0D);
        }
        this.setDeltaMovement(movement);
        this.faceFlightDirection(movement);
        this.hasImpulse = true;
    }

    private void tickBehaviorFallback() {
        if (this.behaviorStateLockTicks > 0 || this.isEating() || this.isFlightInProgress()) {
            return;
        }
        CrowBehaviorState state = this.getBehaviorState();
        if (BirdGroundAnimation.hasWalkMotion(this)) {
            this.setBehaviorState(CrowBehaviorState.WALKING);
            return;
        }
        if (state == CrowBehaviorState.WALKING
                || state == CrowBehaviorState.FORAGING
                || state == CrowBehaviorState.WATCHING
                || state == CrowBehaviorState.ALERT
                || state.isAirborne()) {
            this.setBehaviorState(CrowBehaviorState.IDLE);
        }
    }

    private void tickGroundMovementFacing() {
        if (this.onGround()
                && !this.isFlightInProgress()
                && !this.isInWaterOrBubble()
                && !this.isPassenger()
                && !this.getBehaviorState().isAirborne()) {
            BirdFlightController.faceGroundMovement(this, this.getDeltaMovement(), 1.0E-4D);
        }
    }

    private void startShortFlight(Vec3 target, boolean fleeing) {
        if (this.flightCooldown > 0 || this.flightTicks > 0 || this.landingFlight) {
            return;
        }
        this.escapeFlightActive = fleeing;
        this.landingFlight = false;
        this.flightTarget = target == null ? this.findAirCruiseTarget(fleeing) : this.clampFlightTarget(target);
        this.flightTicks = fleeing
                ? 100 + this.getRandom().nextInt(80)
                : FLIGHT_PROFILE.minFlightTicks() + this.getRandom().nextInt(FLIGHT_PROFILE.maxFlightTicks() - FLIGHT_PROFILE.minFlightTicks() + 1);
        this.timeFlying = 0;
        this.hoverRetargetTicks = this.nextHoverRetargetDelay();
        this.setNoGravity(true);
        this.setFlyingAnimationActive(true);
        this.setOnGround(false);
        this.getNavigation().stop();
        this.setBehaviorStateFor(fleeing ? CrowBehaviorState.FLEEING : CrowBehaviorState.FLYING, fleeing ? 100 : 90);
        Vec3 direction = this.flightTarget.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() <= 1.0E-4D) {
            direction = this.randomHorizontalDirection();
        }
        Vec3 movement = direction.normalize().scale(fleeing ? 0.34D : 0.24D).add(0.0D, 0.12D, 0.0D);
        this.setDeltaMovement(movement);
        this.faceFlightDirection(movement);
        this.fallDistance = 0.0F;
        this.hasImpulse = true;
    }

    private void startEscapeFlight(Vec3 sourcePos) {
        Vec3 away = this.position().subtract(sourcePos).multiply(1.0D, 0.0D, 1.0D);
        if (away.lengthSqr() <= 1.0E-4D) {
            away = this.randomHorizontalDirection();
        }
        Vec3 target = BirdFlightTargeting.findAirTarget(this, FLIGHT_PROFILE, away, true);
        if (target == null) {
            target = this.position().add(away.normalize().scale(8.0D + this.getRandom().nextDouble() * 6.0D)).add(0.0D, 3.0D, 0.0D);
        }
        this.startShortFlight(target, true);
    }

    private void beginLandingFlight() {
        Vec3 landingTarget = this.findLandingTarget();
        if (landingTarget == null) {
            this.extendCruiseAfterUnsafeLanding();
            return;
        }
        this.landingFlight = true;
        this.escapeFlightActive = false;
        this.flightTicks = 55 + this.getRandom().nextInt(45);
        this.flightTarget = landingTarget;
        this.hoverRetargetTicks = 0;
        this.setBehaviorStateFor(CrowBehaviorState.FLYING, 55);
    }

    private void extendCruiseAfterUnsafeLanding() {
        this.landingFlight = false;
        this.escapeFlightActive = false;
        this.flightTicks = 70 + this.getRandom().nextInt(55);
        this.retargetAirCruise(false);
        this.setNoGravity(true);
        this.setBehaviorStateFor(CrowBehaviorState.FLYING, 70);
    }

    private void finishFlight() {
        boolean wasEscaping = this.escapeFlightActive;
        this.flightTicks = 0;
        this.timeFlying = 0;
        this.flightTarget = null;
        this.hoverRetargetTicks = 0;
        this.escapeFlightActive = false;
        this.landingFlight = false;
        this.setNoGravity(false);
        this.setFlyingAnimationActive(false);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.42D, 0.0D, 0.42D));
        this.flightCooldown = wasEscaping ? 110 + this.getRandom().nextInt(100) : 160 + this.getRandom().nextInt(160);
        this.setBehaviorStateFor(wasEscaping ? CrowBehaviorState.ALERT : CrowBehaviorState.IDLE, wasEscaping ? 28 : 14);
    }

    private void retargetAirCruise(boolean fleeing) {
        this.flightTarget = this.findAirCruiseTarget(fleeing);
        this.hoverRetargetTicks = this.nextHoverRetargetDelay();
    }

    private int nextHoverRetargetDelay() {
        return 44 + this.getRandom().nextInt(54);
    }

    private Vec3 findAirCruiseTarget(boolean fleeing) {
        Vec3 direction;
        if (fleeing && this.frightSource != null) {
            Vec3 away = this.position().subtract(this.frightSource);
            direction = away.horizontalDistanceSqr() > 0.01D ? new Vec3(away.x, 0.0D, away.z).normalize() : this.randomHorizontalDirection();
        } else {
            direction = this.getRandom().nextInt(4) == 0 ? this.randomHorizontalDirection() : this.getLookAngle();
        }
        Vec3 target = BirdFlightTargeting.findAirTarget(this, FLIGHT_PROFILE, direction, fleeing);
        if (target != null) {
            return this.clampFlightTarget(target);
        }
        return this.clampFlightTarget(this.position().add(this.randomHorizontalDirection().scale(10.0D)).add(0.0D, this.onGround() ? 4.0D : 1.0D, 0.0D));
    }

    private Vec3 findLandingTarget() {
        Vec3 sharedLanding = BirdFlightTargeting.findNearestDryLandingTarget(this, 10, 18);
        if (sharedLanding != null) {
            return this.clampFlightTarget(sharedLanding);
        }
        Vec3 forwardLanding = BirdFlightTargeting.findLandingInDirection(this, this.getDeltaMovement(), 4, 12, 8, 18);
        return forwardLanding == null ? null : this.clampFlightTarget(forwardLanding);
    }

    private Vec3 clampFlightTarget(Vec3 target) {
        double y = Mth.clamp(target.y, this.level().getMinBuildHeight() + 1.5D, this.level().getMaxBuildHeight() - 2.0D);
        return new Vec3(target.x, y, target.z);
    }

    private Vec3 randomHorizontalDirection() {
        return BirdFlightTargeting.randomHorizontalDirection(this.getRandom());
    }

    private void faceFlightDirection(Vec3 movement) {
        BirdFlightController.faceMovement(this, movement, FLIGHT_PROFILE.maxPitchDegrees());
    }

    private boolean isFlyingAnimationActive() {
        return this.entityData != null && this.entityData.get(FLYING_ANIMATION_ACTIVE);
    }

    private void setFlyingAnimationActive(boolean active) {
        if (this.entityData != null) {
            this.entityData.set(FLYING_ANIMATION_ACTIVE, active);
        }
    }

    private boolean shouldPlayFlyAnimation() {
        return BirdFlightController.shouldPlayFlyAnimation(
                this,
                this.getBehaviorState().isAirborne() || this.isFlyingAnimationActive(),
                this.onGround(),
                this.isNoGravity(),
                this.getDeltaMovement(),
                this.airborneGraceTicks);
    }

    private boolean shouldPlayWalkAnimation(CrowBehaviorState state) {
        if (!BirdGroundAnimation.canPlayWalk(this) || state.isAirborne() || state == CrowBehaviorState.EATING) {
            return false;
        }
        return BirdGroundAnimation.hasWalkMotion(this)
                || state == CrowBehaviorState.WALKING
                || state == CrowBehaviorState.FORAGING;
    }

    private RawAnimation pickIdleAnimation() {
        if (this.tickCount % (100 + this.getId() % 80) == 0) {
            int roll = this.getRandom().nextInt(4);
            this.currentIdleAnimation = roll == 0 ? IDLE_DIFF_1_ANIMATION : (roll == 1 ? IDLE_DIFF_2_ANIMATION : IDLE_ANIMATION);
        }
        return this.currentIdleAnimation;
    }

    private <T extends CrowEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }
        CrowBehaviorState state = this.getBehaviorState();
        if (this.shouldPlayFlyAnimation()) {
            return animationState.setAndContinue(FLY_ANIMATION);
        }
        if (this.shouldPlayWalkAnimation(state)) {
            return animationState.setAndContinue(WALK_ANIMATION);
        }
        if (state == CrowBehaviorState.WATCHING || state == CrowBehaviorState.ALERT) {
            return animationState.setAndContinue(IDLE_DIFF_2_ANIMATION);
        }
        if (state == CrowBehaviorState.EATING || this.eatingTicks > 0) {
            return animationState.setAndContinue(IDLE_DIFF_1_ANIMATION);
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

    private void randomizeModelScale() {
        this.setIndividualModelScale(BirdModelScale.randomIndividualScale(this.getRandom(), this.modelScaleProfile()));
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
    public int getAmbientSoundInterval() {
        return 210;
    }

    @Override
    public float getSoundVolume() {
        return 0.56F;
    }

    private static CrowBehaviorState decodeBehaviorState(int ordinal) {
        CrowBehaviorState[] values = CrowBehaviorState.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return CrowBehaviorState.IDLE;
        }
        return values[ordinal];
    }

    private static final class CrowEatDroppedFoodGoal extends Goal {
        private final CrowEntity crow;
        private ItemEntity target;
        private int repathTicks;

        private CrowEatDroppedFoodGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.crow.canStartForagingGoal() || this.crow.getRandom().nextInt(6) != 0) {
                return false;
            }
            this.target = this.findNearestFood();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null
                    && this.target.isAlive()
                    && isCrowFood(this.target.getItem())
                    && this.crow.distanceToSqr(this.target) < 18.0D * 18.0D
                    && !this.crow.isEating()
                    && !this.crow.isFlightInProgress();
        }

        @Override
        public void start() {
            this.repathTicks = 0;
            this.crow.setBehaviorState(CrowBehaviorState.FORAGING);
        }

        @Override
        public void tick() {
            if (this.target == null) {
                return;
            }
            this.crow.getLookControl().setLookAt(this.target, 25.0F, this.crow.getMaxHeadXRot());
            double distanceSqr = this.crow.distanceToSqr(this.target);
            if (distanceSqr <= 1.65D) {
                this.crow.getNavigation().stop();
                this.crow.consumeItemEntity(this.target);
                return;
            }
            if (--this.repathTicks <= 0 || this.crow.getNavigation().isDone()) {
                this.repathTicks = 10;
                this.crow.getNavigation().moveTo(this.target, 1.0D);
            }
        }

        @Override
        public void stop() {
            this.target = null;
            this.repathTicks = 0;
            if (!this.crow.isEating() && this.crow.getBehaviorState() == CrowBehaviorState.FORAGING) {
                this.crow.setBehaviorState(CrowBehaviorState.IDLE);
            }
        }

        private ItemEntity findNearestFood() {
            ItemEntity best = null;
            double bestDistance = 12.0D * 12.0D;
            for (ItemEntity item : this.crow.level().getEntitiesOfClass(ItemEntity.class, this.crow.getBoundingBox().inflate(12.0D, 4.0D, 12.0D), entity -> entity.isAlive() && isCrowFood(entity.getItem()))) {
                double distance = this.crow.distanceToSqr(item);
                if (distance < bestDistance) {
                    best = item;
                    bestDistance = distance;
                }
            }
            return best;
        }
    }

    private static final class CrowInvestigateShinyGoal extends Goal {
        private final CrowEntity crow;
        private ItemEntity target;
        private int observeTicks;
        private int repathTicks;

        private CrowInvestigateShinyGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.crow.canStartForagingGoal() || this.crow.shinyCooldown > 0 || this.crow.getRandom().nextInt(22) != 0) {
                return false;
            }
            this.target = this.findNearestShiny();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null
                    && this.target.isAlive()
                    && isLowValueShiny(this.target.getItem())
                    && this.observeTicks > 0
                    && !this.crow.isEating()
                    && !this.crow.isFlightInProgress();
        }

        @Override
        public void start() {
            this.observeTicks = 90 + this.crow.getRandom().nextInt(80);
            this.repathTicks = 0;
            this.crow.setBehaviorState(CrowBehaviorState.WATCHING);
        }

        @Override
        public void tick() {
            --this.observeTicks;
            if (this.target == null) {
                return;
            }
            this.crow.getLookControl().setLookAt(this.target, 35.0F, this.crow.getMaxHeadXRot());
            double distanceSqr = this.crow.distanceToSqr(this.target);
            if (distanceSqr > 4.0D && (--this.repathTicks <= 0 || this.crow.getNavigation().isDone())) {
                this.repathTicks = 12;
                this.crow.getNavigation().moveTo(this.target, 0.88D);
            } else if (distanceSqr <= 4.0D) {
                this.crow.getNavigation().stop();
            }
        }

        @Override
        public void stop() {
            this.target = null;
            this.observeTicks = 0;
            this.repathTicks = 0;
            this.crow.shinyCooldown = 220 + this.crow.getRandom().nextInt(180);
            if (this.crow.getBehaviorState() == CrowBehaviorState.WATCHING) {
                this.crow.setBehaviorState(CrowBehaviorState.IDLE);
            }
        }

        private ItemEntity findNearestShiny() {
            ItemEntity best = null;
            double bestDistance = 10.0D * 10.0D;
            for (ItemEntity item : this.crow.level().getEntitiesOfClass(ItemEntity.class, this.crow.getBoundingBox().inflate(10.0D, 4.0D, 10.0D), entity -> entity.isAlive() && isLowValueShiny(entity.getItem()))) {
                double distance = this.crow.distanceToSqr(item);
                if (distance < bestDistance) {
                    best = item;
                    bestDistance = distance;
                }
            }
            return best;
        }
    }

    private static final class CrowWatchPlayerGoal extends Goal {
        private final CrowEntity crow;
        private Player target;
        private int repathTicks;

        private CrowWatchPlayerGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.crow.isEating() || this.crow.isFlightInProgress() || this.crow.getRandom().nextInt(4) != 0) {
                return false;
            }
            this.target = this.findNearestPlayer();
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null
                    && this.target.isAlive()
                    && !this.target.isSpectator()
                    && this.crow.distanceToSqr(this.target) <= 13.0D * 13.0D
                    && !this.crow.isEating()
                    && !this.crow.isFlightInProgress();
        }

        @Override
        public void start() {
            this.repathTicks = 0;
        }

        @Override
        public void tick() {
            double distanceSqr = this.crow.distanceToSqr(this.target);
            this.crow.getLookControl().setLookAt(this.target, 35.0F, this.crow.getMaxHeadXRot());
            if (distanceSqr <= 4.0D * 4.0D && this.crow.flightCooldown <= 0) {
                this.crow.frightenFrom(this.target.position(), this.crow.calmTicks > 0 ? 40 : 70);
                return;
            }
            if (distanceSqr <= 6.0D * 6.0D) {
                this.crow.setBehaviorStateFor(CrowBehaviorState.ALERT, 18);
                if (--this.repathTicks <= 0) {
                    this.repathTicks = 12;
                    Vec3 away = this.crow.position().subtract(this.target.position()).multiply(1.0D, 0.0D, 1.0D);
                    if (away.lengthSqr() > 1.0E-4D) {
                        Vec3 move = this.crow.position().add(away.normalize().scale(3.2D));
                        this.crow.getNavigation().moveTo(move.x, move.y, move.z, 1.03D);
                    }
                }
                return;
            }
            this.crow.getNavigation().stop();
            this.crow.setBehaviorStateFor(CrowBehaviorState.WATCHING, 14);
        }

        @Override
        public void stop() {
            this.target = null;
            this.repathTicks = 0;
            if (this.crow.getBehaviorState() == CrowBehaviorState.WATCHING || this.crow.getBehaviorState() == CrowBehaviorState.ALERT) {
                this.crow.setBehaviorState(CrowBehaviorState.IDLE);
            }
        }

        private Player findNearestPlayer() {
            Player best = null;
            double bestDistance = 12.0D * 12.0D;
            for (Player player : this.crow.level().getEntitiesOfClass(Player.class, this.crow.getBoundingBox().inflate(12.0D), player -> player.isAlive() && !player.isSpectator())) {
                double distance = this.crow.distanceToSqr(player);
                if (distance < bestDistance) {
                    best = player;
                    bestDistance = distance;
                }
            }
            return best;
        }
    }

    private static final class CrowFleeGoal extends Goal {
        private final CrowEntity crow;
        private Player remembered;
        private int repathTicks;

        private CrowFleeGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.crow.isEating() || this.crow.isFlightInProgress() || this.crow.angerMemoryTicks <= 0) {
                return false;
            }
            this.remembered = this.crow.rememberedPlayer();
            return this.remembered != null
                    && this.remembered.isAlive()
                    && this.crow.distanceToSqr(this.remembered) <= 14.0D * 14.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return this.remembered != null
                    && this.remembered.isAlive()
                    && this.crow.angerMemoryTicks > 0
                    && !this.crow.isEating()
                    && !this.crow.isFlightInProgress()
                    && this.crow.distanceToSqr(this.remembered) <= 16.0D * 16.0D;
        }

        @Override
        public void start() {
            this.repathTicks = 0;
            this.crow.setBehaviorStateFor(CrowBehaviorState.ALERT, 40);
            if (this.crow.distanceToSqr(this.remembered) <= 8.0D * 8.0D) {
                this.crow.frightenFrom(this.remembered.position(), 90);
            }
        }

        @Override
        public void tick() {
            this.crow.getLookControl().setLookAt(this.remembered, 35.0F, this.crow.getMaxHeadXRot());
            if (this.crow.distanceToSqr(this.remembered) <= 7.0D * 7.0D && this.crow.flightCooldown <= 0) {
                this.crow.frightenFrom(this.remembered.position(), 90);
                return;
            }
            if (--this.repathTicks <= 0) {
                this.repathTicks = 12;
                Vec3 away = this.crow.position().subtract(this.remembered.position()).multiply(1.0D, 0.0D, 1.0D);
                if (away.lengthSqr() > 1.0E-4D) {
                    Vec3 move = this.crow.position().add(away.normalize().scale(5.0D));
                    this.crow.getNavigation().moveTo(move.x, move.y, move.z, 1.08D);
                }
            }
        }

        @Override
        public void stop() {
            this.remembered = null;
            this.repathTicks = 0;
        }
    }

    private static final class CrowAmbientFlightGoal extends Goal {
        private final CrowEntity crow;

        private CrowAmbientFlightGoal(CrowEntity crow) {
            this.crow = crow;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.crow.flightCooldown <= 0
                    && this.crow.onGround()
                    && this.crow.isActiveTime()
                    && this.crow.getNavigation().isDone()
                    && !this.crow.isEating()
                    && !this.crow.getBehaviorState().isEscape()
                    && this.crow.getRandom().nextInt(170) == 0;
        }

        @Override
        public void start() {
            this.crow.startShortFlight(this.crow.findAirCruiseTarget(false), false);
        }
    }

    public enum GuidePreviewAnimation {
        NONE(null),
        IDLE(IDLE_ANIMATION),
        LOOK_1(IDLE_DIFF_1_ANIMATION),
        LOOK_2(IDLE_DIFF_2_ANIMATION),
        WALK(WALK_ANIMATION),
        FLY(FLY_ANIMATION);

        private final RawAnimation animation;

        GuidePreviewAnimation(RawAnimation animation) {
            this.animation = animation;
        }

        private RawAnimation animation() {
            return this.animation;
        }
    }
}
