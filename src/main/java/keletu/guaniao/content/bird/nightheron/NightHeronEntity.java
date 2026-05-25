/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Plane
 *  net.minecraft.core.SectionPos
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.WorldGenRegion
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.TemptGoal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.pathfinder.BlockPathTypes
 *  net.minecraft.world.phys.Vec3
 *  software.bernie.geckolib.animatable.GeoEntity
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.core.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.core.animation.AnimationController
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.animation.RawAnimation
 *  software.bernie.geckolib.core.object.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package keletu.guaniao.content.bird.nightheron;

import keletu.guaniao.content.bird.nightheron.NightHeronAmbientFlightGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronBehaviorState;
import keletu.guaniao.content.bird.nightheron.NightHeronFlightController;
import keletu.guaniao.content.bird.nightheron.NightHeronFlockGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronForagingGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronFrightGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronHighTransitGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronIdleGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronRoostFlightGoal;
import keletu.guaniao.content.bird.nightheron.NightHeronRoostGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
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

public class NightHeronEntity
extends PathfinderMob
implements GeoEntity {
    private static final EntityDataAccessor<Integer> BEHAVIOR_STATE = SynchedEntityData.defineId(NightHeronEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    static final Ingredient TEMPT_ITEMS = Ingredient.of((ItemLike[])new ItemLike[]{Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH});
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE_DIFF_1_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_1").thenLoop("idle");
    private static final RawAnimation IDLE_DIFF_2_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_2").thenLoop("idle");
    private static final RawAnimation IDLE_DIFF_3_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_3").thenLoop("idle");
    private static final RawAnimation IDLE_DIFF_4_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_4").thenLoop("idle");
    private static final RawAnimation IDLE_DIFF_5_ANIMATION = RawAnimation.begin().thenPlay("idle_diff_5").thenLoop("idle");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation FLY_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_loop");
    private static final RawAnimation FLY_FLAPPING_WING_ANIMATION = RawAnimation.begin().thenPlay("fly_flapping_wing").thenLoop("fly_flapping_wing_loop");
    private static final RawAnimation FLY_FLAPPING_WING_LOOP_ANIMATION = RawAnimation.begin().thenLoop("fly_flapping_wing_loop");
    private static final int ACTIVE_START_TIME = 11000;
    private static final int ACTIVE_END_TIME = 1500;
    private static final int WATER_SEARCH_RADIUS = 8;
    private static final double WALKING_SPEED_THRESHOLD = 0.0025;
    private static final double RUNNING_SPEED_THRESHOLD = 0.018;
    private static final double FLYING_SPEED_THRESHOLD = 0.03;
    private static final float FLIGHT_YAW_TURN_RATE = 10.0f;
    private static final float FLIGHT_PITCH_TURN_RATE = 6.0f;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private NightHeronBehaviorState behaviorState = NightHeronBehaviorState.IDLE;
    private IdleAnimationChoice currentIdleAnimation = IdleAnimationChoice.BASE;
    private long nextIdleAnimationSwapTick;
    private int forcedIdleAnimationTicks;
    private int takeoffFlapTicks;
    private int frightMemoryTicks;
    private int recentFrightCount;
    private int externalFrightTicks;
    private boolean severeExternalFright;
    private Vec3 externalFrightSource;
    private int preyStrikeCooldown;
    private int controlledFlightTicks;
    private int groundedAirborneTicks;
    private int blockedFlightRecoveryActivityTicks;
    private int obstructedFlightTicks;
    private Vec3 lastControlledFlightPosition = Vec3.ZERO;
    private int blockedFlightRecoveryDirectionTicks;
    private Vec3 blockedFlightRecoveryDirection = Vec3.ZERO;
    private BlockPos landingApproachTarget;
    private Vec3 landingApproachDirection = Vec3.ZERO;
    private GuidePreviewAnimation guidePreviewAnimation = GuidePreviewAnimation.NONE;

    public NightHeronEntity(EntityType<? extends NightHeronEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 0.0f);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BEHAVIOR_STATE, NightHeronBehaviorState.IDLE.ordinal());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (BEHAVIOR_STATE.equals(key)) {
            this.behaviorState = NightHeronEntity.decodeBehaviorState((Integer)this.entityData.get(BEHAVIOR_STATE));
        }
        super.onSyncedDataUpdated(key);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FLYING_SPEED, 0.45).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    public static boolean canSpawn(EntityType<NightHeronEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(BlockTags.ANIMALS_SPAWNABLE_ON) || below.is(Blocks.MUD) || below.is(Blocks.CLAY) || below.is(Blocks.SAND) || below.is(Blocks.RED_SAND);
        return validGround && level.getRawBrightness(pos, 0) > 7 && NightHeronEntity.isNearWaterForWorldgen((LevelReader)level, pos, 8);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new NightHeronFrightGoal(this));
        this.goalSelector.addGoal(3, (Goal)new TemptGoal((PathfinderMob)this, 1.0, TEMPT_ITEMS, false));
        this.goalSelector.addGoal(4, (Goal)new NightHeronForagingGoal(this));
        this.goalSelector.addGoal(5, (Goal)new NightHeronHighTransitGoal(this));
        this.goalSelector.addGoal(6, (Goal)new NightHeronAmbientFlightGoal(this));
        this.goalSelector.addGoal(7, (Goal)new NightHeronRoostFlightGoal(this));
        this.goalSelector.addGoal(8, (Goal)new NightHeronRoostGoal(this));
        this.goalSelector.addGoal(9, (Goal)new NightHeronFlockGoal(this));
        this.goalSelector.addGoal(10, (Goal)new NightHeronIdleGoal(this));
        this.goalSelector.addGoal(11, (Goal)new RandomLookAroundGoal((Mob)this));
    }

    public void aiStep() {
        super.aiStep();
        if (this.isControlledFlightActive()) {
            this.fallDistance = 0.0f;
        }
        if (!this.level().isClientSide) {
            this.tickStaleFlightRecovery();
            this.tickFlightStateGuard();
            if (this.takeoffFlapTicks > 0) {
                --this.takeoffFlapTicks;
            }
            if (this.frightMemoryTicks > 0) {
                --this.frightMemoryTicks;
            } else {
                this.recentFrightCount = 0;
            }
            if (this.externalFrightTicks > 0) {
                --this.externalFrightTicks;
            } else {
                this.externalFrightSource = null;
                this.severeExternalFright = false;
            }
            if (this.preyStrikeCooldown > 0) {
                --this.preyStrikeCooldown;
            }
            if (this.blockedFlightRecoveryActivityTicks > 0) {
                --this.blockedFlightRecoveryActivityTicks;
            }
            if (this.blockedFlightRecoveryDirectionTicks > 0) {
                --this.blockedFlightRecoveryDirectionTicks;
                if (this.blockedFlightRecoveryDirectionTicks <= 0) {
                    this.blockedFlightRecoveryDirection = Vec3.ZERO;
                }
            }
            if (this.forcedIdleAnimationTicks > 0) {
                --this.forcedIdleAnimationTicks;
            }
        }
        if (this.getBehaviorState().isAirborne()) {
            this.faceMovementDirection(this.getDeltaMovement());
        }
    }

    public boolean hurt(DamageSource damageSource, float amount) {
        boolean hurt = super.hurt(damageSource, amount);
        if (hurt) {
            Entity attacker = damageSource.getEntity();
            this.receiveFlockFright(attacker != null ? attacker.position() : this.position(), true);
            this.rememberFright(true);
        }
        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("NoGravity", false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.clearSerializedFlightState();
    }

    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        float score = super.getWalkTargetValue(pos, level);
        BlockState below = level.getBlockState(pos.below());
        if (NightHeronEntity.isNearWaterForWorldgen(level, pos, 4)) {
            score += 8.0f;
        }
        if (NightHeronEntity.isWaterEdgeForWorldgen(level, pos)) {
            score += 6.0f;
        }
        if (below.is(Blocks.MUD) || below.is(Blocks.CLAY) || below.is(Blocks.SAND) || below.is(BlockTags.DIRT)) {
            score += 2.0f;
        }
        return score;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.FROG_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PARROT_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return this.isControlledFlightActive() || super.isNoGravity();
    }

    protected void playStepSound(BlockPos pos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.12f, 0.9f);
    }

    public int getAmbientSoundInterval() {
        return 240;
    }

    public float getSoundVolume() {
        return 0.5f;
    }

    boolean isActiveTime() {
        long timeOfDay = this.level().getDayTime() % 24000L;
        return timeOfDay >= 11000L || timeOfDay <= 1500L;
    }

    boolean shouldRoost() {
        return !this.isActiveTime();
    }

    boolean isRoosting() {
        return this.getBehaviorState() == NightHeronBehaviorState.ROOSTING || this.shouldRoost();
    }

    public NightHeronBehaviorState getBehaviorState() {
        if (this.entityData != null) {
            return NightHeronEntity.decodeBehaviorState((Integer)this.entityData.get(BEHAVIOR_STATE));
        }
        return this.behaviorState;
    }

    public void setGuidePreviewAnimation(GuidePreviewAnimation guidePreviewAnimation) {
        this.guidePreviewAnimation = guidePreviewAnimation == null ? GuidePreviewAnimation.NONE : guidePreviewAnimation;
    }

    public GuidePreviewAnimation getGuidePreviewAnimation() {
        return this.guidePreviewAnimation;
    }

    void setBehaviorState(NightHeronBehaviorState behaviorState) {
        this.behaviorState = behaviorState;
        if (this.entityData != null) {
            this.entityData.set(BEHAVIOR_STATE, behaviorState.ordinal());
        }
        if (!behaviorState.isAirborne()) {
            this.controlledFlightTicks = 0;
            this.groundedAirborneTicks = 0;
            this.resetFlightObstructionProbe();
        }
        if (behaviorState != NightHeronBehaviorState.PREEN && behaviorState != NightHeronBehaviorState.NECK_STRETCH) {
            this.forcedIdleAnimationTicks = 0;
        }
    }

    void triggerPreen() {
        this.setBehaviorState(NightHeronBehaviorState.PREEN);
        this.currentIdleAnimation = IdleAnimationChoice.SCRATCH;
        this.forcedIdleAnimationTicks = 88;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.forcedIdleAnimationTicks;
    }

    void triggerNeckStretch() {
        this.setBehaviorState(NightHeronBehaviorState.NECK_STRETCH);
        this.currentIdleAnimation = switch (this.getRandom().nextInt(4)) {
            case 0 -> IdleAnimationChoice.LONG_NECK_1;
            case 1 -> IdleAnimationChoice.LONG_NECK_2;
            case 2 -> IdleAnimationChoice.LONG_NECK_3;
            default -> IdleAnimationChoice.LONG_NECK_5;
        };
        this.forcedIdleAnimationTicks = 76;
        this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.forcedIdleAnimationTicks;
    }

    void markTakeoffFlapping() {
        this.takeoffFlapTicks = 20;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.clearLandingApproach();
    }

    boolean isTakeoffFlapping() {
        return this.takeoffFlapTicks > 0;
    }

    int getControlledFlightTicks() {
        return this.controlledFlightTicks;
    }

    boolean isControlledFlightActive() {
        if (!this.getBehaviorState().isAirborne()) {
            return false;
        }
        return !this.onGround() || this.getDeltaMovement().y > 0.04;
    }

    void finishFlight(NightHeronBehaviorState nextState) {
        this.takeoffFlapTicks = 0;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.fallDistance = 0.0f;
        this.hasImpulse = true;
        this.clearLandingApproach();
        this.setBehaviorState(nextState);
    }

    void settleInterruptedFlight(NightHeronBehaviorState groundedState) {
        if (!this.getBehaviorState().isAirborne()) {
            return;
        }
        if (this.onGround()) {
            this.finishFlight(groundedState);
            return;
        }
        this.takeoffFlapTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.getNavigation().stop();
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x * 0.45, Math.min(movement.y, -0.045), movement.z * 0.45);
        this.fallDistance = 0.0f;
        this.hasImpulse = true;
        this.setBehaviorState(NightHeronBehaviorState.LANDING);
    }

    Vec3 updateLandingApproachDirection(BlockPos landingTarget, Vec3 preferredDirection, double correctionWeight) {
        boolean newTarget;
        Vec3 preferred = this.normalizeHorizontal(preferredDirection);
        boolean bl = newTarget = this.landingApproachTarget == null || !this.landingApproachTarget.equals((Object)landingTarget);
        if (newTarget || this.landingApproachDirection.lengthSqr() <= 1.0E-4) {
            this.landingApproachTarget = landingTarget.immutable();
            this.landingApproachDirection = preferred;
            return this.landingApproachDirection;
        }
        double dot = this.landingApproachDirection.dot(preferred);
        if (dot < -0.25) {
            preferred = this.landingApproachDirection;
            correctionWeight = 0.0;
        }
        correctionWeight = Mth.clamp((double)correctionWeight, (double)0.0, (double)0.35);
        Vec3 blended = this.landingApproachDirection.scale(1.0 - correctionWeight).add(preferred.scale(correctionWeight));
        this.landingApproachDirection = this.normalizeHorizontal(blended);
        return this.landingApproachDirection;
    }

    void clearLandingApproach() {
        this.landingApproachTarget = null;
        this.landingApproachDirection = Vec3.ZERO;
    }

    void markBlockedFlightRecovery() {
        this.blockedFlightRecoveryActivityTicks = 70;
    }

    boolean hasBlockedFlightRecoveryActivity() {
        return this.blockedFlightRecoveryActivityTicks > 0;
    }

    boolean consumeBlockedFlightRecoveryActivity() {
        if (this.blockedFlightRecoveryActivityTicks <= 0) {
            return false;
        }
        this.blockedFlightRecoveryActivityTicks = 0;
        return true;
    }

    boolean tickFlightObstructionProbe(boolean pathBlocked) {
        boolean stagnant;
        if (!this.getBehaviorState().isAirborne() || this.onGround()) {
            this.resetFlightObstructionProbe();
            return false;
        }
        Vec3 currentPosition = this.position();
        if (this.lastControlledFlightPosition == Vec3.ZERO) {
            this.lastControlledFlightPosition = currentPosition;
            return false;
        }
        double progressSqr = currentPosition.subtract(this.lastControlledFlightPosition).horizontalDistanceSqr();
        boolean bl = stagnant = this.controlledFlightTicks > 8 && progressSqr < 0.0025;
        this.obstructedFlightTicks = pathBlocked || this.horizontalCollision || stagnant ? ++this.obstructedFlightTicks : Math.max(0, this.obstructedFlightTicks - 2);
        this.lastControlledFlightPosition = currentPosition;
        return this.obstructedFlightTicks >= 5;
    }

    void resetFlightObstructionProbe() {
        this.obstructedFlightTicks = 0;
        this.lastControlledFlightPosition = Vec3.ZERO;
        this.blockedFlightRecoveryDirectionTicks = 0;
        this.blockedFlightRecoveryDirection = Vec3.ZERO;
    }

    Vec3 getBlockedFlightRecoveryDirection() {
        return this.blockedFlightRecoveryDirectionTicks > 0 ? this.blockedFlightRecoveryDirection : Vec3.ZERO;
    }

    void lockBlockedFlightRecoveryDirection(Vec3 direction, int ticks) {
        this.blockedFlightRecoveryDirection = this.normalizeHorizontal(direction);
        this.blockedFlightRecoveryDirectionTicks = ticks;
    }

    private Vec3 normalizeHorizontal(Vec3 vector) {
        Vec3 horizontal = new Vec3(vector.x, 0.0, vector.z);
        if (horizontal.lengthSqr() <= 1.0E-4) {
            Vec3 movement = this.getDeltaMovement();
            horizontal = new Vec3(movement.x, 0.0, movement.z);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            Vec3 look = this.getLookAngle();
            horizontal = new Vec3(look.x, 0.0, look.z);
        }
        if (horizontal.lengthSqr() <= 1.0E-4) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return horizontal.normalize();
    }

    boolean isTemptingPlayer(Player player) {
        return TEMPT_ITEMS.test(player.getMainHandItem()) || TEMPT_ITEMS.test(player.getOffhandItem());
    }

    boolean canStrikePrey() {
        return this.preyStrikeCooldown <= 0;
    }

    void afterPreyStrike() {
        this.preyStrikeCooldown = 45;
        this.heal(0.5f);
    }

    void rememberFright(boolean severe) {
        this.frightMemoryTicks = 220;
        this.recentFrightCount = Math.min(6, this.recentFrightCount + (severe ? 2 : 1));
    }

    int getRecentFrightCount() {
        return this.recentFrightCount;
    }

    void receiveFlockFright(Vec3 source, boolean severe) {
        this.externalFrightSource = source;
        this.externalFrightTicks = severe ? 100 : 55;
        this.severeExternalFright = severe;
        if (severe) {
            this.rememberFright(true);
        }
    }

    boolean hasExternalFright() {
        return this.externalFrightTicks > 0 && this.externalFrightSource != null;
    }

    boolean hasSevereExternalFright() {
        return this.hasExternalFright() && this.severeExternalFright;
    }

    Vec3 getExternalFrightSource() {
        return this.hasExternalFright() ? this.externalFrightSource : null;
    }

    void clearExternalFright() {
        this.externalFrightTicks = 0;
        this.externalFrightSource = null;
        this.severeExternalFright = false;
    }

    void faceMovementDirection(Vec3 movement) {
        double horizontalLength = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        if (horizontalLength <= 1.0E-4) {
            return;
        }
        float targetYaw = (float)(Mth.atan2((double)movement.z, (double)movement.x) * 57.29577951308232) - 90.0f;
        float yaw = Mth.approachDegrees(this.getYRot(), targetYaw, FLIGHT_YAW_TURN_RATE);
        float targetPitch = (float)(-(Math.atan2(movement.y, horizontalLength) * 57.29577951308232));
        float pitch = Mth.clamp(Mth.approachDegrees(this.getXRot(), targetPitch, FLIGHT_PITCH_TURN_RATE), -22.0f, 22.0f);
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.yBodyRot = yaw;
        this.yBodyRotO = yaw;
        this.yHeadRot = yaw;
        this.yHeadRotO = yaw;
        this.setXRot(pitch);
        this.xRotO = pitch;
    }

    double heightAboveSurface() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int x = Mth.floor((double)this.getX());
        int z = Mth.floor((double)this.getZ());
        int startY = Mth.floor((double)this.getY());
        int minY = this.level().getMinBuildHeight();
        for (int y = startY; y >= minY; --y) {
            mutablePos.set(x, y, z);
            BlockState state = this.level().getBlockState((BlockPos)mutablePos);
            if (state.getCollisionShape((BlockGetter)this.level(), (BlockPos)mutablePos).isEmpty() && !this.level().getFluidState((BlockPos)mutablePos).is(FluidTags.WATER)) continue;
            return Math.max(0.0, this.getY() - ((double)y + 1.0));
        }
        return 18.0;
    }

    boolean isNearWater(BlockPos pos, int radius) {
        return NightHeronEntity.isNearWater((LevelReader)this.level(), pos, radius);
    }

    static boolean isWaterEdge(LevelReader level, BlockPos pos) {
        if (!NightHeronEntity.canReadChunk(level, pos)) {
            return false;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (!NightHeronEntity.canReadChunk(level, adjacentPos) || !level.getFluidState(adjacentPos).is(FluidTags.WATER) && !level.getFluidState(adjacentPos.below()).is(FluidTags.WATER)) continue;
            return true;
        }
        return false;
    }

    static boolean isNearWater(LevelReader level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                if (xOffset * xOffset + zOffset * zOffset > radius * radius) continue;
                for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                    mutablePos.set(pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
                    if (!NightHeronEntity.canReadChunk(level, (BlockPos)mutablePos) || !level.getFluidState((BlockPos)mutablePos).is(FluidTags.WATER)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isNearWaterForWorldgen(LevelReader level, BlockPos pos, int radius) {
        if (level instanceof WorldGenRegion) {
            return NightHeronEntity.isNearWaterInSpawnChunk(level, pos, radius);
        }
        return NightHeronEntity.isNearWater(level, pos, radius);
    }

    private static boolean isWaterEdgeForWorldgen(LevelReader level, BlockPos pos) {
        if (level instanceof WorldGenRegion) {
            return NightHeronEntity.isWaterEdgeInSpawnChunk(level, pos);
        }
        return NightHeronEntity.isWaterEdge(level, pos);
    }

    private static boolean isWaterEdgeInSpawnChunk(LevelReader level, BlockPos pos) {
        int spawnChunkX = SectionPos.blockToSectionCoord((int)pos.getX());
        int spawnChunkZ = SectionPos.blockToSectionCoord((int)pos.getZ());
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (!NightHeronEntity.isInChunk(adjacentPos, spawnChunkX, spawnChunkZ) || !level.getFluidState(adjacentPos).is(FluidTags.WATER) && !level.getFluidState(adjacentPos.below()).is(FluidTags.WATER)) continue;
            return true;
        }
        return false;
    }

    private static boolean isNearWaterInSpawnChunk(LevelReader level, BlockPos pos, int radius) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int spawnChunkX = SectionPos.blockToSectionCoord((int)pos.getX());
        int spawnChunkZ = SectionPos.blockToSectionCoord((int)pos.getZ());
        for (int xOffset = -radius; xOffset <= radius; ++xOffset) {
            for (int zOffset = -radius; zOffset <= radius; ++zOffset) {
                int z;
                int x;
                if (xOffset * xOffset + zOffset * zOffset > radius * radius || !NightHeronEntity.isInChunk(x = pos.getX() + xOffset, z = pos.getZ() + zOffset, spawnChunkX, spawnChunkZ)) continue;
                for (int yOffset = -1; yOffset <= 1; ++yOffset) {
                    mutablePos.set(x, pos.getY() + yOffset, z);
                    if (!level.getFluidState((BlockPos)mutablePos).is(FluidTags.WATER)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInChunk(BlockPos pos, int chunkX, int chunkZ) {
        return NightHeronEntity.isInChunk(pos.getX(), pos.getZ(), chunkX, chunkZ);
    }

    private static boolean isInChunk(int x, int z, int chunkX, int chunkZ) {
        return SectionPos.blockToSectionCoord((int)x) == chunkX && SectionPos.blockToSectionCoord((int)z) == chunkZ;
    }

    static boolean canReadChunk(LevelReader level, BlockPos pos) {
        return level.hasChunk(SectionPos.blockToSectionCoord((int)pos.getX()), SectionPos.blockToSectionCoord((int)pos.getZ()));
    }

    private RawAnimation pickIdleAnimation() {
        if (this.forcedIdleAnimationTicks > 0) {
            return this.currentIdleAnimation.animation;
        }
        if (this.level().getGameTime() >= this.nextIdleAnimationSwapTick) {
            this.currentIdleAnimation = this.chooseIdleAnimation();
            this.nextIdleAnimationSwapTick = this.level().getGameTime() + (long)this.currentIdleAnimation.nextDuration(this.getRandom());
        }
        return this.currentIdleAnimation.animation;
    }

    private IdleAnimationChoice chooseIdleAnimation() {
        int roll = this.getRandom().nextInt(100);
        NightHeronBehaviorState state = this.getBehaviorState();
        if (state == NightHeronBehaviorState.WATER_EDGE_WAIT || state == NightHeronBehaviorState.FORAGING) {
            if (roll < 58) {
                return IdleAnimationChoice.BASE;
            }
            if (roll < 73) {
                return IdleAnimationChoice.LONG_NECK_1;
            }
            if (roll < 86) {
                return IdleAnimationChoice.LONG_NECK_3;
            }
            if (roll < 92) {
                return IdleAnimationChoice.SCRATCH;
            }
            return IdleAnimationChoice.LONG_NECK_5;
        }
        if (this.isRoosting()) {
            if (roll < 48) {
                return IdleAnimationChoice.BASE;
            }
            if (roll < 63) {
                return IdleAnimationChoice.LONG_NECK_1;
            }
            if (roll < 78) {
                return IdleAnimationChoice.LONG_NECK_2;
            }
            if (roll < 90) {
                return IdleAnimationChoice.SCRATCH;
            }
            return IdleAnimationChoice.LONG_NECK_5;
        }
        if (roll < 60) {
            return IdleAnimationChoice.BASE;
        }
        if (roll < 70) {
            return IdleAnimationChoice.LONG_NECK_1;
        }
        if (roll < 80) {
            return IdleAnimationChoice.LONG_NECK_2;
        }
        if (roll < 89) {
            return IdleAnimationChoice.LONG_NECK_3;
        }
        if (roll < 94) {
            return IdleAnimationChoice.SCRATCH;
        }
        return IdleAnimationChoice.LONG_NECK_5;
    }

    private boolean shouldUseFlyingAnimation() {
        if (this.isControlledFlightActive()) {
            return true;
        }
        if (this.onGround() || this.isInWaterOrBubble()) {
            return false;
        }
        return Math.abs(this.getDeltaMovement().y) > 0.08 || this.getDeltaMovement().horizontalDistanceSqr() > 0.03;
    }

    private RawAnimation chooseFlyingAnimation() {
        NightHeronBehaviorState state = this.getBehaviorState();
        if (state == NightHeronBehaviorState.TAKEOFF || this.takeoffFlapTicks > 0) {
            return FLY_FLAPPING_WING_ANIMATION;
        }
        if (state == NightHeronBehaviorState.LOCAL_FLIGHT || state == NightHeronBehaviorState.LOW_FLAP_ESCAPE || state == NightHeronBehaviorState.CLIMB || state == NightHeronBehaviorState.LANDING) {
            return FLY_FLAPPING_WING_LOOP_ANIMATION;
        }
        boolean highFlight = state == NightHeronBehaviorState.HIGH_TRANSIT || state == NightHeronBehaviorState.LONG_FLIGHT_ESCAPE || state == NightHeronBehaviorState.SOARING || state == NightHeronBehaviorState.GLIDE;
        return highFlight && (NightHeronFlightController.shouldGlide(this) || state == NightHeronBehaviorState.GLIDE) ? FLY_LOOP_ANIMATION : FLY_FLAPPING_WING_LOOP_ANIMATION;
    }

    private <T extends NightHeronEntity> PlayState movementController(AnimationState<T> animationState) {
        RawAnimation guidePreviewRawAnimation = this.guidePreviewAnimation.animation();
        if (guidePreviewRawAnimation != null) {
            return animationState.setAndContinue(guidePreviewRawAnimation);
        }
        if (this.shouldUseFlyingAnimation()) {
            return animationState.setAndContinue(this.chooseFlyingAnimation());
        }
        double horizontalSpeed = this.getDeltaMovement().horizontalDistanceSqr();
        if (this.getBehaviorState() == NightHeronBehaviorState.RUN_ESCAPE || horizontalSpeed > 0.018) {
            return animationState.setAndContinue(RUN_ANIMATION);
        }
        if (horizontalSpeed > 0.0025 || !this.getNavigation().isDone()) {
            return animationState.setAndContinue(WALK_ANIMATION);
        }
        return animationState.setAndContinue(this.pickIdleAnimation());
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "movement", 4, this::movementController)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    private void clearSerializedFlightState() {
        this.takeoffFlapTicks = 0;
        this.controlledFlightTicks = 0;
        this.groundedAirborneTicks = 0;
        this.resetFlightObstructionProbe();
        this.clearLandingApproach();
        if (super.isNoGravity()) {
            this.setNoGravity(false);
        }
        if (this.getBehaviorState().isAirborne()) {
            this.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
        if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
            this.setDeltaMovement(0.0, -0.08, 0.0);
            this.hasImpulse = true;
            this.fallDistance = 0.0f;
        }
    }

    private void tickStaleFlightRecovery() {
        if (!this.getBehaviorState().isAirborne()) {
            if (super.isNoGravity()) {
                this.setNoGravity(false);
                if (!this.onGround() && this.getDeltaMovement().lengthSqr() < 0.0025) {
                    this.setDeltaMovement(0.0, -0.08, 0.0);
                    this.hasImpulse = true;
                }
            }
            return;
        }
        if (!this.onGround() && this.controlledFlightTicks > 6 && this.getDeltaMovement().lengthSqr() < 0.0025) {
            this.setNoGravity(false);
            this.setBehaviorState(NightHeronBehaviorState.IDLE);
            this.setDeltaMovement(0.0, -0.08, 0.0);
            this.hasImpulse = true;
            this.fallDistance = 0.0f;
        }
    }

    private void tickFlightStateGuard() {
        NightHeronBehaviorState state = this.getBehaviorState();
        if (!state.isAirborne()) {
            this.controlledFlightTicks = 0;
            this.groundedAirborneTicks = 0;
            this.resetFlightObstructionProbe();
            return;
        }
        if (!this.onGround()) {
            ++this.controlledFlightTicks;
            this.groundedAirborneTicks = 0;
            return;
        }
        if (this.takeoffFlapTicks > 0 && this.getDeltaMovement().y > 0.04) {
            return;
        }
        ++this.groundedAirborneTicks;
        if (this.groundedAirborneTicks >= 2) {
            this.finishFlight(NightHeronBehaviorState.IDLE);
        }
    }

    private static NightHeronBehaviorState decodeBehaviorState(int ordinal) {
        NightHeronBehaviorState[] states = NightHeronBehaviorState.values();
        if (ordinal < 0 || ordinal >= states.length) {
            return NightHeronBehaviorState.IDLE;
        }
        return states[ordinal];
    }

    private static enum IdleAnimationChoice {
        BASE(IDLE_ANIMATION, 70, 130),
        LONG_NECK_1(IDLE_DIFF_1_ANIMATION, 62, 72),
        LONG_NECK_2(IDLE_DIFF_2_ANIMATION, 62, 74),
        LONG_NECK_3(IDLE_DIFF_3_ANIMATION, 62, 74),
        SCRATCH(IDLE_DIFF_4_ANIMATION, 78, 90),
        LONG_NECK_5(IDLE_DIFF_5_ANIMATION, 62, 78);

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

    public static enum GuidePreviewAnimation {
        NONE,
        IDLE,
        LOOK_1,
        LOOK_2,
        LOOK_3,
        SCRATCH,
        LOOK_5,
        WALK,
        RUN,
        FLY_FLAP,
        GLIDE;

        private RawAnimation animation() {
            return switch (this) {
                case NONE -> null;
                case IDLE -> IDLE_ANIMATION;
                case LOOK_1 -> IDLE_DIFF_1_ANIMATION;
                case LOOK_2 -> IDLE_DIFF_2_ANIMATION;
                case LOOK_3 -> IDLE_DIFF_3_ANIMATION;
                case SCRATCH -> IDLE_DIFF_4_ANIMATION;
                case LOOK_5 -> IDLE_DIFF_5_ANIMATION;
                case WALK -> WALK_ANIMATION;
                case RUN -> RUN_ANIMATION;
                case FLY_FLAP -> FLY_FLAPPING_WING_LOOP_ANIMATION;
                case GLIDE -> FLY_LOOP_ANIMATION;
            };
        }
    }
}
