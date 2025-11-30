# Roadmap Seeder Debug Guide - Cloud Run Deployment

## Vấn đề
Ở local thì roadmap thêm vào Postgres được, nhưng khi deploy lên Google Cloud Run thì log "→ Using Google Cloud credentials from environment variable" có chạy mà không thấy thêm vào Postgres trong Google Cloud SQL.

## Các log đã thêm để debug

### 1. Constructor Logs
```
=== RoadmapSeeder Constructor Started ===
→ Using Google Cloud credentials from environment variable
→ Credentials JSON length: {số ký tự} characters
✓ Successfully loaded credentials from environment variable
✓ Google Cloud Storage initialized successfully
RoadmapSeeder initialized - Storage: OK, RoadmapRepo: OK, WeaviateClient: OK
```

### 2. Run Method Logs
```
=== RoadmapSeeder.run() started ===
Bucket name: {bucket-name}
Prefix: {prefix}
Starting to seed roadmaps from GCS bucket: {bucket}/{prefix}
=== RoadmapSeeder.run() completed successfully ===
```

### 3. Listing Files Logs
```
    → Listing files from bucket '{bucket}' with prefix '{prefix}'
      ✓ Added CSV file: {filename}
    → Total blobs: {count}, CSV files: {count}
```

### 4. Processing Files Logs
```
→ Processing file: {file} -> Roadmap: {name}
  Roadmap '{name}' - Postgres: {true/false}, Weaviate: {true/false}
  ✓ Roadmap '{name}' exists in both systems, skipping
  → Adding roadmap '{name}' to Postgres only
  ✓ Successfully added roadmap '{name}' to Postgres
```

### 5. Database Save Logs
```
    → Reading CSV from GCS: {bucket}/{file}
    → Read {count} records from CSV
    → Saving roadmap '{name}' with {count} topics to Postgres
    ✓ Successfully saved roadmap '{name}' with ID: {id}
```

### 6. Error Logs
```
    ✗ Failed to save roadmap '{name}' to Postgres: {error message}
```

## Các điểm cần kiểm tra trong Cloud Run logs

### 1. Kiểm tra Storage có được khởi tạo không
Tìm log:
```
RoadmapSeeder initialized - Storage: OK, RoadmapRepo: OK, WeaviateClient: OK
```

**Nếu thấy "Storage: NULL":**
- Credentials không được load đúng
- Kiểm tra biến môi trường `GOOGLE_CLOUD_CREDENTIALS_JSON`

### 2. Kiểm tra RoadmapRepo có được inject không
Tìm log:
```
RoadmapSeeder initialized - Storage: OK, RoadmapRepo: OK, WeaviateClient: OK
```

**Nếu thấy "RoadmapRepo: NULL":**
- Spring không inject được repository
- Kiểm tra kết nối database
- Kiểm tra datasource configuration

### 3. Kiểm tra run() có được gọi không
Tìm log:
```
=== RoadmapSeeder.run() started ===
```

**Nếu không thấy log này:**
- CommandLineRunner không được trigger
- Có thể bị exception trong constructor

### 4. Kiểm tra bucket name và prefix
Tìm log:
```
Bucket name: {name}
Prefix: {prefix}
```

**Kiểm tra:**
- Bucket name có đúng không
- Prefix có đúng không
- Service account có quyền truy cập không

### 5. Kiểm tra có list được files không
Tìm log:
```
→ Found {count} CSV files
```

**Nếu count = 0:**
- Bucket không có files
- Prefix không đúng
- Service account không có quyền

### 6. Kiểm tra database save operation
Tìm log:
```
    → Saving roadmap '{name}' with {count} topics to Postgres
    ✓ Successfully saved roadmap '{name}' with ID: {id}
```

**Nếu không thấy log "Successfully saved":**
- Tìm log "✗ Failed to save roadmap"
- Kiểm tra connection đến Cloud SQL
- Kiểm tra transaction có commit không

### 7. Kiểm tra summary
Tìm log:
```
=== Seeding Summary: Processed={count}, Skipped={count}, Errors={count} ===
```

## Các lỗi thường gặp và cách khắc phục

### Lỗi 1: Storage is NULL
**Nguyên nhân:**
- Credentials không được load

**Cách khắc phục:**
1. Kiểm tra biến môi trường trong Cloud Run:
   ```bash
   gcloud run services describe [SERVICE_NAME] --region=[REGION] --format="value(spec.template.spec.containers[0].env)"
   ```

2. Verify credentials JSON format:
   - Phải là valid JSON
   - Không có line breaks trong env variable

### Lỗi 2: Cannot connect to Cloud SQL
**Nguyên nhân:**
- Cloud Run không kết nối được với Cloud SQL
- Connection string không đúng

**Cách khắc phục:**
1. Kiểm tra Cloud SQL connection trong Cloud Run:
   ```yaml
   cloudsql-instances: [PROJECT:REGION:INSTANCE]
   ```

2. Kiểm tra datasource URL:
   ```yaml
   spring.datasource.url: jdbc:postgresql:///<database>?cloudSqlInstance=[PROJECT:REGION:INSTANCE]&socketFactory=com.google.cloud.sql.postgres.SocketFactory
   ```

### Lỗi 3: Transaction không commit
**Nguyên nhân:**
- `@Transactional` không hoạt động với self-invocation

**Cách khắc phục:**
- Đã có warning trong code về vấn đề này
- Cần refactor để gọi method qua injected service thay vì `this`

### Lỗi 4: RoadmapRepo is NULL
**Nguyên nhân:**
- Database connection không được thiết lập
- Spring Data JPA không được cấu hình đúng

**Cách khắc phục:**
1. Kiểm tra database connection trong logs
2. Kiểm tra JPA configuration
3. Kiểm tra Cloud SQL instance đang running

## Các bước debug khi deploy lên Cloud Run

### Bước 1: Xem logs ngay sau khi deploy
```bash
gcloud run logs read --service=[SERVICE_NAME] --region=[REGION] --limit=100
```

### Bước 2: Filter logs của RoadmapSeeder
```bash
gcloud run logs read --service=[SERVICE_NAME] --region=[REGION] --filter="textPayload:RoadmapSeeder"
```

### Bước 3: Tìm error logs
```bash
gcloud run logs read --service=[SERVICE_NAME] --region=[REGION] --filter="severity>=ERROR"
```

### Bước 4: Kiểm tra startup sequence
Thứ tự logs phải là:
1. Constructor logs
2. Storage initialization
3. run() method
4. Listing files
5. Processing files
6. Database operations

### Bước 5: Kiểm tra transaction logs
Tìm logs:
- "→ Saving roadmap"
- "✓ Successfully saved roadmap"
- "✗ Failed to save roadmap"

## Checklist để verify

- [ ] Log "=== RoadmapSeeder Constructor Started ===" xuất hiện
- [ ] Log "Storage: OK" xuất hiện (không phải NULL)
- [ ] Log "RoadmapRepo: OK" xuất hiện (không phải NULL)
- [ ] Log "=== RoadmapSeeder.run() started ===" xuất hiện
- [ ] Log "Bucket name" và "Prefix" có giá trị đúng
- [ ] Log "Found X CSV files" với X > 0
- [ ] Log "Processing file" xuất hiện cho từng file
- [ ] Log "Roadmap 'X' - Postgres: false" (nếu chưa có trong DB)
- [ ] Log "→ Saving roadmap" xuất hiện
- [ ] Log "✓ Successfully saved roadmap with ID: X" xuất hiện
- [ ] Log "=== Seeding Summary: Processed=X" xuất hiện

## Thêm debug logging tạm thời

Nếu vẫn chưa tìm ra lỗi, có thể thêm log sau vào `seedRoadmapToPostgresOnly`:

```java
// Trước khi save
log.info("    → roadmapRepo instance: {}", roadmapRepo.getClass().getName());
log.info("    → Roadmap object: name={}, topics={}", roadmap.getName(), roadmap.getTopics().size());

// Sau khi save
log.info("    → Transaction active: {}", TransactionSynchronizationManager.isActualTransactionActive());
log.info("    → Datasource: {}", dataSource.getConnection().getMetaData().getURL());
```

## Kết luận

Với các logs đã thêm, bạn sẽ biết chính xác:
1. Storage có được khởi tạo không
2. Repository có được inject không
3. Files có được list không
4. Database save operation có thành công không
5. Lỗi xảy ra ở đâu trong flow

Khi có lỗi, logs sẽ chỉ ra đúng vị trí và message chi tiết để debug.

