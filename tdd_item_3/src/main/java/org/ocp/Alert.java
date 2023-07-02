package org.ocp;

import java.util.ArrayList;
import java.util.List;

public class Alert {
    private final List<AlertHandler> alertHandlers = new ArrayList<>();

    public void addAlertHandler(AlertHandler handler){
        alertHandlers.add(handler);
    }

    public void check(ApiStateInfo apiStateInfo){
        for (AlertHandler handler: alertHandlers) {
            handler.check(apiStateInfo);
        }
    }
}
