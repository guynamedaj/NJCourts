package edu.njit.njcourts.models;

/**
 * Comprehensive Model class based on PATS system screenshots.
 * Refactored to use the Builder pattern for improved readability and maintainability.
 */
public class Ticket {
    private final String ticketNumber;
    private final String licPlate;
    private final String state;
    private final String make;
    private final String bodyType;
    private final String color;
    private final String violation;
    private final String violDate;
    private final String violTime;
    private final String courtDate;
    private final String courtTime;
    private final String mAppear;
    private final String transferStatCode;
    private final String transferDT;
    private final String courtCode;
    private final String alphaCode;
    private final String seqNum;
    private final String statusCode;
    private final String street;

    private Ticket(Builder builder) {
        this.ticketNumber = builder.ticketNumber;
        this.licPlate = builder.licPlate;
        this.state = builder.state;
        this.make = builder.make;
        this.bodyType = builder.bodyType;
        this.color = builder.color;
        this.violation = builder.violation;
        this.violDate = builder.violDate;
        this.violTime = builder.violTime;
        this.courtDate = builder.courtDate;
        this.courtTime = builder.courtTime;
        this.mAppear = builder.mAppear;
        this.transferStatCode = builder.transferStatCode;
        this.transferDT = builder.transferDT;
        this.courtCode = builder.courtCode;
        this.alphaCode = builder.alphaCode;
        this.seqNum = builder.seqNum;
        this.statusCode = builder.statusCode;
        this.street = builder.street;
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

    public static class Builder {
        private String ticketNumber = "";
        private String licPlate = "";
        private String state = "";
        private String make = "";
        private String bodyType = "";
        private String color = "";
        private String violation = "";
        private String violDate = "";
        private String violTime = "";
        private String courtDate = "";
        private String courtTime = "";
        private String mAppear = "";
        private String transferStatCode = "";
        private String transferDT = "";
        private String courtCode = "";
        private String alphaCode = "";
        private String seqNum = "";
        private String statusCode = "";
        private String street = "";

        public Builder setTicketNumber(String val) { ticketNumber = val; return this; }
        public Builder setLicPlate(String val) { licPlate = val; return this; }
        public Builder setState(String val) { state = val; return this; }
        public Builder setMake(String val) { make = val; return this; }
        public Builder setBodyType(String val) { bodyType = val; return this; }
        public Builder setColor(String val) { color = val; return this; }
        public Builder setViolation(String val) { violation = val; return this; }
        public Builder setViolDate(String val) { violDate = val; return this; }
        public Builder setViolTime(String val) { violTime = val; return this; }
        public Builder setCourtDate(String val) { courtDate = val; return this; }
        public Builder setCourtTime(String val) { courtTime = val; return this; }
        public Builder setMAppear(String val) { mAppear = val; return this; }
        public Builder setTransferStatCode(String val) { transferStatCode = val; return this; }
        public Builder setTransferDT(String val) { transferDT = val; return this; }
        public Builder setCourtCode(String val) { courtCode = val; return this; }
        public Builder setAlphaCode(String val) { alphaCode = val; return this; }
        public Builder setSeqNum(String val) { seqNum = val; return this; }
        public Builder setStatusCode(String val) { statusCode = val; return this; }
        public Builder setStreet(String val) { street = val; return this; }

        public Ticket build() {
            return new Ticket(this);
        }
    }
}
