package application;

import java.time.LocalDateTime;

public class User {

    // Basic Account Information
    private int userId;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;

    // Role Management
    private Role role;

    // Account Security
    private boolean isVerified;
    private boolean isLocked;
    private int failedLoginAttempts;
    private LocalDateTime lastLogin;

    // Premium Features
    private boolean isPremium;
    private Subscription subscription;

    // Reading Features
    private ReadingProgress progress;

    // Constructors
    public User() {
    }

    public User(int userId, String username, String password, String email,
                String phoneNumber, Role role, boolean isVerified,
                boolean isLocked, int failedLoginAttempts,
                boolean isPremium) {

        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isVerified = isVerified;
        this.isLocked = isLocked;
        this.failedLoginAttempts = failedLoginAttempts;
        this.isPremium = isPremium;
    }

    // Getters and Setters

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public ReadingProgress getProgress() {
        return progress;
    }

    public void setProgress(ReadingProgress progress) {
        this.progress = progress;
    }

    // Optional helper method
    public void displayUserInfo() {
        System.out.println("User ID: " + userId);
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
        System.out.println("Premium: " + isPremium);
        System.out.println("Verified: " + isVerified);
        System.out.println("Locked: " + isLocked);
    }
}