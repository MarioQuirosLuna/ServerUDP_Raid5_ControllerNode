package Domain;

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
    private Map<Integer, Fragment> data = null;

    public MetaData(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    
}
