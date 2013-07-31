package issue17;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 7/31/13
 */
public class Parent {
    protected Child child;
    private String cannotsee;
    protected String cansee;

    public Child getChild(){
        return child;
    }

    public String getCannotsee() {
        return cannotsee;
    }

    public void setCannotsee(String cannotsee) {
        this.cannotsee = cannotsee;
    }

    public String getCansee() {
        return cansee;
    }

    public void setCansee(String cansee) {
        this.cansee = cansee;
    }
}
