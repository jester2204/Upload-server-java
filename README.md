# Server: Photo Uploader (Spring Boot + MinIO + MySQL)

## Быстрый старт
1. Установи Docker Desktop, JDK 17 и Maven.
2. Подними инфраструктуру (MinIO + MySQL):
```bash
cd server_loader_photo
docker compose up -d
```
3. Запусти Spring Boot:
```bash
mvn -q -DskipTests spring-boot:run
```
4. Проверка загрузки:
```bash
curl -F "file=@/absolute/path/to/image.jpg" http://localhost:8080/api/photos
```

## Сервисы и доступы
- MinIO Console: `http://localhost:9001` (login `minioadmin`, pass `minioadmin123`)
- MinIO S3 API: `http://localhost:9000`
- MySQL: порт хоста `3307`, БД `photodb`, пользователь `app`/`app123`

## Конфигурация (src/main/resources/application.properties)
- Datasource: `jdbc:mysql://localhost:3307/photodb`
- MinIO: endpoint `http://localhost:9000`, bucket `photos` (создаётся автоматически)
- Multipart: `spring.servlet.multipart.max-file-size=20MB`
- CORS: открыт для прототипа (`*`)

## API
- POST `/api/photos` — multipart upload (`file`), сохраняет в MinIO и метаданные в БД, возвращает JSON с данными и presigned URL.
- GET `/api/photos` — список метаданных
- GET `/api/photos/{id}/url` — presigned URL на скачивание

## Архитектура
- `config/MinioConfig.java` — клиент MinIO и автосоздание бакета
- `config/CorsConfig.java` — глобальный CORS-фильтр
- `photo/model/Photo.java` — JPA-сущность
- `photo/repo/PhotoRepository.java` — репозиторий
- `photo/service/PhotoStorageService.java` — загрузка в MinIO, presign URL
- `photo/web/PhotoController.java` — REST-контроллер

## Почему так (обоснование)
- MinIO — S3-совместимое self-hosted объектное хранилище: идеально для фото/файлов (дешёвое blob-хранилище, presigned-URL, горизонтальное масштабирование). Локально поднимается одной командой.
- Docker Compose — логичный способ контейнеризации инфраструктуры: воспроизводимое окружение, быстрый старт.
- Spring Boot — зрелый стек для REST и multipart; простые CORS/валидация, интеграция с JPA.
- MySQL — надёжная реляционная БД для метаданных (object name, type, size, createdAt), переносима в облака.
- Надёжная структура — бинарные данные в объектном хранилище, метаданные в БД; чистое разделение слоёв (config/model/repo/service/web) повышает поддерживаемость и масштабируемость.

## Примечания
- Если healthcheck MinIO в docker-compose не проходит, можно заменить:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -sf http://localhost:9000/minio/health/live || exit 1"]
  interval: 5s
  timeout: 5s
  retries: 20
```
