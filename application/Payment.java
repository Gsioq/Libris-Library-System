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

    public void processPayment() throws Exception {
        if (amount <= 0) {
            throw new Exception("Amount must be greater than zero.");
        }

        // Simulate a network delay (1.5 seconds) to mimic real-world processing
        Thread.sleep(1500);

        // Simulate a real-world scenario: 20% chance the transaction declines
        double randomChance = Math.random();
        if (randomChance < 0.20) {
            this.paid = false;
            throw new Exception("Declined by provider. Insufficient funds or blocked system.");
        }

        // If it passes the checks, the payment is successful
        this.paid = true;
    }

}