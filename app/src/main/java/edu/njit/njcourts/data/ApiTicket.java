package edu.njit.njcourts.data;

/**
 * DTO for a ticket row as returned by the backend GET /tickets endpoint.
 * Gson deserializes these field names 1:1 from the JSON response.
 */
public class ApiTicket {
    public String ticketNumber;
    public String violationType;
    public String vehicleMake;
    public String vehicleModel;
    public String vehicleColor;
    public String licensePlate;
    public String plateState;
    public String location;
    public String courtCode;
    public String issuedAt;
    public String offenseDisplay;
    public String courtCode;
    public String violDate;
    public String violTime;
    public String courtDate;
    public String courtTime;
}

