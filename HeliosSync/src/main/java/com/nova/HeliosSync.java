package com.nova;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.nova.core.BigQueryDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import com.nova.core.DataLoader;

public class HeliosSync implements HttpFunction {

    private static final Logger logger = LogManager.getLogger(HeliosSync.class);

    private static final String REDIS_HOST = System.getenv("REDIS_HOST");
    private static final int REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        String trigger = request.getFirstHeader("Trigger").orElse("GCS");

        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS_HOST + ":" + REDIS_PORT);

        RedissonClient redisson = Redisson.create(config);
        try {
            DataLoader dataLoader;
            if ("BigQuery".equalsIgnoreCase(trigger)) {
                dataLoader = new BigQueryDataLoader(redisson);
            } else {
                response.setStatusCode(400);
                response.getWriter().write("Unsupported trigger type: " + trigger);
                return;
            }

            dataLoader.loadData();
            response.setStatusCode(200);
            response.getWriter().write("Data processed successfully.");
        } catch (Exception e) {
            logger.error("Error processing data: ", e);
            response.setStatusCode(500);
            response.getWriter().write("Error processing data: " + e.getMessage());
        } finally {
            redisson.shutdown();
        }
    }
}