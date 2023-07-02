package org.ocp;

public abstract class AlertHandler {
    protected AlterRule rule;
    protected Notification notification;

    public AlertHandler(AlterRule rule, Notification notification) {
        this.rule = rule;
        this.notification = notification;
    }

    public abstract void check(ApiStateInfo apiStateInfo);
}
