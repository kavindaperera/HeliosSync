package com.nova;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.nova.config.ConfigLoader;
import com.nova.core.BigQueryDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import com.nova.core.DataLoader;

public class HeliosSync implements HttpFunction {

    private static final Logger logger = LogManager.getLogger(HeliosSync.class);

    private static final String REDIS_HOST = ConfigLoader.get("redis.host");
    private static final int REDIS_PORT = Integer.parseInt(ConfigLoader.get("redis.port"));

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        logger.info("Trigger: {}", request.getFirstHeader("Trigger"));

        String trigger = request.getFirstHeader("Trigger").orElse("BigQuery");

        Config config = new Config();
        String redisUrl = "redis://" + REDIS_HOST + ":" + REDIS_PORT;
        config.useSingleServer().setAddress(redisUrl);

        try {

            RedissonClient redisson = Redisson.create(config);

            logger.info("Connected: {}", redisUrl);

            try {
                DataLoader dataLoader = null;

                switch (trigger) {
                    case "BigQuery":
                        dataLoader = new BigQueryDataLoader(redisson);
                        break;
                    case "GCS":
                        response.setStatusCode(400);
                        response.getWriter().write("Unsupported trigger type: " + trigger);
                        break;
                    default:
                        response.setStatusCode(400);
                        response.getWriter().write("Unsupported trigger type: " + trigger);
                        break;
                }

                if (dataLoader != null) {
                    dataLoader.loadData();
                    response.setStatusCode(200);
                    response.getWriter().write("Data processed successfully.");
                }

            } catch (Exception e) {
                logger.error("Error processing data: ", e);
                response.setStatusCode(500);
                response.getWriter().write("Error processing data: " + e.getMessage());
            } finally {
                redisson.shutdown();
            }

        } catch (Exception e) {
            logger.error("Error connecting to redis : {} : ", redisUrl, e);
            response.setStatusCode(500);
            response.getWriter().write("Error connecting to redis: " + redisUrl);
        }

    }
}