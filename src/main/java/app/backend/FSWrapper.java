package app.backend;

import com.sun.org.apache.regexp.internal.CharacterArrayCharacterIterator;
import com.sun.org.apache.xerces.internal.dom.AttributeMap;
import com.sun.org.apache.xerces.internal.xs.StringList;
import javafx.util.Pair;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * This class represents a scalable, reusable content File System manager.
 *
 * <p>It fetches a template directory structure from an XML document. It uses that structure to fetch the URIs
 * of content files. You can, for instance, traverse a nested directory structure where there is a file to
 * retrieve from each folder. You could change your mind about the way the files should be nested, and all
 * that would be required is you would have to change the simple XML file and your calls to this class. </p>
 *
 * <p>In this File Management system I use a few of my own terms to describe how things work:
 *  <ul>
 *      <li><b>Content</b>: is any kind of file you want to store in the filesystem. A piece of content can be
 *      independent or it can fit within a nested directory structure.</li>
 *      <li><b>Type</b>: The type of a piece of content is used to identify it as part of a set.
 *      Different content files of the same type will be in the same position relative to their surrounding directory
 *      structures. For example, for each creation there is a video that has the text and audio combined. Each instance
 *      of such a video is stored in the same relative position and they have the same use. Thus, the video files are of
 *      the same type.</li>
 *      <li><b>Parameters: </b> are used to name files in sets to make them unique. They are similar to printf format
 *      tokens with the main difference being they take on number IDs. If a child element uses the same parameters in
 *      its nameFormat as a </li>
 *      <li><b>Unit</b>: A unit is a bit of an abstract concept. As explained above, if the same parameter is used in
 *      both a parent directory and its child, the parameter is treated the same. However, a child does not have to use
 *      the parent's parameter for us to search for the file. If we provide the type and the parent's parameters, then
 *      there's enough information to get the file (unless it is part of a fileSet). This is the concept of units:
 *      files are attached to their parent directories and we search them through parameters.  </li>
 *      <li><b>workingDir</b>: This is the directory where the top-level content folder is contained</li>
 *      <li><b>rootContentDir</b>: This directory is always one below the workingDir. It is the top-level content
 *      directory.</li>
 *  </ul>
 *  <p>Not all content has to exist within a unit. For instance, when we started making our simple pronunciation
 *  practice application, sound files were stored independently in the rootContentDir. Content that <i>doesn't </i>
 *  exist within a unit must obviously have a unique name by which to identify it. Content that <i>does </i>
 *  exist within a unit can have the same fileName as the same type of content in other units. Going back to the
 *  creations example, we can have a creation.mp4 for each creation unit.</p>
 *
 */
public class FSWrapper {
    protected Path workingDir;
    protected Path rootContentDir;
    private Document fsStructure;
    private XPath xpath;

    private final String DIR = "dir";
    private final String DIRSET = "dirSet";
    private final String FILE = "file";
    private final String FILESET = "fileSet";
    private final String[] ALLTAGS = {DIR, DIRSET, FILE, FILESET};

    /**
     * Construct the FSWrapper.
     */
    public FSWrapper() {
        workingDir = Paths.get("").toAbsolutePath();
       //  System.out.println("Current working dir: " + workingDir.toString());
        //workingDir = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();

        extractFileStructure();
        xpath = XPathFactory.newInstance().newXPath();

        if(!Files.exists(rootContentDir)) {
            try {
                Files.createDirectory(rootContentDir);
            } catch(IOException e) {
                throw new RuntimeException("Error creating root content directory", e);
            }
        }
    }

    /**
     * Fetch the content filesystem configuration and store it in memory as a Document.
     * There will never be a large configuration because we are simply using this as a single template for file storage,
     * not for storing the repeated data itself.
     */
    private void extractFileStructure() {
        try {
            URI structureLocation = this.getClass().getResource("FileSystem.xml").toURI();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(this.getClass().getResource("FSScheme.xsd")));
            DocumentBuilder builder = dbf.newDocumentBuilder();
            fsStructure = builder.parse(Paths.get(structureLocation).toFile());
        } catch (SAXException | ParserConfigurationException e) {
            throw new RuntimeException("Error parsing FS config", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading FS config", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error fetching FS config", e);
        }

        Element rootElement = fsStructure.getDocumentElement();
        if(!rootElement.getTagName().equals("rootDir")) {
            throw new RuntimeException("No rootDir specified in FS config file");
        }
        if(!rootElement.hasAttribute("name")) {
            throw new RuntimeException("No name attribute for root dir in FS config file");
        }
        rootContentDir = workingDir.resolve(rootElement.getAttribute("name"));
    }

    /**
     * Deletes the directory structure for the given content type
     * @param type The content type
     * @param params The parameters of the unit
     */
    public void deleteFiles(String type, String... params) {
        try {
            List<Pair<String, Path>> pathList = getContent(type, params);
            for(Pair<String, Path> pathPair: pathList) {
                Files.deleteIfExists(pathPair.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create the directory structure for the given content type
     * @param type The content type
     * @param params The parameters of the unit
     */
    public void createDirectoryStruct(String type, String... params) {
        List<Pair<String, Path>> pathList = createPaths(type, params);

        try {
            for (Pair<String, Path> pathPair: pathList) {
                Element typeElem = getElementOfType(pathPair.getKey());
                switch (typeElem.getTagName()) {
                    case DIR:
                    case DIRSET:
                        if (Files.notExists(pathPair.getValue())) {
                            Files.createDirectory(pathPair.getValue());
                        }
                        break;
                    case FILE:
                    case FILESET:
                        if (Files.notExists(pathPair.getValue()) &&
                                typeElem.hasAttribute("createNew") &&
                                typeElem.getAttribute("createNew") == "true") {
                            Files.createFile(pathPair.getValue());
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Form the filepaths for the given type. If the type is a folder, the filepaths for all children are
     * generated. Each child path appears after its parent, upon which it is dependant.
     * @param type The content type
     * @param params The parameters of the unit
     * @return The filePaths of the new content, including content that does not yet exist.
     */
    public List<Pair<String, Path>> createPaths(String type, String... params) {
        List<Pair<String, Path>> pathList = new ArrayList<>();
        Element element = getElementOfType(type);
        if(element == null) {
            return null;
        }

        Path path = workingDir;
        path = resolveParents(type, pathList, path, params);

        Element currentElement = element;
        boolean newElem = true;
        Deque<Integer> indices = new ArrayDeque<>();
        indices.push(0);
        while(!currentElement.equals(element.getParentNode())) {
            int i;
            if(newElem) {
                i = 0;
            } else {
                i = indices.pop();
                currentElement = (Element) currentElement.getParentNode();
                path = path.getParent();
            }
            List<Element> children = getChildElements(currentElement);
            newElem = false;
            for(; i < children.size(); i++) {
                if(children.get(i).hasAttribute("nameFormat")) {
                    String format = children.get(i).getAttribute("nameFormat");
                    path = path.resolve(fillString(format, params));
                } else if(children.get(i).hasAttribute("name")) {
                    path = path.resolve(children.get(i).getAttribute("name"));
                }
                pathList.add(new Pair(currentElement.getAttribute("type"), path));
                if(children.get(i).getTagName().equals(DIR) || children.get(i).getTagName().equals(DIRSET)) {
                    newElem = true;
                    indices.push(i + 1);
                    currentElement = children.get(i);
                    break;
                } else if(children.get(i).getTagName().equals(FILE) || children.get(i).getTagName().equals(FILESET)) {
                    path = path.getParent();
                }
            }
        }
        return pathList;
    }

    private Path resolveParents(String type, List<Pair<String, Path>> pathList, Path path, String... params) {
        List<Element> parentList = getAccessPathOfType(type);
        Collections.reverse(parentList);
        for(Element parent: parentList) {
            if(parent.hasAttribute("nameFormat")) {
                String format = parent.getAttribute("nameFormat");
                path = path.resolve(fillString(format, params));
            } else if(parent.hasAttribute("name")) {
                path = path.resolve(parent.getAttribute("name"));
            }
        }
        pathList.add(new Pair<String, Path>(parentList.get(parentList.size() - 1).getAttribute("type"), path));
        return path;
    }

    private String fillString(String format, String... params) {
        StringBuilder filled = new StringBuilder();
        char[] form = format.toCharArray();
        for(int i = 0; i < form.length - 1; i++) {
            if(form[i] == '%') {
                int paraNum = Character.getNumericValue(form[++i]);
                if(paraNum > 9 || paraNum >= params.length + 1 || paraNum < 0) {
                    continue;
                } else {
                    filled.append((params[paraNum - 1]));
                }
            } else {
                filled.append(form[i]);
            }
        }
        if(form[form.length - 2] != '%') {
            filled.append(form[form.length - 1]);
        }
        return filled.toString();
    }

    /**
     * Retrieves a set of resources of a specific type using its parameters.
     * Parameters are interpreted as being of increasing value. i.e. The first param is %1,
     * the second param is %2, and so on...
     *
     * @param type The content type to fetch, given by the type attribute in the XML
     * @param params The parameters of the file
     * @return The filePaths of the found content.
     */
    public List<Path> getFilesByParameter(String type, String... params) throws IOException {
        List<Path> pathList = new ArrayList<>();

        List<Pair<String, Path>> paths = getContent(type, params);
        List<Element> allChildren = new ArrayList<>();
        getAllChildren(getElementOfType(type), allChildren);
        if(allChildren.isEmpty()) {
            return pathList;
        }

        for(Pair<String, Path> pathPair: paths) {
            for(Element e: allChildren) {
                if(pathPair.getKey().equals(e.getAttribute(type))) {
                    pathList.add(pathPair.getValue());
                }
            }
        }
        return pathList;
    }

    /**
     * Fetches a list of the filepaths of every content file of a particular type.
     *      * This is a relatively expensive operation so it should only be performed at the start of an application.
     *      * @return A list of content files
     * @param type The content type to fetch, given by the type attribute in the XML
     * @return The filePaths of the found content
     */
    public List<Pair<String, Path>> getAllContentOfType(String type) throws IOException {
        return getContent(type);
    }

    /**
     * Reusable method to extract content from the filesystem based on parameters
     */
    private List<Pair<String, Path>> getContent(String type, String... params) throws IOException {
        List<Element> parentList = getAccessPathOfType(type);
        Path currentDir = rootContentDir;
        Deque<Integer> indices = new ArrayDeque<>();
        indices.push(0);
        boolean newDir = true;
        List<Pair<String, Path>> filePaths = new ArrayList<>();

        while(! (Files.isSameFile(currentDir.getParent(), workingDir) && !newDir)){
            // If we're in a new directory, set the index to 0, otherwise get the previous one we pushed
            int i;
            if(newDir) {
                i = 0;
            } else {
                i = indices.pop();
                currentDir = currentDir.getParent();
            }
            List<Path> contentFiles = listFiles(currentDir);
            newDir = false;
            for (; i < contentFiles.size(); i++) {
                Path file = contentFiles.get(i);
                //if the depth of the stack isn't right for the depth of the filetype
                if((parentList.size() - indices.size()) > 1) {
                    if (Files.isDirectory(file)) {
                        currentDir = file;
                        indices.push(i + 1);
                        newDir = true;
                        break;
                    }
                } else if (parentList.get(0).getTagName().equals(FILE) || parentList.get(0).getTagName().equals(FILESET)) {
                    if(params.length > 0) {
                        boolean matchesParams = true;
                        Map<Integer, String> fileParams = extractParamsForUnit(file, parentList.get(parentList.size() - indices.size() - 1));
                        if (fileParams != null) {
                            int length = fileParams.size() < params.length ? fileParams.size() : params.length;
                            for (int j = 0; j < length; j++) {
                                if (!params[j].equals(fileParams.get(j + 1))) {
                                    matchesParams = false;
                                }
                            }
                            if(matchesParams) {
                                attemptExtractContent(file, type, filePaths);
                            }
                        }
                    } else {
                        attemptExtractContent(file, type, filePaths);
                    }

                } else if(parentList.get(0).getTagName().equals(DIR) || parentList.get(0).getTagName().equals(DIRSET)) {
                    if(Files.isDirectory(file)) {
                        if(!attemptExtractContent(file, type, filePaths)) {
                            currentDir = file;
                            indices.push(i + 1);
                            newDir = true;
                            break;
                        }
                    }
                }
            }
        }
        return filePaths;
    }

    /*
     *   This method is responsible for scanning a file and determining if it matches the given type, and extracting the paths
     *   of all the child files. The list of Strings is formatted to reflect the XML configuration.
     *   A file matches the type if it satisfies the following conditions:
     *      - All of its children are the right types according to the XML file (if it's a directory)
     *      - The path from the rootContentDir to the file is consistent with the structure in the XML file
     *      - The names of all the parent directories, as well as the file's name itself, are correct according to the XML file.
    */
    private boolean attemptExtractContent(Path file, String type, List<Pair<String, Path>> filePaths) {
        // If the given content type doesn't exist, return false.
        Element typeElement = getElementOfType(type);

        // If the target file type is a directory
        switch (typeElement.getTagName()) {
            case DIR:
            case DIRSET:
                if (!Files.isDirectory(file)) {
                    return false;
                }

                // For each expected child of the directory (as specified in the XML), we need to find a file or folder that matches it
                // This is the core of the algorithm for matching a file to a content type. We recursively check all the children of
                // directories.
                List<Path> children = listFiles(file);
                List<Element> childElements = getChildElements(typeElement);
                boolean[] haveConvered = new boolean[children.size()];
                int foundElements = 0;
                for (Element childE : childElements) {
                    boolean foundSomething = false;
                    for (Path childF : children) {
                        if (!haveConvered[children.indexOf(childF)] && attemptExtractContent(childF, childE.getAttribute("type"), filePaths)) {
                            haveConvered[children.indexOf(childF)] = true;
                            foundSomething = true;
                        }
                    }
                    if (foundSomething) {
                        foundElements++;
                    }
                }
                // If a directory has a match for each of its expected children, we can safely assume it is usable as there is
                // definitely all the required content in the directory.
                if (foundElements != childElements.size()) {
                    return false;
                }
                // This is the base case of our recursion - the file is not a directory.
                break;
            case FILE:
            case FILESET:
                if (Files.isDirectory(file) || extractParamsForUnit(file, typeElement) == null) {
                    return false;
                }
                break;
            default:
                return false;
        }

        // Having filtered out anything that doesn't match an element of the XML in terms of name and dir structure,
        // we can assume this file is usable
        filePaths.add(new Pair<>(type, file));
        return true;
    }

    /*
        Ensure that a given file matches a given element in terms of name. This will check the parametrisation of all
        the file's parents and ensure they are consistent (The same parameters must have the same values)
     */
    private Map<Integer, String> extractParamsForUnit(Path file, Element element) {
        Path upFile = file;
        HashMap<Integer, String> nameParameters = new HashMap<>();
        List<Element> parentList = getAccessPathOfType(element.getAttribute("type"));

        // the first "parent" is the file itself
        for(Element parent: parentList) {
            if(parent.hasAttribute("name")) {
                if (! upFile.getFileName().toString().equals(parent.getAttribute("name"))) {
                    return null;
                }
                upFile = upFile.getParent();
                continue;
            } else if(!parent.hasAttribute("nameFormat")) {
                // This should never happen
                throw new RuntimeException("No name attribute for element " + element.getAttribute("type") + " in FS config XML");
            }

            String nameFormat = parent.getAttribute("nameFormat");
            HashMap<Integer, String> theseParams = extractParametersFromFilename(upFile, nameFormat);
            if(theseParams == null) {
                return null;
            }
            Set<Integer> keys = theseParams.keySet();
            for(Integer key: keys) {
                if(nameParameters.containsKey(key)) {
                    if(! nameParameters.get(key).equals(theseParams.get(key))) {
                        return null;   // We have found a discrepancy!
                    }
                } else {
                    nameParameters.put(key, theseParams.get(key));
                }
            }
            upFile = upFile.getParent();
        }
        return nameParameters;
    }

    /*
        Extract the parameters from the name of a file, based on a format.
     */
    private HashMap<Integer, String> extractParametersFromFilename(Path file, String nameFormat) {
        HashMap<Integer, String> result = new HashMap<>();
        boolean doesNotMatch = false;
        String fileName = file.getFileName().toString();
        String[] separators = nameFormat.split("(%[0-9])");

        // First we deal with a special case where we start with a parameter. We extract the first parameter and set the
        // parIndex to the index of the next one. The rest of the method treats the name as starting with a separator
        int parIndex = nameFormat.indexOf('%');
        int startOfSeparator = 0, endOfSeparator;
        if(parIndex == 0) {
            if(separators.length == 0) {
                startOfSeparator = fileName.length();
            } else {
                startOfSeparator = fileName.indexOf(separators[1]); // the first string split by regex will actually be empty
            }

            String parameter = fileName.substring(0, startOfSeparator);
            result.put(Character.getNumericValue(nameFormat.charAt(parIndex + 1)), parameter);
            parIndex = nameFormat.indexOf('%', parIndex + 1);
        }

        // Now we loop through the body of the fileName, parsing a parameter at a time using the surrounding separators
        int i;
        for(i = 0; i < separators.length - 1; i++) {
            String separator = separators[i];
            if(separator.equals("")) {
                continue;
            }
            String nextSeparator = separators[i+1];

            if(parIndex == -1) {
                break;
            }
            if(fileName.indexOf(separator) != startOfSeparator) {
                doesNotMatch = true;    // we have detected a discrepancy and this name does not match the format
                break;
            }
            endOfSeparator = startOfSeparator + separator.length(); // actually the character after the separator
            String parameter = fileName.substring(endOfSeparator, fileName.indexOf(nextSeparator) - 1);

            result.put(Character.getNumericValue(nameFormat.charAt(parIndex + 1)), parameter);
            startOfSeparator = fileName.indexOf(nextSeparator);
            parIndex = nameFormat.indexOf('%', parIndex+1);
        }

        // Now we deal with the case of ending with a parameter. At this point if the boolean doesNotMatch has not been
        // set to true, then we must have looped through all the separators. So, we use the last one to get the start of
        // the parameter and extract it
        if(parIndex != -1 && !doesNotMatch) {
            endOfSeparator = startOfSeparator + separators[separators.length - 1].length();

            result.put(Integer.parseInt(nameFormat.substring(parIndex + 1)),
                    fileName.substring(endOfSeparator));
        }

        return doesNotMatch ? null : result;
    }

    private List<Element> getAccessPathOfType(String type) {
        Node currentNode = getElementOfType(type);
        if(currentNode == null) {
            return null;
        }
        ArrayList<Element> parentList = new ArrayList<>();
        do {
            parentList.add((Element) currentNode);
            currentNode = currentNode.getParentNode();
        } while(currentNode.getNodeType() == Node.ELEMENT_NODE);

        return parentList;
    }

    private Element getElementOfType(String type) {
        try {
            return (Element) xpath.compile("//*[@type='" + type + "']").evaluate(fsStructure, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error finding content", e);
        }
    }


    private void getAllChildren(Element element, List<Element> allChildren) {
        List<Element> elements = getChildElements(element);
        for(Element e: elements) {
            allChildren.add(e);
            getAllChildren(e, allChildren);
        }
    }

    private List<Element> getChildElements(Element element) {
        List<Element> children = new ArrayList<>();
        for(String tag: ALLTAGS) {
            NodeList nodes = element.getElementsByTagName(tag);
            for(int i = 0; i < nodes.getLength(); i++) {
                if(nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    children.add((Element) nodes.item(i));
                }
            }
        }
        return children;
    }

    private List<Path> listFiles(Path dir) {
        List<Path> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path path: directoryStream) {
                fileNames.add(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }
}