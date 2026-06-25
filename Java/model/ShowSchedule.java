package model;

import java.sql.Date;
import java.sql.Time;

public class ShowSchedule {
    private int scheduleId;
    private int showId;
    private int venueId;
    private Date showDate;
    private Time startTime;
    private Time endTime;
    private int totalSeats;
    private int availableSeats;
    private String status;

    public ShowSchedule() {}

    public ShowSchedule(int showId, int venueId, Date showDate,
                        Time startTime, Time endTime, int totalSeats) {
        this.showId = showId;
        this.venueId = venueId;
        this.showDate = showDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.status = "Scheduled";
    }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getShowId() { return showId; }
    public void setShowId(int showId) { this.showId = showId; }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public Date getShowDate() { return showDate; }
    public void setShowDate(Date showDate) { this.showDate = showDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}