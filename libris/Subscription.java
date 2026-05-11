package libris;

public class Subscription {
	String planName;
    double price;
    boolean active;

    public Subscription(String planName, double price) {
        this.planName = planName;
        this.price = price;
        this.active = false;
    }

    public void activate() {
        active = true;
    }

    public void cancel() {
        active = false;
    }
}
