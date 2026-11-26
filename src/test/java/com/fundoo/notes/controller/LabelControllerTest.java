package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.service.LabelService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelControllerTest {

    @Mock
    private LabelService labelService;

    @InjectMocks
    private LabelController labelController;

    @Test
    void updateLabelDelegatesToServiceAndReturnsOk() {
        LabelRequestDto request = new LabelRequestDto();
        request.setName("Office");
        LabelResponseDto response = new LabelResponseDto(10L, "Office");

        when(labelService.updateLabel(10L, "Bearer token", request))
                .thenReturn(response);

        ResponseEntity<LabelResponseDto> result =
                labelController.updateLabel(10L, request, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(10L, result.getBody().getId());
        assertEquals("Office", result.getBody().getName());
    }
}
