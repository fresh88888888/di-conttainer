package org.ocp;

public class TpsAlertHandler extends AlertHandler{

    public TpsAlertHandler(AlterRule rule, Notification notification) {
        super(rule, notification);
    }

    @Override
    public void check(ApiStateInfo apiStateInfo) {
        long tps = apiStateInfo.getRequestCount() / apiStateInfo.getDurationOfSeconds();
        if (tps > rule.getMatchedRule(apiStateInfo.getApi()).getMaxTps()){
            notification.toNotify(NotificationEmergencyLevel.URGENCY, "...");
        }
    }
}
