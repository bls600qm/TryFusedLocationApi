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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;

import static jp.rika.sumitomo.tryfusedlocationapi.R.id.map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener,GoogleMap.OnInfoWindowCloseListener,
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
    private static double latitudeA;
    private static double longitudeA;
    private static double latitudeB;
    private static double longitudeB;
    private static double latitudeC;
    private static double longitudeC;



    Exif exifclass =new Exif();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // LocationRequest を生成して精度、インターバルを設定
        locationRequest = LocationRequest.create();

        // 測位の精度、消費電力の優先度
        locationPriority = priority[1];

        if (locationPriority == priority[0]) {
            // 位置情報の精度を優先する場合
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(16);
        } else if (locationPriority == priority[1]) {
            // 消費電力を考慮する場合
            locationRequest.setPriority(locationPriority);
            locationRequest.setInterval(60000);
            locationRequest.setFastestInterval(16);
        } else if (locationPriority == priority[2]) {
            // "city" level accuracy
            locationRequest.setPriority(locationPriority);
        } else {
            // 外部からのトリガーでの測位のみ
            locationRequest.setPriority(locationPriority);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //assetsフォルダの写真からexif取得
        try {

            InputStream stream_inA = this.getResources().getAssets().open("sample.JPG");

            ExifInterface exifA = new ExifInterface(stream_inA);

            if (exifA != null) {
                String latitude = exifA.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//緯度
                String latitudeRef = exifA.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);//北緯or南緯
                String longitude = exifA.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//経度
                String longitudeRef = exifA.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);//東経or西経

                //時,分,秒（60進数)
                Log.d("exif", "latitudeA : " + latitude);
                Log.d("exif", "latitudeRefA : " + latitudeRef);
                Log.d("exif", "longitudeA : " + longitude);
                Log.d("exif", "longitudeRefA : " + longitudeRef);

                //10進数に変換
                latitudeA = exifclass.ExifHourMinSecToDegreesLatitude(latitude);
                longitudeA = exifclass.ExifHourMinSecToDegreesLongitude(longitude);

                Log.d("DegreeExif", "latitude : " + exifclass.ExifLatitudeToDegrees(latitudeRef, latitude));
                Log.d("DegreeExif", "longitude : " + exifclass.ExifLongitudeToDegrees(longitudeRef, longitude));

            }
        } catch (IOException e) {
            Log.d("exif", "exifA is null");
            e.printStackTrace();
        }

        //assetsフォルダの写真からexif取得
        try {
            InputStream stream_inB = this.getResources().getAssets().open("nissay.JPG");
            ExifInterface exifB = new ExifInterface(stream_inB);

            if (exifB != null) {
                String latitude = exifB.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//緯度
                String latitudeRef = exifB.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);//北緯or南緯
                String longitude = exifB.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//経度
                String longitudeRef= exifB.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);//東経or西経

                //時,分,秒（60進数)
                Log.d("exif", "latitudeB : " + latitude);
                Log.d("exif", "latitudeRefB : " + latitudeRef);
                Log.d("exif", "longitudeB : " + longitude);
                Log.d("exif", "longitudeRefB : " + longitudeRef);

                //10進数に変換
                latitudeB = exifclass.ExifHourMinSecToDegreesLatitude(latitude);
                longitudeB = exifclass.ExifHourMinSecToDegreesLongitude(longitude);

                Log.d("DegreeExif", "latitudeB : " + exifclass.ExifLatitudeToDegrees(latitudeRef, latitude));
                Log.d("DegreeExif", "longitudeB : " + exifclass.ExifLongitudeToDegrees(longitudeRef, longitude));

                final LatLng photoB = new LatLng(latitudeB,longitudeB);

            }
        } catch (IOException e) {
            Log.d("exif", "exifB is null");
            e.printStackTrace();
        }
        try {
            InputStream stream_inC = this.getResources().getAssets().open("tonda.JPG");
            ExifInterface exifC = new ExifInterface(stream_inC);

            if (exifC != null) {
                String latitude = exifC.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//緯度
                String latitudeRef = exifC.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);//北緯or南緯
                String longitude = exifC.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//経度
                String longitudeRef= exifC.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);//東経or西経

                //時,分,秒（60進数)
                Log.d("exif", "latitudeC : " + latitude);
                Log.d("exif", "latitudeRefC : " + latitudeRef);
                Log.d("exif", "longitudeC : " + longitude);
                Log.d("exif", "longitudeRefC : " + longitudeRef);

                //10進数に変換
                latitudeC = exifclass.ExifHourMinSecToDegreesLatitude(latitude);
                longitudeC = exifclass.ExifHourMinSecToDegreesLongitude(longitude);

                Log.d("DegreeExif", "latitudeC : " + exifclass.ExifLatitudeToDegrees(latitudeRef, latitude));
                Log.d("DegreeExif", "longitudeC : " + exifclass.ExifLongitudeToDegrees(longitudeRef, longitude));


            }
        } catch (IOException e) {
            Log.d("exif", "exifB is null");
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
            Log.d("debug", "permission grantedです!(1)");

            mMap = googleMap;
            mMap.setLocationSource(this);
            mMap.setMyLocationEnabled(true);//右上の現在位置アイコンの表示
            mMap.setOnMyLocationButtonClickListener(this);

            setMarker();//写真のマーカーをセット

            mMap.setOnInfoWindowClickListener(this);//マーカ情報をタップしたら写真出す
            mMap.setOnInfoWindowCloseListener(this);

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

        }else{
            Log.d("debug","onLocationChangedない");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission grantedです!(2)");

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

    private Marker mPhotoA;
    private Marker mPhotoB;
    private Marker mPhotoC;



    //写真の位置情報マーカー
    private void setMarker(){


        //マーカーを追加
        LatLng markerPosA = new LatLng(latitudeA,longitudeA);
        LatLng markerPosB = new LatLng(latitudeB,longitudeB);
        LatLng markerPosC = new LatLng(latitudeC,longitudeC);

        mPhotoA = mMap.addMarker(new MarkerOptions()
                .position(markerPosA)
                .title("ラピュタ")
                .snippet("sampleですよ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

        mPhotoB = mMap.addMarker(new MarkerOptions()
                .position(markerPosB)
                        .title("スタバ")
                        .snippet("新大阪ニッセイビル")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        mPhotoC = mMap.addMarker(new MarkerOptions()
                .position(markerPosC)
                .title("とんだ駅")
                .snippet("necafee")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosA, 10));//10:都市 //15:街路

        /*
        MarkerOptions optionsA = new MarkerOptions();
        MarkerOptions optionsB = new MarkerOptions();
        MarkerOptions optionsC = new MarkerOptions();
        optionsA.position(markerPosA)
                .title("ラピュタ")
                .snippet("sampleですよ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
              //  .icon(BitmapDescriptorFactory.fromResource(R.drawable.sample));
        optionsB.position(markerPosB)
                .title("スタバ")
                .snippet("新大阪ニッセイビル")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        optionsC.position(markerPosC)
                .title("とんだ駅")
                .snippet("necafee")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));




        mMap.addMarker(optionsA);
        mMap.addMarker(optionsB);
        mMap.addMarker(optionsC);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosA, 10));//10:都市 //15:街路
        */




    }

    //写真マーカ情報をタップした時の挙動
    @Override
    public void onInfoWindowClick(Marker marker) {


        // Use the equals() method on a Marker to check for equals.  Do not use ==.
        if (marker.equals(mPhotoA)) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.sample));
            Toast.makeText(this, "サンプル１の画像",
                    Toast.LENGTH_SHORT).show();
        } else if (marker.equals(mPhotoB)) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.nissay));
            Toast.makeText(this, "新大阪ニッセイビルの画像",
                    Toast.LENGTH_SHORT).show();
        }else if (marker.equals(mPhotoC)) {
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.tonda));
            Toast.makeText(this, "とんだ駅の画像",
                    Toast.LENGTH_SHORT).show();
        }


    }
    @Override
    public void onInfoWindowClose(Marker marker) {
        Toast.makeText(this, "Close Info Window", Toast.LENGTH_SHORT).show();
        mMap.clear();
        setMarker();



    }



}