package model;

public class Performer {
    private int performerId;
    private int employeeId;
    private String stageName;
    private String specialty;
    private int experienceYears;

    public Performer() {}

    public Performer(int employeeId, String stageName, String specialty, int experienceYears) {
        this.employeeId = employeeId;
        this.stageName = stageName;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
    }

    public int getPerformerId() { return performerId; }
    public void setPerformerId(int performerId) { this.performerId = performerId; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public int getExperienceYears() { return experienceYears; }
    public void setExperienceYears(int experienceYears) { this.experienceYears = experienceYears; }
}