package app.backend.filesystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a template folder as defined in the FS config XML.
 */
public class TemplateFolder extends TemplateFile implements Iterable<TemplateFile> {

    private List<TemplateFile> _children;

    public TemplateFolder(String type, String format, boolean multi, boolean parameters, boolean createNew, TemplateFolder parent) {
        super(type, format, multi, parameters, createNew, parent);
        _children = new ArrayList<>();
    }

    @Override
    public boolean isParent() {
        return true;
    }

    public List<TemplateFile> getChildren() {
        List<TemplateFile> children = new ArrayList<>();
        for(TemplateFile child: _children) {
            children.add(child);
        }
        return children;
    }

    /**
     * Get all children that lie under a template directory
     * @param allChildren A list that will be filled with the children template files (this is a recursive method)
     */
    public void getChildrenRecursively(List<TemplateFile> allChildren) {
        List<TemplateFile> children = getChildren();
        for(TemplateFile child: children) {
            allChildren.add(child);
            if(child.isParent()) {
                ((TemplateFolder) child).getChildrenRecursively(allChildren);
            }
        }
    }

    public int numChildren() {
        return _children.size();
    }

    void addChild(TemplateFile tFile) {
        _children.add(tFile);
    }

    @Override
    public Iterator<TemplateFile> iterator() {
        return _children.iterator();
    }

}
