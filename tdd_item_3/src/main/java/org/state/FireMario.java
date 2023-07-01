package org.state;

public class FireMario implements IMario{
    private static final FireMario instance = new FireMario();
    private FireMario(){}

    public static FireMario getInstance(){
        return instance;
    }

    @Override
    public State getName() {
        return State.FIRE;
    }

    @Override
    public void obtainMushRoom(MarioStateMachine stateMachine) {
        //do nothing
    }

    @Override
    public void obtainCape(MarioStateMachine stateMachine) {
        //do nothing
    }

    @Override
    public void obtainFireFlower(MarioStateMachine stateMachine) {
        //do nothing
    }

    @Override
    public void meetMonster(MarioStateMachine stateMachine) {
        stateMachine.setCurrentState(SmallMario.getInstance());
        stateMachine.setScope(stateMachine.getScope() - 300);
    }
}
