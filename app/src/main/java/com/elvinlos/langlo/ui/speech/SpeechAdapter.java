package com.elvinlos.langlo.ui.speech;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.R;

import java.util.List;

class SpeechAdapter extends RecyclerView.Adapter<SpeechAdapter.ViewHolder> {
    private List<SpeechItem> items;
    private OnItemClickListener listener;

    interface OnItemClickListener {
        void onItemClick(SpeechItem item);
    }

    public SpeechAdapter(List<SpeechItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_speech, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpeechItem item = items.get(position);
        holder.bind(item, listener);
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

        void bind(SpeechItem item, OnItemClickListener listener) {
            titleText.setText(item.getTitle());
            score.setText(item.getScore() + "%");
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
