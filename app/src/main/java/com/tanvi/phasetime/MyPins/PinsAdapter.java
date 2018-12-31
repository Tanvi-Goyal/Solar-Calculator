package com.tanvi.phasetime.MyPins;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanvi.phasetime.R;

import java.util.ArrayList;

class PinsAdapter extends RecyclerView.Adapter<PinsAdapter.ViewHolder>{

    public static int pos;
    public static ArrayList<MyPinDataClass> mData;
    private LayoutInflater mInflater;
    Context mContext;

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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.location.setText(mData.get(position).getLocation());
//        holder.sunrise.setText(mData.get(position).getSunrise());
//        holder.sunset.setText(mData.get(position).getSunset());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, sunset, sunrise, location;

        public ViewHolder(View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.location);
//            sunset = itemView.findViewById(R.id.sunset);
//            sunrise = itemView.findViewById(R.id.sunrise);
        }
    }

}
