package com.nova.cache;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.nova.config.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;

public class HeliosCache implements HttpFunction {

    private static final Logger logger = LogManager.getLogger(HeliosCache.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        Optional<String> keyParam = request.getFirstQueryParameter("key");
        Optional<String> formatParam = request.getFirstQueryParameter("format");

        if (keyParam.isEmpty() || formatParam.isEmpty()) {
            response.setStatusCode(400); // Bad Request
            response.getWriter().write("Missing 'key' or 'format' query parameter.");
            return;
        }

        logger.info("Request Received: key={}, format={}", keyParam, formatParam);

        String key = keyParam.get();
        String format = formatParam.get().toLowerCase();

        RedissonClient redisson;

        try {
            redisson = createRedissonClient();
        } catch (Exception e) {
            logger.error("Error connecting to redis : ", e);
            response.setStatusCode(500);
            response.getWriter().write("Error connecting to redis");
            return;
        }

        String data = (String) redisson.getBucket(key).get();

        if (data == null) {
            response.setStatusCode(404); // Not Found
            response.getWriter().write("No data found for the given key: " + key);
            return;
        }

        switch (format) {
            case "csv":
                response.appendHeader("Content-Disposition", "attachment; filename=" + key + ".csv");
                response.appendHeader("Content-Type", "text/csv");
                break;
            case "json":
                response.appendHeader("Content-Disposition", "attachment; filename=" + key + ".json");
                response.appendHeader("Content-Type", "application/json");
                break;
            default:
                response.setStatusCode(400); // Bad Request
                response.getWriter().write("Unsupported format: " + format);
                return;
        }

        try (BufferedWriter writer = response.getWriter()) {
            writer.write(data);
        } catch (IOException e) {
            response.setStatusCode(500); // Internal Server Error
            response.getWriter().write("Error writing data to response: " + e.getMessage());
        }

    }

    private static RedissonClient createRedissonClient() throws IOException {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer().setAddress(Settings.get("redisson.address"));
        String password = Settings.get("redisson.password");
        if (password != null && !password.isEmpty()) {
            serverConfig.setPassword(password);
        }
        logger.info("Connecting: {}", config.toJSON());
        return Redisson.create(config);
    }


}
