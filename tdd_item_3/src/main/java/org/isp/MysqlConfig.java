package org.isp;

public class MysqlConfig implements Updater{
    //...省略其他配置信息...
    private ConfigSource configSource;

    public MysqlConfig(ConfigSource configSource) {
        this.configSource = configSource;
    }

    @Override
    public void update() {
        //...
    }
}
