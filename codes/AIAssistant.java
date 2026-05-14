package application;

public class AIAssistant {

    public static String askAIForUI(String question) {
        question = question.toLowerCase().trim();

        // BOOK RECOMMENDATIONS
        if (question.contains("recommend") || question.contains("suggest")) {

            if (question.contains("fiction")) {
                return "Here are some fiction recommendations:\n"
                        + "- To Kill a Mockingbird\n"
                        + "- The Great Gatsby\n"
                        + "- The Alchemist";
            }

            else if (question.contains("science")) {
                return "Try these science books:\n"
                        + "- A Brief History of Time\n"
                        + "- Cosmos\n"
                        + "- The Selfish Gene";
            }

            else if (question.contains("history")) {
                return "Recommended history books:\n"
                        + "- Sapiens\n"
                        + "- Guns, Germs, and Steel\n"
                        + "- The Diary of Anne Frank";
            }

            else if (question.contains("romance")) {
                return "Romance recommendations:\n"
                        + "- Pride and Prejudice\n"
                        + "- Me Before You\n"
                        + "- The Notebook";
            }

            else {
                return "You can search books by genre, title, or author in our catalog.";
            }
        }

        // BORROWING
        else if (question.contains("borrow")) {
            return "To borrow a book:\n"
                    + "1. Search for your desired book\n"
                    + "2. Check availability\n"
                    + "3. Click borrow/request\n"
                    + "4. Wait for librarian approval.";
        }

        // RETURNING
        else if (question.contains("return")) {
            return "Return books before the due date to avoid penalties. "
                    + "Visit the library or use the return feature in your account.";
        }

        // DUE DATE
        else if (question.contains("due date")
              || question.contains("when is my book due")) {
            return "You can view your due dates in the 'My Borrowed Books' section.";
        }

        // FINES
        else if (question.contains("fine")
              || question.contains("penalty")
              || question.contains("late fee")) {
            return "Late returns may result in fines depending on library policy.";
        }

        // ACCOUNT
        else if (question.contains("account")
              || question.contains("login")
              || question.contains("register")) {
            return "You can create an account using your school email and log in anytime.";
        }

        // SUBSCRIPTION / PREMIUM
        else if (question.contains("premium")
              || question.contains("subscription")) {
            return "Premium members get extended borrowing time and exclusive features.";
        }

        // EBOOKS / PDF
        else if (question.contains("ebook")
              || question.contains("pdf")
              || question.contains("digital")) {
            return "Digital books can be accessed through the E-Library section.";
        }

        // LOST BOOK
        else if (question.contains("lost book")) {
            return "Please contact the librarian immediately for replacement procedures.";
        }

        // LIBRARY HOURS
        else if (question.contains("hours")
              || question.contains("open")) {
            return "Library hours are Monday-Friday, 8 AM to 5 PM.";
        }

        // PASSWORD RESET
        else if (question.contains("forgot password")
              || question.contains("reset password")) {
            return "Use the 'Forgot Password' option on the login page.";
        }

        // RESERVATION
        else if (question.contains("reserve")
              || question.contains("reservation")) {
            return "Books can be reserved if currently unavailable.";
        }

        // CONTACT
        else if (question.contains("contact")
              || question.contains("librarian")) {
            return "You may contact the library staff through the Help section.";
        }

        // DEFAULT RESPONSE
        else {
            return "I'm here to help with books, borrowing, returns, accounts, and library services. Try asking something specific!";
        }
    }
}