package application;

public class AutoModerator {

    public static boolean isAppropriate(String text) {
        String[] bannedWords = {
            "hate",
            "violence",
            "illegal",
            "explicit",
            "spam"
        };

        text = text.toLowerCase();

        for(String word : bannedWords){
            if(text.contains(word)){
                return false;
            }
        }

        return true;
    }
}