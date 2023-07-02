package org.ocp;

public class ErrorAlertHandler extends AlertHandler{

    public ErrorAlertHandler(AlterRule rule, Notification notification) {
        super(rule, notification);
    }

    @Override
    public void check(ApiStateInfo apiStateInfo) {
        if (apiStateInfo.getErrorCount() > rule.getMatchedRule(apiStateInfo.getApi()).getMaxErrorCount()){
            notification.toNotify(NotificationEmergencyLevel.SEVERE, "...");
        }
    }
}
