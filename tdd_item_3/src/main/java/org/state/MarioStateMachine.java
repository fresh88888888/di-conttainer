package org.state;

// 状态机
public class MarioStateMachine {
    private int scope;
    private IMario currentState;

    public MarioStateMachine() {
        this.scope = 0;
        this.currentState = SmallMario.getInstance();
    }

    public void obtainMushRoom(){
        this.currentState.obtainMushRoom(this);
    }

    public void obtainCape(){
        this.currentState.obtainCape(this);
    }

    public void obtainFireFlower(){
        currentState.obtainFireFlower(this);
    }

    public void meetMonster(){
        currentState.meetMonster(this);
    }

    public int getScope() {
        return scope;
    }

    public State getCurrentState() {
        return currentState.getName();
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setCurrentState(IMario currentState) {
        this.currentState = currentState;
    }
}
