/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.ai.util.LandRandomPos
 *  net.minecraft.world.phys.Vec3
 */
package keletu.guaniao.content.bird.nightheron;

import java.util.EnumSet;
import keletu.guaniao.content.bird.nightheron.NightHeronBehaviorState;
import keletu.guaniao.content.bird.nightheron.NightHeronEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class NightHeronIdleGoal
extends Goal {
    private final NightHeronEntity nightHeron;
    private int remainingTicks;
    private IdleAction action = IdleAction.STAND;

    public NightHeronIdleGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        return this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isAirborne() && !this.nightHeron.getBehaviorState().isEscape() && this.nightHeron.getTarget() == null && (this.nightHeron.hasBlockedFlightRecoveryActivity() || this.nightHeron.getRandom().nextInt(10) == 0);
    }

    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isAirborne() && !this.nightHeron.getBehaviorState().isEscape();
    }

    public void start() {
        this.action = this.nightHeron.consumeBlockedFlightRecoveryActivity() ? IdleAction.STROLL : this.chooseAction();
        this.remainingTicks = this.randomBetween(50, 140);
        if (this.action == IdleAction.STROLL) {
            this.remainingTicks = this.randomBetween(35, 80);
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.MICRO_STROLL);
            this.moveToShortStrollTarget();
            return;
        }
        if (this.action == IdleAction.PREEN) {
            this.nightHeron.triggerPreen();
            this.remainingTicks = 80;
            return;
        }
        if (this.action == IdleAction.NECK_STRETCH) {
            this.nightHeron.triggerNeckStretch();
            this.remainingTicks = 72;
            return;
        }
        if (this.action == IdleAction.LOOK) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.LOOK_AROUND);
            this.lookAtRandomPoint();
            return;
        }
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.REST_STAND);
        this.nightHeron.getNavigation().stop();
    }

    public void stop() {
        this.remainingTicks = 0;
        if (!this.nightHeron.getBehaviorState().isAirborne() && !this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    public void tick() {
        --this.remainingTicks;
        if (this.action == IdleAction.STROLL) {
            if (this.nightHeron.getNavigation().isDone() && this.remainingTicks > 20 && this.nightHeron.getRandom().nextInt(18) == 0) {
                this.moveToShortStrollTarget();
            }
            return;
        }
        if (this.action == IdleAction.LOOK && this.remainingTicks % 24 == 0) {
            this.lookAtRandomPoint();
        }
        this.nightHeron.getNavigation().stop();
    }

    private IdleAction chooseAction() {
        int roll = this.nightHeron.getRandom().nextInt(100);
        if (this.nightHeron.isRoosting()) {
            if (roll < 58) {
                return IdleAction.STAND;
            }
            if (roll < 74) {
                return IdleAction.LOOK;
            }
            if (roll < 86) {
                return IdleAction.NECK_STRETCH;
            }
            if (roll < 95) {
                return IdleAction.PREEN;
            }
            return IdleAction.STROLL;
        }
        if (roll < 26) {
            return IdleAction.STAND;
        }
        if (roll < 44) {
            return IdleAction.LOOK;
        }
        if (roll < 75) {
            return IdleAction.STROLL;
        }
        if (roll < 90) {
            return IdleAction.NECK_STRETCH;
        }
        return IdleAction.PREEN;
    }

    private void moveToShortStrollTarget() {
        Vec3 target = LandRandomPos.getPos((PathfinderMob)this.nightHeron, (int)5, (int)3);
        if (target != null) {
            this.nightHeron.getNavigation().moveTo(target.x, target.y, target.z, 0.16);
        }
    }

    private void lookAtRandomPoint() {
        float angle = this.nightHeron.getRandom().nextFloat() * ((float)Math.PI * 2);
        double distance = 3.0 + this.nightHeron.getRandom().nextDouble() * 4.0;
        this.nightHeron.getLookControl().setLookAt(this.nightHeron.getX() + (double)Mth.cos((float)angle) * distance, this.nightHeron.getEyeY(), this.nightHeron.getZ() + (double)Mth.sin((float)angle) * distance);
    }

    private int randomBetween(int min, int max) {
        return min + this.nightHeron.getRandom().nextInt(max - min + 1);
    }

    private static enum IdleAction {
        STAND,
        LOOK,
        STROLL,
        PREEN,
        NECK_STRETCH;

    }
}

