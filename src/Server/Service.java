package Server;

public class Service {

    private String name;
    private int num = 1;

    public Service(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getRessource() {
        return num;
    }
}