package model;

public class Venue {
    private int venueId;
    private String venueName;
    private String location;
    private int capacity;
    private String status;

    public Venue() {}

    public Venue(String venueName, String location, int capacity) {
        this.venueName = venueName;
        this.location = location;
        this.capacity = capacity;
        this.status = "Available";
    }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}