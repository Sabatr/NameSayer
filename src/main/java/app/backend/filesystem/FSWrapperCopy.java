package app.backend.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * This class represents a scalable, reusable content File System manager.
 *
 * <p>A template directory structure is fetched by the factory class from an XML document.
 * This class uses that structure to fetch the URIs
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
 *      <li><b>_workingDir</b>: This is the directory where the top-level content folder is contained</li>
 *      <li><b>_rootContentDir</b>: This directory is always one below the _workingDir. It is the top-level content
 *      directory.</li>
 *  </ul>
 *  <p>Not all content has to exist within a unit. For instance, when we started making our simple pronunciation
 *  practice application, sound files were stored independently in the _rootContentDir. Content that <i>doesn't </i>
 *  exist within a unit must obviously have a unique name by which to identify it. Content that <i>does </i>
 *  exist within a unit can have the same fileName as the same type of content in other units. Going back to the
 *  creations example, we can have a creation.mp4 for each creation unit.</p>
 *
 *  <p> see {@link FSWrapperFactory for a description of the XML scheme}</p>
 *
 * @author Marc Burgess
 */
public class FSWrapperCopy {
    private Path _workingDir;
    private Path _rootContentDir;
    private TemplateFolder _templateRoot;

    static final String DIR = "dir";
    static final String DIRSET = "dirSet";
    static final String FILE = "file";
    static final String FILESET = "fileSet";

    FSWrapperCopy(TemplateFolder templateRoot, Path workingDir, Path rootContentDir) {
        _workingDir = workingDir;
        _rootContentDir = rootContentDir;
        _templateRoot = templateRoot;
    }

    /**
     * Copies content from one directory structure to another.
     */
    public void copyTo(FSWrapperCopy targetFS) throws IOException {
        // Determine if the target has all the right types
        List<TemplateFile> thisContent = new ArrayList<>();
         _templateRoot.getChildrenRecursively(thisContent);

        for(TemplateFile templateFile: thisContent) {
            if(targetFS.getTemplateFileByType(templateFile.getType()) == null) {
                throw new RuntimeException("Can't copy to target directory structure");
            }
        }

        // Fetch all the content types at the top level - the content below them will be carried alongside
        List<TemplateFile> topLevelContent = _templateRoot.getChildren();
        List<FileInstance> allContent = new ArrayList<>();
        for(TemplateFile contentType: topLevelContent) {
            allContent.addAll(getAllContentOfType(contentType.getType()));
        }

        // Extract the parameters for each content type then add the files to the target fs using the type and parameters
        for(FileInstance fileInst: allContent) {
            Map<Integer, String> parameters = extractParamsForUnit(fileInst.getPath(), fileInst.getTemplate());
            if(parameters == null) {
                parameters = new HashMap<>();
            }
            String[] parameterList = new String[Collections.max(parameters.keySet())];
            for(int i = 0; i < parameterList.length; i++) {
                parameterList[i] = parameters.getOrDefault(i + 1, "");
            }
            targetFS.createDirectoryStruct(fileInst.getTemplate().getType(), parameterList);
            targetFS.copyFileTo(fileInst.getTemplate(), fileInst.getPath(), parameterList);
        }
    }

    /**
     * Copy a single file into the filesystem being managed by the FSWrapper that this method is
     * called on.
     */
    private void copyFileTo(TemplateFile tFile, Path from, String[] params) throws IOException {
        List<Path> pathList = new ArrayList<>();
        Path path = _workingDir;

        List<TemplateFile> parentList = tFile.accessPath();
        for(TemplateFile parent: parentList) {
            if(parent.isParameterised()) {
                path = path.resolve(tFile.fillFormat(params));
            } else {
                path = path.resolve(parent.getNameFormat());
            }
            pathList.add(path);
        }

        Path toPath = pathList.get(pathList.size() - 1);
        if(Files.notExists(toPath)) {
            Files.copy(from, pathList.get(pathList.size() - 1));
        }
    }

    /**
     * Deletes the directory structure for the given content type
     * @param type The content type
     * @param params The parameters of the unit
     */
    public void deleteFiles(String type, String... params) {
        TemplateFile tFile = getTemplateFileByType(type);
        if(tFile == null) {
            return;
        }

        try {
            List<FileInstance> pathList = getContent(tFile, params);
            if(pathList == null) {
                return;
            }
            for(FileInstance fileInst: pathList) {
                Files.deleteIfExists(fileInst.getPath());
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
        List<FileInstance> pathList = createPaths(type, params);
        try {
            for (FileInstance file: pathList) {
                TemplateFile tFile = file.getTemplate();
                Path filePath = file.getPath();
                switch (tFile.getTagName()) {
                    case DIR:
                    case DIRSET:
                        if (Files.notExists(filePath)) {
                            Files.createDirectory(filePath);
                        }
                        break;
                    case FILE:
                    case FILESET:
                        if (Files.notExists(filePath) && tFile.shouldCreateNew()) {
                            Files.createFile(filePath);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
       //     throw new RuntimeException(e);
        }
    }

    /**
     * Safely create a file. This creates all the parents if they don't exist.
     */
    public Path createFilePath(String type, String... params) {
        Path path = _workingDir;
        TemplateFile tFile = getTemplateFileByType(type);
        List<TemplateFile> parentList = tFile.accessPath();
        for(TemplateFile parent: parentList) {
            if(parent.isParameterised()) {
                path = path.resolve(parent.fillFormat(params));
            } else {
                path = path.resolve(parent.getNameFormat());
            }
        }
        return path;
    }

    /**
     * Form the file paths for the given type. If the type is a folder, the file paths for all children are
     * generated. Each child path appears after its parent, upon which it is dependant.
     * @param type The content type
     * @param params The parameters of the unit
     * @return The filePaths of the new content, including content that does not yet exist.
     */
    public List<FileInstance> createPaths(String type, String... params) {
        List<FileInstance> pathList = new ArrayList<>();

        Path path = _rootContentDir;
        TemplateFile tFile = getTemplateFileByType(type);
        List<TemplateFile> parentList = tFile.accessPath();
        tFile = parentList.get(1);
        if(tFile.isParameterised()) {
            path = path.resolve(tFile.fillFormat(params));
        } else {
            path = path.resolve(tFile.getNameFormat());
        }

        pathList.add(new FileInstance(tFile, path, params));

        TemplateFile currentType = tFile;
        boolean newType = true;
        Deque<Integer> indices = new ArrayDeque<>();
        indices.push(0);

        if(tFile.getParent() != null) {
            tFile = tFile.getParent();
        }
        while(!currentType.equals(tFile)) {
            int i;
            if(newType) {
                i = 0;
            } else {
                i = indices.pop();
                currentType = currentType.getParent();
                path = path.getParent();
            }

            if(currentType.isParent()) {
                List<TemplateFile> children = ((TemplateFolder) currentType).getChildren();
                newType = false;
                for (; i < children.size(); i++) {
                    TemplateFile currentChild = children.get(i);

                    if (children.get(i).isParameterised()) {
                        path = path.resolve(currentChild.fillFormat(params));
                    } else {
                        path = path.resolve(currentChild.getNameFormat());
                    }
                    pathList.add(new FileInstance(currentType, path, params));
                    switch(currentChild.getTagName()) {
                        case DIR:
                        case DIRSET:
                            newType = true;
                            indices.push(i + 1);
                            currentType = children.get(i);
                            break;
                        case FILE:
                        case FILESET:
                            path = path.getParent();
                    }
                    if(newType) {
                        break;
                    }
                }
            }
        }
        return pathList;
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
    public List<FileInstance> getFilesByParameter(String type, String... params) {
        TemplateFile tFile = getTemplateFileByType(type);

        if(tFile == null) {
            return null;
        } else {
            return  getContent(tFile, params);
        }
    }

    /**
     * Fetches a list of the file paths of every content file of a particular type.
     *      * This is a relatively expensive operation so it should only be performed at the start of an application.
     *      * @return A list of content files
     * @param type The content type to fetch, given by the type attribute in the XML
     * @return The filePaths of the found content
     */
    public List<FileInstance> getAllContentOfType(String type) {
        TemplateFile tFile = getTemplateFileByType(type);
        if(tFile == null) {
            return null;
        } else {
            return getContent(tFile);
        }
    }

    /**
     * Reusable method to extract content from the filesystem based on parameters
     */
    private List<FileInstance> getContent(TemplateFile tFile, String... params) {
        List<TemplateFile> parentList = tFile.accessPath();
        Path currentDir = _rootContentDir;
        Deque<Integer> indices = new ArrayDeque<>();
        indices.push(0);
        boolean newDir = true;
        List<FileInstance> filePaths = new ArrayList<>();

        boolean atTop = false;
        while(!atTop){
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
                //if the depth of the stack isn't right for the depth of the file type
                if((parentList.size() - indices.size()) > 1) {
                    if (Files.isDirectory(file)) {
                        currentDir = file;
                        indices.push(i + 1);
                        newDir = true;
                        break;
                    }
                }
                switch(parentList.get(0).getTagName()) {
                    case DIR:
                    case DIRSET:
                        if(Files.isDirectory(file)) {
                            if(!attemptExtractContent(file, tFile, filePaths)) {
                                currentDir = file;
                                indices.push(i + 1);
                                newDir = true;
                                break;
                            }
                        }
                        break;
                    case FILE:
                    case FILESET:
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
                                    attemptExtractContent(file, tFile, filePaths);
                                }
                            }
                        } else {
                            attemptExtractContent(file, tFile, filePaths);
                        }
                        break;
                    default:
                            return null;
                }
            }
            try {
                atTop = (Files.isSameFile(currentDir.getParent(), _workingDir) && !newDir);
            } catch (IOException e) {
                e.printStackTrace();
                atTop = true;
            }
        }
        return filePaths;
    }

    /*
     *   This method is responsible for scanning a file and determining if it matches the given type, and extracting the paths
     *   of all the child files. The list of Strings is formatted to reflect the XML configuration.
     *   A file matches the type if it satisfies the following conditions:
     *      - All of its children are the right types according to the XML file (if it's a directory)
     *      - The path from the _rootContentDir to the file is consistent with the structure in the XML file
     *      - The names of all the parent directories, as well as the file's name itself, are correct according to the XML file.
    */
    private boolean attemptExtractContent(Path file, TemplateFile tFile, List<FileInstance> filePaths) {
        // If the target file type is a directory
        Map<Integer, String> params;
        switch (tFile.getTagName()) {
            case DIR:
            case DIRSET:
                if (!Files.isDirectory(file)) {
                    return false;
                }
                TemplateFolder tFolder = (TemplateFolder) tFile;

                // For each expected child of the directory (as specified in the XML), we need to find a file or folder that matches it
                // This is the core of the algorithm for matching a file to a content type. We recursively check all the children of
                // directories.
                List<Path> children = listFiles(file);
                boolean[] haveCovered = new boolean[children.size()];
                int foundElements = 0;
                for (TemplateFile child: tFolder) {
                    boolean foundSomething = false;
                    for (Path childF : children) {
                        if (!haveCovered[children.indexOf(childF)] && attemptExtractContent(childF, child, filePaths)) {
                            haveCovered[children.indexOf(childF)] = true;
                            foundSomething = true;
                        }
                    }
                    if (foundSomething) {
                        foundElements++;
                    }
                }
                // If a directory has a match for each of its expected children, we can safely assume it is usable as there is
                // definitely all the required content in the directory.
                if (foundElements != tFolder.numChildren()) {
                    return false;
                }
                params = extractParamsForUnit(file, tFile);
                break;

            // This is the base case of our recursion - the file is not a directory.
            case FILE:
            case FILESET:
                params = extractParamsForUnit(file, tFile);
                if (Files.isDirectory(file) || params == null) {
                    return false;
                }
                break;
            default:
                return false;
        }

        // Having filtered out anything that doesn't match an element of the XML in terms of name and dir structure,
        // we can assume this file is usable
        filePaths.add(new FileInstance(tFile, file, params));
        return true;
    }

    /*
        Ensure that a given file matches a given element in terms of name. This will check the parametrisation of all
        the file's parents and ensure they are consistent (The same parameters must have the same values)
     */
    private Map<Integer, String> extractParamsForUnit(Path file, TemplateFile tFile) {
        if(file == null || tFile == null) {
            return null;
        }
        Path upFile = file;
        HashMap<Integer, String> nameParameters = new HashMap<>();
        List<TemplateFile> parentList = tFile.accessPath();
        Collections.reverse(parentList);
        // the first "parent" is the file itself
        for(TemplateFile parent: parentList) {
            if(!parent.isParameterised()) {
                if (! upFile.getFileName().toString().equals(parent.getNameFormat())) {
                    return null;
                }
                upFile = upFile.getParent();
                continue;
            }

            HashMap<Integer, String> theseParams = parent.extractParametersFromFilename(upFile);
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

    private TemplateFile getTemplateFileByType(String type) {
        List<TemplateFile> tFiles = new ArrayList<>();
        _templateRoot.getChildrenRecursively(tFiles);
        for(TemplateFile templateFile: tFiles) {
            if(templateFile.getType().equals(type)) {
                return templateFile;
            }
        }
        return null;
    }


}