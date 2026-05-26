package com.mobissime.forms.controller;

import com.mobissime.forms.objects.Block;
import com.mobissime.forms.objects.Canvas;
import com.mobissime.forms.objects.Trigger;
import com.mobissime.forms.pojos.FormsStats;
import com.mobissime.forms.service.FormsParserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/rest/forms")
public class FormsController {

    private final FormsParserService parserService;

    public FormsController(FormsParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping("/stats/{formName}")
    public FormsStats getStats(@PathVariable String formName) {
        try {
            return parserService.getStats(formName);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found: " + formName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing form: " + e.getMessage());
        }
    }

    @GetMapping("/canvas/{formName}")
    public List<Canvas> getCanvases(@PathVariable String formName) {
        try {
            return parserService.getCanvases(formName);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found: " + formName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing form: " + e.getMessage());
        }
    }

    @GetMapping("/blocks/{formName}")
    public List<Block> getBlocks(@PathVariable String formName) {
        try {
            List<Block> blocks = parserService.getBlocks(formName);
            for (Block blk : blocks) {
                List<Trigger> triggers = parserService.getTriggersForBlock(formName, blk.getName());
                blk.setNumberOfTriggers(triggers.size());
            }
            return blocks;
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found: " + formName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing form: " + e.getMessage());
        }
    }

    @GetMapping("/triggersforblock/{formName}/{blockName}")
    public List<Trigger> getTriggersForBlock(@PathVariable String formName, @PathVariable String blockName) {
        try {
            List<Trigger> triggers = parserService.getTriggersForBlock(formName, blockName);
            triggers.forEach(t -> {
                if (t.getTriggerText() != null) {
                    t.setTriggerText(t.getTriggerText().replace("&#10;", "<br />"));
                }
            });
            return triggers;
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found: " + formName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing form: " + e.getMessage());
        }
    }
}
