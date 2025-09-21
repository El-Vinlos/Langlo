package com.elvinlos.langlo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private final Context context;
    private final List<Deck> deckList;

    public DeckAdapter(Context context, List<Deck> deckList) {
        this.context = context;
        this.deckList = deckList;
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck deck = deckList.get(position);
        holder.title.setText(deck.getTitle());
        holder.description.setText(deck.getDescription());
        holder.cardCount.setText(deck.getCardCount() + " cards");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CardStudyActivity.class);
            intent.putExtra("deckName", deck.getFolderName());
            Log.d("DeckAdapter", "Clicked deck: " + deck.getTitle() +
                    " | folder=" + deck.getFolderName());
            context.startActivity(intent);
        });    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }

    static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, cardCount;

        public DeckViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textDeckTitle);
            description = itemView.findViewById(R.id.textDeckDescription);
            cardCount = itemView.findViewById(R.id.textDeckCount);
        }
    }
}
