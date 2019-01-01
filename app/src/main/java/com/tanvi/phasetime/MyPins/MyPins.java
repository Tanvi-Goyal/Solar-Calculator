package com.tanvi.phasetime.MyPins;

import android.content.Intent;
import android.os.AsyncTask;
import android.renderscript.Script;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tanvi.phasetime.MapActivity;
import com.tanvi.phasetime.R;

import java.util.ArrayList;

public class MyPins extends AppCompatActivity {

    RecyclerView rv;
    PinsAdapter adapter;
    public ArrayList<MyPinDataClass> pins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pins);
        Intent intent = new Intent(MyPins.this,MapActivity.class);
        rv = findViewById(R.id.rv);
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
//        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, (view, position) -> {
//            // TODO Handle item click
//            double geoLocationCoords[] = adapter.getGeoLocation(position);
//            double latitude = geoLocationCoords[0];
//            double longitude = geoLocationCoords[1];
//            intent.putExtra("latitude",latitude);
//            intent.putExtra("longitude",longitude);
//            startActivity(intent);
//        }));
        adapter = new PinsAdapter(this, pins);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(adapter);

        new FetchMyPins().execute();
    }

    public class FetchMyPins extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child: dataSnapshot.getChildren()) {
                        MyPinDataClass data = child.getValue(MyPinDataClass.class);
                        pins.add(data);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }
}
