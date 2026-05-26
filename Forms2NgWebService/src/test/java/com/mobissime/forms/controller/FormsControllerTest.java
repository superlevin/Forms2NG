package com.mobissime.forms.controller;

import com.mobissime.forms.objects.Block;
import com.mobissime.forms.objects.Canvas;
import com.mobissime.forms.objects.Trigger;
import com.mobissime.forms.pojos.FormsStats;
import com.mobissime.forms.service.FormsParserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileNotFoundException;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FormsController.class)
class FormsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FormsParserService parserService;

    @Test
    void getStats_returnsOk() throws Exception {
        FormsStats stats = new FormsStats();
        stats.setNumberOfBlocks(2);
        stats.setNumberOfItems(5);
        stats.setNumberOfTriggers(3);
        stats.setNumberOfCanvas(1);

        when(parserService.getStats("MYFORM")).thenReturn(stats);

        mockMvc.perform(get("/rest/forms/stats/MYFORM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfBlocks").value(2))
                .andExpect(jsonPath("$.numberOfItems").value(5));
    }

    @Test
    void getStats_returns404WhenNotFound() throws Exception {
        when(parserService.getStats("MISSING")).thenThrow(new FileNotFoundException("not found"));

        mockMvc.perform(get("/rest/forms/stats/MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBlocks_returnsOk() throws Exception {
        Block blk = new Block();
        blk.setName("BLK_TEST");

        when(parserService.getBlocks("MYFORM")).thenReturn(List.of(blk));
        when(parserService.getTriggersForBlock("MYFORM", "BLK_TEST")).thenReturn(List.of());

        mockMvc.perform(get("/rest/forms/blocks/MYFORM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("BLK_TEST"));
    }

    @Test
    void getCanvases_returnsOk() throws Exception {
        Canvas cvs = new Canvas();
        cvs.setName("CVS_MAIN");

        when(parserService.getCanvases("MYFORM")).thenReturn(List.of(cvs));

        mockMvc.perform(get("/rest/forms/canvas/MYFORM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("CVS_MAIN"));
    }

    @Test
    void getTriggersForBlock_returnsOk() throws Exception {
        Trigger trg = new Trigger();
        trg.setName("WHEN-NEW-FORM-INSTANCE");
        trg.setTriggerText("NULL;");

        when(parserService.getTriggersForBlock("MYFORM", "BLK1")).thenReturn(List.of(trg));

        mockMvc.perform(get("/rest/forms/triggersforblock/MYFORM/BLK1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("WHEN-NEW-FORM-INSTANCE"));
    }
}
