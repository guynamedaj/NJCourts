package edu.njit.njcourts.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.njit.njcourts.R;
import edu.njit.njcourts.models.Ticket;

/**
 * Adapter for Task 15: Ticket Selection UX.
 * Updated to match the latest Ticket model based on PATS system screenshots.
 */
public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<Ticket> tickets;
    private final OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
        void onTicketInfoClick(Ticket ticket);
    }

    public TicketAdapter(List<Ticket> tickets, OnTicketClickListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.bind(ticket, listener);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView textTicketNumber, textViolation, textVehicle;
        ImageButton btnTicketInfo;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            textTicketNumber = itemView.findViewById(R.id.text_ticket_number);
            textViolation = itemView.findViewById(R.id.text_violation);
            textVehicle = itemView.findViewById(R.id.text_vehicle);
            btnTicketInfo = itemView.findViewById(R.id.btn_ticket_info);
        }

        public void bind(Ticket ticket, OnTicketClickListener listener) {
            textTicketNumber.setText("Ticket # " + ticket.getTicketNumber());
            textViolation.setText(ticket.getViolation());
            
            String vehicleSummary = ticket.getColor() + " " + ticket.getMake() + " " + ticket.getBodyType() + 
                                   " on " + ticket.getStreet();
            textVehicle.setText(vehicleSummary);

            itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
            btnTicketInfo.setOnClickListener(v -> listener.onTicketInfoClick(ticket));
        }
    }
}
