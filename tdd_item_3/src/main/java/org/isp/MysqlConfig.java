package org.isp;

import java.util.Map;

public class MysqlConfig implements Viewer{
    //...省略其他配置信息...
    private ConfigSource configSource;

    public MysqlConfig(ConfigSource configSource) {
        this.configSource = configSource;
    }
    @Override
    public String outputInPlainText() {
        //...
        return null;
    }

    @Override
    public Map<String, String> output() {
        //...
        return null;
    }
}
