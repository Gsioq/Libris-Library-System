package userAuth;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		AuthService auth = new AuthService();
		Scanner scanner = new Scanner(System.in);
		
		//Test Register
		System.out.println("-Test Registration-");
		System.out.print("Enter username: ");
		String username = scanner.nextLine();
		
		System.out.print("Enter password:");
		String password = scanner.nextLine();
		
		System.out.print("Enter role (ADMIN, READER, LIBRARIAN, CONTENT_MODERATOR)");
		String roleIn = scanner.nextLine().toUpperCase();
		
		Role role = Role.valueOf(roleIn);
		
		System.out.println(auth.register(username,  password,  role));
		
		//Test Verify
		System.out.println("\nAcc Verification");
		System.out.print("Enter username: ");
		String verifyUser = scanner.nextLine();
		System.out.println(auth.verify(verifyUser));
		
		
		//Test Logins
		System.out.println("\nTest Login");
		
		while(true) {
			System.out.print("\nEnter username: ");
			String loginUser = scanner.nextLine();
			
			System.out.print("Enter password:");
			String loginPass = scanner.nextLine();
			
			String result = auth.login(loginUser, loginPass);
			System.out.println(result);
			
			if(result.startsWith("Login successful")) {
				break;
			}
			if(result.contains("locked")) {
				break;
			}
			}
	}

}
