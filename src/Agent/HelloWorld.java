package Agent;
public class HelloWorld {

    private String name;

    public HelloWorld(String n) {
        this.name = n;
    }

    public void Hello() {
        System.out.println("Hello World " + name);
    }
}