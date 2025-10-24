package com.elvinlos.langlo.ui.cardstudy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.elvinlos.langlo.Card;
import com.elvinlos.langlo.R;

public class CardAdapter extends ListAdapter<Card, CardAdapter.CardViewHolder> {

    private static final DiffUtil.ItemCallback<Card> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Card oldItem, @NonNull Card newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public CardAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = getItem(position);
        holder.textEnglish.setText(card.getEnglish());
        holder.textVietnamese.setText(card.getVietnamese());

        holder.itemView.setOnLongClickListener(v -> {
            Context context = v.getContext();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            String textToCopy = card.getEnglish() + " - " + card.getVietnamese();
            ClipData clip = ClipData.newPlainText("card_content", textToCopy);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
            return true;
        });

    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        final TextView textEnglish, textVietnamese;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textEnglish = itemView.findViewById(R.id.textEnglish);
            textVietnamese = itemView.findViewById(R.id.textVietnamese);
        }
    }
}
