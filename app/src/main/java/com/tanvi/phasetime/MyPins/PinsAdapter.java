package com.tanvi.phasetime.MyPins;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanvi.phasetime.MainActivity;
import com.tanvi.phasetime.MapActivity;
import com.tanvi.phasetime.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

class PinsAdapter extends RecyclerView.Adapter<PinsAdapter.ViewHolder> {

    public static int pos;
    public static ArrayList<MyPinDataClass> mData;
    Context mContext;
    private LayoutInflater mInflater;

    public PinsAdapter(FragmentActivity context, ArrayList<MyPinDataClass> arr) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = arr;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.pin_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.location.setText(mData.get(position).getLocation());
        holder.latitude.setText( new DecimalFormat("##.#####").format(mData.get(position).getLatitude()));
        holder.longitude.setText( new DecimalFormat("##.#####").format(mData.get(position).getLatitude()));
        holder.model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext() , MapActivity.class);
                intent.putExtra("from_activity" , "Adapter");
                intent.putExtra("latitude" ,mData.get(position).getLatitude() );
                intent.putExtra("longitude" , mData.get(position).getLongitude());
                v.getContext().startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView latitude, longitude, location;
        LinearLayout model;

        public ViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            model = itemView.findViewById(R.id.model);
        }
    }

}
