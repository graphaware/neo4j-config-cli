FROM eclipse-temurin:17.0.5_8-jre-ubi9-minimal

RUN mkdir -p /opt
COPY target/neo4j-config-cli-*.jar /opt/app.jar

VOLUME /import

USER 1001
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app.jar"]
