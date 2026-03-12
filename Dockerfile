# Stage 1: Build
# Chỉ cần 1 Stage duy nhất là Run
# Dùng bản JRE (Java Runtime Environment) cho nhẹ, không cần bản JDK to
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy file .jar từ thư mục build của máy host (hoặc GitHub Actions) vào container
# Lưu ý: Đường dẫn này phải khớp với nơi sinh ra file jar
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]