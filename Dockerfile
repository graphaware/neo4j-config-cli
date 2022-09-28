FROM eclipse-temurin:17-jre-alpine

RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
RUN mkdir -p /opt && chown -R 1001:1001 /opt
COPY target/neo4j-config-cli-*.jar /opt/app.jar
USER 1001
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
