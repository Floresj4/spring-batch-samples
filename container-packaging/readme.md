# Container packaging

Package a SpringBoot application as an [Open Container Initiutive](https://opencontainers.org/) (OCI) container.

### Build

`mvn clean spring-boot:build-image`

Will build and tag a container image

`mvn clean package`

Will build an executable jar for local test and development.