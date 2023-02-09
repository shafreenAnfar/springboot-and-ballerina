# springboot-and-ballerina

A sample code base which touches key features of each technology. The sample is based on a simple API written for a social-media site which has users and associated posts. Following is the high level component diagram.

<img src="springboot-and-ballerina.png" alt="drawing" width='500'/>

Following are the features used for the implementation

1. Configuring verbs and URLs
2. Error handlers for sending customized error messages
3. Adding constraints/validations
4. OpenAPI specification for Generating API docs
5. Accessing database
6. Configurability
7. HTTP client 
8. Resiliency - Retry
9. Docker image generation
10. Tracing (Couldn’t get it to work with Spring boot Feign client)

# Setting up each environment

## Spring boot
You can use the below docker compose commands.
1. docker compose -f springboot-docker-compose-db.yml up
2. docker compose -f springboot-docker-compose.yml up

Or you can use the below docker commands.

Order|Service| Docker command
-|-|-
1|MqSQL| docker run --env MYSQL_ROOT_PASSWORD=dummypassword --env MYSQL_USER=social_media_user --env MYSQL_PASSWORD=dummypassword --env MYSQL_DATABASE=social_media_database --name mysql -p 3306:3306 --network host mysql:8-oracle
2|Zipkin| docker run -p 9411:9411 --network host openzipkin/zipkin
3|Sentiment analysis| docker run -p 8088:8088 --network host shafreen/springboot-sentiment-api:0.0.1
4|Social Media| docker run -p 8080:8080 --network host shafreen/springboot-social-media:0.0.1

### Try out
- To send request open `springboot-social-media.http` file using VS Code with `REST Client` extension
- Zipkin URL - http://localhost:9411/zipkin/

