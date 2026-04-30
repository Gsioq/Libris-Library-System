package userAuth;
import java.util.Date;

public class LoginActivity {
    private String username;
    private String status;
    private long timestamp;

    public LoginActivity(String username, String status, long timestamp) {
        this.username = username;
        this.status = status;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "User: " + username +
               " | Status: " + status +
               " | Time: " + new Date(timestamp);
    }
}
