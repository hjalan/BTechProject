public class Resource {
    private char sign;
    private int usableAfter;
    private String resourceName;

    public char getSign() {
        return sign;
    }

    public void setSign(char sign) {
        this.sign = sign;
    }

    public int getUsableAfter() {
        return usableAfter;
    }

    public void setUsableAfter(int usableAfter) {
        this.usableAfter = usableAfter;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    @Override
    public String toString() {
        return usableAfter + "," + resourceName;
    }
}
