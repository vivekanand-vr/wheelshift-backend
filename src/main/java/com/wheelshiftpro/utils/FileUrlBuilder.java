package com.wheelshiftpro.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Static utility class for building file URLs from file IDs.
 * Uses static initialization from Spring properties to avoid passing
 * dependencies everywhere.
 * 
 * Usage in MapStruct:
 * 
 * @Mapping(target = "primaryImageUrl", expression =
 *                 "java(FileUrlBuilder.buildFileUrl(car.getPrimaryImageId()))")
 * 
 *                 Usage in Services:
 *                 String url = FileUrlBuilder.buildFileUrl(fileId);
 * 
 *                 Usage anywhere in code:
 *                 List<String> urls =
 *                 FileUrlBuilder.buildFileUrls(commaSeparatedIds);
 */
@Component
@RequiredArgsConstructor
public class FileUrlBuilder {

  private static String BASE_ACCESS_URL;

  @Value("${file.storage.access-url}")
  private String accessUrl;

  /**
   * Initialize static variable from Spring property after bean construction.
   * This allows static methods to use Spring configuration.
   */
  @PostConstruct
  private void init() {
    BASE_ACCESS_URL = accessUrl;
  }

  /**
   * Build URL for a single file ID.
   * 
   * @param fileId File UUID
   * @return Full URL to access the file, or null if fileId is null/empty
   * 
   * @example
   *          String url =
   *          FileUrlBuilder.buildFileUrl("550e8400-e29b-41d4-a716-446655440000");
   *          // Returns:
   *          "http://localhost:8080/api/v1/files/550e8400-e29b-41d4-a716-446655440000"
   */
  public static String buildFileUrl(String fileId) {
    if (fileId == null || fileId.trim().isEmpty()) {
      return null;
    }

    if (BASE_ACCESS_URL == null) {
      throw new IllegalStateException(
          "FileUrlBuilder not initialized. Ensure 'file.storage.access-url' is configured in application.properties");
    }

    String cleanFileId = fileId.trim();
    return BASE_ACCESS_URL.endsWith("/")
        ? BASE_ACCESS_URL + cleanFileId
        : BASE_ACCESS_URL + "/" + cleanFileId;
  }

  /**
   * Build URLs for comma-separated file IDs.
   * Commonly used for gallery images and documents stored as TEXT in database.
   * 
   * @param fileIds Comma-separated file UUIDs
   * @return List of full URLs to access the files, or empty list if fileIds is
   *         null/empty
   * 
   * @example
   *          List<String> urls =
   *          FileUrlBuilder.buildFileUrls("uuid1,uuid2,uuid3");
   *          // Returns: ["http://.../files/uuid1", "http://.../files/uuid2",
   *          "http://.../files/uuid3"]
   */
  public static List<String> buildFileUrls(String fileIds) {
    if (fileIds == null || fileIds.trim().isEmpty()) {
      return Collections.emptyList();
    }

    return Arrays.stream(fileIds.split(","))
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .map(FileUrlBuilder::buildFileUrl)
        .collect(Collectors.toList());
  }

  /**
   * Build URLs for a list of file IDs.
   * Useful when file IDs are already in a List format.
   * 
   * @param fileIdList List of file UUIDs
   * @return List of full URLs to access the files, or empty list if fileIdList is
   *         null/empty
   * 
   * @example
   *          List<String> ids = Arrays.asList("uuid1", "uuid2");
   *          List<String> urls = FileUrlBuilder.buildFileUrls(ids);
   */
  public static List<String> buildFileUrls(List<String> fileIdList) {
    if (fileIdList == null || fileIdList.isEmpty()) {
      return Collections.emptyList();
    }

    return fileIdList.stream()
        .filter(id -> id != null && !id.trim().isEmpty())
        .map(FileUrlBuilder::buildFileUrl)
        .collect(Collectors.toList());
  }

  /**
   * Get the base access URL configured in application.properties.
   * 
   * @return Base URL for file access (e.g., "http://localhost:8080/api/v1/files")
   */
  public static String getBaseAccessUrl() {
    if (BASE_ACCESS_URL == null) {
      throw new IllegalStateException(
          "FileUrlBuilder not initialized. Ensure 'file.storage.access-url' is configured in application.properties");
    }
    return BASE_ACCESS_URL;
  }

  /**
   * Check if FileUrlBuilder has been initialized with configuration.
   * 
   * @return true if initialized, false otherwise
   */
  public static boolean isInitialized() {
    return BASE_ACCESS_URL != null;
  }

  /**
   * Method to join list of strings to comma-separated string.
   * Used for converting List<String> file IDs to TEXT column format.
   *
   * @param list List of strings
   * @return Comma-separated string or null if list is null/empty
   */
  public static String joinList(List<String> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    return String.join(",", list);
  }

  /**
   * Method to split comma-separated string to list.
   * Used for converting TEXT column format to List<String> file IDs.
   *
   * @param value Comma-separated string
   * @return List of strings or null if value is null/empty
   */
  public static List<String> splitToList(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return List.of(value.split(","));
  }
}
