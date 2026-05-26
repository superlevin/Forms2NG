package com.mobissime.forms.controller;

import com.mobissime.forms.pojos.MenuEntry;
import com.mobissime.forms.service.FormsParserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/menu")
public class MenuController {

    private final FormsParserService parserService;

    public MenuController(FormsParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping("/")
    public List<MenuEntry> getFormMenuList() {
        return toMenuEntries(parserService.listFilesByType("_fmb.xml"));
    }

    @GetMapping("/mmb")
    public List<MenuEntry> getMenuModuleList() {
        return toMenuEntries(parserService.listFilesByType("_mmb.xml"));
    }

    @GetMapping("/olb")
    public List<MenuEntry> getObjectLibraryList() {
        return toMenuEntries(parserService.listFilesByType("_olb.xml"));
    }

    private List<MenuEntry> toMenuEntries(List<String> names) {
        return names.stream()
                .map(name -> {
                    MenuEntry e = new MenuEntry();
                    e.setFormsName(name);
                    return e;
                })
                .collect(Collectors.toList());
    }
}
