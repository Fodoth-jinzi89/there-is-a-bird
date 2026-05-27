package EdDYON.guaniao.content.bird.nightheron;

import java.util.EnumSet;
import EdDYON.guaniao.content.bird.nightheron.NightHeronBehaviorState;
import EdDYON.guaniao.content.bird.nightheron.NightHeronEntity;
import EdDYON.guaniao.content.bird.nightheron.NightHeronLandingSelector;
import net.minecraft.world.entity.ai.goal.Goal;

public class NightHeronRoostGoal
extends Goal {
    private final NightHeronEntity nightHeron;
    private int remainingTicks;

    public NightHeronRoostGoal(NightHeronEntity nightHeron) {
        this.nightHeron = nightHeron;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    public boolean canUse() {
        return this.nightHeron.shouldRoost() && this.nightHeron.onGround() && this.nightHeron.getTarget() == null && this.isNearRoostCover() && this.nightHeron.getRandom().nextInt(55) == 0;
    }

    public boolean canContinueToUse() {
        return this.remainingTicks > 0 && this.nightHeron.shouldRoost() && this.nightHeron.onGround() && !this.nightHeron.getBehaviorState().isEscape();
    }

    public void start() {
        this.remainingTicks = 110 + this.nightHeron.getRandom().nextInt(190);
        this.nightHeron.setBehaviorState(NightHeronBehaviorState.ROOSTING);
        if (this.nightHeron.getNavigation().isDone()) {
            this.nightHeron.getNavigation().stop();
        }
    }

    public void stop() {
        this.remainingTicks = 0;
        if (!this.nightHeron.getBehaviorState().isEscape()) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.IDLE);
        }
    }

    public void tick() {
        --this.remainingTicks;
        if (this.nightHeron.getBehaviorState() != NightHeronBehaviorState.SOCIAL_SPACING) {
            this.nightHeron.setBehaviorState(NightHeronBehaviorState.ROOSTING);
        }
        if (this.remainingTicks % 70 == 0 && this.nightHeron.getRandom().nextInt(3) == 0) {
            this.nightHeron.triggerNeckStretch();
        }
    }

    private boolean isNearRoostCover() {
        return NightHeronLandingSelector.isRoostingSpot(this.nightHeron.level(), this.nightHeron.blockPosition()) || NightHeronLandingSelector.hasRoostCoverNear(this.nightHeron.level(), this.nightHeron.blockPosition(), 5);
    }
}

