package application;

public class Review {
    private String username;
    private String bookTitle;
    private int rating;
    private String reviewText;

    public Review(String username, String bookTitle,
                  int rating, String reviewText){
        this.username = username;
        this.bookTitle = bookTitle;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public String getUsername(){ return username; }
    public String getBookTitle(){ return bookTitle; }
    public int getRating(){ return rating; }
    public String getReviewText(){ return reviewText; }
}
