package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Ticket {
    private int ticketId;
    private int scheduleId;
    private int customerId;
    private Integer discountId;
    private String seatNumber;
    private BigDecimal price;
    private Timestamp purchaseDate;
    private String status;

    public Ticket() {}

    public Ticket(int scheduleId, int customerId, Integer discountId,
                  String seatNumber, BigDecimal price) {
        this.scheduleId = scheduleId;
        this.customerId = customerId;
        this.discountId = discountId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = "Booked";
    }


    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getDiscountId() { return discountId; }
    public void setDiscountId(Integer discountId) { this.discountId = discountId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Timestamp getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Timestamp purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}