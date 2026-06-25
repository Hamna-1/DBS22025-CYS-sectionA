package model;

public class Show {
    private int showId;
    private String title;
    private String genre;
    private int durationMinutes;
    private String description;
    private String status;

    public Show() {}

    public Show(String title, String genre, int durationMinutes, String description) {
        this.title = title;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.status = "Active";
    }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}