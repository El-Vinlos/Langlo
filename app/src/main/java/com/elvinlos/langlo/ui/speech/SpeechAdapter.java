package com.elvinlos.langlo.ui.speech;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

class SpeechAdapter extends RecyclerView.Adapter<SpeechAdapter.ViewHolder> {

    private List<SpeechItem> items;
    private OnItemClickListener listener;
    private DatabaseReference scoresRef;
    private String uid;
    private Context context;

    interface OnItemClickListener {
        void onItemClick(SpeechItem item);
    }

    public SpeechAdapter(List<SpeechItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
            scoresRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("scores");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_speech, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpeechItem item = items.get(position);
        holder.bind(item, listener, context);

        if (scoresRef != null) {
            scoresRef.child(item.getTitle()).child("score")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Integer scoreValue = snapshot.getValue(Integer.class);
                                if (scoreValue != null) {
                                    item.setScore(scoreValue);
                                    holder.updateScoreColor(scoreValue, context);
                                }
                            } else {
                                holder.score.setText("0%");
                                holder.score.setTextColor(
                                        ContextCompat.getColor(context, android.R.color.darker_gray)
                                );
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView score;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textView_recognizedText);
            score = itemView.findViewById(R.id.textView_pronunciationScore);
        }

        void bind(SpeechItem item, OnItemClickListener listener, Context context) {
            titleText.setText(item.getTitle());
            score.setText(item.getScore() + "%");
            updateScoreColor(item.getScore(), context);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        void updateScoreColor(int scoreValue, Context context) {
            score.setText(scoreValue + "%");

            int colorRes;
            if (scoreValue >= 85) {
                colorRes = android.R.color.holo_blue_light;
            } else if (scoreValue >= 70) {
                colorRes = android.R.color.holo_green_light;
            } else if (scoreValue >= 50) {
                colorRes = android.R.color.holo_orange_light;
            } else {
                colorRes = android.R.color.holo_red_light;
            }

            score.setTextColor(ContextCompat.getColor(context, colorRes));
        }
    }
}
