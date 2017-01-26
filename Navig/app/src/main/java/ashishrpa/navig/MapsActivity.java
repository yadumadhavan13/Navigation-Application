package ashishrpa.navig;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.speech.RecognizerIntent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionFinderListener ,View.OnClickListener {
//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener ,View.OnClickListener {
    private GoogleMap mMap;
    private Button btnFindPath,btnSearchLocation;
    private ImageButton imageButtonOrigin, imageButtonDestination;
    private ToggleButton toggleButton;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    protected static final int RESULT_SPEECH_ORIGIN = 1;
    protected static final int RESULT_SPEECH_DESTINATION = 2;
    private final int NORMAL = 0;
    private final int SEARCH_OPTION = 0;
    private final int PATH_OPTION = 1;
    private int flag=SEARCH_OPTION;
    public final static File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/temproute.png");
    public final static String EXTRA_MESSAGE = myFile.toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_maps);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //mapFragment.setHasOptionsMenu(true);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        btnSearchLocation = (Button) findViewById(R.id.btnSearchLocation);
        toggleButton =(ToggleButton) findViewById(R.id.toggleButton);

        imageButtonOrigin =(ImageButton) findViewById(R.id.ib_origin);
        imageButtonDestination =(ImageButton) findViewById(R.id.ib_destination);

        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        btnFindPath.setOnClickListener(this);
        btnSearchLocation.setOnClickListener(this);
        toggleButton.setOnClickListener(this);
        imageButtonOrigin.setOnClickListener(this);
        imageButtonDestination.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                Toast.makeText(MapsActivity.this, "Save map Selected", Toast.LENGTH_SHORT).show();
                saveTemporaryMapImage();
                Toast.makeText(MapsActivity.this, "map Save", Toast.LENGTH_LONG).show();
                Log.e("MapsActivity", "map Save Selected");
                return true;
            }
            case R.id.show: {
                Toast.makeText(MapsActivity.this, "Show map Selected", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ShowActivity.class);
                String message = myFile.toString();
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);

                return true;
            }
            case R.id.help: {
                Toast.makeText(MapsActivity.this, "Help icon Selected", Toast.LENGTH_LONG).show();
                Intent intentHelp = new Intent(this, AboutActivity.class);
                Log.e("MapsActivity", "Help icon Selected");
                startActivity(intentHelp);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnFindPath:
                Toast.makeText(getApplicationContext(),"Path Clicked",Toast.LENGTH_SHORT).show();
                flag = PATH_OPTION;
                sendRequest();

                break;
            case R.id.btnSearchLocation:
                Toast.makeText(getApplicationContext(),"Search Clicked",Toast.LENGTH_SHORT).show();
                flag = SEARCH_OPTION;
                searchLocation();
                break;
            case R.id.toggleButton:
                Toast.makeText(getApplicationContext(),"Terrain Clicked",Toast.LENGTH_SHORT).show();
                setTerrainForMap();
                break;
            case R.id.ib_origin:
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH_ORIGIN);
                    etOrigin.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
                Toast.makeText(getApplicationContext(),"origin Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.ib_destination:
                Intent intent2 = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent2.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent2, RESULT_SPEECH_DESTINATION);
                    etDestination.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
                Toast.makeText(getApplicationContext(),"destination Clicked",Toast.LENGTH_SHORT).show();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH_ORIGIN: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    etOrigin.setText(text.get(0));
                }
                break;
            }
            case RESULT_SPEECH_DESTINATION: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    etDestination.setText(text.get(0));
                }
                break;
            }
        }
    }

    private void sendRequest() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder((DirectionFinderListener) this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void searchLocation(){
        String searchText = etOrigin.getText().toString();
        List<Address> addressList =null;
        if(searchText!=null||!searchText.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(searchText,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            if(toggleButton.isChecked()){
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }else {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }

        }
    }

    private void setTerrainForMap(){
        if(toggleButton.isChecked()){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
//        if(TERRAIN_TYPE == NORMAL){
//            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        }if(TERRAIN_TYPE == SATELLAITE){
//            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        }if(TERRAIN_TYPE == TERRAIN){
//            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//        }else {
//            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        }
        if(flag==PATH_OPTION){
            sendRequest();
        }else if(flag==SEARCH_OPTION){
            searchLocation();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng ernakulam = new LatLng(10.024638, 76.329932);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ernakulam, 18));
        originMarkers.add(mMap.addMarker(new MarkerOptions()
                .title("Ernakulam Kakkanadu")
                .position(ernakulam)));

        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    public void saveTemporaryMapImage(){
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream(myFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Log.e("STMI ","File Saved Successfully");
                } catch (Exception e) {
                    Log.e("STMI ","File NOT Saved");
                    e.printStackTrace();
                }
            }
        };

        mMap.snapshot(callback);

    }


}
