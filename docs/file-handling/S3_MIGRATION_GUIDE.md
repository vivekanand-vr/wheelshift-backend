# S3 Migration Guide - WheelShift Pro File Storage

## Overview

This guide explains how to migrate from local file storage to AWS S3 (or any S3-compatible cloud storage) with **zero changes** to your entity files and minimal changes to your application code.

## Why This Migration is Easy

- ✅ **File IDs Instead of URLs**: Entities store UUIDs, not hardcoded URLs
- ✅ **Centralized Metadata**: All file information in `file_metadata` table
- ✅ **Abstract Storage Layer**: Service layer handles storage implementation
- ✅ **Backward Compatible**: Can run hybrid (local + S3) during migration

---

## Architecture Comparison

### Current Architecture (Local Storage)
```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
│  Car Entity: primaryImageId = "550e8400-..."            │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  FileStorageService                     │
│  - uploadFile() → saves to /uploads/images/             │
│  - getFileAsResource() → reads from local disk          │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   file_metadata Table                   │
│  file_id: 550e8400-...                                  │
│  storage_path: images/20260131_143022_550e8400.jpg      │
│  public_url: http://localhost:8080/api/v1/files/550e... │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  Local File System                      │
│  /uploads/images/20260131_143022_550e8400.jpg           │
└─────────────────────────────────────────────────────────┘
```

### After S3 Migration
```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
│  Car Entity: primaryImageId = "550e8400-..."            │
│              (NO CHANGES!)                              │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                  FileStorageService                     │
│  - uploadFile() → saves to S3                           │
│  - getFileAsResource() → generates S3 presigned URL     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   file_metadata Table                   │
│  file_id: 550e8400-...                                  │
│  storage_path: images/20260131_143022_550e8400.jpg      │
│  public_url: https://bucket.s3.region.amazonaws.com/... │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      AWS S3 Bucket                      │
│  wheelshift-pro-files/images/20260131_...jpg            │
└─────────────────────────────────────────────────────────┘
```

---

## Migration Steps

### Phase 1: Preparation

#### 1.1 Add AWS SDK Dependency

Add to `pom.xml`:
```xml
<!-- AWS S3 SDK -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.56</version>
</dependency>
```

#### 1.2 Configure AWS Credentials

Add to `application.properties`:
```properties
# Storage Type Configuration
file.storage.type=s3
# Options: local, s3, hybrid

# AWS S3 Configuration
aws.s3.bucket-name=wheelshift-pro-files
aws.s3.region=us-east-1
aws.s3.access-key=${AWS_ACCESS_KEY}
aws.s3.secret-key=${AWS_SECRET_KEY}

# S3 Public URL (for CloudFront or direct S3)
aws.s3.public-url=https://wheelshift-pro-files.s3.us-east-1.amazonaws.com
# Or use CloudFront: https://d123456789.cloudfront.net
```

Or use environment variables:
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

#### 1.3 Create S3 Bucket

```bash
# Using AWS CLI
aws s3 mb s3://wheelshift-pro-files --region us-east-1

# Set bucket policy for public read (optional)
aws s3api put-bucket-policy --bucket wheelshift-pro-files --policy file://bucket-policy.json
```

**bucket-policy.json:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::wheelshift-pro-files/*"
    }
  ]
}
```

### Phase 2: Code Updates

#### 2.1 Create S3 Configuration Class

```java
package com.wheelshiftpro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                    )
                )
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                    )
                )
                .build();
    }
}
```

#### 2.2 Create Storage Strategy Interface

```java
package com.wheelshiftpro.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageStrategy {
    
    /**
     * Store file and return storage path
     */
    String store(MultipartFile file, String storagePath) throws IOException;
    
    /**
     * Load file as resource
     */
    Resource loadAsResource(String storagePath) throws IOException;
    
    /**
     * Delete file
     */
    void delete(String storagePath) throws IOException;
    
    /**
     * Generate public URL
     */
    String generatePublicUrl(String fileId);
}
```

#### 2.3 Create Local Storage Strategy

```java
package com.wheelshiftpro.service.storage;

import com.wheelshiftpro.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component("localStorageStrategy")
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${file.storage.base-path:uploads}")
    private String baseStoragePath;

    @Value("${file.storage.base-url:/api/v1/files}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String storagePath) throws IOException {
        Path location = Paths.get(baseStoragePath).resolve(storagePath);
        Files.createDirectories(location.getParent());
        Files.copy(file.getInputStream(), location, StandardCopyOption.REPLACE_EXISTING);
        return storagePath;
    }

    @Override
    public Resource loadAsResource(String storagePath) throws IOException {
        try {
            Path filePath = Paths.get(baseStoragePath).resolve(storagePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + storagePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("File not found: " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) throws IOException {
        Path filePath = Paths.get(baseStoragePath).resolve(storagePath).normalize();
        Files.deleteIfExists(filePath);
    }

    @Override
    public String generatePublicUrl(String fileId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(baseUrl)
                .path("/")
                .path(fileId)
                .toUriString();
    }
}
```

#### 2.4 Create S3 Storage Strategy

```java
package com.wheelshiftpro.service.storage;

import com.wheelshiftpro.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Component("s3StorageStrategy")
@RequiredArgsConstructor
@Slf4j
public class S3StorageStrategy implements StorageStrategy {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-url}")
    private String publicUrl;

    @Override
    public String store(MultipartFile file, String storagePath) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("File uploaded to S3: {}", storagePath);
            return storagePath;
            
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", storagePath, e);
            throw new FileStorageException("Failed to upload file to S3", e);
        }
    }

    @Override
    public Resource loadAsResource(String storagePath) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            byte[] data = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
            return new ByteArrayResource(data);
            
        } catch (S3Exception e) {
            log.error("Failed to load file from S3: {}", storagePath, e);
            throw new FileStorageException("Failed to load file from S3", e);
        }
    }

    @Override
    public void delete(String storagePath) throws IOException {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted from S3: {}", storagePath);
            
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", storagePath, e);
            throw new FileStorageException("Failed to delete file from S3", e);
        }
    }

    @Override
    public String generatePublicUrl(String fileId) {
        // For public buckets, return direct URL
        return publicUrl + "/" + fileId;
    }

    /**
     * Generate presigned URL for private buckets (alternative approach)
     */
    public String generatePresignedUrl(String storagePath, Duration duration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
            
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL for: {}", storagePath, e);
            throw new FileStorageException("Failed to generate presigned URL", e);
        }
    }
}
```

#### 2.5 Update FileStorageServiceImpl

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileMetadataMapper fileMetadataMapper;
    
    @Value("${file.storage.type:local}")
    private String storageType;
    
    private final Map<String, StorageStrategy> storageStrategies;
    
    // Spring will inject all StorageStrategy beans
    @Autowired
    public FileStorageServiceImpl(
            FileMetadataRepository fileMetadataRepository,
            FileMetadataMapper fileMetadataMapper,
            List<StorageStrategy> strategies) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileMetadataMapper = fileMetadataMapper;
        this.storageStrategies = strategies.stream()
                .collect(Collectors.toMap(
                    s -> s.getClass().getAnnotation(Component.class).value(),
                    s -> s
                ));
    }
    
    private StorageStrategy getStorageStrategy() {
        String strategyName = storageType.equals("s3") 
            ? "s3StorageStrategy" 
            : "localStorageStrategy";
        return storageStrategies.get(strategyName);
    }
    
    @Override
    public FileMetadataResponse uploadFile(MultipartFile file, FileUploadRequest request) {
        // ... existing validation code ...
        
        StorageStrategy strategy = getStorageStrategy();
        
        try {
            // Store file using selected strategy
            String storagePath = strategy.store(file, relativePath);
            
            // Generate public URL using selected strategy
            String publicUrl = strategy.generatePublicUrl(fileId);
            
            // ... rest of the method remains the same ...
        } catch (IOException e) {
            throw new FileStorageException("Could not store file", e);
        }
    }
    
    @Override
    public Resource getFileAsResource(String fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findByFileIdAndStatus(fileId, FileStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        StorageStrategy strategy = getStorageStrategy();
        
        try {
            return strategy.loadAsResource(fileMetadata.getStoragePath());
        } catch (IOException e) {
            throw new FileStorageException("File not found", e);
        }
    }
    
    @Override
    public void hardDeleteFile(String fileId) {
        FileMetadata fileMetadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "fileId", fileId));
        
        StorageStrategy strategy = getStorageStrategy();
        
        try {
            strategy.delete(fileMetadata.getStoragePath());
        } catch (IOException e) {
            log.error("Failed to delete physical file: {}", fileMetadata.getStoragePath(), e);
            throw new FileStorageException("Could not delete file from storage", e);
        }
        
        fileMetadataRepository.delete(fileMetadata);
    }
}
```

### Phase 3: Data Migration

#### 3.1 Migration Script

```java
package com.wheelshiftpro.migration;

import com.wheelshiftpro.entity.FileMetadata;
import com.wheelshiftpro.repository.FileMetadataRepository;
import com.wheelshiftpro.service.storage.S3StorageStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3MigrationRunner implements CommandLineRunner {

    private final FileMetadataRepository fileMetadataRepository;
    private final S3StorageStrategy s3StorageStrategy;
    
    @Value("${file.storage.base-path:uploads}")
    private String localBasePath;
    
    @Value("${migration.enabled:false}")
    private boolean migrationEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!migrationEnabled) {
            log.info("Migration disabled. Set migration.enabled=true to run.");
            return;
        }
        
        log.info("Starting S3 migration...");
        
        List<FileMetadata> allFiles = fileMetadataRepository.findAll();
        int successCount = 0;
        int failureCount = 0;
        
        for (FileMetadata fileMetadata : allFiles) {
            try {
                migrateFile(fileMetadata);
                successCount++;
                log.info("Migrated: {} ({}/{})", 
                    fileMetadata.getOriginalFilename(), 
                    successCount + failureCount, 
                    allFiles.size());
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to migrate: {}", fileMetadata.getOriginalFilename(), e);
            }
        }
        
        log.info("Migration completed. Success: {}, Failures: {}", successCount, failureCount);
    }
    
    private void migrateFile(FileMetadata fileMetadata) throws Exception {
        // Load file from local storage
        Path localPath = Paths.get(localBasePath).resolve(fileMetadata.getStoragePath());
        byte[] fileContent = Files.readAllBytes(localPath);
        
        // Create MultipartFile from local file
        MultipartFile multipartFile = new MockMultipartFile(
            fileMetadata.getStoredFilename(),
            fileMetadata.getOriginalFilename(),
            fileMetadata.getMimeType(),
            fileContent
        );
        
        // Upload to S3
        s3StorageStrategy.store(multipartFile, fileMetadata.getStoragePath());
        
        // Update public URL
        String newPublicUrl = s3StorageStrategy.generatePublicUrl(fileMetadata.getFileId());
        fileMetadata.setPublicUrl(newPublicUrl);
        fileMetadataRepository.save(fileMetadata);
    }
}
```

#### 3.2 Run Migration

```properties
# application.properties
migration.enabled=true
file.storage.type=s3
```

```bash
# Run the application - migration will execute on startup
./mvnw spring-boot:run
```

### Phase 4: Verification

#### 4.1 Test Endpoints

```bash
# Upload new file (should go to S3)
curl -X POST http://localhost:8080/api/v1/files/upload \
  -F "file=@test.jpg" \
  -F "uploadSource=test"

# Download file (should come from S3)
curl -X GET http://localhost:8080/api/v1/files/{fileId}

# Check statistics
curl -X GET http://localhost:8080/api/v1/files/statistics
```

#### 4.2 Verify S3 Contents

```bash
# List files in S3
aws s3 ls s3://wheelshift-pro-files/ --recursive

# Check file count
aws s3 ls s3://wheelshift-pro-files/ --recursive | wc -l
```

---

## Hybrid Approach (During Migration)

You can run both local and S3 storage simultaneously:

```java
@Service
public class HybridStorageStrategy implements StorageStrategy {
    
    private final LocalStorageStrategy localStrategy;
    private final S3StorageStrategy s3Strategy;
    private final FileMetadataRepository repository;
    
    @Override
    public String store(MultipartFile file, String storagePath) throws IOException {
        // Store in both locations
        localStrategy.store(file, storagePath);
        return s3Strategy.store(file, storagePath);
    }
    
    @Override
    public Resource loadAsResource(String storagePath) throws IOException {
        // Try S3 first, fallback to local
        try {
            return s3Strategy.loadAsResource(storagePath);
        } catch (Exception e) {
            log.warn("S3 load failed, trying local: {}", storagePath);
            return localStrategy.loadAsResource(storagePath);
        }
    }
}
```

---

## Cost Optimization

### 1. Use CloudFront CDN
```properties
# application.properties
aws.cloudfront.domain=d123456789.cloudfront.net
aws.s3.public-url=https://d123456789.cloudfront.net
```

### 2. Use S3 Lifecycle Policies
```json
{
  "Rules": [
    {
      "Id": "ArchiveOldFiles",
      "Status": "Enabled",
      "Transitions": [
        {
          "Days": 90,
          "StorageClass": "GLACIER"
        }
      ]
    }
  ]
}
```

### 3. Enable S3 Intelligent-Tiering
```bash
aws s3api put-bucket-intelligent-tiering-configuration \
  --bucket wheelshift-pro-files \
  --id auto-archive \
  --intelligent-tiering-configuration file://tiering-config.json
```

---

## Security Best Practices

### 1. Private Buckets + Presigned URLs
```java
// For private files (documents, etc.)
public String getDocumentUrl(String fileId) {
    FileMetadata metadata = repository.findByFileId(fileId).orElseThrow();
    return s3StorageStrategy.generatePresignedUrl(
        metadata.getStoragePath(), 
        Duration.ofHours(1) // URL expires in 1 hour
    );
}
```

### 2. Enable S3 Encryption
```bash
aws s3api put-bucket-encryption \
  --bucket wheelshift-pro-files \
  --server-side-encryption-configuration \
  '{"Rules": [{"ApplyServerSideEncryptionByDefault": {"SSEAlgorithm": "AES256"}}]}'
```

### 3. Use IAM Roles (Production)
```java
// Instead of access keys in config
@Bean
public S3Client s3Client() {
    return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(InstanceProfileCredentialsProvider.create())
            .build();
}
```

---

## Rollback Plan

If you need to rollback to local storage:

1. Change configuration:
```properties
file.storage.type=local
```

2. Keep S3 files as backup
3. Application will use local files again
4. No entity changes needed!

---

## Summary

- ✅ **Zero Entity Changes**: File IDs remain the same
- ✅ **Flexible Architecture**: Switch between local/S3/hybrid
- ✅ **Gradual Migration**: Migrate at your own pace
- ✅ **Easy Rollback**: Change one config property
- ✅ **Future Proof**: Can switch to any cloud provider

**Key Takeaway**: Because you're storing file IDs instead of URLs, migrating storage backends is just a matter of updating the storage strategy implementation!

---

**Last Updated:** January 31, 2026
**Version:** 1.0