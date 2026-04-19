package userAuth;
import java.util.HashMap;

public class AuthService {
	private static final long LOCK_DURATION = 15 * 60 * 1000; // 15 minutes
	private HashMap<String, User> users = new HashMap<>();
	
	//Registration
	public String register(String username, String password, Role role) {
		if(users.containsKey(username)) {
			return "Username already exists.";
		}
		else {
			User newUser = new User(username, password, role.name());
			users.put(username, newUser);
			
			return "Account created. Please verify account.";
		}
	}
	
	//Acc Verification
	public String verify(String username) {
		User user = users.get(username);
		if(user == null) return "User not found.";
		else user.verifyAccount();
		
		return "Account verified successfully.";
	}
	
	//Login
	public String login(String username, String password) {
		User user = users.get(username);
		
		if(user == null) return "User not found.";
		
		long currentTime = System.currentTimeMillis();
		if(user.getLockTime() > currentTime) {
			long remaining = (user.getLockTime() - currentTime) / 1000;
			return "Account locked. Try again in " + remaining + "seconds.";
		}
		
		if(!user.isVerified()) return "Account not verified.";
		if(!user.getPassword().equals(password)) {
			user.incrementAttempts();
			if (user.getAttempts() >= 3) {
				user.setLockTime(currentTime + LOCK_DURATION);
				user.resetAttempts();
				return "Too many failed attempts. Account locked for 15 minutes.";
			} return "Incorrect password. Attempt " + user.getAttempts() + "/3";
		}
		
		user.resetAttempts();
		return "Login successful! Role: " + user.getRole();
	}

}
