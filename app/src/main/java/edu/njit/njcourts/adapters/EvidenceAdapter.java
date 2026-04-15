package edu.njit.njcourts.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import edu.njit.njcourts.R;
import edu.njit.njcourts.data.AppDatabase;
import edu.njit.njcourts.data.PhotoEvidenceEntity;

/**
 * Task 27: Adapter for Evidence Dashboard with Deletion Support.
 */
public class EvidenceAdapter extends RecyclerView.Adapter<EvidenceAdapter.EvidenceViewHolder> {

    private List<PhotoEvidenceEntity> evidenceList = new ArrayList<>();

    public void setEvidenceList(List<PhotoEvidenceEntity> list) {
        this.evidenceList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EvidenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evidence, parent, false);
        return new EvidenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EvidenceViewHolder holder, int position) {
        holder.bind(evidenceList.get(position));
    }

    @Override
    public int getItemCount() {
        return evidenceList.size();
    }

    static class EvidenceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView textStatus;
        ImageButton btnDelete;

        public EvidenceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.img_evidence_thumbnail);
            textStatus = itemView.findViewById(R.id.text_evidence_status);
            btnDelete = itemView.findViewById(R.id.btn_delete_evidence);
        }

        public void bind(PhotoEvidenceEntity entity) {
            if (entity.photoBlob != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(entity.photoBlob, 0, entity.photoBlob.length);
                imgThumbnail.setImageBitmap(bitmap);
            }
            textStatus.setText(entity.syncStatus);
            
            // Color code status
            if ("SYNCED".equals(entity.syncStatus)) {
                textStatus.setBackgroundColor(0xFF2E7D32); // Green
            } else if ("FAILED".equals(entity.syncStatus)) {
                textStatus.setBackgroundColor(0xFFB71C1C); // Red
            } else {
                textStatus.setBackgroundColor(0xFF757575); // Grey
            }

            // Task 27: Explicit Delete Button
            btnDelete.setOnClickListener(v -> showDeleteConfirmation(entity));
        }

        private void showDeleteConfirmation(PhotoEvidenceEntity entity) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete Evidence?")
                    .setMessage("Remove this photo from the ticket? This cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase db = AppDatabase.getDatabase(itemView.getContext());
                            db.evidenceDao().deleteEvidence(entity);
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .show();
        }
    }
}
