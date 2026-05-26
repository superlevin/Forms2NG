package com.mobissime.forms.service;

import com.mobissime.forms.config.FormsProperties;
import com.mobissime.forms.objects.Block;
import com.mobissime.forms.objects.Canvas;
import com.mobissime.forms.objects.FormModule;
import com.mobissime.forms.objects.Item;
import com.mobissime.forms.objects.MenuModule;
import com.mobissime.forms.objects.Module;
import com.mobissime.forms.objects.ObjectLibrary;
import com.mobissime.forms.objects.Trigger;
import com.mobissime.forms.pojos.FormsStats;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class FormsParserService {

    private static final Pattern SAFE_MODULE_NAME = Pattern.compile("[A-Za-z0-9._-]+");

    private final FormsProperties props;
    private final JAXBContext jaxbContext;

    public FormsParserService(FormsProperties props) throws JAXBException {
        this.props = props;
        this.jaxbContext = JAXBContext.newInstance(Module.class);
    }

    private File resolveFile(String name, String suffix) throws FileNotFoundException {
        if (!SAFE_MODULE_NAME.matcher(name).matches()) {
            throw new FileNotFoundException("File not found: invalid module name");
        }
        Path basePath = Paths.get(props.getFormsPath()).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(name + suffix).normalize();
        if (!resolvedPath.startsWith(basePath) || !Files.isRegularFile(resolvedPath)) {
            throw new FileNotFoundException("File not found: " + resolvedPath);
        }
        return resolvedPath.toFile();
    }

    private Module parseModule(String name, String suffix) throws Exception {
        Unmarshaller u = jaxbContext.createUnmarshaller();
        XMLInputFactory inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try (InputStream inputStream = Files.newInputStream(resolveFile(name, suffix).toPath())) {
            XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);
            try {
                return (Module) u.unmarshal(xmlStreamReader);
            } finally {
                xmlStreamReader.close();
            }
        }
    }

    public List<String> listFilesByType(String suffix) {
        File folder = new File(props.getFormsPath());
        List<String> names = new ArrayList<>();
        if (!folder.exists() || !folder.isDirectory()) {
            return names;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return names;
        }
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(suffix)) {
                names.add(f.getName().replace(suffix, ""));
            }
        }
        return names;
    }

    public FormModule getFormModule(String formName) throws Exception {
        Module mod = parseModule(formName, "_fmb.xml");
        if (mod.getFormModule() == null) {
            throw new IllegalStateException("File does not contain a FormModule: " + formName);
        }
        return mod.getFormModule();
    }

    public MenuModule getMenuModule(String menuName) throws Exception {
        Module mod = parseModule(menuName, "_mmb.xml");
        if (mod.getMenuModule() == null) {
            throw new IllegalStateException("File does not contain a MenuModule: " + menuName);
        }
        return mod.getMenuModule();
    }

    public ObjectLibrary getObjectLibrary(String olbName) throws Exception {
        Module mod = parseModule(olbName, "_olb.xml");
        if (mod.getObjectLibrary() == null) {
            throw new IllegalStateException("File does not contain an ObjectLibrary: " + olbName);
        }
        return mod.getObjectLibrary();
    }

    public List<Block> getBlocks(String formName) throws Exception {
        FormModule form = getFormModule(formName);
        List<Block> blocks = new ArrayList<>();
        for (Object o : form.getCoordinateOrAlertOrAttachedLibrary()) {
            if (o instanceof Block b) {
                blocks.add(b);
            }
        }
        return blocks;
    }

    public List<Canvas> getCanvases(String formName) throws Exception {
        FormModule form = getFormModule(formName);
        List<Canvas> canvases = new ArrayList<>();
        for (Object o : form.getCoordinateOrAlertOrAttachedLibrary()) {
            if (o instanceof Canvas c) {
                canvases.add(c);
            }
        }
        return canvases;
    }

    public List<Trigger> getTriggersForBlock(String formName, String blockName) throws Exception {
        List<Block> blocks = getBlocks(formName);
        List<Trigger> triggers = new ArrayList<>();
        for (Block blk : blocks) {
            if (blk.getName().equals(blockName)) {
                for (Object o : blk.getDataSourceArgumentOrDataSourceColumnOrItem()) {
                    if (o instanceof Trigger t) {
                        triggers.add(t);
                    }
                }
                break;
            }
        }
        return triggers;
    }

    /**
     * Returns the block for the given form, or {@code null} when the block does not exist.
     */
    public Block getBlock(String formName, String blockName) throws Exception {
        for (Block blk : getBlocks(formName)) {
            if (blk.getName().equals(blockName)) {
                return blk;
            }
        }
        return null;
    }

    public FormsStats getStats(String formName) throws Exception {
        List<Block> blocks = getBlocks(formName);
        List<Trigger> allTriggers = new ArrayList<>();
        List<Item> allItems = new ArrayList<>();

        for (Block blk : blocks) {
            for (Object o : blk.getDataSourceArgumentOrDataSourceColumnOrItem()) {
                if (o instanceof Item item) {
                    allItems.add(item);
                } else if (o instanceof Trigger t) {
                    allTriggers.add(t);
                }
            }
        }

        FormsStats stats = new FormsStats();
        stats.setNumberOfBlocks(blocks.size());
        stats.setNumberOfTriggers(allTriggers.size());
        stats.setNumberOfItems(allItems.size());
        stats.setNumberOfCanvas(getCanvases(formName).size());
        return stats;
    }
}
