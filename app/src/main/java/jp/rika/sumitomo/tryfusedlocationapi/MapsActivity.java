package jp.rika.sumitomo.tryfusedlocationapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, LocationSource {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    private OnLocationChangedListener onLocationChangedListener = null;

    private int priority[] = {LocationRequest.PRIORITY_HIGH_ACCURACY, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER, LocationRequest.PRIORITY_NO_POWER};
    private int locationPriority;

    //exif
    private double latitudeA;
    private double longitudeA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // LocationRequest を生成して精度、インターバルを設定
        locationRequest = LocationRequest.create();

        // 測位の精度、消費電力の優先度
        locationPriority = priority[1];

        if(locationPriority == priority[0]){
            // 位置情報の精度を優先する場合
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(16);
        }
        else if(locationPriority == priority[1]){
            // 消費電力を考慮する場合
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(16);
        }
        else if(locationPriority == priority[2]){
            // "city" level accuracy
            locationRequest.setPriority(locationPriority);
        }
        else{
            // 外部からのトリガーでの測位のみ
            locationRequest.setPriority(locationPriority);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //assetsフォルダの写真からexif取得
        try {
            InputStream stream_in = this.getResources().getAssets().open("sample.JPG");
            ExifInterface exif = new ExifInterface(stream_in);

            if (exif != null) {
                String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//緯度
                String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);//北緯or南緯
                String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//経度
                String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);//東経or西経

                //時,分,秒（60進数)
                Log.d("exif", "latitude : " + latitude);
                Log.d("exif", "latitudeRef : " + latitudeRef);
                Log.d("exif", "longitude : " + longitude);
                Log.d("exif", "longitudeRef : " + longitudeRef);

                //10進数に変換
                latitudeA = ExifHourMinSecToDegreesLatitude(latitude);
                longitudeA = ExifHourMinSecToDegreesLongitude(longitude);

                Log.d("DegreeExif","latitude : " + ExifLatitudeToDegrees(latitudeRef,latitude));
                Log.d("DegreeExif","longitude : " + ExifLongitudeToDegrees(longitudeRef,longitude));

            }
        } catch (IOException e) {
            Log.d("exif","exif is null");
            e.printStackTrace();
        }
    }

    // onResumeフェーズに入ったら接続
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    // onPauseで切断
    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted１");

            mMap = googleMap;
            mMap.setLocationSource(this);
            mMap.setMyLocationEnabled(true);//右上の現在位置アイコンの表示
            mMap.setOnMyLocationButtonClickListener(this);
            //写真のマーカーをセット
            setMarker();
        }
        else{
            Log.d("debug", "permission error");
            return;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("debug","onLocationChanged");
        if (onLocationChangedListener != null) {
            onLocationChangedListener.onLocationChanged(location);

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Log.d("debug","location="+lat+","+lng);

            Toast.makeText(this, "location="+lat+","+lng, Toast.LENGTH_SHORT).show();

            // Add a marker and move the camera
            LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(newLocation).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted２");

            // FusedLocationApi
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        }
        else{
            Log.d("debug", "permission error");
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("debug", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("debug", "onConnectionFailed");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "onMyLocationButtonClick", Toast.LENGTH_SHORT).show();

        return false;
    }

    // OnLocationChangedListener calls activate() method
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        this.onLocationChangedListener = null;
    }



    //写真の位置情報マーカー
    private void setMarker(){
        //マーカーを追加
        LatLng markerPos = new LatLng(latitudeA,longitudeA);
        MarkerOptions options = new MarkerOptions();
        options.position(markerPos)
                .title("Exif")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.sample));


        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPos, 10));//10:都市 //15:街路

    }

    //以下Exif情報の取得変換 ****************************************************
    private double ExifHourMinSecToDegreesLatitude(String latitudE) {
        String hourminsec[] = latitudE.split(",");
        String hour[] = hourminsec[0].split("/");
        String min[] = hourminsec[1].split("/");
        String sec[] = hourminsec[2].split("/");
        double dhour = (double)Integer.parseInt(hour[0]) / (double)Integer.parseInt(hour[1]);
        double dmin = (double)Integer.parseInt(min[0]) / (double)Integer.parseInt(min[1]);
        double dsec = (double)Integer.parseInt(sec[0]) / (double)Integer.parseInt(sec[1]);
        double degrees = dhour + dmin / 60.0 + dsec / 3600.0;
        return degrees;
    }

    private double ExifHourMinSecToDegreesLongitude(String longitudE) {
        String hourminsec[] = longitudE.split(",");
        String hour[] = hourminsec[0].split("/");
        String min[] = hourminsec[1].split("/");
        String sec[] = hourminsec[2].split("/");
        double dhour = (double)Integer.parseInt(hour[0]) / (double)Integer.parseInt(hour[1]);
        double dmin = (double)Integer.parseInt(min[0]) / (double)Integer.parseInt(min[1]);
        double dsec = (double)Integer.parseInt(sec[0]) / (double)Integer.parseInt(sec[1]);
        double degrees = dhour + dmin / 60.0 + dsec / 3600.0;
        return degrees;
    }

    private String ExifLatitudeToDegrees(String ref, String latitudE) {
        String answer = String.valueOf(ref.equals("S") ? -1.0 : 1.0 * ExifHourMinSecToDegreesLatitude(latitudE));
        return answer;
    }

    private String ExifLongitudeToDegrees(String ref, String longitudE) {
        String answer = String.valueOf(ref.equals("W") ? -1.0 : 1.0 * ExifHourMinSecToDegreesLongitude(longitudE));
        return answer;
    }
}