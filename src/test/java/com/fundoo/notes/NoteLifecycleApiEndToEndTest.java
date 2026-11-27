package com.fundoo.notes;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoo.notes.controller.NoteController;
import com.fundoo.notes.dto.request.NoteRequestDto;
import com.fundoo.notes.dto.response.NoteResponseDto;
import com.fundoo.notes.exception.GlobalExceptionHandler;
import com.fundoo.notes.service.NoteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteLifecycleApiEndToEndTest {

    private MockMvc mockMvc;
    private NoteService noteService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        noteService = mock(NoteService.class);
        objectMapper = new ObjectMapper()
                .disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new NoteController(noteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void putUpdatesNoteThroughTheWebLayer() throws Exception {
        NoteRequestDto request = new NoteRequestDto(
                "Updated title",
                "Updated description"
        );

        when(noteService.updateNote(
                eq(10L),
                any(NoteRequestDto.class),
                eq("Bearer token")))
                .thenReturn(new NoteResponseDto(
                        10L,
                        "Updated title",
                        "Updated description",
                        false,
                        false,
                        false,
                        List.of()
                ));

        mockMvc.perform(put("/api/notes/10")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));
    }

    @Test
    void putRejectsBlankTitle() throws Exception {
        mockMvc.perform(put("/api/notes/10")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"Body\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.title")
                        .value("Note title is required"));
    }

    @Test
    void patchRestoresNoteThroughTheWebLayer() throws Exception {
        when(noteService.restoreNote(10L, "Bearer token"))
                .thenReturn(new NoteResponseDto(
                        10L,
                        "Project",
                        "Project note",
                        false,
                        false,
                        false,
                        List.of()
                ));

        mockMvc.perform(patch("/api/notes/10/restore")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.trashed").value(false));
    }

    @Test
    void deletePermanentlyRemovesNoteThroughTheWebLayer() throws Exception {
        mockMvc.perform(delete("/api/notes/10")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(noteService).permanentlyDeleteNote(10L, "Bearer token");
    }
}
