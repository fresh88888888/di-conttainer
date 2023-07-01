package org.state;

public class SmallMario implements IMario{
    private static final SmallMario instance = new SmallMario();
    private SmallMario(){}

    public static SmallMario getInstance(){
        return instance;
    }

    @Override
    public State getName() {
        return State.SMALL;
    }

    @Override
    public void obtainMushRoom(MarioStateMachine stateMachine) {
        stateMachine.setCurrentState(SuperMario.getInstance());
        stateMachine.setScope(stateMachine.getScope() + 100);
    }

    @Override
    public void obtainCape(MarioStateMachine stateMachine) {
        stateMachine.setCurrentState(CapeMario.getInstance());
        stateMachine.setScope(stateMachine.getScope() + 200);
    }

    @Override
    public void obtainFireFlower(MarioStateMachine stateMachine) {
        stateMachine.setCurrentState(FireMario.getInstance());
        stateMachine.setScope(stateMachine.getScope() + 300);
    }

    @Override
    public void meetMonster(MarioStateMachine stateMachine) {
        // do nothing
    }
}
