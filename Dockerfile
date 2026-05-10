# ==========================================
# ビルドステージ（Build Stage）
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Gradle Wrapper と設定ファイルをコピー（キャッシュ活用）
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# 依存関係を先にダウンロード（ソース変更時のキャッシュ活用）
RUN ./gradlew dependencies --no-daemon || true

# ソースコードをコピーしてビルド
COPY src src
RUN ./gradlew bootJar --no-daemon

# ==========================================
# 実行ステージ（Production Stage）
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

# ビルド成果物のみをコピー（イメージサイズ削減）
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
