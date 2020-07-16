package com.example.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class MyService extends Service {

    boolean started = false;
    FusedLocationProviderClient client;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    String folderLocation;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        Log.d("Service","Service created");
        client = LocationServices.getFusedLocationProviderClient(this);

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";

        final File folder = new File(folderLocation);
        if (folder.exists() == false){
            boolean result = folder.mkdir();
            if (result == true){
                Log.d("File Read/Write","Folder created");
            }
        }

        //configurations, how soon, how often
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setSmallestDisplacement(100);


        //define action, once there's a new Location reading
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double lng = data.getLongitude();
                    String msg = lat  + "," + lng;

                    File targetFile = new File(folderLocation, "data.txt");

                    try {
                        FileWriter writer = new FileWriter(targetFile, true);
                        writer.write(msg + "\n");
                        writer.flush();
                        writer.close();

                    } catch (Exception e){
                        Toast.makeText(MyService.this, "Failed to write", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            };
        };
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false) {
            started = true;
            if (checkPermission() == true){
                client.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
            }
            Log.d("Service", "Service started");
            Toast.makeText(this, "Service running", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("Service","Service is still running");
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        client.removeLocationUpdates(mLocationCallback);
        Log.d("Service","Service exited");
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private boolean checkPermission(){
        int permissionCheck_Course = ContextCompat.checkSelfPermission(MyService.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(MyService.this,Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Course == PermissionChecker.PERMISSION_GRANTED ||
                permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED){
            return true;
        }else {
            Toast.makeText(this, "Error no permission", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
