package org.ocp;

public class ApplicationContext {
    private AlterRule rule;
    private Notification notification;
    private Alert alert;

    public void initializeBeans(){
        rule = new AlterRule();
        notification = new Notification();
        alert = new Alert();
        alert.addAlertHandler(new TpsAlertHandler(rule, notification));
        alert.addAlertHandler(new ErrorAlertHandler(rule, notification));
    }

    public Alert getAlert() {
        return alert;
    }

    private static final ApplicationContext context = new ApplicationContext();
    private ApplicationContext(){
        initializeBeans();
    }

    public static ApplicationContext getInstance(){
        return context;
    }
    
}
