package com.wheelshiftpro.service.impl;

import com.wheelshiftpro.dto.request.FileUploadRequest;
import com.wheelshiftpro.dto.response.FileMetadataResponse;
import com.wheelshiftpro.entity.Employee;
import com.wheelshiftpro.entity.FileMetadata;
import com.wheelshiftpro.enums.AuditCategory;
import com.wheelshiftpro.enums.AuditLevel;
import com.wheelshiftpro.enums.FileStatus;
import com.wheelshiftpro.enums.FileType;
import com.wheelshiftpro.exception.FileStorageException;
import com.wheelshiftpro.exception.ResourceNotFoundException;
import com.wheelshiftpro.mapper.FileMetadataMapper;
import com.wheelshiftpro.repository.EmployeeRepository;
import com.wheelshiftpro.repository.FileMetadataRepository;
import com.wheelshiftpro.security.EmployeeUserDetails;
import com.wheelshiftpro.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageServiceImpl Tests")
class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileMetadataMapper fileMetadataMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "baseStoragePath", tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "baseUrl", "/api/v1/files");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setUpAuthenticatedEmployee(Long employeeId) {
        Employee e = new Employee();
        e.setId(employeeId);
        EmployeeUserDetails principal = mock(EmployeeUserDetails.class);
        when(principal.getId()).thenReturn(employeeId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(employeeRepository.getReferenceById(employeeId)).thenReturn(e);
    }

    @Nested
    @DisplayName("uploadFile")
    class UploadFile {

        @Test
        @DisplayName("should upload file successfully and persist metadata")
        void happyPath() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", new byte[1024]);

            FileMetadata saved = new FileMetadata();
            saved.setId(100L);
            saved.setFileId("test-uuid");
            saved.setOriginalFilename("photo.jpg");

            FileMetadataResponse response = FileMetadataResponse.builder()
                    .id(100L)
                    .fileId("test-uuid")
                    .originalFilename("photo.jpg")
                    .build();

            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);
            when(fileMetadataMapper.toResponse(saved)).thenReturn(response);

            setUpAuthenticatedEmployee(50L);

            FileMetadataResponse result = fileStorageService.uploadFile(file, null);

            assertThat(result).isNotNull();
            assertThat(result.getFileId()).isEqualTo("test-uuid");
            verify(fileMetadataRepository).save(any(FileMetadata.class));
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(100L), eq("UPLOAD"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should set status ACTIVE and build public URL on upload")
        void setsStatusActiveAndPublicUrl() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", new byte[1024]);

            FileMetadata saved = new FileMetadata();
            saved.setId(100L);
            saved.setFileId("test-uuid");

            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);
            when(fileMetadataMapper.toResponse(saved)).thenReturn(new FileMetadataResponse());

            fileStorageService.uploadFile(file, null);

            ArgumentCaptor<FileMetadata> captor = ArgumentCaptor.forClass(FileMetadata.class);
            verify(fileMetadataRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(FileStatus.ACTIVE);
            assertThat(captor.getValue().getPublicUrl()).contains("/api/v1/files/");
        }

        @Test
        @DisplayName("should populate upload source and uploaded-by from request")
        void populatesRequestFields() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", new byte[512]);

            FileUploadRequest request = FileUploadRequest.builder()
                    .uploadSource("car_images")
                    .uploadedBy("employee-42")
                    .build();

            FileMetadata saved = new FileMetadata();
            saved.setId(1L);

            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);
            when(fileMetadataMapper.toResponse(saved)).thenReturn(new FileMetadataResponse());

            fileStorageService.uploadFile(file, request);

            ArgumentCaptor<FileMetadata> captor = ArgumentCaptor.forClass(FileMetadata.class);
            verify(fileMetadataRepository).save(captor.capture());
            assertThat(captor.getValue().getUploadSource()).isEqualTo("car_images");
            assertThat(captor.getValue().getUploadedBy()).isEqualTo("employee-42");
        }

        @Test
        @DisplayName("should throw FileStorageException for empty file")
        void emptyFile_throws() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> fileStorageService.uploadFile(emptyFile, null))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("empty");

            verify(fileMetadataRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when image file exceeds 10 MB size limit")
        void fileTooLarge_throws() {
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "big.jpg", "image/jpeg", largeContent);

            assertThatThrownBy(() -> fileStorageService.uploadFile(largeFile, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds maximum allowed size");

            verify(fileMetadataRepository, never()).save(any());
        }

        @Test
        @DisplayName("should not call audit service when upload fails")
        void noAuditOnFailure() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.jpg", "image/jpeg", new byte[0]);

            assertThatThrownBy(() -> fileStorageService.uploadFile(emptyFile, null))
                    .isInstanceOf(FileStorageException.class);

            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("should resolve authenticated employee for audit on upload")
        void auditFieldPopulatedWhenAuthenticated() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", new byte[512]);

            FileMetadata saved = new FileMetadata();
            saved.setId(1L);
            saved.setFileId("uuid-1");

            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);
            when(fileMetadataMapper.toResponse(saved)).thenReturn(new FileMetadataResponse());

            setUpAuthenticatedEmployee(42L);

            fileStorageService.uploadFile(file, null);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), employeeCaptor.capture(), any());
            assertThat(employeeCaptor.getValue()).isNotNull();
            assertThat(employeeCaptor.getValue().getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("should pass null performedBy to audit when unauthenticated")
        void auditFieldNullWhenUnauthenticated() {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "photo.jpg", "image/jpeg", new byte[512]);

            FileMetadata saved = new FileMetadata();
            saved.setId(1L);
            saved.setFileId("uuid-1");

            when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(saved);
            when(fileMetadataMapper.toResponse(saved)).thenReturn(new FileMetadataResponse());

            // no security context set
            fileStorageService.uploadFile(file, null);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(auditService).log(any(), any(), any(), any(), employeeCaptor.capture(), any());
            assertThat(employeeCaptor.getValue()).isNull();
        }
    }

    @Nested
    @DisplayName("softDeleteFile")
    class SoftDeleteFile {

        @Test
        @DisplayName("should soft-delete file and log HIGH audit")
        void happyPath() {
            String fileId = "test-uuid";
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(100L);
            fileMetadata.setFileId(fileId);
            fileMetadata.setStatus(FileStatus.ACTIVE);

            when(fileMetadataRepository.findByFileId(fileId)).thenReturn(Optional.of(fileMetadata));
            when(fileMetadataRepository.save(fileMetadata)).thenReturn(fileMetadata);

            setUpAuthenticatedEmployee(50L);

            fileStorageService.softDeleteFile(fileId);

            assertThat(fileMetadata.getStatus()).isEqualTo(FileStatus.DELETED);
            verify(fileMetadataRepository).save(fileMetadata);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(100L), eq("SOFT_DELETE"),
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not found")
        void fileNotFound_throws() {
            when(fileMetadataRepository.findByFileId("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.softDeleteFile("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("File");

            verify(fileMetadataRepository, never()).save(any());
            verifyNoInteractions(auditService);
        }

        @Test
        @DisplayName("audit level must be HIGH for soft delete")
        void auditLevelHigh() {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(1L);
            fileMetadata.setFileId("fid");

            when(fileMetadataRepository.findByFileId("fid")).thenReturn(Optional.of(fileMetadata));
            when(fileMetadataRepository.save(any())).thenReturn(fileMetadata);

            fileStorageService.softDeleteFile("fid");

            verify(auditService).log(any(), any(), any(), eq(AuditLevel.HIGH), any(), any());
        }
    }

    @Nested
    @DisplayName("hardDeleteFile")
    class HardDeleteFile {

        @Test
        @DisplayName("should hard-delete file: remove physical file, delete record, log HIGH audit")
        void happyPath() throws IOException {
            String fileId = "test-uuid";
            Path subDir = Files.createDirectory(tempDir.resolve("images"));
            Path realFile = Files.createFile(subDir.resolve("testfile.jpg"));
            assertThat(realFile).exists();

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(100L);
            fileMetadata.setFileId(fileId);
            fileMetadata.setStoragePath("images/testfile.jpg");
            fileMetadata.setOriginalFilename("testfile.jpg");

            when(fileMetadataRepository.findByFileId(fileId)).thenReturn(Optional.of(fileMetadata));

            setUpAuthenticatedEmployee(50L);

            fileStorageService.hardDeleteFile(fileId);

            assertThat(realFile).doesNotExist();
            verify(fileMetadataRepository).delete(fileMetadata);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(100L), eq("HARD_DELETE"),
                    eq(AuditLevel.HIGH), any(), any());
        }

        @Test
        @DisplayName("should audit before deleting DB record")
        void auditBeforeDelete() throws IOException {
            String fileId = "test-uuid";
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(10L);
            fileMetadata.setFileId(fileId);
            fileMetadata.setStoragePath("other/nonexistent.xyz");
            fileMetadata.setOriginalFilename("test.xyz");

            when(fileMetadataRepository.findByFileId(fileId)).thenReturn(Optional.of(fileMetadata));

            fileStorageService.hardDeleteFile(fileId);

            var inOrder = inOrder(auditService, fileMetadataRepository);
            inOrder.verify(auditService).log(any(), any(), eq("HARD_DELETE"), any(), any(), any());
            inOrder.verify(fileMetadataRepository).delete(fileMetadata);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not found")
        void fileNotFound_throws() {
            when(fileMetadataRepository.findByFileId("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.hardDeleteFile("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("File");

            verify(fileMetadataRepository, never()).delete(any());
            verifyNoInteractions(auditService);
        }
    }

    @Nested
    @DisplayName("archiveFile")
    class ArchiveFile {

        @Test
        @DisplayName("should archive file and log REGULAR audit")
        void happyPath() {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(1L);
            fileMetadata.setFileId("fid");
            fileMetadata.setStatus(FileStatus.ACTIVE);

            when(fileMetadataRepository.findByFileId("fid")).thenReturn(Optional.of(fileMetadata));
            when(fileMetadataRepository.save(fileMetadata)).thenReturn(fileMetadata);

            fileStorageService.archiveFile("fid");

            assertThat(fileMetadata.getStatus()).isEqualTo(FileStatus.ARCHIVED);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(1L), eq("ARCHIVE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not found")
        void fileNotFound_throws() {
            when(fileMetadataRepository.findByFileId("xxx")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.archiveFile("xxx"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(auditService);
        }
    }

    @Nested
    @DisplayName("restoreFile")
    class RestoreFile {

        @Test
        @DisplayName("should restore file to ACTIVE and log REGULAR audit")
        void happyPath() {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setId(1L);
            fileMetadata.setFileId("fid");
            fileMetadata.setStatus(FileStatus.DELETED);

            when(fileMetadataRepository.findByFileId("fid")).thenReturn(Optional.of(fileMetadata));
            when(fileMetadataRepository.save(fileMetadata)).thenReturn(fileMetadata);

            fileStorageService.restoreFile("fid");

            assertThat(fileMetadata.getStatus()).isEqualTo(FileStatus.ACTIVE);
            verify(auditService).log(eq(AuditCategory.SYSTEM), eq(1L), eq("RESTORE"),
                    eq(AuditLevel.REGULAR), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not found")
        void fileNotFound_throws() {
            when(fileMetadataRepository.findByFileId("xxx")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.restoreFile("xxx"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(auditService);
        }
    }

    @Nested
    @DisplayName("getFileMetadata")
    class GetFileMetadata {

        @Test
        @DisplayName("should return metadata for existing file")
        void happyPath() {
            String fileId = "test-uuid";
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId);

            FileMetadataResponse expected = FileMetadataResponse.builder()
                    .fileId(fileId)
                    .build();

            when(fileMetadataRepository.findByFileId(fileId)).thenReturn(Optional.of(fileMetadata));
            when(fileMetadataMapper.toResponse(fileMetadata)).thenReturn(expected);

            FileMetadataResponse result = fileStorageService.getFileMetadata(fileId);

            assertThat(result.getFileId()).isEqualTo(fileId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not found")
        void fileNotFound_throws() {
            when(fileMetadataRepository.findByFileId("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.getFileMetadata("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("File");
        }
    }

    @Nested
    @DisplayName("getFileAsResource")
    class GetFileAsResource {

        @Test
        @DisplayName("should throw ResourceNotFoundException when file not in DB")
        void fileNotFoundInDb_throws() {
            when(fileMetadataRepository.findByFileIdAndStatus("unknown", FileStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> fileStorageService.getFileAsResource("unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("File");
        }

        @Test
        @DisplayName("should throw FileStorageException when physical file missing from disk")
        void physicalFileMissing_throws() {
            String fileId = "test-uuid";
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId);
            fileMetadata.setOriginalFilename("photo.jpg");
            fileMetadata.setStoragePath("images/nonexistent.jpg");

            when(fileMetadataRepository.findByFileIdAndStatus(fileId, FileStatus.ACTIVE))
                    .thenReturn(Optional.of(fileMetadata));

            assertThatThrownBy(() -> fileStorageService.getFileAsResource(fileId))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("not found or not readable");
        }

        @Test
        @DisplayName("should return resource when physical file exists on disk")
        void happyPath() throws IOException {
            String fileId = "test-uuid";
            Path imagesDir = Files.createDirectory(tempDir.resolve("images"));
            Path realFile = Files.createFile(imagesDir.resolve("photo.jpg"));
            Files.writeString(realFile, "fake image content");

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileId(fileId);
            fileMetadata.setOriginalFilename("photo.jpg");
            fileMetadata.setStoragePath("images/photo.jpg");

            when(fileMetadataRepository.findByFileIdAndStatus(fileId, FileStatus.ACTIVE))
                    .thenReturn(Optional.of(fileMetadata));

            var resource = fileStorageService.getFileAsResource(fileId);

            assertThat(resource).isNotNull();
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
        }
    }

    @Nested
    @DisplayName("validateFile")
    class ValidateFile {

        @Test
        @DisplayName("should throw for empty file")
        void emptyFile_throws() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            assertThatThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("should throw when file exceeds size limit for its type")
        void fileTooLarge_throws() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(15L * 1024 * 1024); // 15 MB > 10 MB IMAGE limit

            assertThatThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceeds maximum allowed size");
        }

        @Test
        @DisplayName("should throw for disallowed extension given explicit FileType")
        void invalidExtension_throws() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("malware.exe");

            assertThatThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not allowed");
        }

        @Test
        @DisplayName("should throw when filename is null")
        void nullFilename_throws() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn(null);

            assertThatThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Filename");
        }

        @Test
        @DisplayName("should pass validation for valid image file")
        void validFile_passes() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getContentType()).thenReturn("image/jpeg");

            assertThatNoException().isThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE));
        }

        @Test
        @DisplayName("should throw for disallowed MIME type")
        void invalidMimeType_throws() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getContentType()).thenReturn("application/x-msdownload");

            assertThatThrownBy(() -> fileStorageService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MIME type");
        }
    }
}
