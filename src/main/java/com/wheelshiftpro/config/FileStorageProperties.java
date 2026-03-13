package com.wheelshiftpro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for file storage system.
 * Binds to properties prefixed with 'file.storage' in application.properties.
 * 
 * Usage:
 * 
 * @Autowired
 *            private FileStorageProperties fileStorageProperties;
 *            String url = fileStorageProperties.getAccessUrl();
 */
@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageProperties {

  /**
   * Base URL for accessing files via API
   * Example: http://localhost:8080/api/v1/files
   */
  private String accessUrl;

  /**
   * Base path where files are stored locally
   * Example: uploads
   */
  private String basePath = "uploads";

  /**
   * Storage type: local, s3, or hybrid
   */
  private String type = "local";

  /**
   * Maximum file size in bytes (default: 20MB)
   */
  private Long maxFileSize = 20 * 1024 * 1024L;

  /**
   * Maximum request size in bytes for batch uploads (default: 100MB)
   */
  private Long maxRequestSize = 100 * 1024 * 1024L;

  /**
   * AWS S3 configuration (nested properties)
   */
  private S3Properties s3 = new S3Properties();

  @Data
  public static class S3Properties {
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
    private String publicUrl;
  }

  /**
   * Helper method to build file URL from file ID
   */
  public String buildFileUrl(String fileId) {
    if (accessUrl == null || accessUrl.isEmpty()) {
      throw new IllegalStateException("file.storage.access-url is not configured");
    }
    return accessUrl.endsWith("/")
        ? accessUrl + fileId
        : accessUrl + "/" + fileId;
  }

  /**
   * Helper method to check if S3 is enabled
   */
  public boolean isS3Enabled() {
    return "s3".equalsIgnoreCase(type);
  }

  /**
   * Helper method to check if local storage is enabled
   */
  public boolean isLocalStorageEnabled() {
    return "local".equalsIgnoreCase(type);
  }

  /**
   * Helper method to check if hybrid mode is enabled
   */
  public boolean isHybridMode() {
    return "hybrid".equalsIgnoreCase(type);
  }
}