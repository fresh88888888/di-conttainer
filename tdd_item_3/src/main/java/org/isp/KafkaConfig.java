package org.isp;

public class KafkaConfig implements Updater{
    //...省略其他配置信息...
    private ConfigSource configSource;

    public KafkaConfig(ConfigSource configSource) {
        this.configSource = configSource;
    }

    @Override
    public void update() {
        //...
    }
}
