package org.ocp;

import java.util.Map;

public class AlterRule {
    private long maxTps;
    private long maxErrorCount;

    public long getMaxErrorCount() {
        return maxErrorCount;
    }

    public void setMaxErrorCount(long maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
    }

    public long getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(long maxTps) {
        this.maxTps = maxTps;
    }

    public AlterRule getMatchedRule(String api){
        //TODO: 查找API相对应的告警规则
        return new AlterRule();
    }
}
