package com.nova.sync.loader;


import com.google.cloud.bigquery.*;
import com.nova.config.Settings;
import com.nova.core.DataLoader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.redisson.api.RedissonClient;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code BigQueryDataLoader} is a class responsible for loading data from Google BigQuery
 * and storing it as CSV in a Redis database.
 * <p>
 * This class retrieves the BigQuery project ID and query from the configuration, executes
 * the query, converts the results to CSV format, and stores the CSV data in Redis using
 * the current date as the key.
 * </p>
 */
public class BigQueryDataLoader extends DataLoader {

    private static final String BIGQUERY_PROJECT_ID = Settings.get("bigquery.project.id");
    private static final String BIGQUERY_QUERY = Settings.get("bigquery.query");

    /**
     * Constructs a {@code BigQueryDataLoader} with the specified Redisson client.
     *
     * @param redisson the Redisson client used to interact with Redis
     */
    public BigQueryDataLoader(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    public void loadData() {

        // Validate configuration
        if (BIGQUERY_PROJECT_ID == null || BIGQUERY_QUERY == null) {
            logger.error("BigQuery project ID or query not specified in environment variables.");
            return;
        }

        try {
            // Initialize BigQuery client
            BigQuery bigquery = BigQueryOptions.newBuilder()
                    .setProjectId(BIGQUERY_PROJECT_ID)
                    .build()
                    .getService();

            // Execute BigQuery query
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(BIGQUERY_QUERY).build();

            TableResult result = bigquery.query(queryConfig);

            // Get column names from the query result
            List<String> headers = Objects.requireNonNull(result.getSchema()).getFields().stream()
                    .map(Field::getName)
                    .toList();

            // Convert query results to CSV format
            StringWriter stringWriter = new StringWriter();
            CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader(headers.toArray(new String[0]))
                    .build();

            try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
                result.iterateAll().forEach(row -> {
                    try {
                        List<String> values = headers.stream()
                                .map(header -> row.get(header).getStringValue())
                                .collect(Collectors.toList());
                        csvPrinter.printRecord(values);
                    } catch (IOException e) {
                        logger.error("Error writing record to CSV: ", e);
                    }
                });
            }

            // Get the current date and use it as the Redis key
            String currentDate = LocalDate.now().toString();
            String csvData = stringWriter.toString();

            // Store the CSV data in Redis
            redisson.getBucket(currentDate).set(csvData);

            logger.info("Data loaded from BigQuery to Redis with key: {}", currentDate);

        } catch (Exception e) {
            logger.error("Error loading data from BigQuery: ", e);
        }
    }

}
