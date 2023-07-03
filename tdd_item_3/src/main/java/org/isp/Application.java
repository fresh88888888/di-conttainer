package org.isp;

public class Application {
    static ConfigSource configSource = new ZookeeperConfigSource(/*省略参数*/);
    public static final RedisConfig redisConfig = new RedisConfig(configSource);
    public static final MysqlConfig mysqlConfig = new MysqlConfig(configSource);
    public static final KafkaConfig kafkaConfig = new KafkaConfig(configSource);
    public static final ApiMetrics apiMetrics = new ApiMetrics();
    public static final DbMetrics dbMetrics = new DbMetrics();

    public static void main(String[] args){
        ScheduledUpdater redisSchedulerUpdater = new ScheduledUpdater(300, 300, redisConfig);
        redisSchedulerUpdater.run();

        ScheduledUpdater kafkaSchedulerUpdater = new ScheduledUpdater(60, 60, kafkaConfig);
        kafkaSchedulerUpdater.run();

        SimpleHttpServer server = new SimpleHttpServer("192.168.1.1", "8080");
        server.addViewers("/config", mysqlConfig);
        server.addViewers("/config", redisConfig);
        server.addViewers("/metrics", apiMetrics);
        server.addViewers("/metrics", dbMetrics);
        server.run();
    }
}
