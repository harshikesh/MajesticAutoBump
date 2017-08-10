package com.mejestic.bump.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mejestic.R;
import com.mejestic.bump.application.CustomNotificationManager;
import com.mejestic.bump.database.DBHelper;
import com.mejestic.bump.service.SamplingService;
import com.mejestic.bump.util.Constants;
import com.mejestic.bump.util.DirectionsJSONParser;
import com.mejestic.bump.util.Utils;
import com.squareup.picasso.Picasso;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.mejestic.bump.util.Constants.FINAL_STRING;
import static com.mejestic.bump.util.Constants.LOG_TAG;
import static com.mejestic.bump.util.Constants.MAP;
import static com.mejestic.bump.util.Constants.MAP_2;

public class MainActivity extends AppCompatActivity
    implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
    GoogleApiClient.OnConnectionFailedListener, TextWatcher, Response.Listener,
    Response.ErrorListener {

  private static final float INIT_ZOOM = 14;
  private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
  public static final int MEDIA_TYPE_IMAGE = 1;
  private static final String IMAGE_DIRECTORY_NAME = "PDRCamera";

  private static final String MESSAGES_CHILD = "pothole_location";
  private static final int PERMISSION_LOCATION_REQUEST_CODE = 102;
  private CustomMapFragment mMapFragment;
  private final String TAG = LOG_TAG + MainActivity.class.getSimpleName();

  private DBHelper dbHelper;
  private GoogleMap mGoogleMap;
  private BroadcastReceiver receiver;
  private GoogleApiClient mGoogleApiClient;
  private String mName;
  private String mEmail;
  private Uri mPhotoUrl;
  private TextView nameView;
  private TextView emailView;
  private ImageView photoView;
  private LatLng mCurrentLatLng;
  private LatLng mDestinationLatLng;
  private NavigationView navigationView;
  private Polyline mPolyline;
  // Storage Permissions
  private static final int REQUEST_EXTERNAL_STORAGE = 1;
  private static String[] PERMISSIONS_STORAGE = {
      Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  private Bitmap mDotMarkerBitmap;
  private SharedPreferences mSharedPref;
  private EditText distnationFill;
  private Button distnationFillButton;
  private ArrayList<Marker> mMarkerArrayList = new ArrayList<>();
  private TextView tempView;
  private RequestQueue requestQue;
  private FirebaseAuth mFirebaseAuth;
  private FirebaseUser mFirebaseUser;
  private DatabaseReference mFirebaseDatabaseReference;
  private FirebaseAnalytics mFirebaseAnalytics;
  private FirebaseRemoteConfig mFirebaseRemoteConfig;
  private CustomNotificationManager mCustomNotificationManager;
  private int mSatellite = 0;
  private Button cameraButton;
  private Uri fileUri;
  private ImageView imgPreview;
  private VideoView videoPreview;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    GoogleSignInOptions gso =
        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
    mGoogleApiClient =
        new GoogleApiClient.Builder(this).enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();
    showPermissionDialog();
    navigationView = (NavigationView) findViewById(R.id.nav_view);
    imgPreview = (ImageView) findViewById(R.id.imgPreview);
    navigationView.setNavigationItemSelectedListener(this);
    distnationFill = (EditText) findViewById(R.id.destination_fill);
    distnationFillButton = (Button) findViewById(R.id.dest_button);
    distnationFill.addTextChangedListener(this);
    distnationFillButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        String location = distnationFill.getText().toString();
        if (location != null && !location.equals("")) {
          new GeocoderTask(getApplicationContext()).execute(location);
        }
      }
    });
    View header = navigationView.getHeaderView(0);
    nameView = (TextView) header.findViewById(R.id.nameView);
    emailView = (TextView) header.findViewById(R.id.emailView);
    photoView = (ImageView) header.findViewById(R.id.photoView);
    tempView = (TextView) header.findViewById(R.id.temp);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("PDR");
    cameraButton = (Button) findViewById(R.id.camera);
    cameraButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        captureImage();
      }
    });
    mName = mSharedPref.getString("name", "name");
    mEmail = mSharedPref.getString("email", "email@gmail.com");
    Boolean bool = mSharedPref.getBoolean("issignin", false);
    mPhotoUrl = Uri.parse(mSharedPref.getString("url", ""));
    updateUI(bool);

    // Initialse Broadcast reciever for getting marker lat long
    receiver = new BroadcastReceiver() {
      @Override public void onReceive(Context context, Intent intent) {
        String s = intent.getStringExtra(SamplingService.SPEED);
        Log.d(TAG, s);
        String[] split = s.split(",");
        LatLng p = new LatLng(Double.valueOf(split[0]), Double.valueOf(split[1]));
        mFirebaseDatabaseReference.child(MESSAGES_CHILD)
            .push()
            .setValue(new PojoLatlng(p.latitude, p.longitude));
        createMarker(p);
        mCustomNotificationManager.createNotifcation();
      }
    };

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    mMapFragment = new CustomMapFragment.Builder().setMapReadyListener(this).build();
    initMap();
    verifyStoragePermissions(this);
    dbHelper = new DBHelper(this);
    if (!isMyServiceRunning(SamplingService.class)) {
      Intent intent = new Intent(MainActivity.this, SamplingService.class);
      startService(intent);
    }

    // Initialize Firebase Auth
    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseUser = mFirebaseAuth.getCurrentUser();
    if (mFirebaseUser != null) {
      mPhotoUrl = mFirebaseUser.getPhotoUrl();
    }
    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

    // Initialize Firebase Measurement.
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    // Initialize Firebase Remote Config.
    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    // Define Firebase Remote Config Settings.
    FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build();

    // Define default config values. Defaults are used when fetched config values are not
    // available. Eg: if an error occurred fetching values from the server.
    Map<String, Object> defaultConfigMap = new HashMap<>();
    defaultConfigMap.put("friendly_msg_length", 10L);
    mSharedPref.edit().putBoolean(Constants.IS_NOTICATION, true).apply();
    // Apply config settings and default values.
    mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
    mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

    mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        mCurrentLatLng = getCurrentLatLng();
        if (dataSnapshot.hasChildren() && mCurrentLatLng != null) {
          DataSnapshot friendsSnapshot = dataSnapshot.child(MESSAGES_CHILD);
          for (DataSnapshot friendSnapshot : friendsSnapshot.getChildren()) {
            PojoLatlng p = friendSnapshot.getValue(PojoLatlng.class);
            System.out.println(p.lat + "" + p.lon);
            LatLng latlng = new LatLng(p.lat, p.lon);
            createMarker(latlng);
            long dist = Utils.calculateLatLngDistance(mCurrentLatLng, latlng);
            if (dist <= 100 && mSharedPref.getBoolean(Constants.IS_NOTICATION, false)) {
              mCustomNotificationManager.createNotifcation();
            }
          }
        }
      }

      @Override public void onCancelled(DatabaseError databaseError) {

      }
    });

    mCustomNotificationManager = new CustomNotificationManager(getApplicationContext());
  }

  private void showPermissionDialog() {
    if (!Utils.checkMapPermission(this)) {
      ActivityCompat.requestPermissions(this, new String[] {
          Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
      }, PERMISSION_LOCATION_REQUEST_CODE);
    }
  }

  //capture image from camera
  private void captureImage() {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
    // start the image capture Intent
    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // if the result is capturing Image
    if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        // successfully captured the image
        // display it in image view
        previewCapturedImage();
      } else if (resultCode == RESULT_CANCELED) {
        // user cancelled Image capture
        Toast.makeText(getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT)
            .show();
      } else {
        // failed to capture image
        Toast.makeText(getApplicationContext(), "Sorry! Failed to capture image",
            Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void previewCapturedImage() {
    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 8;
      final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  public Uri getOutputMediaFileUri(int type) {
    return Uri.fromFile(getOutputMediaFile(type));
  }

  //capturing and return image / video
  private static File getOutputMediaFile(int type) {

    File mediaStorageDir =
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            IMAGE_DIRECTORY_NAME);
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        return null;
      }
    }
    // Create a media file name
    String timeStamp =
        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE) {
      mediaFile =
          new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    } else {
      return null;
    }

    return mediaFile;
  }

  //method to create marker on map
  private void createMarker(LatLng point) {
    if (isDestroyed()) {
      return;
    }
    int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
    mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(mDotMarkerBitmap);
    Drawable shape = getResources().getDrawable(R.drawable.map_red_marker);
    shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
    shape.draw(canvas);
    if (mGoogleMap != null) {
      Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(point)
          .anchor(.5f, .5f)
          .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)));
    }
  }

  //verify whether user can store information on sd card
  public static void verifyStoragePermissions(Activity activity) {
    int permission =
        ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (permission != PackageManager.PERMISSION_GRANTED) {
      // We don't have permission so prompt the user
      ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }
  }

  //called when user presses back button
  @Override public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  @SuppressWarnings("StatementWithEmptyBody") @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.nav_settings) {
      Intent in = new Intent(this, SettingsActivity.class);
      startActivity(in);
    } else if (id == R.id.nav_logout) {
      signOut();
    } else if (id == R.id.nav_share) {
      shareUrl();
    } else if (id == R.id.feedback) {
      Intent in = new Intent(this, FeedBackActivity.class);
      startActivity(in);
    } else if (id == R.id.nav_send) {
      sendSOS();
    } else if (id == R.id.change_map) {
      changeMapStyle();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  //change styling of map satellite/ non satellite
  private void changeMapStyle() {
    mSatellite = 1 - mSatellite;
    if (mSatellite == 1) {
      mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    } else {
      mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    }
  }

  //share current lat long to friends
  private void sendSOS() {
    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
    if (mCurrentLatLng != null) {
      sendIntent.putExtra("sms_body",
          "Help Me : " + MAP + mCurrentLatLng.latitude + "," + mCurrentLatLng.longitude + MAP_2);
    } else {
      sendIntent.putExtra("sms_body", "Help Me");
    }
    sendIntent.setType("vnd.android-dir/mms-sms");
    startActivity(sendIntent);
  }

  //share app downloading path url to friends
  private void shareUrl() {
    try {
      Intent i = new Intent(Intent.ACTION_SEND);
      i.setType("text/plain");
      i.putExtra(Intent.EXTRA_SUBJECT, "RoadBump Application");
      String sAux = "\nLet me recommend you this application\n\n";
      sAux = sAux + "https://play.google.com/store/apps/details?id=com.google.android.gm&hl=en";
      i.putExtra(Intent.EXTRA_TEXT, sAux);
      startActivity(Intent.createChooser(i, "choose one"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override protected void onStart() {
    super.onStart();
  }

  protected void onPause() {
    super.onPause();
  }

  protected void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter(SamplingService.SPEED));
    getWeather();
  }

  @Override protected void onStop() {
    super.onStop();
  }

  //check sampling service is running or not.
  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
        Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  //initialise map
  private void initMap() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.map_container, mMapFragment)
        .commit();
  }

  // sign out from Google api
  private void signOut() {
    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
      @Override public void onResult(Status status) {
        finishActivity();
      }
    });
  }

  // finish the main_activity go back to signinactivity
  private void finishActivity() {
    mSharedPref.edit().putBoolean("issignin", false).apply();
    Intent in = new Intent(this, SignInActivity.class);
    startActivity(in);
    finish();
  }

  @Override public void onConnectionFailed(ConnectionResult connectionResult) {
    Log.d(TAG, "onConnectionFailed:" + connectionResult);
  }

  // update ui for name, email, photo url.
  private void updateUI(boolean signedIn) {

    if (signedIn) {

      nameView.setText(mName);
      emailView.setText(mEmail);
      Picasso.with(getApplicationContext()).load(mPhotoUrl).into(photoView);
    } else {
      nameView.setText("name");
      emailView.setText("email@email.com");
    }
  }

  //callback from map is ready
  @Override public void onMapReady(GoogleMap googleMap) {
    Log.d(TAG, "OnMapReady");
    mGoogleMap = googleMap;
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    Criteria criteria = new Criteria();

    Utils.checkMapPermission(getApplicationContext());
    Location location =
        locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
    if (location != null) {
      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
          new LatLng(location.getLatitude(), location.getLongitude()), INIT_ZOOM));
    }
    paintMap();
  }

  private void paintMap() {
    GregorianCalendar gcal = new GregorianCalendar();
    String fileName = "speedbump_" +
        gcal.get(Calendar.YEAR) +
        "_" +
        Integer.toString(gcal.get(Calendar.MONTH) + 1) +
        ".csv";
    File file = getAlbumStorageDir("speedbump");
    File captureFileName = new File(file, fileName);
    BufferedReader buffreader = null;
    try {
      buffreader = new BufferedReader(new FileReader(captureFileName));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    String line = null;
    try {
      while ((line = buffreader.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(line, ",");
        if (st.countTokens() == 7) {
          String s = st.nextToken();

          if (!"sample".equals(s)) {
            throw new NoSuchElementException("not 'sample'");
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        buffreader.close();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // get sd card pictures directory
  public File getAlbumStorageDir(String albumName) {
    // Get the directory for the user's public pictures directory.
    File file =
        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            albumName);
    if (!file.mkdirs()) {
      Log.e(LOG_TAG, "Directory not created");
    }
    return file;
  }

  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  @Override public void afterTextChanged(Editable s) {

  }

  //Get weather gives the current weather in Celsius from worldweatheronline.com
  private void getWeather() {
    mCurrentLatLng = getCurrentLatLng();
    if (mCurrentLatLng != null) {
      String url = Constants.BASE_URL
          + Constants.API_KEY
          + mCurrentLatLng.latitude
          + ","
          + mCurrentLatLng.longitude
          + FINAL_STRING;
      requestQue = Volley.newRequestQueue(this);
      JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, (String) null,
          new Response.Listener<JSONObject>() {
            @Override public void onResponse(JSONObject response) {
              JSONObject res = ((JSONObject) response);
              try {
                JSONObject obj = (JSONObject) res.get("data");
                JSONArray arr = (JSONArray) obj.get("current_condition");
                String temp = (String) ((JSONObject) arr.get(0)).get("temp_C");
                Log.d(TAG, obj.toString());
                tempView.setText(temp + " \'C");
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }, new Response.ErrorListener() {
        @Override public void onErrorResponse(VolleyError error) {
          Log.d(TAG, error.toString());
        }
      });

      requestQue.add(jor);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
  }

  @Override public void onErrorResponse(VolleyError error) {
    Log.d(TAG, error.toString());
  }

  @Override public void onResponse(Object response) {
    JSONObject res = ((JSONObject) response);
    try {
      JSONObject obj = (JSONObject) res.get("data");
      Log.d(TAG, obj.toString());
      tempView.setText("");
    } catch (Exception e) {

    }
  }

  //Background task class for get the place lat long using place string
  private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

    private final Context mContext;

    public GeocoderTask(Context context) {
      mContext = context;
    }

    @Override protected List<Address> doInBackground(String... locationName) {
      Geocoder geocoder = new Geocoder(mContext);
      List<Address> addresses = null;

      try {
        // Getting a maximum of 3 Address that matches the input text
        addresses = geocoder.getFromLocationName(locationName[0], 3);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return addresses;
    }

    @Override protected void onPostExecute(List<Address> addresses) {

      if (addresses == null || addresses.size() == 0) {
        Toast.makeText(mContext, "No Location found", Toast.LENGTH_SHORT).show();
        return;
      }
      Address address = (Address) addresses.get(0);

      // Creating an instance of GeoPoint, to display in Google Map
      LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
      mDestinationLatLng = latLng;
      String addressText = String.format("%s, %s",
          address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
          address.getCountryName());
      mCurrentLatLng = getCurrentLatLng();
      if (mCurrentLatLng == null || mDestinationLatLng == null) {
        Toast.makeText(getApplicationContext(), "GPS Issue", Toast.LENGTH_SHORT).show();
        return;
      }
      String url = getDirectionsUrl(mCurrentLatLng, mDestinationLatLng);
      DownloadTask downloadTask = new DownloadTask();
      // Start downloading json data from Google Directions API
      downloadTask.execute(url);

      MarkerOptions markerOptions = new MarkerOptions();
      markerOptions.position(latLng);
      markerOptions.title(addressText);

      Marker marker = mGoogleMap.addMarker(markerOptions);
      for (Marker marker1 : mMarkerArrayList) {
        marker1.remove();
      }
      mMarkerArrayList.add(marker);
      mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }
  }

  //Download task is background thread class for downloading the path json from source to destination
  private class DownloadTask extends AsyncTask<String, Void, String> {

    private String downloadUrl(String strUrl) throws IOException {
      String data = "";
      InputStream iStream = null;
      HttpURLConnection urlConnection = null;
      try {
        URL url = new URL(strUrl);
        // Creating an http connection to communicate with url
        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.connect();
        iStream = urlConnection.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
        StringBuffer sb = new StringBuffer();
        String line = "";
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }
        data = sb.toString();
        br.close();
      } catch (Exception e) {
        Log.d("Exception created", e.toString());
      } finally {
        if (iStream != null) {
          iStream.close();
        }
        urlConnection.disconnect();
      }
      return data;
    }

    // Downloading data in background thread
    @Override protected String doInBackground(String... url) {
      // For storing data from web service
      String data = "";

      try {
        data = downloadUrl(url[0]);
      } catch (Exception e) {
        Log.d("Background Task", e.toString());
      }
      return data;
    }

    // Executes in UI thread, after the execution of
    // doInBackground()
    @Override protected void onPostExecute(String result) {
      super.onPostExecute(result);
      ParserTask parserTask = new ParserTask();
      parserTask.execute(result);
    }
  }

  /** Parsing Google Places in JSON format */
  private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    @Override protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

      JSONObject jObject;
      List<List<HashMap<String, String>>> routes = null;

      try {
        jObject = new JSONObject(jsonData[0]);
        DirectionsJSONParser parser = new DirectionsJSONParser();
        // Starts parsing data
        routes = parser.parse(jObject);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override protected void onPostExecute(List<List<HashMap<String, String>>> result) {
      ArrayList<LatLng> points = null;
      PolylineOptions lineOptions = null;

      for (int i = 0; i < result.size(); i++) {
        points = new ArrayList<LatLng>();
        lineOptions = new PolylineOptions();

        List<HashMap<String, String>> path = result.get(i);
        // Fetching all the points in i-th route
        for (int j = 0; j < path.size(); j++) {
          HashMap<String, String> point = path.get(j);

          double lat = Double.parseDouble(point.get("lat"));
          double lng = Double.parseDouble(point.get("lng"));
          LatLng position = new LatLng(lat, lng);

          points.add(position);
        }

        lineOptions.addAll(points);
        lineOptions.width(4);
        lineOptions.color(Color.BLUE);
      }
      if (mPolyline != null) {
        mPolyline.remove();
      }
      if (lineOptions != null) {
        mPolyline = mGoogleMap.addPolyline(lineOptions);
      }
    }
  }

  //Creating url for maps to get the direction between two lat long.
  private String getDirectionsUrl(LatLng origin, LatLng dest) {

    String strorigin = "origin=" + origin.latitude + "," + origin.longitude;

    String strdest = "destination=" + dest.latitude + "," + dest.longitude;
    String sensor = "sensor=false";
    String parameters = strorigin + "&" + strdest + "&" + sensor;
    String toll = "&avoid=tolls";
    String output = "json";
    String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    if (mSharedPref.getBoolean(Constants.AVOID_TOLL, false)) {
      url = url + toll;
    }
    return url;
  }

  // Gives the current lat long , with location manager using some other application data
  private LatLng getCurrentLatLng() {
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    Criteria criteria = new Criteria();

    Utils.checkMapPermission(getApplicationContext());
    Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    if (location != null) {
      mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
    return mCurrentLatLng;
  }
}