package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.response.AttachmentResponseDto;
import com.fundoo.notes.entity.Attachment;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.AttachmentNotFoundException;
import com.fundoo.notes.exception.InvalidAttachmentException;
import com.fundoo.notes.repository.AttachmentRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Class Name  : AttachmentServiceImplTest
 * Description : Tests attachment validation, storage and retrieval logic.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @TempDir
    Path tempDir;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    private AttachmentServiceImpl attachmentService;

    /**
     * Creates the service with a temporary storage directory.
     */
    @BeforeEach
    void setUp() {
        // A strict one megabyte limit keeps the oversize test small.
        attachmentService = new AttachmentServiceImpl(
                attachmentRepository,
                userRepository,
                tokenService,
                tempDir.toString(),
                1,
                "text/plain,application/pdf,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
    }

    /**
     * Prepares valid authentication mocks for a test user.
     *
     * @return the mocked authenticated user
     */
    private User mockAuthenticatedUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Utakarsh");
        user.setEmail("utakarsh@example.com");
        user.setPassword("password");

        when(tokenService.isTokenValid("token")).thenReturn(true);
        when(tokenService.extractSubject("token"))
                .thenReturn("utakarsh@example.com");
        when(userRepository.findByEmail("utakarsh@example.com"))
                .thenReturn(Optional.of(user));

        return user;
    }

    /**
     * Verifies that a valid file is written and saved with metadata.
     */
    @Test
    void shouldUploadAttachmentSuccessfully() {
        User user = mockAuthenticatedUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8)
        );

        when(attachmentRepository.save(any(Attachment.class)))
                .thenAnswer(invocation -> {
                    Attachment attachment = invocation.getArgument(0);
                    attachment.setId(10L);
                    return attachment;
                });

        AttachmentResponseDto response =
                attachmentService.uploadAttachment(file, "Bearer token");

        ArgumentCaptor<Attachment> captor =
                ArgumentCaptor.forClass(Attachment.class);
        verify(attachmentRepository).save(captor.capture());
        Attachment savedAttachment = captor.getValue();

        assertEquals(10L, response.getId());
        assertEquals("notes.txt", response.getOriginalFileName());
        assertEquals("text/plain", response.getContentType());
        assertEquals(5L, response.getFileSize());
        assertEquals(user.getId(), savedAttachment.getUser().getId());
        assertTrue(Files.exists(Path.of(savedAttachment.getFilePath())));
    }

    /**
     * Verifies that legacy Excel workbooks are accepted.
     */
    @Test
    void shouldUploadLegacyExcelAttachmentSuccessfully() {
        mockAuthenticatedUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.xls",
                "application/vnd.ms-excel",
                "excel".getBytes(StandardCharsets.UTF_8)
        );

        when(attachmentRepository.save(any(Attachment.class)))
                .thenAnswer(invocation -> {
                    Attachment attachment = invocation.getArgument(0);
                    attachment.setId(11L);
                    return attachment;
                });

        AttachmentResponseDto response =
                attachmentService.uploadAttachment(file, "Bearer token");

        assertEquals(11L, response.getId());
        assertEquals("report.xls", response.getOriginalFileName());
        assertEquals("application/vnd.ms-excel", response.getContentType());
    }

    /**
     * Verifies that modern Excel workbooks are accepted.
     */
    @Test
    void shouldUploadModernExcelAttachmentSuccessfully() {
        mockAuthenticatedUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "excel".getBytes(StandardCharsets.UTF_8)
        );

        when(attachmentRepository.save(any(Attachment.class)))
                .thenAnswer(invocation -> {
                    Attachment attachment = invocation.getArgument(0);
                    attachment.setId(12L);
                    return attachment;
                });

        AttachmentResponseDto response =
                attachmentService.uploadAttachment(file, "Bearer token");

        assertEquals(12L, response.getId());
        assertEquals("report.xlsx", response.getOriginalFileName());
        assertEquals(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getContentType()
        );
    }

    /**
     * Verifies that unsupported MIME types are rejected.
     */
    @Test
    void shouldRejectUnsupportedFileType() {
        mockAuthenticatedUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "virus.exe",
                "application/x-msdownload",
                "bad".getBytes(StandardCharsets.UTF_8)
        );

        InvalidAttachmentException exception = assertThrows(
                InvalidAttachmentException.class,
                () -> attachmentService.uploadAttachment(file, "Bearer token")
        );

        assertEquals("Unsupported file type", exception.getMessage());
    }

    /**
     * Verifies that files larger than the configured limit are rejected.
     */
    @Test
    void shouldRejectOversizedFile() {
        mockAuthenticatedUser();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.txt",
                "text/plain",
                new byte[1024 * 1024 + 1]
        );

        InvalidAttachmentException exception = assertThrows(
                InvalidAttachmentException.class,
                () -> attachmentService.uploadAttachment(file, "Bearer token")
        );

        assertEquals(
                "File size must not exceed 1 MB",
                exception.getMessage()
        );
    }

    /**
     * Verifies that only the current user's attachments are listed.
     */
    @Test
    void shouldListAttachmentsForAuthenticatedUser() {
        User user = mockAuthenticatedUser();
        Attachment attachment = new Attachment(
                user,
                "notes.txt",
                "stored.txt",
                "text/plain",
                5L,
                tempDir.resolve("stored.txt").toString(),
                LocalDateTime.now()
        );

        when(attachmentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId()))
                .thenReturn(List.of(attachment));

        List<AttachmentResponseDto> responses =
                attachmentService.getAllAttachments("Bearer token");

        assertEquals(1, responses.size());
        assertEquals("notes.txt", responses.get(0).getOriginalFileName());
    }

    /**
     * Verifies that stored file bytes are returned for an owned attachment.
     */
    @Test
    void shouldDownloadAttachmentBytes() throws IOException {
        User user = mockAuthenticatedUser();
        Path storedPath = tempDir.resolve("stored.txt");
        Files.write(storedPath, "hello".getBytes(StandardCharsets.UTF_8));

        Attachment attachment = new Attachment(
                user,
                "notes.txt",
                "stored.txt",
                "text/plain",
                5L,
                storedPath.toString(),
                LocalDateTime.now()
        );

        when(attachmentRepository.findByIdAndUserId(10L, user.getId()))
                .thenReturn(Optional.of(attachment));

        byte[] content =
                attachmentService.downloadAttachment(10L, "Bearer token");

        assertArrayEquals(
                "hello".getBytes(StandardCharsets.UTF_8),
                content
        );
    }

    /**
     * Verifies that a missing stored file produces a not found exception.
     */
    @Test
    void shouldFailWhenStoredFileIsMissing() {
        User user = mockAuthenticatedUser();
        Attachment attachment = new Attachment(
                user,
                "notes.txt",
                "missing.txt",
                "text/plain",
                5L,
                tempDir.resolve("missing.txt").toString(),
                LocalDateTime.now()
        );

        when(attachmentRepository.findByIdAndUserId(10L, user.getId()))
                .thenReturn(Optional.of(attachment));

        AttachmentNotFoundException exception = assertThrows(
                AttachmentNotFoundException.class,
                () -> attachmentService.downloadAttachment(
                        10L,
                        "Bearer token"
                )
        );

        assertEquals("Attachment file is missing", exception.getMessage());
    }
}
