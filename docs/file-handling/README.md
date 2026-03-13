# File Storage System - Implementation Guide

## Overview

This is a comprehensive file storage system for WheelShift Pro that acts as an internal S3-like storage solution. It supports multiple file types (images, PDFs, Excel, CSV, documents) with automatic segregation, metadata tracking, and easy retrieval using file IDs and public URLs.

## Features

- ✅ **Multi-type File Support**: Images, PDFs, Excel, CSV, Documents, and Others
- ✅ **Automatic File Segregation**: Files organized by type in separate directories
- ✅ **UUID-based File IDs**: Unique identifiers for each file
- ✅ **Public URL Generation**: Direct access URLs for file retrieval
- ✅ **File Metadata Tracking**: Complete metadata including size, type, upload source
- ✅ **Batch Upload Support**: Upload multiple files in one request
- ✅ **Soft & Hard Delete**: Flexible deletion options
- ✅ **File Archiving**: Archive files without deletion
- ✅ **Storage Statistics**: Track storage usage by file type
- ✅ **File Validation**: Size and type validation before upload
- ✅ **REST API**: Complete RESTful API with Swagger documentation

## Architecture

The system follows the WheelShift Pro layered architecture:

```
┌─────────────────────────────────────┐
│    FileStorageController            │  ← REST endpoints
├─────────────────────────────────────┤
│    FileStorageService/Impl          │  ← Business logic
├─────────────────────────────────────┤
│    FileMetadataRepository           │  ← Data access
├─────────────────────────────────────┤
│    FileMetadata Entity              │  ← JPA entity
├─────────────────────────────────────┤
│    MySQL Database + File System     │  ← Persistent storage
└─────────────────────────────────────┘

Supporting:
├── FileType & FileStatus Enums
├── DTOs (Request/Response)
├── FileMetadataMapper
└── Custom Exceptions
```

## File Structure

```
uploads/                          # Base storage directory
├── images/                       # IMAGE files (jpg, png, gif, etc.)
│   ├── 20260131_143022_uuid1.jpg
│   └── 20260131_143045_uuid2.png
├── pdfs/                         # PDF files
│   └── 20260131_143100_uuid3.pdf
├── excel/                        # Excel files (xls, xlsx)
│   └── 20260131_143115_uuid4.xlsx
├── csv/                          # CSV files
│   └── 20260131_143130_uuid5.csv
├── documents/                    # Document files (doc, docx, txt)
│   └── 20260131_143145_uuid6.docx
└── other/                        # Other file types
    └── 20260131_143200_uuid7.zip
```

## Database Schema

### file_metadata Table

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| file_id | VARCHAR(64) | Unique UUID identifier |
| original_filename | VARCHAR(255) | Original uploaded filename |
| stored_filename | VARCHAR(255) | Filename in storage |
| file_type | VARCHAR(20) | FILE_TYPE enum value |
| mime_type | VARCHAR(128) | MIME type of the file |
| file_size | BIGINT | Size in bytes |
| file_extension | VARCHAR(10) | File extension |
| storage_path | VARCHAR(512) | Relative storage path |
| public_url | VARCHAR(512) | Public access URL |
| upload_source | VARCHAR(64) | Source identifier (e.g., car_images) |
| uploaded_by | VARCHAR(128) | User who uploaded |
| status | VARCHAR(20) | FILE_STATUS enum value |
| metadata_json | TEXT | Additional metadata (JSON) |
| created_at | DATETIME | Creation timestamp |
| updated_at | DATETIME | Last update timestamp |

## Installation Steps

### 1. Add Migration File

Place `V11__Add_File_Storage_System.sql` in:
```
src/main/resources/db/migration/
```

### 2. Add Java Files

Add the following files to your project:

**Enums** (`src/main/java/com/wheelshiftpro/enums/`):
- `FileType.java`
- `FileStatus.java`

**Entity** (`src/main/java/com/wheelshiftpro/entity/`):
- `FileMetadata.java`

**DTOs** (`src/main/java/com/wheelshiftpro/dto/`):
- `request/FileUploadRequest.java`
- `response/FileMetadataResponse.java`
- `response/FileBatchUploadResponse.java`

**Repository** (`src/main/java/com/wheelshiftpro/repository/`):
- `FileMetadataRepository.java`

**Mapper** (`src/main/java/com/wheelshiftpro/mapper/`):
- `FileMetadataMapper.java`

**Service** (`src/main/java/com/wheelshiftpro/service/`):
- `FileStorageService.java`
- `impl/FileStorageServiceImpl.java`

**Controller** (`src/main/java/com/wheelshiftpro/controller/`):
- `FileStorageController.java`

**Exception** (`src/main/java/com/wheelshiftpro/exception/`):
- `FileStorageException.java`

### 3. Configuration

Add to `application.properties`:

```properties
# File Storage Configuration
file.storage.base-path=uploads
file.storage.base-url=/api/v1/files

# Spring multipart configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.file-size-threshold=2KB
```

### 4. Create Uploads Directory

Create the base directory (will be auto-created on first upload):
```bash
mkdir uploads
```

## API Endpoints

### Upload Files

#### Single File Upload
```http
POST /api/v1/files/upload
Content-Type: multipart/form-data

Parameters:
- file: MultipartFile (required)
- uploadSource: String (optional) - e.g., "car_images"
- uploadedBy: String (optional) - e.g., "john.doe"
- additionalMetadata: JSON String (optional)

Response:
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileId": "550e8400-e29b-41d4-a716-446655440000",
    "originalFilename": "car-photo.jpg",
    "fileType": "IMAGE",
    "mimeType": "image/jpeg",
    "fileSize": 2048576,
    "publicUrl": "http://localhost:8080/api/v1/files/550e8400-e29b-41d4-a716-446655440000",
    "status": "ACTIVE",
    "createdAt": "2026-01-31T14:30:22"
  }
}
```

#### Batch File Upload
```http
POST /api/v1/files/upload/batch
Content-Type: multipart/form-data

Parameters:
- files: MultipartFile[] (required)
- uploadSource: String (optional)
- uploadedBy: String (optional)

Response:
{
  "success": true,
  "message": "Batch upload completed",
  "data": {
    "totalFiles": 5,
    "successCount": 4,
    "failureCount": 1,
    "successfulUploads": [...],
    "failures": [...]
  }
}
```

### Download/View Files

#### Download File
```http
GET /api/v1/files/{fileId}?download=true

Example:
GET /api/v1/files/550e8400-e29b-41d4-a716-446655440000?download=true

Response: Binary file data with Content-Disposition: attachment
```

#### View File (Inline)
```http
GET /api/v1/files/{fileId}

Example:
GET /api/v1/files/550e8400-e29b-41d4-a716-446655440000

Response: Binary file data with Content-Disposition: inline
```

### Retrieve Metadata

#### Get File Metadata
```http
GET /api/v1/files/{fileId}/metadata

Response:
{
  "success": true,
  "data": {
    "fileId": "550e8400-e29b-41d4-a716-446655440000",
    "originalFilename": "car-photo.jpg",
    "fileType": "IMAGE",
    ...
  }
}
```

#### Get All Files (Paginated)
```http
GET /api/v1/files?page=0&size=10&sort=createdAt,desc

Response:
{
  "success": true,
  "data": {
    "content": [...],
    "totalElements": 50,
    "totalPages": 5,
    "number": 0,
    "size": 10
  }
}
```

#### Get Files by Type
```http
GET /api/v1/files/type/IMAGE?page=0&size=10

Valid types: IMAGE, PDF, EXCEL, CSV, DOCUMENT, OTHER
```

#### Get Files by Upload Source
```http
GET /api/v1/files/source/car_images?page=0&size=10
```

#### Get Files by User
```http
GET /api/v1/files/user/john.doe?page=0&size=10
```

#### Get Multiple Files Metadata
```http
POST /api/v1/files/batch-metadata
Content-Type: application/json

Body:
[
  "550e8400-e29b-41d4-a716-446655440000",
  "660e8400-e29b-41d4-a716-446655440001"
]
```

### File Management

#### Soft Delete
```http
DELETE /api/v1/files/{fileId}/soft

Marks file as DELETED (keeps file in storage)
```

#### Hard Delete
```http
DELETE /api/v1/files/{fileId}/hard

Permanently removes file from storage and database
```

#### Archive File
```http
PUT /api/v1/files/{fileId}/archive

Marks file as ARCHIVED
```

#### Restore File
```http
PUT /api/v1/files/{fileId}/restore

Restores DELETED or ARCHIVED file to ACTIVE
```

### Statistics & Cleanup

#### Get Storage Statistics
```http
GET /api/v1/files/statistics

Response:
{
  "success": true,
  "data": {
    "totalFiles": 150,
    "totalStorageBytes": 524288000,
    "totalStorageMB": 500.0,
    "totalStorageGB": 0.49,
    "byFileType": {
      "IMAGE": {
        "count": 80,
        "storageBytes": 314572800,
        "storageMB": 300.0
      },
      "PDF": {...},
      ...
    },
    "byStatus": {
      "ACTIVE": 140,
      "DELETED": 8,
      "ARCHIVED": 2
    }
  }
}
```

#### Cleanup Old Deleted Files
```http
POST /api/v1/files/cleanup?daysOld=30

Removes files marked as DELETED for more than 30 days
```

## Usage Examples

### In Your Car Entity

Add file IDs to your Car entity:

```java
@Entity
public class Car {
    // ... other fields
    
    @Column(name = "primary_image_id")
    private String primaryImageId; // Store the file ID
    
    @Column(name = "gallery_image_ids", columnDefinition = "TEXT")
    private String galleryImageIds; // Comma-separated file IDs
    
    @Column(name = "document_ids", columnDefinition = "TEXT")
    private String documentIds; // Comma-separated file IDs for PDFs, etc.
    
    // Helper methods
    public String getPrimaryImageUrl() {
        return primaryImageId != null 
            ? "http://localhost:8080/api/v1/files/" + primaryImageId 
            : null;
    }
    
    public List<String> getGalleryImageUrls() {
        if (galleryImageIds == null || galleryImageIds.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(galleryImageIds.split(","))
                .map(id -> "http://localhost:8080/api/v1/files/" + id)
                .collect(Collectors.toList());
    }
}
```

### In Your Car Service

```java
@Service
public class CarServiceImpl implements CarService {
    
    private final FileStorageService fileStorageService;
    
    public CarResponse createCar(CarRequest request, MultipartFile primaryImage, 
                                 MultipartFile[] galleryImages) {
        // Upload primary image
        FileMetadataResponse primaryImageResponse = fileStorageService.uploadFile(
            primaryImage,
            FileUploadRequest.builder()
                .uploadSource("car_primary_images")
                .uploadedBy(getCurrentUser())
                .build()
        );
        
        // Upload gallery images
        FileBatchUploadResponse galleryResponse = fileStorageService.uploadFiles(
            galleryImages,
            FileUploadRequest.builder()
                .uploadSource("car_gallery_images")
                .uploadedBy(getCurrentUser())
                .build()
        );
        
        // Create car with file IDs
        Car car = new Car();
        car.setPrimaryImageId(primaryImageResponse.getFileId());
        car.setGalleryImageIds(
            galleryResponse.getSuccessfulUploads().stream()
                .map(FileMetadataResponse.Simplified::getFileId)
                .collect(Collectors.joining(","))
        );
        
        // Save car...
        return mapper.toResponse(carRepository.save(car));
    }
}
```

### In Your Frontend (React/JavaScript)

```javascript
// Single file upload
const uploadFile = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('uploadSource', 'car_images');
  formData.append('uploadedBy', 'john.doe');
  
  const response = await fetch('http://localhost:8080/api/v1/files/upload', {
    method: 'POST',
    body: formData
  });
  
  const result = await response.json();
  console.log('File ID:', result.data.fileId);
  console.log('Public URL:', result.data.publicUrl);
  
  return result.data;
};

// Display image
const ImageComponent = ({ fileId }) => {
  const imageUrl = `http://localhost:8080/api/v1/files/${fileId}`;
  return <img src={imageUrl} alt="Car" />;
};

// Batch upload
const uploadMultipleFiles = async (files) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  formData.append('uploadSource', 'car_gallery');
  
  const response = await fetch('http://localhost:8080/api/v1/files/upload/batch', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
};
```

## File Type Configuration

Each file type has specific configuration:

| Type | Extensions | Max Size | Storage Path |
|------|-----------|----------|--------------|
| IMAGE | jpg, jpeg, png, gif, bmp, webp, svg | 10 MB | images/ |
| PDF | pdf | 20 MB | pdfs/ |
| EXCEL | xls, xlsx, xlsm, xlsb | 15 MB | excel/ |
| CSV | csv | 10 MB | csv/ |
| DOCUMENT | doc, docx, txt, rtf, odt | 15 MB | documents/ |
| OTHER | * | 10 MB | other/ |

To modify these, edit the `FileType` enum.

## Error Handling

The system throws appropriate exceptions:

- `FileStorageException`: File storage operation failures
- `ResourceNotFoundException`: File not found
- `IllegalArgumentException`: Validation failures

Example error response:
```json
{
  "success": false,
  "message": "File size exceeds maximum allowed size of 10 MB for IMAGE files",
  "timestamp": "2026-01-31T14:30:22"
}
```

## Best Practices

1. **Always store file IDs, not URLs**: URLs can change, file IDs are permanent
2. **Use upload sources**: Tag files with their source (e.g., "car_images", "invoices")
3. **Implement cleanup jobs**: Regularly run cleanup to remove old deleted files
4. **Validate on frontend too**: Don't just rely on backend validation
5. **Use batch upload for multiple files**: More efficient than individual uploads
6. **Soft delete first**: Use soft delete before hard delete for data recovery
7. **Monitor storage**: Regularly check storage statistics

## Testing

### Using cURL

```bash
# Upload a file
curl -X POST http://localhost:8080/api/v1/files/upload \
  -F "file=@/path/to/image.jpg" \
  -F "uploadSource=car_images" \
  -F "uploadedBy=test_user"

# Download a file
curl -X GET http://localhost:8080/api/v1/files/{fileId}?download=true \
  -o downloaded-file.jpg

# Get statistics
curl -X GET http://localhost:8080/api/v1/files/statistics
```

### Using Postman

1. Import the Swagger documentation from `/swagger-ui.html`
2. Test all endpoints with sample files
3. Verify file segregation by type

## Security Considerations

1. **Add authentication**: Protect upload endpoints with Spring Security
2. **File scanning**: Consider adding antivirus scanning for uploads
3. **Rate limiting**: Implement rate limiting for upload endpoints
4. **CORS configuration**: Configure CORS for frontend access
5. **Access control**: Implement user-based file access control

## Future Enhancements

- [ ] Image resizing and thumbnail generation
- [ ] File compression
- [ ] CDN integration
- [ ] S3 or cloud storage integration
- [ ] File versioning
- [ ] Advanced search and filtering
- [ ] Bulk operations API
- [ ] File sharing with expiration
- [ ] Analytics and access logs

## Troubleshooting

### Issue: Files not uploading
- Check `spring.servlet.multipart.max-file-size` setting
- Verify uploads directory has write permissions
- Check file type is allowed in `FileType` enum

### Issue: Files not accessible
- Verify file exists in storage path
- Check file status is ACTIVE
- Ensure file ID is correct

### Issue: OutOfMemory errors
- Reduce `max-file-size` and `max-request-size`
- Implement streaming for large files
- Increase JVM heap size

## Support

For issues and questions:
1. Check the WheelShift Pro development guide
2. Review the API documentation at `/swagger-ui.html`
3. Contact the development team

---

**Version**: 1.0
**Last Updated**: January 31, 2026
**Author**: WheelShift Pro Development Team