package EdDYON.guaniao.content.bird.crow;

public enum CrowBehaviorState {
    IDLE(false),
    WALKING(false),
    WATCHING(false),
    FORAGING(false),
    EATING(false),
    ALERT(false),
    FLYING(true),
    FLEEING(true);

    private final boolean airborne;

    CrowBehaviorState(boolean airborne) {
        this.airborne = airborne;
    }

    public boolean isAirborne() {
        return this.airborne;
    }

    public boolean isEscape() {
        return this == FLEEING;
    }
}
