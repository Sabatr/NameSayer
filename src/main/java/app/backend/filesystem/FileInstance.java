package app.backend.filesystem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A FileInstance represents a real file in the filesystem. It contains the template element that corresponds to it,
 * the path to the file and its parameters
 */
public class FileInstance {

    private TemplateFile _template;
    private Path _file;
    private Map<Integer, String> _parameters;

    /**
     * Construct a FileInstance
     * @param tFile The template element that corresponds to the file
     * @param file The path of the file that this object represents
     * @param parameters HashMap of parameters where the key is the number of the parameter
     */
    public FileInstance(TemplateFile tFile, Path file, Map<Integer, String> parameters) {
        _template = tFile;
        _file = file;
        _parameters = parameters;
    }

    /**
     * Construct a FileInstance
     * @param tFile The template element that corresponds to the file
     * @param file The path of the file that this object represents
     * @param parameters Array of parameters where the index of a parameter corresponds to its number
     */
    public FileInstance(TemplateFile tFile, Path file, String[] parameters) {
        _template = tFile;
        _file = file;

        _parameters = new HashMap<>();
        for(int i = 0; i < parameters.length; i++) {
            if(parameters[i] != null && !parameters[i].isEmpty()) {
                _parameters.put(i, parameters[i]);
            }
        }
    }

    public Path getPath() {
        return _file;
    }

    public TemplateFile getTemplate() {
        return _template;
    }

    public Map<Integer, String> getParameters() {
        return _parameters;
    }
}
