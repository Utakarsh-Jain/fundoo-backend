package com.fundoo.notes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fundoo.notes.controller.LabelController;
import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.exception.DuplicateLabelException;
import com.fundoo.notes.exception.GlobalExceptionHandler;
import com.fundoo.notes.service.LabelService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LabelApiEndToEndTest {

    private MockMvc mockMvc;
    private LabelService labelService;

    @BeforeEach
    void setUp() {
        labelService = mock(LabelService.class);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        SimpleModule dateModule = new SimpleModule();
        dateModule.addSerializer(LocalDateTime.class,
                new JsonSerializer<LocalDateTime>() {
                    @Override
                    public void serialize(
                            LocalDateTime value,
                            JsonGenerator generator,
                            SerializerProvider serializers)
                            throws IOException {

                        generator.writeString(value.toString());
                    }
                });

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(dateModule)
                .build();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new LabelController(labelService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void putRenamesLabelThroughTheWebLayer() throws Exception {
        when(labelService.updateLabel(
                eq(1L), eq("Bearer token"), any(LabelRequestDto.class)))
                .thenReturn(new LabelResponseDto(1L, "Office"));

        mockMvc.perform(put("/api/labels/1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Office  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Office"));
    }

    @Test
    void putRejectsBlankLabelName() throws Exception {
        mockMvc.perform(put("/api/labels/1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name")
                        .value("Label name is required"));
    }

    @Test
    void putReturnsConflictForDuplicateName() throws Exception {
        when(labelService.updateLabel(
                eq(1L), eq("Bearer token"), any(LabelRequestDto.class)))
                .thenThrow(new DuplicateLabelException(
                        "Label already exists"));

        mockMvc.perform(put("/api/labels/1")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Work\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Label already exists"));
    }
}
