package com.elvinlos.langlo;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final Context context;
    private final List<Card> cardList;
    private final List<Card> filteredList;

    public CardAdapter(Context context, List<Card> cardList) {
        this.context = context;
        this.cardList = cardList;
        this.filteredList = new ArrayList<>(cardList);
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
        Card card = filteredList.get(position);

        holder.textEnglish.setText(card.getEnglish());
        holder.textVietnamese.setText(card.getVietnamese());

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView textEnglish, textVietnamese;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textEnglish = itemView.findViewById(R.id.textEnglish);
            textVietnamese = itemView.findViewById(R.id.textVietnamese);
        }
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(cardList);
        } else {
            String lower = query.toLowerCase();
            for (Card c : cardList) {
                if (c.getEnglish().toLowerCase().contains(lower) ||
                        c.getVietnamese().toLowerCase().contains(lower)) {
                    filteredList.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void updateCards(List<Card> newCards) {
        cardList.clear();
        cardList.addAll(newCards);

        filteredList.clear();
        filteredList.addAll(newCards);

        notifyDataSetChanged();
    }

}
