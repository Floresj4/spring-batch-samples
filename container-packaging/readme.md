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

## Local Execution

`java -jar container-packaging.jar inputFile=s3://...`