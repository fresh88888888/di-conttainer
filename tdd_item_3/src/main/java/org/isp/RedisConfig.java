package org.isp;

public class RedisConfig implements Updater{
    //...省略其他配置信息...
    private ConfigSource configSource;

    public RedisConfig(ConfigSource configSource) {
        this.configSource = configSource;
    }

    @Override
    public void update() {
        //..
    }
}
