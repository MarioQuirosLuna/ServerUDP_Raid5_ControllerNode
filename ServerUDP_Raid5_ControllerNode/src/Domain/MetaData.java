package Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mario
 */
public class MetaData {
    
    /**
     * id: Metadata identification
     * name: Name file
     * data: Fragments that make up the book
     */
    
    private int id;
    private String name;
    private List<Fragment> fragments = null;

    public MetaData(int id, String name) {
        this.id = id;
        this.name = name;
        this.fragments = new ArrayList<Fragment>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Fragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<Fragment> fragments) {
        this.fragments = fragments;
    }

    @Override
    public String toString() {
        return "MetaData{" + "id=" + id + ", name=" + name + ", fragments=" + fragments + '}';
    }
    
    
    
}
