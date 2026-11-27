package com.fundoo.notes;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoo.notes.controller.AttachmentController;
import com.fundoo.notes.dto.response.AttachmentResponseDto;
import com.fundoo.notes.exception.GlobalExceptionHandler;
import com.fundoo.notes.exception.InvalidAttachmentException;
import com.fundoo.notes.service.AttachmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Class Name  : AttachmentApiEndToEndTest
 * Description : Exercises attachment endpoints through MockMvc.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@ExtendWith(MockitoExtension.class)
class AttachmentApiEndToEndTest {

    @Mock
    private AttachmentService attachmentService;

    private MockMvc mockMvc;

    /**
     * Creates reusable attachment metadata for web-layer tests.
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
     * Builds the standalone MockMvc instance for the attachment controller.
     */
    @BeforeEach
    void setUp() {
        // Java time handling is disabled because error responses use LocalDateTime.
        ObjectMapper objectMapper = new ObjectMapper()
                .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AttachmentController(attachmentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper),
                        new ByteArrayHttpMessageConverter()
                )
                .build();
    }

    /**
     * Verifies that multipart upload returns created status and metadata.
     */
    @Test
    void shouldUploadAttachmentThroughTheWebLayer() throws Exception {
        when(attachmentService.uploadAttachment(
                any(MultipartFile.class),
                eq("Bearer token")))
                .thenReturn(buildMetadata());

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(new MockMultipartFile(
                                "file",
                                "notes.txt",
                                "text/plain",
                                "hello".getBytes(StandardCharsets.UTF_8)
                        ))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.originalFileName").value("notes.txt"));
    }

    /**
     * Verifies that validation errors are converted into bad requests.
     */
    @Test
    void shouldReturnBadRequestForInvalidAttachment() throws Exception {
        when(attachmentService.uploadAttachment(
                any(MultipartFile.class),
                eq("Bearer token")))
                .thenThrow(new InvalidAttachmentException(
                        "Unsupported file type"
                ));

        mockMvc.perform(multipart("/api/attachments/upload")
                        .file(new MockMultipartFile(
                                "file",
                                "virus.exe",
                                "application/x-msdownload",
                                "bad".getBytes(StandardCharsets.UTF_8)
                        ))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Unsupported file type"));
    }

    /**
     * Verifies that the metadata list is exposed as JSON.
     */
    @Test
    void shouldListAttachmentsThroughTheWebLayer() throws Exception {
        when(attachmentService.getAllAttachments("Bearer token"))
                .thenReturn(List.of(buildMetadata()));

        mockMvc.perform(get("/api/attachments")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    /**
     * Verifies that download responses include binary content and headers.
     */
    @Test
    void shouldDownloadAttachmentThroughTheWebLayer() throws Exception {
        when(attachmentService.getAttachment(10L, "Bearer token"))
                .thenReturn(buildMetadata());
        when(attachmentService.downloadAttachment(10L, "Bearer token"))
                .thenReturn("hello".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/attachments/10/download")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(
                        "hello".getBytes(StandardCharsets.UTF_8)
                ))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.TEXT_PLAIN_VALUE
                ))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION));
    }
}
