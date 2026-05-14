package application;

public class Discussion {
    private String username;
    private String title;
    private String content;

    public Discussion(String username, String title, String content){
        this.username = username;
        this.title = title;
        this.content = content;
    }

    public String getUsername(){ return username; }
    public String getTitle(){ return title; }
    public String getContent(){ return content; }
}