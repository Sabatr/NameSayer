package app.backend.filesystem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;

/**
 * This class is responsible for constructing a FSWrapper from a template directory structure represented in an XML File.
 * It transforms the XML hierarchy into a {@link TemplateFile hierarchy}
 */
public class FSWrapperFactory {

    private Path _workingDir;
    private Path _rootContentDir;
    private Document _fsStructure;
    private Element _rootElement;

    public FSWrapperFactory(URI structure) {
        this(structure, null);
    }

    /**
     * Construct the FSWrapperFactory.
     */
    public FSWrapperFactory(URI structure, Path customContentDir) {
        // The constructor first resolves the working directory where all the content will be stored.
        if(!structure.toString().contains("jar")) {
            _workingDir = Paths.get("").toAbsolutePath();
        } else {
            try {
                _workingDir = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            } catch (URISyntaxException e) {
                //e.printStackTrace();
                _workingDir = Paths.get("").toAbsolutePath();
            }
        }

        extractFileStructure(structure);

        // make sure the root element of the XML file is correct. Retrieve the name of the root content folder and
        // resolve its path
        _rootElement = _fsStructure.getDocumentElement();
        if(customContentDir == null) {
            if (!_rootElement.getTagName().equals("rootDir")) {
                throw new RuntimeException("No rootDir specified in FS config file");
            }
            if (!_rootElement.hasAttribute("name")) {
                throw new RuntimeException("No name attribute for root dir in FS config file");
            }
            _rootContentDir = _workingDir.resolve(_rootElement.getAttribute("name"));
        } else {
            if(!Files.exists(customContentDir)) {
                throw new RuntimeException("Contentdir doesn't exist");
            }
            _rootContentDir = customContentDir;
            _workingDir = customContentDir.getParent();
            _fsStructure.getDocumentElement().setAttribute("name", customContentDir.getFileName().toString());
        }

        if(!Files.exists(_rootContentDir)) {
            try {
                Files.createDirectory(_rootContentDir);
            } catch(IOException e) {
                throw new RuntimeException("Error creating root content directory", e);
            }
        }
    }

    /*----------------  Setting up the XML schema  ----------------*/

    /**
     * Fetches the content filesystem configuration and stores it in memory as a Document.
     * There will never be a large configuration because we are simply using this as a single template for file storage,
     * not for storing the repeated data itself.
     */
    private void extractFileStructure(URI structureLocation) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            try {
                dbf.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                        newSchema(this.getClass().getResource("FSScheme.xsd")));
            } catch (FileSystemNotFoundException e) {
                try {
                    // try resolve the schema location. If the program is packaged in a jar, we need to get the
                    // zip filesystem provider
                    URI schemeLocation = this.getClass().getResource("FSScheme.xsd").toURI();
                    if(schemeLocation.toString().contains("jar") && "jar".equals(schemeLocation.getScheme())){
                        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
                            if (provider.getScheme().equalsIgnoreCase("jar")) {
                                try {
                                    provider.getFileSystem(schemeLocation);
                                } catch (FileSystemNotFoundException e1) {
                                    // in this case we need to initialize it first:
                                    provider.newFileSystem(schemeLocation, Collections.emptyMap());
                                }
                            }
                        }
                    }
                } catch (URISyntaxException e2) {
                    //.printStackTrace();
                }
            }

            DocumentBuilder builder = dbf.newDocumentBuilder();

            if(structureLocation.toString().contains("jar") && "jar".equals(structureLocation.getScheme())){
                for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
                    if (provider.getScheme().equalsIgnoreCase("jar")) {
                        try {
                            provider.getFileSystem(structureLocation);
                        } catch (FileSystemNotFoundException e) {
                            // in this case we need to initialize it first:
                            provider.newFileSystem(structureLocation, Collections.emptyMap());
                        }
                    }
                }
                //System.out.println(structureLocation.toString().substring(structureLocation.toString().lastIndexOf('/') + 1));
                _fsStructure = builder.parse(this.getClass().getResourceAsStream(
                        structureLocation.toString().substring(structureLocation.toString().lastIndexOf('/') + 1)));
            } else {
                _fsStructure = builder.parse(Paths.get(structureLocation).toFile());
            }

        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException("Error parsing FS config", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading FS config", e);
        }
    }

    public FSWrapper buildFSWrapper() {
        TemplateFolder rootDirTemplate = getTemplateDirectoryStructure();
        return new FSWrapper(rootDirTemplate, _workingDir, _rootContentDir);
    }

    private TemplateFolder getTemplateDirectoryStructure() {
        // We already ensured that the root XML element has the correct tag and attributes in the constructor
        //      so we can go ahead and meake the

        TemplateFolder rootDirTemplate = new TemplateFolder("rootDir", _rootElement.getAttribute("name"),
                false, false, false, null);
        recursivelyExploreXML(rootDirTemplate, _rootElement);
        return rootDirTemplate;
    }

    private void recursivelyExploreXML(TemplateFolder root, Element rootElement) {
        TemplateFile.TemplateFileBuilder builder = new TemplateFile.TemplateFileBuilder();
        NodeList nodes = rootElement.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = ((Element) nodes.item(i));

                if (e.hasAttribute("type")) {
                    builder.type(e.getAttribute("type"));
                } else {
                    throw new RuntimeException("No type attribute on " + e.getTagName() + " element");
                }

                boolean isDir = false;
                builder.canBeMultiple(false);

                String tag = e.getTagName();
                switch (tag) {
                    case FSWrapper.DIR:
                        isDir = true;
                    case FSWrapper.FILE:
                        if(e.hasAttribute("name")) {
                            builder.nameFormat(e.getAttribute("name")).hasParameters(false);
                        } else if(e.hasAttribute("nameFormat")) {
                            builder.nameFormat(e.getAttribute("nameFormat")).hasParameters(true);
                        } else {
                            throw new RuntimeException("No name attribute in DIR or FILE element");
                        }
                        break;
                    case FSWrapper.DIRSET:
                        isDir = true;
                    case FSWrapper.FILESET:
                        builder.canBeMultiple(true);
                        if(e.hasAttribute("nameFormat")) {
                            builder.nameFormat(e.getAttribute("nameFormat")).hasParameters(true);
                        } else {
                            throw new RuntimeException("No name attribute in *SET element");
                        }
                        break;
                    default:
                        throw new RuntimeException("Element with unrecognised tag: " + tag);
                }

                if (e.hasAttribute("createNew") && e.getAttribute("createNew").equals("true")) {
                    builder.shouldCreateNew(true);
                } else {
                    builder.shouldCreateNew(false);
                }

                boolean allChildren = false;
                if (e.hasAttribute("needAllChildren") && e.getAttribute("needAllChildren").equals("true")) {
                    allChildren = true;
                }

                TemplateFile tFile;
                if(isDir) {
                    TemplateFolder tFolder = builder.parent(root).buildFolder(allChildren);
                    recursivelyExploreXML(tFolder, e);
                    tFile = tFolder;
                } else {
                    tFile = builder.parent(root).buildFile();
                }
                root.addChild(tFile);
            }
        }
    }
}
