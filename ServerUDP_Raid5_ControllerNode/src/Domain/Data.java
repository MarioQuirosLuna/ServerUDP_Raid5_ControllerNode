package Domain;

/**
 *
 * @author mario
 */
public class Data {
    
    /**
     * position: Position of the book fragment 
     * name: Name file
     * content: Content of the book
     */
    
    private int position;
    private String name;
    private String content;

    public Data(int position, String name, String content) {
        this.position = position;
        this.name = name;
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Data{" + "position=" + position + ", name=" + name + ", content=" + content + '}';
    }
    
    
    
}
