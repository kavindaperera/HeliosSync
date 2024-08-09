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

import java.io.IOException;

/**
 * {@code HeliosSync} is a Google Cloud Function that handles HTTP requests to trigger data loading processes.
 * <p>
 * This class implements the {@link HttpFunction} interface and uses the Redisson client to interact with a Redis
 * database. Based on the value of the "Trigger" header in the HTTP request, it will instantiate the appropriate
 * {@link DataLoader} implementation and execute the data loading process.
 * </p>
 */
public class HeliosSync implements HttpFunction {

    private static final Logger logger = LogManager.getLogger(HeliosSync.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        String trigger = request.getFirstHeader("Trigger").orElse("BigQuery");

        logger.info("Trigger: {}", trigger);

        RedissonClient redisson = null;

        try {
            redisson = createRedissonClient();
            logger.info("Connected: {}", redisson.getConfig().toYAML());
        } catch (Exception e) {
            logger.error("Error connecting to redis : ", e);
            response.setStatusCode(500);
            response.getWriter().write("Error connecting to redis");
        }

        if (redisson != null) {

            try {

                DataLoader dataLoader = null;

                switch (trigger) {
                    case "BigQuery":
                        dataLoader = new BigQueryDataLoader(redisson);
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
            }  finally {
                redisson.shutdown();
            }
        }

    }

    /**
     * Creates and configures a Redisson client from the YAML configuration file.
     *
     * @return the configured Redisson client
     * @throws IOException if there is an error reading the configuration file
     */
    private RedissonClient createRedissonClient() throws IOException {
        Config config = Config.fromYAML(HttpFunction.class.getClassLoader().getResource("redisson-config.yml"));
        return Redisson.create(config);
    }

}