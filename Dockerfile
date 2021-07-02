FROM maven:3.8.1-jdk-11-slim as build
COPY src /build/src
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml clean package

FROM openjdk:11-jre-slim
COPY --from=build /build/target/NoPeak*.jar /usr/local/lib/noPeak.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/noPeak.jar"]
