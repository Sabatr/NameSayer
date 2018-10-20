package app.backend.filesystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a template file as defined in the FS config XML.
 */
public class TemplateFile {

    private String _type;
    private String _nameFormat;
    private boolean _multiAllowed;
    private boolean _isParameterised;
    private boolean _shouldCreateNew;
    private TemplateFolder _parent;

    public TemplateFile(String type, String format, boolean multi, boolean parameters, boolean createNew, TemplateFolder parent) {
        _type = type;
        _nameFormat = format;
        _multiAllowed = multi;
        _isParameterised = parameters;
        _shouldCreateNew = createNew;
        _parent = parent;
    }

    public String getType() {
        return _type;
    }

    public boolean isMultiple() {
        return _multiAllowed;
    }

    public boolean isParameterised() { return _isParameterised; }

    public boolean isParent() {
        return false;
    }

    public boolean shouldCreateNew() { return  _shouldCreateNew; }

    public String getTagName() {
        if(isMultiple()) {
            if(isParent()) {
                return FSWrapper.DIRSET;
            } else {
                return FSWrapper.FILESET;
            }
        } else {
            if(isParent()) {
                return FSWrapper.DIR;
            } else {
                return FSWrapper.FILE;
            }
        }
    }

    public String getNameFormat() {
        return _nameFormat;
    }

    public TemplateFolder getParent() {
        return _parent;
    }

    public List<TemplateFile> accessPath() {
        if(_parent == null) {
            List<TemplateFile> list = new ArrayList<TemplateFile>();
            list.add(this);
            return list;
        } else {
            List<TemplateFile> list = _parent.accessPath();
            list.add(this);
            return list;
        }
    }

    /*
        Extract the parameters from the name of a file, based on the format of this TemplateFile
     */
    public HashMap<Integer, String> extractParametersFromFilename(Path file) {
        HashMap<Integer, String> result = new HashMap<>();
        boolean doesNotMatch = false;
        String fileName = file.getFileName().toString();
        String[] separators = _nameFormat.split("(%[0-9])");

        // First we deal with a special case where we start with a parameter. We extract the first parameter and set the
        // parIndex to the index of the next one. The rest of the method treats the name as starting with a separator
        int parIndex = _nameFormat.indexOf('%');
        int startOfSeparator = 0, endOfSeparator;
        if(parIndex == 0) {
            if(separators.length == 0) {
                startOfSeparator = fileName.length();
            } else {
                startOfSeparator = fileName.indexOf(separators[1]); // the first string split by regex will actually be empty
            }
            if(startOfSeparator == -1) {
                return null;
            }

            String parameter = fileName.substring(0, startOfSeparator);
            result.put(Character.getNumericValue(_nameFormat.charAt(parIndex + 1)), parameter);
            parIndex = _nameFormat.indexOf('%', parIndex + 1);
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
            if(!fileName.contains(separator)) {
                doesNotMatch = true;    // we have detected a discrepancy and this name does not match the format
                break;
            }

            endOfSeparator = startOfSeparator + separator.length(); // actually the character after the separator
            String parameter;
            try {
                parameter = fileName.substring(endOfSeparator, fileName.indexOf(nextSeparator, endOfSeparator));
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                return null;
            }
            result.put(Character.getNumericValue(_nameFormat.charAt(parIndex + 1)), parameter);
            startOfSeparator = fileName.indexOf(nextSeparator, endOfSeparator);
            parIndex = _nameFormat.indexOf('%', parIndex+1);
        }

        // Now we deal with the case of ending with a parameter. At this point if the boolean doesNotMatch has not been
        // set to true, then we must have looped through all the separators. So, we use the last one to get the start of
        // the parameter and extract it
        if(parIndex != -1 && !doesNotMatch) {
            endOfSeparator = startOfSeparator + separators[separators.length - 1].length();

            result.put(Integer.parseInt(_nameFormat.substring(parIndex + 1)),
                    fileName.substring(endOfSeparator));
        }

        return doesNotMatch ? null : result;
    }

    /**
     *
     * @param params The parameters to copy in, in order. If a higher-number parameter is present in the format string
     *               but not lower numbers, empty strings are inserted in the place of the lower-number parameters.
     * @return The filled string.
     */
    public String fillFormat(String... params) {
        StringBuilder filled = new StringBuilder();
        char[] form = _nameFormat.toCharArray();
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

    static class TemplateFileBuilder {
        private String _type;
        private String _nameFormat;
        private boolean _multiple;
        private boolean _isParameterised;
        private boolean _createNew;
        private TemplateFolder _parent;

        public TemplateFileBuilder type(String type) { _type = type; return this; }

        public TemplateFileBuilder nameFormat(String nf) { _nameFormat = nf; return this; }

        public TemplateFileBuilder canBeMultiple(boolean multi) { _multiple = multi; return this; }

        public TemplateFileBuilder hasParameters(boolean isParameterised) { _isParameterised = isParameterised; return this; }

        public TemplateFileBuilder shouldCreateNew(boolean createNew) { _createNew = createNew; return  this; }

        public TemplateFileBuilder parent(TemplateFolder parent) { _parent = parent; return this; }

        public TemplateFolder buildFolder() {
            return new TemplateFolder(_type, _nameFormat, _multiple, _isParameterised, _createNew, _parent);
        }

        public TemplateFile buildFile() {
            return new TemplateFile(_type, _nameFormat, _multiple, _isParameterised, _createNew, _parent);
        }
    }
}
