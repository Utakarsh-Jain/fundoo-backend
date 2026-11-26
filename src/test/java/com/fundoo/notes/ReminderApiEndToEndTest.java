package com.fundoo.notes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fundoo.notes.controller.ReminderController;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.exception.GlobalExceptionHandler;
import com.fundoo.notes.exception.ReminderNotFoundException;
import com.fundoo.notes.service.ReminderService;

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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReminderApiEndToEndTest {

    private MockMvc mockMvc;
    private ReminderService reminderService;
    private LocalDateTime reminderTime;

    @BeforeEach
    void setUp() {
        reminderService = mock(ReminderService.class);
        reminderTime = LocalDateTime.now().plusDays(2).withNano(0);

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
        dateModule.addDeserializer(LocalDateTime.class,
                new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(
                            JsonParser parser,
                            DeserializationContext context)
                            throws IOException {

                        return LocalDateTime.parse(parser.getValueAsString());
                    }
                });

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(dateModule)
                .build();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReminderController(reminderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getReturnsRemindersThroughTheWebLayer() throws Exception {
        when(reminderService.getAllReminders("Bearer token"))
                .thenReturn(List.of(new ReminderResponseDto(
                        100L, 20L, "Project", reminderTime)));

        mockMvc.perform(get("/api/reminders")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].noteTitle").value("Project"))
                .andExpect(jsonPath("$[0].reminderTime")
                        .value(reminderTime.toString()));
    }

    @Test
    void putUpdatesReminderThroughTheWebLayer() throws Exception {
        when(reminderService.updateReminder(
                eq(100L), eq("Bearer token"), any(ReminderUpdateRequestDto.class)))
                .thenReturn(new ReminderResponseDto(
                        100L, 20L, "Project", reminderTime));

        mockMvc.perform(put("/api/reminders/100")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reminderTime\":\"" + reminderTime + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.reminderTime")
                        .value(reminderTime.toString()));
    }

    @Test
    void putRejectsPastReminderTime() throws Exception {
        String past = LocalDateTime.now().minusDays(1).withNano(0).toString();

        mockMvc.perform(put("/api/reminders/100")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reminderTime\":\"" + past + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.reminderTime")
                        .value("Reminder time must be in the future"));
    }

    @Test
    void putReturnsNotFoundForMissingReminder() throws Exception {
        when(reminderService.updateReminder(
                eq(100L), eq("Bearer token"), any(ReminderUpdateRequestDto.class)))
                .thenThrow(new ReminderNotFoundException(
                        "Reminder not found"));

        mockMvc.perform(put("/api/reminders/100")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reminderTime\":\"" + reminderTime + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Reminder not found"));
    }

    @Test
    void deleteRemovesReminderThroughTheWebLayer() throws Exception {
        mockMvc.perform(delete("/api/reminders/100")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(reminderService).deleteReminder(100L, "Bearer token");
    }
}
