package libris;

public class ReadingProgress {
	String bookTitle;
    int currentPage;
    int totalPages;

    public ReadingProgress(String bookTitle, int totalPages) {
        this.bookTitle = bookTitle;
        this.totalPages = totalPages;
        this.currentPage = 0;
    }

    public void updateProgress(int page) {
        if (page >= 0 && page <= totalPages) {
            currentPage = page;
        }
    }

    public double getPercentage() {
        return (currentPage * 100.0) / totalPages;
    }
}
