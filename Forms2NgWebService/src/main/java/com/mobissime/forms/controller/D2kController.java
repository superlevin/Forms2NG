package com.mobissime.forms.controller;

import com.mobissime.forms.objects.MenuModule;
import com.mobissime.forms.objects.ObjectLibrary;
import com.mobissime.forms.service.FormsParserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/rest/d2k")
public class D2kController {

    private final FormsParserService parserService;

    public D2kController(FormsParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping("/mmb/{name}")
    public MenuModule getMenuModule(@PathVariable String name) {
        try {
            return parserService.getMenuModule(name);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MMB not found: " + name);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing MMB: " + e.getMessage());
        }
    }

    @GetMapping("/olb/{name}")
    public ObjectLibrary getObjectLibrary(@PathVariable String name) {
        try {
            return parserService.getObjectLibrary(name);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "OLB not found: " + name);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing OLB: " + e.getMessage());
        }
    }
}
