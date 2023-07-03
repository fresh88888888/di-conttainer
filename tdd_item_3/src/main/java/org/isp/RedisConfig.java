package org.isp;

import java.util.Map;

public class RedisConfig implements Updater, Viewer{
    //...省略其他配置信息...
    private ConfigSource configSource;

    public RedisConfig(ConfigSource configSource) {
        this.configSource = configSource;
    }

    @Override
    public void update() {
        //...
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
