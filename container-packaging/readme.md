# Container packaging

Package a SpringBoot application as an [Open Container Initiutive](https://opencontainers.org/) (OCI) container for remote deployment.

This application provides a `RestController` with endpoints for executing batch jobs and checking their status.

## Build

To build and tag a container image

`mvn clean spring-boot:build-image`

To build an executable jar for local test and development.

`mvn clean package`

## Container Execution

Execution with docker requires port binding for the controller handling job execution requests and AWS credentials to download the test file.  The following command will launch the latest container image.

`docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=[your_key_here] -e AWS_SECRET_ACCESS_KEY=[your_key_here] container-packaging:latest`

The embedded tomcat server launches on the default port `8080` for launching jobs.

## Endpoints

Each endpoint returns the same response structure for simplicity.

```
{
    "id": [jobId],
    "status": {
        "exitCode": "",
        "exitDescription": "",
        "running": true
    }
}
```

### /run

Run a batch job.  The response will contain the job id for status checks.

```
POST http://localhost:8080/run
{
    "name": "batchProcessingJob",
    "jobParameters": {
        "inputFile": "s3://..."
    }
}
```

| Field  | Description |
|--------|-------------|
| Name   | The registered bean name for the batch Job |
| jobParameters | A collection of parameters for the batch application. |

### /status
Get a batch job status.

```
GET http://localhost:8080?id={jobId}
```

| Field  | Description   |
|--------|---------------|
| jobId  | job execution id  |

## Local Execution

`java -jar container-packaging.jar inputFile=s3://...`