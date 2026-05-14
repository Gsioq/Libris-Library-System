package application;

public class Payment {
	String paymentMethod;
    double amount;
    boolean paid;

    public Payment(String paymentMethod, double amount) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paid = false;
    }

    public void processPayment() {
        if (amount > 0) {
            paid = true;
        }
    }

}