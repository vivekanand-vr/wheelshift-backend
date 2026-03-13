package com.wheelshiftpro.utils;

import com.wheelshiftpro.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for building file URLs from file IDs.
 * Centralizes URL generation logic to avoid hardcoded URLs throughout the
 * codebase.
 * 
 * Usage in entities:
 * 
 * @Autowired (via setter injection in @PostLoad or via
 *            ApplicationContextProvider)
 *            private FileUrlHelper fileUrlHelper;
 * 
 *            Better usage: Use in DTOs/Services rather than entities
 */
@Component
@RequiredArgsConstructor
public class FileUrlHelper {

  private final FileStorageProperties fileStorageProperties;

  /**
   * Build URL for a single file ID
   * 
   * @param fileId File UUID
   * @return Full URL to access the file, or null if fileId is null/empty
   */
  public String buildFileUrl(String fileId) {
    if (fileId == null || fileId.trim().isEmpty()) {
      return null;
    }
    return fileStorageProperties.buildFileUrl(fileId.trim());
  }

  /**
   * Build URLs for comma-separated file IDs
   * 
   * @param fileIds Comma-separated file UUIDs
   * @return List of full URLs to access the files
   */
  public List<String> buildFileUrls(String fileIds) {
    if (fileIds == null || fileIds.trim().isEmpty()) {
      return Collections.emptyList();
    }

    return Arrays.stream(fileIds.split(","))
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .map(this::buildFileUrl)
        .collect(Collectors.toList());
  }

  /**
   * Build URLs for a list of file IDs
   * 
   * @param fileIdList List of file UUIDs
   * @return List of full URLs to access the files
   */
  public List<String> buildFileUrlsFromList(List<String> fileIdList) {
    if (fileIdList == null || fileIdList.isEmpty()) {
      return Collections.emptyList();
    }

    return fileIdList.stream()
        .filter(id -> id != null && !id.trim().isEmpty())
        .map(this::buildFileUrl)
        .collect(Collectors.toList());
  }

  /**
   * Get the base access URL
   * 
   * @return Base URL for file access
   */
  public String getAccessUrl() {
    return fileStorageProperties.getAccessUrl();
  }
}