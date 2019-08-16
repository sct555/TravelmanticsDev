package com.example.travelmantics_dev;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView imageDeal;

    public DealAdapter() {
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        deals = FirebaseUtil.mDeals;

        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row, parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position);
        holder.bind(deal);

/*
        Log.d("CustomMessage","--------------------------------------------------------------------------------");
        Log.d("CustomMessage","start onBindViewHolder()");
        Log.d("CustomMessage","holder.tvTitle: " + holder.tvTitle);
        Log.d("CustomMessage","holder.tvDescription: " + holder.tvDescription);
        Log.d("CustomMessage","deal.getPrice() returns: " + holder.tvPrice);
        Log.d("CustomMessage","deal.getImageUrl(): " + deal.getImageUrl());
        Log.d("CustomMessage","associated name deal.getImageName(): " + deal.getImageName());
*/
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class  DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;

        public DealViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            imageDeal = (ImageView) itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);
        }


        public void bind(TravelDeal deal) {
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText("R " + deal.getPrice());

            showImage(deal.getImageUrl());

            Log.d("CustomMessage","--------------------------------------------------------------------------------");
            Log.d("CustomMessage","start bind()");
            Log.d("CustomMessage","deals.get(getAdapterPosition()) : " + deals.get(getAdapterPosition()));
            Log.d("CustomMessage","deal.getTitle() returns: " + deal.getTitle());
            Log.d("CustomMessage","deal.getDescription() returns: " + deal.getDescription());
            Log.d("CustomMessage","deal.getPrice() returns: " + deal.getPrice());
            Log.d("CustomMessage","showImage called with deal.getImageUrl(): " + deal.getImageUrl());
            Log.d("CustomMessage","associated name deal.getImageName(): " + deal.getImageName());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            //Log.d("Click", String.valueOf(position));
            TravelDeal selectedDeal = deals.get(position);
            Intent intent = new Intent(view.getContext(), com.example.travelmantics_dev.DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if (url != null && url.isEmpty()==false) {
                Picasso.get()
                        .load(url)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_STORE)                        .resize(210, 210)
                        .centerCrop()
                        .placeholder(R.mipmap.travelmantics_launcher)
                        .into(imageDeal);
            }
            else{
                imageDeal.setImageDrawable(null);
            }
        }
    }
}
