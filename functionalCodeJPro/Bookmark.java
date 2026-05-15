package application;

public class Bookmark {
	String bookTitle;
    int pageNumber;

    public Bookmark(String bookTitle, int pageNumber) {
        this.bookTitle = bookTitle;
        this.pageNumber = pageNumber;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}