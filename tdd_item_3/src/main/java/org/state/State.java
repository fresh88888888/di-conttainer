package org.state;

//状态枚举
public enum State {

    SMALL(0), SUPER(1), FIRE(2), CAPE(3);
    private final int value;

    State(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
