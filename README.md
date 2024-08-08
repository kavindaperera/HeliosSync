# 🌟 HeliosSync: Illuminate Your Data Pipelines with Seamless Synchronization 🌟

HeliosSync is a Java-based Cloud Function designed to synchronize data from Google BigQuery or Google Cloud Storage (GCS) to a Redis database. This tool allows for daily synchronization and is highly configurable, making it easy to adapt to various data sources and requirements.

## Features 🚀

- **Configurable**: Set up environment variables to customize the data source and Redis configuration. 🛠️
- **Support for BigQuery and GCS**: Load data from Google BigQuery or GCS and push it to Redis. 🌐
- **Logging**: Integrated with Log4j for robust logging. 📝
- **Modular Design**: Utilizes a parent-child class architecture for maintainability and extensibility. 🔧

## Prerequisites 📋

- **Google Cloud Account**: Set up Google Cloud Storage and/or BigQuery. ☁️
- **Redis Server**: Accessible Redis instance. 🔴
- **Java 11**: The project uses Java 11 for development. ☕
- **Maven**: For building the project. 🔨

## Setup and Configuration ⚙️

1. **Clone the Repository**

    ```bash
    git clone https://github.com/kavindaperera/HeliosSync.git
    cd helios-sync
    ```

2. **Configure Environment Variables**

    Set the following environment variables in your deployment environment:

    ```bash
    export REDIS_HOST=your-redis-host
    export REDIS_PORT=6379
    export BIGQUERY_QUERY="SELECT * FROM `your_project.your_dataset.your_table`"
    export GCS_BUCKET=your-gcs-bucket
    ```

3. **Build the Project**

    Use Maven to build the project:

    ```bash
    mvn clean package
    ```

4. **Deploy the Cloud Function**

    Deploy the Cloud Function using the Google Cloud CLI:

    ```bash
    gcloud functions deploy HeliosSync \
        --entry-point com.nova.HeliosSync \
        --runtime java11 \
        --trigger-http \
        --allow-unauthenticated \
        --region YOUR_REGION \
        --set-env-vars REDIS_HOST=your-redis-host,REDIS_PORT=6379,BIGQUERY_QUERY="SELECT * FROM `your_project.your_dataset.your_table`",GCS_BUCKET=your-gcs-bucket
    ```

## Usage 📤

The Cloud Function can be triggered via HTTP requests. The `Trigger` header determines which data source to use:

- **Trigger with BigQuery**:

    ```bash
    curl -X GET https://REGION-PROJECT_ID.cloudfunctions.net/HeliosSync \
        -H "Trigger: BigQuery"
    ```

- **Trigger with GCS**:

    ```bash
    curl -X GET https://REGION-PROJECT_ID.cloudfunctions.net/HeliosSync \
        -H "Trigger: GCS"
    ```

## Testing 🧪

Unit tests are provided to ensure the functionality of the core components. Use Maven to run the tests:

```bash
mvn test
