package com.tanvi.phasetime;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tanvi.phasetime.MyPins.MyPinDataClass;
import com.tanvi.phasetime.MyPins.MyPins;

import org.joda.time.DateTime;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int DIALOG_ID = 0;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));


    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mPlacePicker;


    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    ImageButton date_picker_button ;
    ImageButton date_forward_button ;
    ImageButton date_backward_button ;
    private View alertLayout;
    public static int year_x , month_x , day_x;
    TextView date_textview;

    public static double latitude;
    public static double longitude;
    private TextView sunrise_textView;
    private TextView sunset_textView;
    private TextView moonrise_textview;
    private TextView moonset_textview;
    private int milisecondsDelay;
    DecimalFormat df;
    private Marker marker;
    String from_activity ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_picker);
        sunrise_textView = findViewById(R.id.sun_rise_textview);
        sunset_textView = findViewById(R.id.sun_set_textview);
        moonrise_textview = findViewById(R.id.moon_rise_textview);
        moonset_textview = findViewById(R.id.moon_set_textview);
        date_textview = findViewById(R.id.date);
        date_forward_button = findViewById(R.id.date_forward_btn);
        date_backward_button = findViewById(R.id.date_backward_btn);

        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        final Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        date_textview.setText(day_x + "/" + month_x + "/" + year_x);


         from_activity = this.getIntent().getStringExtra("from_activity");
        if(from_activity!=null) {
            if(from_activity.equals("Adapter")){
                initMap();


            }
        }



        getLocationPermission();

        showDialogOnButtonClick();
//        dateBackAndAhead(day_x,month_x,year_x);

    }

//    private void dateBackAndAhead(final int day_x, final int month_x, final int year_x) {
//
//        date_backward_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Date dateBack = new Date(year_x , month_x, day_x);
//                Calendar calBack = Calendar.getInstance();
//                calBack.setTime(dateBack);
//                calBack.add(Calendar.DATE, -1); // subtract 1 day
//
//                dateBack = calBack.getTime();
//                Log.d(TAG, "dateBackAndAhead: Added date" + dateBack.toString());
//
//
//            }
//        });
//
//
//        date_forward_button.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            @Override
//            public void onClick(View v) {
////                Date dateAhead = new Date(year_x , month_x, day_x);
////                Calendar calAhead = Calendar.getInstance();
////                calAhead.setTime(dateAhead);
////                calAhead.add(Calendar.DAY_OF_YEAR, 1); // add 1 day
////
////                dateAhead = calAhead.getTime();
////                Log.d(TAG, "dateBackAndAhead: Added date" + dateAhead.getDay());
////                date_textview.setText(dateAhead.getDay() + "/" + dateAhead.getMonth()+1 + "/" + dateAhead.getYear());
//
//
//                Date dt = new Date(year_x , month_x, day_x);
//                DateTime dtOrg = new DateTime(dt);
//                DateTime dtPlusOne = dtOrg.plusDays(1);
//                Log.d(TAG, "onClick: ahead" + dtPlusOne.dayOfYear()+ "/" + dtPlusOne.getMonthOfYear() + "/" + dtPlusOne.getYear());
//                date_textview.setText(dtPlusOne.dayOfYear()+ "/" + dtPlusOne.getMonthOfYear() + "/" + dtPlusOne.getYear());
//                dateBackAndAhead(dtPlusOne.getDayOfYear(),dtPlusOne.getMonthOfYear() ,dtPlusOne.getYear());
//
////
////                Date dt = new Date(year_x , month_x, day_x);
////                Log.d(TAG, "onClick: " + LocalDateTime.from(dt.toInstant()).plusDays(1));
//            }
//        });
//
//    }

    //------ Date Select Module------//

    private void showDialogOnButtonClick() {


        date_picker_button = findViewById(R.id.date_dialog_btn);
        date_picker_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });
    }



    @Override
    protected Dialog onCreateDialog(int id) {

        if(id == DIALOG_ID){
            return new DatePickerDialog(this , dPickerListerner , year_x , month_x , day_x);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener dPickerListerner
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month +1 ;
            day_x  = dayOfMonth;

//            Toast.makeText(MapActivity.this, day_x + "/" + month_x + "/" + year_x,Toast.LENGTH_SHORT).show();
            date_textview.setText(day_x + "/" + month_x + "/" + year_x);
            Date date = new Date(year_x , month_x-1, day_x);
            Log.wtf(TAG , date.toString());
            getSunRiseSunSetTime();
            getMoonRiseMoonSetTime(date , latitude , longitude);
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            if(from_activity==null){
                getDeviceLocation();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                init();
            }else{

                latitude = getIntent().getDoubleExtra("latitude",0.0 );
                longitude = getIntent().getDoubleExtra("longitude" , 0.0);
//                getLocationPermission();
                updateMap(new LatLng(latitude, longitude));
                locateSavedavedPins(latitude,longitude);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

//                init();
            }

        }
    }



    private void init() {
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage() );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage() );
                }
            }
        });

        hideSoftKeyboard();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

            Date date = new Date(year_x , month_x-1, day_x);

            getSunRiseSunSetTime();
            getMoonRiseMoonSetTime(date , address.getLatitude() , address.getLongitude());
            MyPinDataClass pin = new MyPinDataClass(address.toString(), address.getLatitude(), address.getLongitude());
            SavePins(pin);
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                            //initialize with device location Latitude and Longitude and Current Date
                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();

                            Date date = new Date(year_x , month_x-1, day_x);

                            getSunRiseSunSetTime();
//                            getMoonRiseMoonSetTime(date, latitude , longitude);


                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    public double calculateTime(double latitude, double longitude, int day, int month, int year, boolean sunrise) {

        //Calculating the day of the year
        double N1 = Math.floor(275 * month / 9);
        double N2 = Math.floor((month + 9) / 12);
        double N3 = (1 + Math.floor((year - 4 * Math.floor(year / 4) + 2) / 3));
        double N = N1 - (N2 * N3) + day - 30;
        double D2R = Math.PI/180;
        double R2D = 180 / Math.PI;
        //convert the longitude to hour value and calculate an approximate time
        double lngHour = longitude / 15;
        double t;
        double zenith = 90.83333333333333;
        if(sunrise) {
            t = N + ((6 - lngHour) / 24);
        }
        else {
            t = N + ((18 - lngHour) / 24);
        }

        //calculate the Sun's mean anomaly
        double M = (0.9856 * t) - 3.289;

        //calculate the Sun's true longitude
        double L = M + (1.916 * Math.sin(M * D2R)) + (0.020 * Math.sin(2 * M * D2R)) + 282.634;
        if (L > 360) {
            L = L - 360;
        } else if (L < 0) {
            L = L + 360;
        }

        //calculate the Sun's right ascension
        double RA = R2D * Math.atan(0.91764 * Math.tan(L * D2R));
        if (RA > 360) {
            RA = RA - 360;
        } else if (RA < 0) {
            RA = RA + 360;
        }

        //right ascension value needs to be in the same quadrant as L
        double Lquadrant  = (Math.floor( L/90)) * 90;
        double RAquadrant = (Math.floor(RA/90)) * 90;
        RA += (Lquadrant - RAquadrant);

        //right ascension value needs to be converted into hours
        RA /= 15;

        //calculate the Sun's declination
        double sinDec = 0.39782 * Math.sin(L * D2R);
        double cosDec = Math.cos(Math.asin(sinDec));

        //calculate the Sun's local hour angle
        double cosH = (Math.cos(zenith * D2R) - (sinDec * Math.sin(latitude * D2R))) / (cosDec * Math.cos(latitude * D2R));
        if (cosH >  1) {
            Toast.makeText(this, R.string.sunnotrise,Toast.LENGTH_SHORT).show();
        }

        else if (cosH < -1) {
            Toast.makeText(this, R.string.sunnotset,Toast.LENGTH_SHORT).show();
        }

        else {
            //finish calculating H and convert into hours
            double H;
            if(sunrise) {
                H = 360 - R2D * Math.acos(cosH);
            }
            else {
                H = R2D * Math.acos(cosH);
            }
            H/=15;

            double T = H + RA - (0.06571 * t) - 6.622;
            double UT = T - lngHour;
            if (UT > 24) {
                UT = UT - 24;
            } else if (UT < 0) {
                UT = UT + 24;
            }
            double localT = UT + 5.50;
            return localT;
        }
        return -1;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void getSunRiseSunSetTime() {

        Log.wtf(TAG , "Details are :" + latitude + " , " + longitude + " , " + day_x + " , " + month_x + " ," + year_x);
        double sunrise = calculateTime(latitude,longitude,day_x,month_x,year_x,true);
        double sunset = calculateTime(latitude,longitude,day_x,month_x,year_x,false);
        Log.i("Time",SystemClock.elapsedRealtime()+"");
        if(sunrise != -1 && sunset != -1) {
            int sunriseNumber = (int) sunrise;
            int sunsetNumber = (int) sunset;
            String sunriseDecimalString = df.format(sunrise - sunriseNumber);
            String sunsetDecimalString = df.format(sunset - sunsetNumber);
            double sunriseDecimal = Double.parseDouble(sunriseDecimalString);
            double sunsetDecimal = Double.parseDouble(sunsetDecimalString);
            if(sunriseDecimal > 0.60) {
                sunriseDecimal-=0.60;
                sunriseNumber++;
            }
            if(sunsetDecimal > 0.60) {
                sunsetDecimal-=0.60;
                sunsetNumber++;
            }
            int sunriseNo = (int) (sunriseDecimal*100);
            int sunsetNo = (int) (sunsetDecimal*100);
            if(sunriseNo < 10) {
                sunriseDecimalString = "0"+ sunriseNo;
            }
            else {
                sunriseDecimalString = sunriseNo + "";
            }
            if(sunsetNo < 10) {
                sunsetDecimalString = "0" + sunsetNo;
            }
            else {
                sunsetDecimalString = sunsetNo + "";
            }
            String sunriseTime = sunriseNumber + ":" + sunriseDecimalString + " A.M";
            String sunsetTime = sunsetNumber + ":" + sunsetDecimalString + " P.M";
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            milisecondsDelay = Math.abs(hour - sunsetNumber -1)*3600*1000;
            Log.i("ms",milisecondsDelay+"");
            sunrise_textView.setText(sunriseTime);
            sunset_textView.setText(sunsetTime);
            scheduleNotification(getNotification("Golden Hour is at : "), milisecondsDelay);
        }
    }

    public  void locateSavedavedPins(Double latitude, Double longitude) {

//        moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM,
//                "");

        Date date = new Date(MapActivity.year_x , MapActivity.month_x, MapActivity.day_x);
        getSunRiseSunSetTime();
        getMoonRiseMoonSetTime(date , latitude,longitude);
    }

    public void updateMap(LatLng position) {
        mMap.clear();
        marker = mMap.addMarker(new MarkerOptions().position(position)
                .title("Marker in position")
                .draggable(true));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,13));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)      // Sets the center of the map to location user
                .zoom(18)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void getMoonRiseMoonSetTime(Date currentDate, double latitude, double longitude) {


        Log.wtf(TAG , "Moon Details " + currentDate + " " + latitude + " " + longitude);

        SunTimes st = SunTimes.compute()
                .on(currentDate).at(latitude, longitude)
                .twilight(SunTimes.Twilight.VISUAL) // default, equals SUNRISE/SUNSET
                .execute();
        Date sunriseTime = st.getRise();
        Date sunsetTime = st.getSet();
        Log.wtf(TAG , sunriseTime.toString() +"  " + sunsetTime.toString());

        MoonTimes mt = MoonTimes.compute()
                .on(currentDate).at(latitude , longitude)
                .execute();

        Date moonriseTime = mt.getRise();
        Date moonsetTime = mt.getSet();
        moonrise_textview.setText(moonriseTime.getHours() + ":"  + moonriseTime.getMinutes() + " ") ;
        moonset_textview.setText(moonsetTime.getHours() + ":" + moonsetTime.getMinutes());
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);


            try {
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());

                Date date = new Date(year_x , month_x-1, day_x);

                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                getSunRiseSunSetTime();
                getMoonRiseMoonSetTime(date , latitude , longitude);
                MyPinDataClass pin = new MyPinDataClass(String.valueOf(place.getName()), longitude, latitude);
                SavePins(pin);

            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };

    public void MyPins(View view) {
        Toast.makeText(this, "My Pins", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MyPins.class));
    }


    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("The rise of golden hour");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_notification);
        return builder.build();
    }

    public void SavePins(MyPinDataClass pin) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.push().setValue(pin);
    }

}











