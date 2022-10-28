FROM maven:3.8.1-jdk-11-slim as build
COPY src /build/src
COPY pom.xml /build/pom.xml
RUN mvn -f /build/pom.xml clean package

FROM openjdk:11-jre-slim
RUN apt-get update && apt-get install -y psutils procps && apt-get clean
COPY --from=build /build/target/NoPeak*.jar /usr/local/lib/noPeak.jar
COPY estimateFragLength.jar /usr/local/lib/estimateFragLength.jar
COPY container_scripts/* /bin/
CMD ["java","-jar","/usr/local/lib/noPeak.jar"]
