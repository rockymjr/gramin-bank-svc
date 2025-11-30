# Stage 1: build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# copy maven wrapper & pom first for layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# download deps
RUN chmod +x ./mvnw && ./mvnw -B dependency:go-offline

# copy source and build
COPY src ./src
RUN ./mvnw -B -DskipTests package

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# create non-root user
RUN useradd -m appuser
USER appuser

# copy jar from builder
COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
