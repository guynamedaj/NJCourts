package edu.njit.njcourts.adapters;

import android.app.Dialog;
import android.content.Context;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.njit.njcourts.R;
import edu.njit.njcourts.data.AppDatabase;

/**
 * Task 27 + 31: Evidence grid adapter. Handles both local (byte[] blob)
 * and remote (S3 presigned URL) photos so the grid reflects everything
 * known about a ticket across devices.
 */
public class EvidenceAdapter extends RecyclerView.Adapter<EvidenceAdapter.EvidenceViewHolder> {

    private List<EvidenceItem> items = new ArrayList<>();

    public void setItems(List<EvidenceItem> list) {
        this.items = list;
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
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
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

        public void bind(EvidenceItem item) {
            if (item.photoBlob != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(item.photoBlob, 0, item.photoBlob.length);
                imgThumbnail.setImageBitmap(bitmap);
            } else if (item.remoteUrl != null) {
                Glide.with(itemView.getContext())
                    .load(item.remoteUrl)
                    .centerCrop()
                    .into(imgThumbnail);
            } else {
                imgThumbnail.setImageDrawable(null);
            }

            imgThumbnail.setOnClickListener(v -> showFullImage(item));

            textStatus.setVisibility(View.VISIBLE);
            if ("SYNCED".equals(item.syncStatus)) {
                textStatus.setText("\u2713 Submitted");
                textStatus.setBackgroundColor(0xFF2E7D32);
            } else if ("FAILED".equals(item.syncStatus)) {
                textStatus.setText("Failed");
                textStatus.setBackgroundColor(0xFFB71C1C);
            } else {
                textStatus.setText("Not Synced");
                textStatus.setBackgroundColor(0xFF757575);
            }

            // Allow deletion only for local unsynced photos (e.g. bad capture, retake).
            // Once submitted, deletion is restricted to the web app (chain of custody).
            if (item.canDelete() && !"SYNCED".equals(item.syncStatus)) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> showDeleteConfirmation(item));
            } else {
                btnDelete.setVisibility(View.GONE);
                btnDelete.setOnClickListener(null);
            }
        }

        private void showFullImage(EvidenceItem item) {
            Context ctx = itemView.getContext();
            Dialog dialog = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_full_image);
            ImageView fullImage = dialog.findViewById(R.id.img_full);

            if (item.photoBlob != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(item.photoBlob, 0, item.photoBlob.length);
                fullImage.setImageBitmap(bitmap);
            } else if (item.remoteUrl != null) {
                Glide.with(ctx)
                    .load(item.remoteUrl)
                    .fitCenter()
                    .into(fullImage);
            }

            fullImage.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }

        private void showDeleteConfirmation(EvidenceItem item) {
            if (item.localEntity == null) return;
            new AlertDialog.Builder(itemView.getContext())
                .setTitle("Delete Evidence?")
                .setMessage("Remove this photo from the ticket? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) ->
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getDatabase(itemView.getContext());
                        db.evidenceDao().deleteEvidence(item.localEntity);
                    })
                )
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_menu_delete)
                .show();
        }
    }
}
