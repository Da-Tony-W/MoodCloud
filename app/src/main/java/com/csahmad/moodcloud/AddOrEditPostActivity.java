package com.csahmad.moodcloud;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.concurrent.TimeoutException;
import android.location.LocationManager;
import com.google.android.gms.location.LocationRequest;

/** The activity for adding a {@link Post} or editing an existing one. */
public class AddOrEditPostActivity extends AppCompatActivity {

    private static final int TAKE_IMAGE_REQUEST = 0;
    private static final int READ_LOCATION_REQUEST = 1;
    private final static int REQUEST_GET_DATE = 3;
    private String image;
    private Drawable defaultImage;
    private ImageButton moodPhoto;
    private TextView dateString;
    private Calendar date;
    private Bundle setDate;
    private int setDay;
    private int setMonth;
    private int setYear;
    private ImageButton deletePhoto;
    private double[] locationArray = null;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == TAKE_IMAGE_REQUEST) {

            if (resultCode == RESULT_OK) {

                String cameraImage = intent.getStringExtra("IMAGE");

                if (cameraImage != null) {
                    this.image = cameraImage;
                    Bitmap bitmap = intent.getParcelableExtra("BITMAP");
                    this.moodPhoto.setImageBitmap(bitmap);
                    deletePhoto.setVisibility(View.VISIBLE);
                }

            } else if (resultCode == RESULT_CANCELED)
                Toast.makeText(getApplicationContext(), "Photo was cancelled!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), "Unknown bug! Please report!", Toast.LENGTH_LONG).show();
        }

        if (requestCode == REQUEST_GET_DATE){
            if(resultCode == RESULT_OK){
                setDate = intent.getBundleExtra("set_date");
                setDay = setDate.getInt("day");
                setMonth = setDate.getInt("month");
                setYear = setDate.getInt("year");
                this.date = DateConverter.toDate(setYear,setMonth,setDay);

                dateString.setText(DateConverter.dateToString(date));

            }

        }
    }

    /** Request permission from the user to access location. */
    public void requestLocationPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                READ_LOCATION_REQUEST);
    }

    /**
     * Return whether this app currently has permission to access location.
     *
     * @return whether this app currently has permission to access location
     */
    public boolean haveLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)

            return false;

        return true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_post);
        PostController postController = new PostController();
        moodPhoto = (ImageButton) findViewById(R.id.moodPhoto);
        defaultImage = moodPhoto.getDrawable();
        dateString = (TextView) findViewById(R.id.postDate);
        deletePhoto = (ImageButton) findViewById(R.id.delimage);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // TODO: 2017-03-31 Find out why Android Studio won't let me put this in a separate method
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            requestLocationPermission();
        }

        if (haveLocationPermission()) {

            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location == null){

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationListener locationListener= new LocationListener() {

                    @Override
                    public void onLocationChanged(Location location) {

                        locationArray = new double[]{location.getLatitude(),
                                location.getLongitude(), location.getAltitude()};
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                        ;
                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                        ;
                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                        ;
                    }
                };

                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }

            else {
                locationArray = new double[]{location.getLatitude(),
                        location.getLongitude(), location.getAltitude()};
            }
        }

        if(isNetworkAvailable()) {
            Intent intent = getIntent();
            String id = intent.getStringExtra("POST_ID");



            try {

                final EditText textExplanation = (EditText) findViewById(R.id.body);
                final EditText textTrigger = (EditText) findViewById(R.id.trigger);

                final RadioGroup moodButtons = (RadioGroup) findViewById(R.id.moodRadioGroup);
                final RadioGroup statusButtons = (RadioGroup) findViewById(R.id.statusRadioGroup);
                final EditText latitudetext = (EditText) findViewById(R.id.latitude);
                final EditText longitudetext = (EditText) findViewById(R.id.longitude);
                final EditText altitudetext = (EditText)findViewById(R.id.altitude);



                if (id == null) {

                    dateString.setText(DateConverter.dateToString(Calendar.getInstance()));

                    if (locationArray != null) {
                        latitudetext.setText(Double.toString(locationArray[0]));
                        longitudetext.setText(Double.toString(locationArray[1]));
                        altitudetext.setText(Double.toString(locationArray[2]));
                    }

                    moodPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, TakePhotoActivity.class);
                            startActivityForResult(intent,TAKE_IMAGE_REQUEST);
                        }
                    });
                    ImageButton dateButton = (ImageButton) findViewById(R.id.datebutton);
                    dateButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            Context context = view.getContext();
                            Intent intent = new Intent(context, SelectDateActivity.class);
                            startActivityForResult(intent,REQUEST_GET_DATE);
                        }
                    });
                    final ImageButton deletePhoto = (ImageButton) findViewById(R.id.delimage);
                    deletePhoto.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            moodPhoto.setImageDrawable(defaultImage);
                            deletePhoto.setVisibility(View.INVISIBLE);
                            image = null;
                        }
                    });

                    Button postButton = (Button) findViewById(R.id.postButton);
                    postButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (textExplanation.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), "Want to say something?", Toast.LENGTH_LONG).show();
                            } else if (onRadioButtonClicked(moodButtons) == 8) {
                                Toast.makeText(getApplicationContext(), "Want to select a mood?", Toast.LENGTH_LONG).show();
                            } else if (textTrigger.getText().toString().equals("") && (image == null)) {
                                Toast.makeText(getApplicationContext(), "Want to say why?", Toast.LENGTH_LONG).show();
                            }
                            else if (onStatusButtonClicked(statusButtons) == 4) {
                                Toast.makeText(getApplicationContext(), "Want to select a social context?", Toast.LENGTH_LONG).show();
                            } else {
                                Profile profile = LocalData.getSignedInProfile(getApplicationContext());
                                if (date == null){
                                    date = Calendar.getInstance();
                                }

                                Post post = new Post(textExplanation.getText().toString().replace("\\s+$", ""), onRadioButtonClicked(moodButtons),
                                        textTrigger.getText().toString().replace("\\s+$", ""), image, onStatusButtonClicked(statusButtons),
                                        profile.getId(), null, date);
                                PostController postController = new PostController();
                                postController.addOrUpdatePosts(post);
                                ProfileController profileController = new ProfileController();
                                profileController.addOrUpdateProfiles(profile);


                                finish();
                            }
                        }

                    });
                } else {

                    moodPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, TakePhotoActivity.class);
                            startActivityForResult(intent,TAKE_IMAGE_REQUEST);
                        }
                    });
                    ImageButton dateButton = (ImageButton) findViewById(R.id.datebutton);
                    dateButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            Context context = view.getContext();
                            Intent intent = new Intent(context, SelectDateActivity.class);
                            startActivityForResult(intent,REQUEST_GET_DATE);

                        }
                    });
                    final ImageButton deletePhoto = (ImageButton) findViewById(R.id.delimage);
                    deletePhoto.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view){
                            moodPhoto.setImageDrawable(defaultImage);
                            deletePhoto.setVisibility(View.INVISIBLE);
                            image = null;
                        }
                    });

                    final Post oldPost = postController.getPostFromId(id);

                    if (oldPost.getTriggerImage() != null) {
                        Bitmap bitmap = ImageConverter.toBitmap(oldPost.getTriggerImage());
                        this.moodPhoto.setImageBitmap(bitmap);
                        deletePhoto.setVisibility(View.VISIBLE);
                        image = oldPost.getTriggerImage();
                    }

                    String oldExplannation = oldPost.getText();
                    String oldTrigger = oldPost.getTriggerText();
                    int oldmood = oldPost.getMood();
                    int oldcontext = oldPost.getContext();
                    Calendar olddate = oldPost.getDate();

                    double[] oldlocationArray = oldPost.getLocation();

                    String oldLatitude = null;
                    String oldLongitude = null;
                    String oldAltitude = null;


                    if (oldlocationArray == null) {

                        if (locationArray != null) {
                            oldLatitude = Double.toString(locationArray[0]);
                            oldLongitude = Double.toString(locationArray[1]);
                            oldAltitude = Double.toString(locationArray[2]);
                        }
                    }

                    else {
                        oldLatitude = Double.toString(oldlocationArray[0]);
                        oldLongitude = Double.toString(oldlocationArray[1]);
                        oldAltitude = Double.toString(oldlocationArray[2]);
                    }


                    textTrigger.setText(oldTrigger);
                    textExplanation.setText(oldExplannation);
                    moodButtons.check(RadioConverter.getMoodButtonId(oldmood));
                    statusButtons.check(RadioConverter.getContextButtonId(oldcontext));
                    dateString.setText(DateConverter.dateToString(olddate));

                    if (oldLatitude != null) latitudetext.setText(oldLatitude);
                    altitudetext.setText(oldAltitude);
                    longitudetext.setText(oldLongitude);



                    Button postButton = (Button) findViewById(R.id.postButton);
                    postButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (textExplanation.getText().toString().equals("")) {
                                Toast.makeText(getApplicationContext(), "Want to say something?", Toast.LENGTH_LONG).show();
                            } else if (onRadioButtonClicked(moodButtons) == 8) {
                                Toast.makeText(getApplicationContext(), "Want to select a mood?", Toast.LENGTH_LONG).show();
                            } else if (textTrigger.getText().toString().equals("") && image == null) {
                                Toast.makeText(getApplicationContext(), "Want to say why?", Toast.LENGTH_LONG).show();
                            } else if (onStatusButtonClicked(statusButtons) == 4) {
                                Toast.makeText(getApplicationContext(), "Want to select a social context?", Toast.LENGTH_LONG).show();
                            } else {
                                oldPost.setMood(onRadioButtonClicked(moodButtons));
                                oldPost.setContext(onStatusButtonClicked(statusButtons));
                                oldPost.setText(textExplanation.getText().toString().replace("\\s+$", ""));
                                oldPost.setTriggerText(textTrigger.getText().toString().replace("\\s+$", ""));
                                oldPost.setTriggerImage(image);
                                if (date != null)
                                oldPost.setDate(date);


                                PostController postController = new PostController();
                                postController.addOrUpdatePosts(oldPost);


                                //Context context = v.getContext();
                                //Intent intent = new Intent(context, ViewPostActivity.class);

                                //intent.putExtra("POST_ID", oldPost.getId());
                                finish();
                                //startActivity(intent);
                            }


                        }

                    });
                }

            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }//else{

        //}






        ImageButton imageButton = (ImageButton) findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                finish();
            }}
        );


    }
    //Based on https://developer.android.com/guide/topics/ui/controls/radiobutton.html
    //A converter function that converts the selected radio button in a radio group to a mood
    public Integer onRadioButtonClicked(View view) {

        int checked = ((RadioGroup) view).getCheckedRadioButtonId();
        Integer mood = null;

        Log.i("selected", Integer.toString(view.getId()));
        Log.i("selected", Integer.toString(R.id.confused_selected));

        switch(checked) {

            case R.id.angry_selected:
                mood = Mood.ANGRY;
                break;

            case R.id.confused_selected:
                mood = Mood.CONFUSED;
                break;

            case R.id.scared_selected:
                mood = Mood.SCARED;
                break;

            case R.id.happy_selected:
                mood = Mood.HAPPY;
                break;

            case R.id.sad_selected:
                mood = Mood.SAD;
                break;

            case R.id.surprised_selected:
                mood = Mood.SURPRISED;
                break;

            case R.id.ashamed_selected:
                mood = Mood.ASHAMED;
                break;

            case R.id.disgusted_selected:
                mood = Mood.DISGUSTED;
                break;

            default:
                //throw new RuntimeException("fsdfdssdfsd");
                return 8;
        }

        return mood;
    }

    public Integer onStatusButtonClicked(View view) {

        int checked = ((RadioGroup) view).getCheckedRadioButtonId();
        Integer status = null;

        switch (checked) {
            case R.id.alone_selected:
                status = SocialContext.ALONE;
                break;
            case R.id.crowd_selected:
                status = SocialContext.WITH_CROWD;
                break;
            case R.id.group_selected:
                status = SocialContext.WITH_GROUP;
                break;
            default:
                //throw new RuntimeException("other fsdfdssdfsd");
                return 4;
        }
        return status;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }






}