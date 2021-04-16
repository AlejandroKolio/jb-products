# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

## Descripion

The goal of service is to provide general information about released products
This is a small demo project based on Spring Boot and some
additional services like redis in Docker containers.

This project requires java 11, kotlin, Docker and Docker Compose, as it is compiled and run in containers.

Building and Running
--------------------
Build containers and run:

    docker-compose up -d

Verify that the app answers at <http://localhost:8080/products/status>

Stop containers:

    docker-compose down

Speciication and API
--------------------
    path to spec: src/main/resources/openapi.yaml
![Alt text](src/main/resources/openapi.png "Title")