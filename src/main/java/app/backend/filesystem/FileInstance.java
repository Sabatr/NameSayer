package app.backend.filesystem;

import java.nio.file.Path;
import java.util.Map;

public class FileInstance {

    private TemplateFile _template;
    private Path _file;
    private Map<Integer, String> _parameters;

    public FileInstance(TemplateFile tFile, Path file, Map<Integer, String> parameters) {
        _template = tFile;
        _file = file;
        _parameters = parameters;
    }

    public Path getPath() {
        return _file;
    }


    public TemplateFile getTemplate() {
        return _template;
    }
}
