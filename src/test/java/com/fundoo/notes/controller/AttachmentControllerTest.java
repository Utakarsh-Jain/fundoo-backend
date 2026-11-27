package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.AttachmentResponseDto;
import com.fundoo.notes.service.AttachmentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Class Name  : AttachmentControllerTest
 * Description : Tests attachment controller responses and headers.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    /**
     * Builds reusable attachment metadata for controller tests.
     *
     * @return the attachment metadata
     */
    private AttachmentResponseDto buildMetadata() {
        return new AttachmentResponseDto(
                10L,
                "notes.txt",
                "text/plain",
                5L,
                LocalDateTime.now()
        );
    }

    /**
     * Verifies that an upload returns created status and metadata.
     */
    @Test
    void shouldReturnCreatedWhenAttachmentIsUploaded() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8)
        );
        AttachmentResponseDto metadata = buildMetadata();

        when(attachmentService.uploadAttachment(
                any(MockMultipartFile.class),
                eq("Bearer token")))
                .thenReturn(metadata);

        ResponseEntity<AttachmentResponseDto> result =
                attachmentController.uploadAttachment(
                        "Bearer token",
                        file
                );

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(metadata, result.getBody());
        verify(attachmentService).uploadAttachment(file, "Bearer token");
    }

    /**
     * Verifies that the metadata listing returns the service result.
     */
    @Test
    void shouldReturnAllAttachments() {
        List<AttachmentResponseDto> metadata = List.of(buildMetadata());
        when(attachmentService.getAllAttachments("Bearer token"))
                .thenReturn(metadata);

        ResponseEntity<List<AttachmentResponseDto>> result =
                attachmentController.getAllAttachments("Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(metadata, result.getBody());
    }

    /**
     * Verifies that one attachment metadata response is returned.
     */
    @Test
    void shouldReturnOneAttachmentMetadata() {
        AttachmentResponseDto metadata = buildMetadata();
        when(attachmentService.getAttachment(10L, "Bearer token"))
                .thenReturn(metadata);

        ResponseEntity<AttachmentResponseDto> result =
                attachmentController.getAttachment(10L, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(metadata, result.getBody());
    }

    /**
     * Verifies that inline view responses carry the correct headers.
     */
    @Test
    void shouldViewAttachmentInline() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        when(attachmentService.getAttachment(10L, "Bearer token"))
                .thenReturn(buildMetadata());
        when(attachmentService.downloadAttachment(10L, "Bearer token"))
                .thenReturn(content);

        ResponseEntity<byte[]> result =
                attachmentController.viewAttachment(10L, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN, result.getHeaders().getContentType());
        assertNotNull(result.getHeaders().getFirst(
                HttpHeaders.CONTENT_DISPOSITION
        ));
        org.junit.jupiter.api.Assertions.assertTrue(
                result.getHeaders()
                        .getFirst(HttpHeaders.CONTENT_DISPOSITION)
                        .contains("inline")
        );
        assertArrayEquals(content, result.getBody());
    }

    /**
     * Verifies that download responses force a file download.
     */
    @Test
    void shouldDownloadAttachmentAsFile() {
        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);
        when(attachmentService.getAttachment(10L, "Bearer token"))
                .thenReturn(buildMetadata());
        when(attachmentService.downloadAttachment(10L, "Bearer token"))
                .thenReturn(content);

        ResponseEntity<byte[]> result =
                attachmentController.downloadAttachment(10L, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(
                result.getHeaders()
                        .getFirst(HttpHeaders.CONTENT_DISPOSITION)
                        .contains("attachment")
        );
        assertArrayEquals(content, result.getBody());
    }
}
