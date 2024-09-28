/*
 * aTrainingTracker (ANT+ BTLE)
 * Copyright (C) 2011 - 2019 Rainer Blind <rainer.blind@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0
 */

package com.atrainingtracker.trainingtracker.activities;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.atrainingtracker.R;
import com.atrainingtracker.banalservice.ActivityType;
import com.atrainingtracker.banalservice.BANALService;
import com.atrainingtracker.banalservice.devices.DeviceType;
import com.atrainingtracker.banalservice.Protocol;
import com.atrainingtracker.banalservice.database.DevicesDatabaseManager;
import com.atrainingtracker.banalservice.dialogs.InstallANTShitDialog;
import com.atrainingtracker.banalservice.filters.FilterData;
import com.atrainingtracker.banalservice.fragments.DeviceTypeChoiceFragment;
import com.atrainingtracker.banalservice.fragments.EditDeviceDialogFragment;
import com.atrainingtracker.banalservice.fragments.RemoteDevicesFragment;
import com.atrainingtracker.banalservice.fragments.RemoteDevicesFragmentTabbedContainer;
import com.atrainingtracker.banalservice.fragments.SportTypeListFragment;
import com.atrainingtracker.banalservice.helpers.BatteryStatusHelper;
import com.atrainingtracker.trainingtracker.exporter.ExportManager;
import com.atrainingtracker.trainingtracker.exporter.ExportWorkoutIntentService;
import com.atrainingtracker.trainingtracker.exporter.FileFormat;
import com.atrainingtracker.trainingtracker.TrainingApplication;
import com.atrainingtracker.trainingtracker.database.TrackingViewsDatabaseManager;
import com.atrainingtracker.trainingtracker.dialogs.DeleteOldWorkoutsDialog;
import com.atrainingtracker.trainingtracker.dialogs.EnableBluetoothDialog;
import com.atrainingtracker.trainingtracker.dialogs.GPSDisabledDialog;
import com.atrainingtracker.trainingtracker.dialogs.ReallyDeleteWorkoutDialog;
import com.atrainingtracker.trainingtracker.dialogs.StartOrResumeDialog;
import com.atrainingtracker.trainingtracker.fragments.ExportStatusDialogFragment;
import com.atrainingtracker.trainingtracker.fragments.StartAndTrackingFragmentTabbedContainer;
import com.atrainingtracker.trainingtracker.fragments.WorkoutSummariesWithMapListFragment;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.MyLocationsFragment;
import com.atrainingtracker.trainingtracker.fragments.mapFragments.TrackOnMapTrackingFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.AltitudeCorrectionFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.CloudUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.DisplayFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.EmailUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.FancyWorkoutNameListFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.FileExportFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.LocationSourcesFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.PebbleScreenFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.RootPrefsFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.RunkeeperUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.SearchFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.StartSearchFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.StravaUploadFragment;
import com.atrainingtracker.trainingtracker.fragments.preferences.TrainingpeaksUploadFragment;
import com.atrainingtracker.trainingtracker.helpers.DeleteWorkoutTask;
import com.atrainingtracker.trainingtracker.interfaces.ReallyDeleteDialogInterface;
import com.atrainingtracker.trainingtracker.interfaces.RemoteDevicesSettingsInterface;
import com.atrainingtracker.trainingtracker.interfaces.ShowWorkoutDetailsInterface;
import com.atrainingtracker.trainingtracker.interfaces.StartOrResumeInterface;
import com.atrainingtracker.trainingtracker.onlinecommunities.strava.StravaGetAccessTokenActivity;
import com.atrainingtracker.trainingtracker.segments.SegmentsDatabaseManager;
import com.atrainingtracker.trainingtracker.segments.StarredSegmentsListFragment;
import com.atrainingtracker.trainingtracker.segments.StarredSegmentsTabbedContainer;
import com.dropbox.core.android.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.List;

// import android.support.v7.app.AlertDialog;


/**
 * Created by rainer on 14.01.16.
 */
public class MainActivityWithNavigation
        extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        DeviceTypeChoiceFragment.OnDeviceTypeSelectedListener,
        RemoteDevicesFragment.OnRemoteDeviceSelectedListener,
        RemoteDevicesSettingsInterface,
        ShowWorkoutDetailsInterface,
        BANALService.GetBanalServiceInterface,
        ReallyDeleteDialogInterface,
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        StartAndTrackingFragmentTabbedContainer.UpdateActivityTypeInterface,
        StarredSegmentsListFragment.StartSegmentDetailsActivityInterface,
        StartOrResumeInterface {
    private static final String BROKER_URL = "tcp://101.119.143.227";
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(MainActivityWithNavigation.class);
    private MqttHandler mqttHandler;
    private static final String CLIENT_ID = "client_id";
    public static final String SELECTED_FRAGMENT_ID = "SELECTED_FRAGMENT_ID";
    public static final String SELECTED_FRAGMENT = "SELECTED_FRAGMENT";
    private static final String TAG = "MainActivityNavigation";
    private static final boolean DEBUG = TrainingApplication.DEBUG && false;
    private static final int DEFAULT_SELECTED_FRAGMENT_ID = R.id.drawer_start_tracking;
    // private static final int REQUEST_ENABLE_BLUETOOTH            = 1;
    private static final int REQUEST_INSTALL_GOOGLE_PLAY_SERVICE = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    // private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE       = 3;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_AND_WRITE_EXTERNAL_STORAGE = 4;
    private static final long WAITING_TIME_BEFORE_DISCONNECTING = 5 * 60 * 1000; // 5 min
    private static final int CRITICAL_BATTERY_LEVEL = 30;
    private LocationRequest locationRequest;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    protected TrainingApplication mTrainingApplication;
    // remember which fragment should be shown
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    protected int mSelectedFragmentId = DEFAULT_SELECTED_FRAGMENT_ID;
    // the views
    protected DrawerLayout mDrawerLayout;
    protected NavigationView mNavigationView;
    protected MenuItem mPreviousMenuItem;
    protected Fragment mFragment;
    protected Handler mHandler;  // necessary to wait some time before we disconnect from the BANALService when the app is paused.
    protected boolean mStartAndNotResume = true;        // start a new workout or continue with the previous one
    protected BANALService.BANALServiceComm mBanalServiceComm = null;
    LinkedList<ConnectionStatusListener> mConnectionStatusListeners = new LinkedList<>();
    private LocationManager locationManager;
    /* Broadcast Receiver to adapt the title based on the tracking state */
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    BroadcastReceiver mStartTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.Tracking);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Tracking);
        }
    };

    // protected ActivityType mActivityType = ActivityType.GENERIC;  // no longer necessary since we have the getActivity() method
    // protected long mWorkoutID = -1;
    BroadcastReceiver mPauseTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.Paused);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Pause);
        }
    };
    BroadcastReceiver mStopTrackingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTitle(R.string.app_name);
            mNavigationView.getMenu().findItem(R.id.drawer_start_tracking).setTitle(R.string.Start);

            checkBatteryStatus();
        }
    };
    private IntentFilter mStartTrackingFilter;
    private boolean mAlreadyTriedToRequestDropboxToken = false;
    // class BANALConnection implements ServiceConnection
    private ServiceConnection mBanalConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Log.i(TAG, "onServiceConnected");

            mBanalServiceComm = (BANALService.BANALServiceComm) service; // IBANALService.Stub.asInterface(service);

            // create all the filters
            for (FilterData filterData : TrackingViewsDatabaseManager.getAllFilterData()) {
                mBanalServiceComm.createFilter(filterData);
            }

            // inform listeners
            for (ConnectionStatusListener connectionStatusListener : mConnectionStatusListeners) {
                connectionStatusListener.connectedToBanalService();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Log.i(TAG, "onServiceDisconnected");

            mBanalServiceComm = null;

            // inform listeners
            for (ConnectionStatusListener connectionStatusListener : mConnectionStatusListeners) {
                connectionStatusListener.disconnectedFromBanalService();
            }
        }
    };
    protected Runnable mDisconnectFromBANALServiceRunnable = new Runnable() {
        @Override
        public void run() {
            disconnectFromBANALService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        // some initialization
        mTrainingApplication = (TrainingApplication) getApplication();
        mHandler = new Handler();

        mStartTrackingFilter = new IntentFilter(TrainingApplication.REQUEST_START_TRACKING);
        mStartTrackingFilter.addAction(TrainingApplication.REQUEST_RESUME_FROM_PAUSED);

        // now, create the UI
        setContentView(R.layout.main_activity_with_navigation);

        Toolbar toolbar = findViewById(R.id.apps_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar supportAB = getSupportActionBar();
        // supportAB.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        supportAB.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.TrainingTracker, R.string.TrainingTracker);
        actionBarDrawerToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setItemIconTintList(null);  // avoid converting the icons to black and white or gray and white
        mNavigationView.setNavigationItemSelectedListener(this);

        if (!BANALService.isProtocolSupported(this, Protocol.BLUETOOTH_LE)) {
            MenuItem menuItem = mNavigationView.getMenu().findItem(R.id.drawer_pairing_BTLE);
            menuItem.setEnabled(false);
            menuItem.setCheckable(false);
        }

        // getPermissions
        if (!TrainingApplication.havePermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && !TrainingApplication.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_AND_WRITE_EXTERNAL_STORAGE);
        }
        if (!TrainingApplication.havePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        if (!TrainingApplication.havePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        // check ANT+ installation
        if (TrainingApplication.checkANTInstallation() && BANALService.isANTProperlyInstalled(this)) {
            showInstallANTShitDialog();
        }


        if (savedInstanceState != null) {
            mSelectedFragmentId = savedInstanceState.getInt(SELECTED_FRAGMENT_ID, DEFAULT_SELECTED_FRAGMENT_ID);
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, "mFragment");
        } else {
            if (getIntent().hasExtra(SELECTED_FRAGMENT)) {
                switch (SelectedFragment.valueOf(getIntent().getStringExtra(SELECTED_FRAGMENT))) {
                    case START_OR_TRACKING:
                        mSelectedFragmentId = R.id.drawer_start_tracking;
                        break;

                    case WORKOUT_LIST:
                        mSelectedFragmentId = R.id.drawer_workouts;
                        break;
                }
            }
            // now, create and show the main fragment
            onNavigationItemSelected(mNavigationView.getMenu().findItem(mSelectedFragmentId));
        }


        if (TrainingApplication.trackLocation()) {
            // check whether GPS is enabled
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }
        }
        createLocationRequest();

        // Check for permissions and start location updates
        checkLocationPermissions();
        int playServicesStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (playServicesStatus != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(playServicesStatus, this, REQUEST_INSTALL_GOOGLE_PLAY_SERVICE);
            if (dialog != null) {
                if (TrainingApplication.showInstallPlayServicesDialog()) {
                    dialog.show();
                }
                // Start unfiltered GPS if Google Play Services are not available
                startUnfilteredGPS();
            }
        } else {
            // Use filtered GPS when Google Play Services are available
            startFilteredGPS();
        }

        registerReceiver(playServicesReceiver, new IntentFilter(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE));
    }

    private BroadcastReceiver playServicesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if Google Play Services is available
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
                // Google Play Services are available
                startFilteredGPS(); // Call your method to start using filtered GPS
            } else {
                // Handle the case where Google Play Services are not available
                Toast.makeText(context, "Google Play Services not available", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permissions are granted; request location updates
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start requesting location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


        private void startFilteredGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create a LocationRequest with the desired accuracy and interval
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)      // 5 seconds between location updates
                .setFastestInterval(2000);  // 2 seconds for the fastest updates

        // Define a LocationCallback to handle the location updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle each location update (filtered GPS)
                    updateLocationUI(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request the missing permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION); // Define REQUEST_LOCATION_PERMISSION as a constant integer
            return; // Exit the method as permission has not been granted yet
        }

// If permissions are already granted, request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Check if the permission request was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with requesting location updates
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }
            } else {
                // Permission was denied. Handle the case where location permission is denied (e.g., show a message to the user)
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy
        locationRequest.setInterval(5000); // Set the desired interval for active location updates
        locationRequest.setFastestInterval(2000); // Set the fastest interval for location updates
        locationRequest.setSmallestDisplacement(10); // Set the minimum displacement for updates (10 meters)
    }

    private void startUnfilteredGPS() {
        // Get the LocationManager service
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check if the GPS provider is enabled
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Request location updates from the GPS provider
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Missing Permission", "The location permission is missing");
                return;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, // Unfiltered GPS provider
                    5000L,                        // Minimum time interval between updates (milliseconds)
                    10.0f,                        // Minimum distance interval between updates (meters)
                    locationListener               // Listener to handle location updates
            );
        } else {
            // Handle the case where GPS is disabled
            Toast.makeText(this, "GPS is not enabled. Please enable GPS.", Toast.LENGTH_LONG).show();
            // Optionally, direct the user to the location settings screen
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    // Define the LocationListener for unfiltered GPS updates
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Handle location updates (unfiltered GPS)
            updateLocationUI(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onProviderDisabled(String provider) { }
    };

    private void updateLocationUI(Location location) {
        // Your code to handle location updates and display the information in the UI
        // Example:
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d("Location Update", "Lat: " + latitude + ", Lon: " + longitude);
    }

    // Method to stop unfiltered GPS updates
    private void stopUnfilteredGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUnfilteredGPS();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.yourapp.PLAY_SERVICES_ACTION"); // Use the appropriate action
        registerReceiver(playServicesReceiver, filter);
        if (DEBUG) Log.d(TAG, "onResume");

        if (mBanalServiceComm == null) {
            bindService(new Intent(this, BANALService.class), mBanalConnection, Context.BIND_AUTO_CREATE);
        }

        mHandler.removeCallbacks(mDisconnectFromBANALServiceRunnable);

        checkPreferences();

        getWindow().getDecorView().setKeepScreenOn(TrainingApplication.keepScreenOn());

        if (TrainingApplication.NoUnlocking()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }

        if (TrainingApplication.forcePortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        // register the receivers
        registerReceiver(mStartTrackingReceiver, mStartTrackingFilter);
        registerReceiver(mPauseTrackingReceiver, new IntentFilter(TrainingApplication.REQUEST_PAUSE_TRACKING));
        registerReceiver(mStopTrackingReceiver, new IntentFilter(TrainingApplication.REQUEST_STOP_TRACKING));

        upgradeDropboxV2();
    }

    protected void checkPreferences() {
        boolean validSettings = true; // Flag to track if all settings are valid

        // Check Strava settings
        if (TrainingApplication.uploadToStrava()) {
            if (TrainingApplication.getStravaAccessToken() == null) {
                TrainingApplication.setUploadToStrava(false);
                validSettings = false; // Mark as invalid
            } else if (TrainingApplication.getStravaTokenExpiresAt() == 0) {
                Log.i(TAG, "Migrating to new Strava OAuth");
                startActivityForResult(new Intent(this, StravaGetAccessTokenActivity.class), StravaUploadFragment.GET_STRAVA_ACCESS_TOKEN);
            }
        }

        // Check RunKeeper settings
        if (TrainingApplication.uploadToRunKeeper() && TrainingApplication.getRunkeeperToken() == null) {
            TrainingApplication.setUploadToRunkeeper(false);
            validSettings = false; // Mark as invalid
        }

        // Check TrainingPeaks settings
        if (TrainingApplication.uploadToTrainingPeaks() && TrainingApplication.getTrainingPeaksRefreshToken() == null) {
            TrainingApplication.setUploadToTrainingPeaks(false);
            validSettings = false; // Mark as invalid
        }

        // Inform the user if any settings are invalid
        if (!validSettings) {
            Toast.makeText(this, "Some settings are invalid. Please check your authentication tokens.", Toast.LENGTH_LONG).show();
        }
    }


    protected void upgradeDropboxV2() {
        if (TrainingApplication.uploadToDropbox() && !TrainingApplication.hasDropboxToken()) {

            String accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                TrainingApplication.storeDropboxToken(accessToken);
            } else {
                if (mAlreadyTriedToRequestDropboxToken) {
                    TrainingApplication.deleteDropboxToken();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.title_request_dropbox_token)
                            .setIcon(R.drawable.dropbox_logo_blue)
                            .setMessage(R.string.message_request_dropbox_token)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mAlreadyTriedToRequestDropboxToken = true;
                                    Auth.startOAuth2Authentication(MainActivityWithNavigation.this, TrainingApplication.getDropboxAppKey());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TrainingApplication.deleteDropboxToken();
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        }

        switch (requestCode) {
            case REQUEST_INSTALL_GOOGLE_PLAY_SERVICE:
                if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
                    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    // Connection successful, do something
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // Connection suspended
                                }
                            })
                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(ConnectionResult connectionResult) {
                                    // Connection failed
                                }
                            })
                            .build();

                    googleApiClient.connect();
                } else {
                    // Failed to install Google Play services
                    Toast.makeText(this, "Failed to install Google Play services. Please try again.", Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_ENABLE_BLUETOOTH:
                BluetoothManager bluetoothManager = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                }
                BluetoothAdapter bluetoothAdapter = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    bluetoothAdapter = bluetoothManager.getAdapter();
                }

                if (resultCode == RESULT_OK) {
                    // Bluetooth is now enabled
                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                        if (DEBUG) Log.i(TAG, "Bluetooth is now enabled");
                        startPairing(Protocol.BLUETOOTH_LE); // Start pairing process
                    }
                } else {
                    // User did not enable Bluetooth
                    if (DEBUG) Log.i(TAG, "Bluetooth is NOT enabled");
                    Toast.makeText(this, "Bluetooth is required for this feature.", Toast.LENGTH_SHORT).show();
                }
                break;

            default: // Handle other request codes or let fragments handle it
                if (DEBUG) Log.i(TAG, "requestCode not handled");
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (DEBUG) Log.i(TAG, "onSaveInstanceState");

        //Save the fragment's instance
        if (mFragment != null && mFragment.isAdded()) {
            getSupportFragmentManager().putFragment(savedInstanceState, "mFragment", mFragment);
        }

        savedInstanceState.putInt(SELECTED_FRAGMENT_ID, mSelectedFragmentId);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUnfilteredGPS();
        unregisterReceiver(playServicesReceiver);
        if (DEBUG) Log.d(TAG, "onPause");

        try {
            unregisterReceiver(mStartTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }
        try {
            unregisterReceiver(mPauseTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }
        try {
            unregisterReceiver(mStopTrackingReceiver);
        } catch (IllegalArgumentException e) {
        }


        mHandler.postDelayed(mDisconnectFromBANALServiceRunnable, WAITING_TIME_BEFORE_DISCONNECTING);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy");
        mqttHandler.disconnect();
        disconnectFromBANALService();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (DEBUG) Log.i(TAG, "onNavigationItemSelected");

        if (menuItem == null) {
            return false;
        }

        mDrawerLayout.closeDrawers();

        // uncheck previous menuItem
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        mPreviousMenuItem = menuItem;
        menuItem.setChecked(true);

        mSelectedFragmentId = menuItem.getItemId();

        mFragment = null;
        String tag = null;
        int titleId = R.string.app_name;

        switch (mSelectedFragmentId) {
            case R.id.drawer_start_tracking:
                mFragment = StartAndTrackingFragmentTabbedContainer.newInstance(getActivityType(), StartAndTrackingFragmentTabbedContainer.CONTROL_ITEM);
                tag = StartAndTrackingFragmentTabbedContainer.TAG;
                break;

            case R.id.drawer_map:
                mFragment = TrackOnMapTrackingFragment.newInstance();
                tag = TrackOnMapTrackingFragment.TAG;
                break;

            case R.id.drawer_segments:
                titleId = R.string.segments;
                mFragment = new StarredSegmentsTabbedContainer();
                tag = StarredSegmentsTabbedContainer.TAG;
                break;

            case R.id.drawer_workouts:
                titleId = R.string.tab_workouts;
                mFragment = new WorkoutSummariesWithMapListFragment();
                tag = WorkoutSummariesWithMapListFragment.TAG;
                break;

            case R.id.drawer_pairing_ant:
                titleId = R.string.pairing_ANT;
                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.ANT_PLUS);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

            case R.id.drawer_pairing_BTLE:
                titleId = R.string.pairing_bluetooth;
                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.BLUETOOTH_LE);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

            case R.id.drawer_my_sensors:
                titleId = R.string.myRemoteDevices;
                mFragment = RemoteDevicesFragmentTabbedContainer.newInstance(Protocol.ALL, DeviceType.ALL);
                tag = DeviceTypeChoiceFragment.TAG;
                break;

            case R.id.drawer_my_locations:
                titleId = R.string.my_locations;
                mFragment = new MyLocationsFragment();
                tag = MyLocationsFragment.TAG;
                break;

            case R.id.drawer_settings:
                titleId = R.string.settings;
                mFragment = new RootPrefsFragment();
                tag = RootPrefsFragment.TAG;
                break;


            case R.id.drawer_privacy_policy:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.atrainingtracker.com/privacy.html"));
                startActivity(browserIntent);
                return true;


            default:
                Log.d(TAG, "setting a new content fragment not yet implemented");
                Toast.makeText(this, "setting a new content fragment not yet implemented", Toast.LENGTH_SHORT);
        }

        if (mFragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content, mFragment, tag);
            fragmentTransaction.commit();
        }
        setTitle(titleId);

        return true;
    }

    @Override
    public void onBackPressed() {
        // TODO: optimize: when showing "deeper fragments", we only want to go back one step and not completely to the start_tracking fragment
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 0
                && mSelectedFragmentId != R.id.drawer_start_tracking) {
            onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_start_tracking));
        } else {
            super.onBackPressed();
        }
    }

    /* Called when an options item is clicked */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.i(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.itemDeleteOldWorkouts:  // TODO: move to somewhere else?  automatically??
                if (DEBUG) Log.i(TAG, "option itemDeleteOldWorkouts pressed");
                deleteOldWorkouts();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    protected ActivityType getActivityType() {
        if (mBanalServiceComm == null) {
            return ActivityType.getDefaultActivityType();
        } else {
            return mBanalServiceComm.getActivityType();
        }
    }

    @Override
    public void updateActivityType(int selectedItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, StartAndTrackingFragmentTabbedContainer.newInstance(getActivityType(), selectedItem));
        // if (addToBackStack) { fragmentTransaction.addToBackStack(null); }
        fragmentTransaction.commit();
    }

    @Override
    public void enableBluetoothRequest() {
        if (DEBUG) Log.i(TAG, "enableBluetoothRequest");

        showEnableBluetoothDialog();
    }

    @Override
    public void startPairing(Protocol protocol) {
        if (DEBUG) Log.d(TAG, "startPairingActivity: " + protocol);
        switch (protocol) {
            case ANT_PLUS:
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_pairing_ant));
                // changeContentFragment(R.id.drawer_pairing_ant);
                return;

            case BLUETOOTH_LE:
                onNavigationItemSelected(mNavigationView.getMenu().findItem(R.id.drawer_pairing_BTLE));
                // changeContentFragment(R.id.drawer_pairing_BTLE);
                return;
        }

        Toast.makeText(getApplicationContext(), "TODO: must implement the startPairing for" + protocol.name(), Toast.LENGTH_SHORT).show();
    }

    // @Override
    // public void startWorkoutDetailsActivity(long workoutId, WorkoutDetailsActivity.SelectedFragment selectedFragment)
    // {
    //     if (DEBUG) Log.i(TAG, "startWorkoutDetailsActivity(" + workoutId + ")");

    //     Bundle bundle = new Bundle();
    //     bundle.putLong(WorkoutSummaries.WORKOUT_ID, workoutId);
    //     bundle.putString(WorkoutDetailsActivity.SELECTED_FRAGMENT, selectedFragment.name());
    //     Intent workoutDetailsIntent = new Intent(this, WorkoutDetailsActivity.class);
    //     workoutDetailsIntent.putExtras(bundle);
    //     startActivity(workoutDetailsIntent);
    // }

    protected void checkBatteryStatus() {
        final List<DevicesDatabaseManager.NameAndBatteryPercentage> criticalBatteryDevices = DevicesDatabaseManager.getCriticalBatteryDevices(CRITICAL_BATTERY_LEVEL);
        if (criticalBatteryDevices.size() > 0) {

            final List<String> stringList = new LinkedList<>();
            for (DevicesDatabaseManager.NameAndBatteryPercentage device : criticalBatteryDevices) {
                stringList.add(getString(R.string.critical_battery_message_format,
                        device.name, getString(BatteryStatusHelper.getBatteryStatusNameId(device.batteryPercentage))));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(criticalBatteryDevices.size() == 1 ? R.string.check_battery_status_title_1 : R.string.check_battery_status_title_many);
            builder.setItems(stringList.toArray(new String[stringList.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditDeviceDialogFragment editDeviceDialogFragment = EditDeviceDialogFragment.newInstance(criticalBatteryDevices.get(which).deviceId);
                    editDeviceDialogFragment.show(getSupportFragmentManager(), EditDeviceDialogFragment.TAG);
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void startSegmentDetailsActivity(int segmentId, SegmentDetailsActivity.SelectedFragment selectedFragment) {
        if (DEBUG) Log.i(TAG, "startSegmentDetailsActivity: segmentId=" + segmentId);

        Bundle bundle = new Bundle();
        bundle.putLong(SegmentsDatabaseManager.Segments.SEGMENT_ID, segmentId);
        bundle.putString(WorkoutDetailsActivity.SELECTED_FRAGMENT, selectedFragment.name());
        Intent segmentDetailsIntent = new Intent(this, SegmentDetailsActivity.class);
        segmentDetailsIntent.putExtras(bundle);
        startActivity(segmentDetailsIntent);
    }

    @Override
    public void exportWorkout(long id, FileFormat fileFormat) {
        if (DEBUG) Log.i(TAG, "exportWorkout");

        ExportManager exportManager = new ExportManager(this, TAG);
        exportManager.exportWorkoutTo(id, fileFormat);
        exportManager.onFinished(TAG);

        startService(new Intent(this, ExportWorkoutIntentService.class));
    }

    @Override
    public void showExportStatusDialog(long workoutId) {
        if (DEBUG) Log.i(TAG, "startExportDetailsActivity");

        ExportStatusDialogFragment exportStatusFragment = ExportStatusDialogFragment.newInstance(workoutId);
        exportStatusFragment.show(getSupportFragmentManager(), ExportStatusDialogFragment.TAG);
    }

    @Override
    public void confirmDeleteWorkout(long workoutId) {
        ReallyDeleteWorkoutDialog newFragment = ReallyDeleteWorkoutDialog.newInstance(workoutId);
        newFragment.show(getSupportFragmentManager(), ReallyDeleteWorkoutDialog.TAG);
    }

    @Override
    public void reallyDeleteWorkout(long workoutId) {
        (new DeleteWorkoutTask(this)).execute(workoutId);
    }

    public void deleteOldWorkouts() {
        if (DEBUG) Log.i(TAG, "deleteOldWorkouts");

        DeleteOldWorkoutsDialog deleteOldWorkoutsDialog = new DeleteOldWorkoutsDialog();
        deleteOldWorkoutsDialog.show(getSupportFragmentManager(), DeleteOldWorkoutsDialog.TAG);
    }

    @Override
    public void onDeviceTypeSelected(DeviceType deviceType, Protocol protocol) {
        if (DEBUG)
            Log.i(TAG, "onDeviceTypeSelected(" + deviceType.name() + "), mProtocol=" + protocol);

        RemoteDevicesFragmentTabbedContainer fragment = RemoteDevicesFragmentTabbedContainer.newInstance(protocol, deviceType);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, RemoteDevicesFragmentTabbedContainer.TAG);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onRemoteDeviceSelected(long deviceId) {
        EditDeviceDialogFragment editDeviceDialogFragment = EditDeviceDialogFragment.newInstance(deviceId);
        editDeviceDialogFragment.show(getSupportFragmentManager(), EditDeviceDialogFragment.TAG);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // the connection to the BANALService
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // stolen from http://stackoverflow.com/questions/32487206/inner-preferencescreen-not-opens-with-preferencefragmentcompat
    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat, PreferenceScreen preferenceScreen) {
        if (DEBUG) Log.i(TAG, "onPreferenceStartScreen: " + preferenceScreen.getKey());
        String key = preferenceScreen.getKey();
        PreferenceFragmentCompat fragment = null;
        if (key.equals("root")) {
            fragment = new RootPrefsFragment();
        } else if (key.equals("display")) {
            fragment = new DisplayFragment();
        }
        else if (key.equals("search_settings")) {
            fragment = new SearchFragment();
        } else if (key.equals(TrainingApplication.PREF_KEY_START_SEARCH)) {
            fragment = new StartSearchFragment();
        } else if (key.equals("fileExport")) {
            fragment = new FileExportFragment();
        } else if (key.equals("cloudUpload")) {
            fragment = new CloudUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_EMAIL_UPLOAD)) {
            fragment = new EmailUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_STRAVA)) {
            fragment = new StravaUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_RUNKEEPER)) {
            fragment = new RunkeeperUploadFragment();
        } else if (key.equals(TrainingApplication.PREFERENCE_SCREEN_TRAINING_PEAKS)) {
            fragment = new TrainingpeaksUploadFragment();
        } else if (key.equals("pebbleScreen")) {
            fragment = new PebbleScreenFragment();
        } else if (key.equals("prefsLocationSources")) {
            fragment = new LocationSourcesFragment();
        } else if (key.equals("altitudeCorrection")) {
            fragment = new AltitudeCorrectionFragment();
        } else if (key.equals("sportTypes")) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, new SportTypeListFragment(), preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();
            return true;
        } else if (key.equals("fancyWorkoutNames")) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, new FancyWorkoutNameListFragment(), preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();
            return true;
        } else {
            Log.d(TAG, "WTF: unknown key");
        }


        if (fragment != null) {
            Bundle args = new Bundle();
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
            fragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content, fragment, preferenceScreen.getKey());
            ft.addToBackStack(preferenceScreen.getKey());
            ft.commit();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void registerConnectionStatusListener(ConnectionStatusListener connectionStatusListener) {
        mConnectionStatusListeners.add(connectionStatusListener);
    }

    @Override
    public BANALService.BANALServiceComm getBanalServiceComm() {
        return mBanalServiceComm;
    }

    private void disconnectFromBANALService() {
        if (DEBUG) Log.i(TAG, "disconnectFromBANALService");

        if (mBanalServiceComm != null) {
            // on some devices an exception is thrown here - look into that
            unbindService(mBanalConnection);
            mBanalServiceComm = null;
        }
    }

    private void showGPSDisabledAlertToUser() {
        GPSDisabledDialog gpsDisabledDialog = new GPSDisabledDialog();
        gpsDisabledDialog.show(getSupportFragmentManager(), GPSDisabledDialog.TAG);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // showing several dialogs
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showEnableBluetoothDialog() {
        EnableBluetoothDialog enableBluetoothDialog = new EnableBluetoothDialog();
        enableBluetoothDialog.show(getSupportFragmentManager(), EnableBluetoothDialog.TAG);
    }

    private void showInstallANTShitDialog() {
        InstallANTShitDialog installANTShitDialog = new InstallANTShitDialog();
        installANTShitDialog.show(getSupportFragmentManager(), InstallANTShitDialog.TAG);
    }

    /***********************************************************************************************/

    @Override
    public void showStartOrResumeDialog() {
        StartOrResumeDialog startOrResumeDialog = new StartOrResumeDialog();
        startOrResumeDialog.show(getSupportFragmentManager(), StartOrResumeDialog.TAG);
    }


    /***********************************************************************************************/
    /* Implementation of the StartOrResumeInterface                                                */
    @Override
    public void chooseStart() {
        // Set flag to indicate a new workout should be started
        TrainingApplication.setResumeFromCrash(false);

        // Update the UI on the main thread using an anonymous inner class
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.tvStart);
                if (tv != null) {
                    tv.setText(R.string.start_new_workout);
                }
            }
        });

        // Start background task for MQTT connection and message publishing
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Initialize and connect MQTT handler
                    mqttHandler = new MqttHandler(BROKER_URL, CLIENT_ID);
                    mqttHandler.connect();
                    Log.d("MQTT CONNECT", "MQTT handler has been created and connected");

                    // Publish a message after connection is established
                    publishMessage("house/bulb", "testMessage2");
                } catch (Exception e) {
                    // Log any exceptions that occur during MQTT operations
                    Log.e("MQTT ERROR", "Error in MQTT operations", e);
                }
            }
        }).start();
    }



    @Override
    public void chooseResume() {
        TrainingApplication.setResumeFromCrash(true);

        TextView tv = findViewById(R.id.tvStart);
        if (tv != null) {
            tv.setText(R.string.resume_workout);
        }
        subscribeToTopic("testTopic");
    }

    private void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message " + message, Toast.LENGTH_SHORT).show();
        if (mqttHandler != null) {
            mqttHandler.publish(topic, message);
        }
        Log.d("PUBLISH", "string has been published");
    }

    private void subscribeToTopic(String topic){
        Toast.makeText(this, "Subscribing to topic " + topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
        Log.d("SUBSCRIBE", "string has been published");
    }

    public enum SelectedFragment {START_OR_TRACKING, WORKOUT_LIST}

}
