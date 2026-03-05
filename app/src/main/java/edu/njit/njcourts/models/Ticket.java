package edu.njit.njcourts.models;

/**
 * Comprehensive Model class based on PATS system screenshots.
 */
public class Ticket {
    private String ticketNumber;
    private String licPlate;
    private String state;
    private String make;
    private String bodyType;
    private String color;
    private String violation;
    private String violDate;
    private String violTime;
    private String courtDate;
    private String courtTime;
    private String mAppear;
    private String transferStatCode;
    private String transferDT;
    
    private String courtCode;
    private String alphaCode;
    private String seqNum;
    private String statusCode;
    private String street;

    public Ticket(String ticketNumber, String licPlate, String state, String make, String bodyType, 
                  String color, String violation, String violDate, String violTime, 
                  String courtDate, String courtTime, String mAppear, String transferStatCode, 
                  String transferDT, String courtCode, String alphaCode, String seqNum, 
                  String statusCode, String street) {
        this.ticketNumber = ticketNumber;
        this.licPlate = licPlate;
        this.state = state;
        this.make = make;
        this.bodyType = bodyType;
        this.color = color;
        this.violation = violation;
        this.violDate = violDate;
        this.violTime = violTime;
        this.courtDate = courtDate;
        this.courtTime = courtTime;
        this.mAppear = mAppear;
        this.transferStatCode = transferStatCode;
        this.transferDT = transferDT;
        this.courtCode = courtCode;
        this.alphaCode = alphaCode;
        this.seqNum = seqNum;
        this.statusCode = statusCode;
        this.street = street;
    }

    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public String getLicPlate() { return licPlate; }
    public String getState() { return state; }
    public String getMake() { return make; }
    public String getBodyType() { return bodyType; }
    public String getColor() { return color; }
    public String getViolation() { return violation; }
    public String getViolDate() { return violDate; }
    public String getViolTime() { return violTime; }
    public String getCourtDate() { return courtDate; }
    public String getCourtTime() { return courtTime; }
    public String getmAppear() { return mAppear; }
    public String getTransferStatCode() { return transferStatCode; }
    public String getTransferDT() { return transferDT; }
    public String getCourtCode() { return courtCode; }
    public String getAlphaCode() { return alphaCode; }
    public String getSeqNum() { return seqNum; }
    public String getStatusCode() { return statusCode; }
    public String getStreet() { return street; }

    @Override
    public String toString() {
        return ticketNumber;
    }
}
