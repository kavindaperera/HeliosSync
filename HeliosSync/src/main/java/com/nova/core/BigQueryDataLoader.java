package com.nova.core;


import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.nova.config.ConfigLoader;
import org.redisson.api.RedissonClient;

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

        BigQuery bigquery = BigQueryOptions.newBuilder()
                .setProjectId(BIGQUERY_PROJECT_ID)
                .build()
                .getService();

        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(BIGQUERY_QUERY).build();

        try {
            TableResult result = bigquery.query(queryConfig);
            result.iterateAll().forEach(row -> {
                redisson.getBucket(row.get("id").getStringValue()).set(row.get("value").getStringValue());
            });
            logger.info("Data loaded from BigQuery to Redis.");
        } catch (Exception e) {
            logger.error("Error loading data from BigQuery: ", e);
        }
    }

}
