package com.nova.core;


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.nova.config.ConfigLoader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.redisson.api.RedissonClient;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;

public class BigQueryDataLoader extends DataLoader {

    private static final String BIGQUERY_PROJECT_ID = ConfigLoader.get("bigquery.project.id");
    private static final String BIGQUERY_QUERY = ConfigLoader.get("bigquery.query");

    public BigQueryDataLoader(RedissonClient redisson) {
        super(redisson);
    }

    @Override
    public void loadData() {

        if (BIGQUERY_PROJECT_ID == null || BIGQUERY_QUERY == null) {
            logger.error("BigQuery project ID or query not specified in environment variables.");
            return;
        }

        try {

            BigQuery bigquery = BigQueryOptions.newBuilder()
                    .setProjectId(BIGQUERY_PROJECT_ID)
                    .build()
                    .getService();

            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(BIGQUERY_QUERY).build();

            TableResult result = bigquery.query(queryConfig);

            // Convert query results to CSV format
            StringWriter stringWriter = new StringWriter();
            CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                    .setHeader("name", "age")
                    .build();

            try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
                result.iterateAll().forEach(row -> {
                    try {
                        csvPrinter.printRecord(row.get("name").getStringValue(), row.get("age").getStringValue());
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
