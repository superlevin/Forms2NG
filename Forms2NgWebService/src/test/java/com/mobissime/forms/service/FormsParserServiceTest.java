package com.mobissime.forms.service;

import com.mobissime.forms.config.FormsProperties;
import com.mobissime.forms.objects.Block;
import com.mobissime.forms.objects.Canvas;
import com.mobissime.forms.pojos.FormsStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormsParserServiceTest {

    @TempDir
    Path tempDir;

    FormsParserService service;

    private static final String SAMPLE_FMB_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Module xmlns="http://xmlns.oracle.com/Forms" version="12">
              <FormModule Name="TESTFORM" Title="Test Form">
                <Block Name="BLK_TEST" DatabaseBlock="true">
                  <Item Name="ITEM1"/>
                  <Trigger Name="WHEN-NEW-ITEM-INSTANCE" TriggerText="NULL;"/>
                </Block>
                <Canvas Name="CVS_MAIN"/>
              </FormModule>
            </Module>
            """;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(tempDir.resolve("TESTFORM_fmb.xml"), SAMPLE_FMB_XML);

        FormsProperties props = new FormsProperties();
        props.setFormsPath(tempDir.toString());
        service = new FormsParserService(props);
    }

    @Test
    void listFilesByType_returnsFmbNames() {
        List<String> names = service.listFilesByType("_fmb.xml");
        assertEquals(1, names.size());
        assertEquals("TESTFORM", names.get(0));
    }

    @Test
    void getBlocks_returnsExpectedBlock() throws Exception {
        List<Block> blocks = service.getBlocks("TESTFORM");
        assertEquals(1, blocks.size());
        assertEquals("BLK_TEST", blocks.get(0).getName());
    }

    @Test
    void getCanvases_returnsExpectedCanvas() throws Exception {
        List<Canvas> canvases = service.getCanvases("TESTFORM");
        assertEquals(1, canvases.size());
        assertEquals("CVS_MAIN", canvases.get(0).getName());
    }

    @Test
    void getStats_returnsCorrectCounts() throws Exception {
        FormsStats stats = service.getStats("TESTFORM");
        assertEquals(1, stats.getNumberOfBlocks());
        assertEquals(1, stats.getNumberOfTriggers());
        assertEquals(1, stats.getNumberOfItems());
        assertEquals(1, stats.getNumberOfCanvas());
    }

    @Test
    void getTriggersForBlock_returnsExpectedTriggers() throws Exception {
        var triggers = service.getTriggersForBlock("TESTFORM", "BLK_TEST");
        assertEquals(1, triggers.size());
        assertEquals("WHEN-NEW-ITEM-INSTANCE", triggers.get(0).getName());
    }

    @Test
    void getBlocks_throwsFileNotFoundForMissingForm() {
        assertThrows(FileNotFoundException.class, () -> service.getBlocks("NONEXISTENT"));
    }
}
