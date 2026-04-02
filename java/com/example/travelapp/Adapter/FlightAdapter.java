package com.example.travelapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelapp.Model.Flight;
import com.example.travelapp.SearchActivity;
import com.example.travelapp.TicketDetailActivity;
import com.example.travelapp.databinding.ViewholderFlightaBinding;

import java.util.ArrayList;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.Viewholder> {

    private final ArrayList<Flight> flights;
    private Context context;

    public FlightAdapter(ArrayList<Flight> flights) {
        this.flights = flights;
    }

    @NonNull
    @Override
    public FlightAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context =  parent.getContext();
        ViewholderFlightaBinding bind = ViewholderFlightaBinding.inflate(LayoutInflater.from(context),parent,false);
        return new Viewholder(bind);

    }

    @Override
    public void onBindViewHolder(@NonNull FlightAdapter.Viewholder holder, int position) {
     Flight flight = flights.get(position);
        if (flight == null) {
            Log.e("FlightAdapter", "Flight object is null at position: " + position);
            return;
        }
     Glide.with(context)
             .load(flight.getairlineLogo())
             .into(holder.bind.logo);

     holder.bind.fromtxt.setText(flight.getFrom());
     holder.bind.fromShortTxt.setText(flight.getFromShort());
     holder.bind.toTxt.setText(flight.getTo());
     holder.bind.toShortTxt.setText(flight.getToShort());
     holder.bind.arrivalTxt.setText(flight.getArrivalTime());
     holder.bind.classTxt.setVisibility(View.VISIBLE);
     holder.bind.classTxt.setText(flight.getClassSeat());
     holder.bind.priceTxt.setText("£"+flight.getPrice());


        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            // Save number of passengers to the flight object before passing it
            flight.setNumberOfPassengers(((SearchActivity)context).getNumPassenger());
            intent.putExtra("flight", flight);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderFlightaBinding bind;
        public Viewholder(ViewholderFlightaBinding bind) {
            super(bind.getRoot());
            this.bind = bind;
        }
    }
}
