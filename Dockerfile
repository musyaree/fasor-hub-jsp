FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM tomcat:10.1-jdk21
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /src/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
ENV JAVA_OPTS="-Duser.timezone=Asia/Jakarta"
EXPOSE 8080
