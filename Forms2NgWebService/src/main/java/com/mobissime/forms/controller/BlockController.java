package com.mobissime.forms.controller;

import com.mobissime.forms.objects.Block;
import com.mobissime.forms.service.FormsParserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/rest/blocks")
public class BlockController {

    private final FormsParserService parserService;

    public BlockController(FormsParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping("/{formName}/{blockName}")
    public Block getBlock(@PathVariable String formName, @PathVariable String blockName) {
        try {
            Block blk = parserService.getBlock(formName, blockName);
            if (blk == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found: " + blockName);
            }
            return blk;
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found: " + formName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}
