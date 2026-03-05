package edu.njit.njcourts.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import edu.njit.njcourts.models.Ticket;

/**
 * Task 6: Set Up MVVM Architecture (ViewModel Layer).
 * This class will manage ticket data and photo state.
 */
public class TicketViewModel extends ViewModel {
    private final MutableLiveData<List<Ticket>> tickets = new MutableLiveData<>();
    private final MutableLiveData<Ticket> selectedTicket = new MutableLiveData<>();

    public LiveData<List<Ticket>> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> ticketList) {
        tickets.setValue(ticketList);
    }

    public LiveData<Ticket> getSelectedTicket() {
        return selectedTicket;
    }

    public void selectTicket(Ticket ticket) {
        selectedTicket.setValue(ticket);
    }
    
    // Peer Placeholder: Add methods for RoomDB integration (Task 25)
}
