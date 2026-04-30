package userAuth;

public class User {
	private String username;
	private String password;
	private Role role;
	private int attempts;
	private long lockTime;
	private boolean isVerified;
	
	public User(String username, String password, Role role) {
		setUsername(username);
		setPassword(password);
		setRole(role);
		this.isVerified = false;
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
	
	public Role getRole() {
		return role;
	}
	
	public void setRole(Role role) {
		this.role = role;
	}
	
	public int getAttempts() {
		return attempts;
	}
	
	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}
	
	public long getLockTime() {
		return lockTime;
	}
	
	public void setLockTime(long lockTime) {
		this.lockTime = lockTime;
	}
	
	public boolean isVerified() {
		return isVerified;
	}
	
	public void verifyAccount() {
		this.isVerified = true;
	}
	
	public void incrementAttempts() {
		attempts++;
	}
	public void resetAttempts() {
		attempts = 0;
	}

}
