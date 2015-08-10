package com.cycloon_marc.osmbonus;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.ViewTreeObserver;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MapView map;
    private IMapController mapController;

    LocationManager locationManager;
    ArrayList<GeoPoint> waypoints = new ArrayList<>();
    ArrayList<OverlayItem> overlayItemArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity activity = this;

        map = (MapView) findViewById(R.id.map);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        final GeoPoint startPoint = new GeoPoint(53.065346, 6.326113);
        mapController = map.getController();
        mapController.setZoom(14);
        //mapController.setCenter(startPoint);
        ViewTreeObserver vto = map.getViewTreeObserver();

        //set location
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastLocation != null){
            updateLoc(lastLocation);
        }

        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getController().setCenter(startPoint);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

                    map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    map.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        map.setTileSource(TileSourceFactory.MAPNIK);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
        startMarker.setTitle("Start point");

        map.getOverlays().add(startMarker);
        map.invalidate();

        new Thread(new Runnable() {
            public void run() {
                //orginal version
                //RoadManager roadManager = new OSRMRoadManager();
                //mapquest version
                RoadManager roadManager = new MapQuestRoadManager("ji4nVIWGbEL8FANiCYM0JNGet7Fj9O8B");
                roadManager.addRequestOption("routeType=bicycle");
                roadManager.addRequestOption("locale=nl_NL");
                //final ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(startPoint);
                GeoPoint endPoint = new GeoPoint(52.976009, 6.558900);
                waypoints.add(endPoint);

                Marker endMarker = new Marker(map);
                endMarker.setPosition(endPoint);
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
                endMarker.setTitle("End point");
                map.getOverlays().add(endMarker);
                mapController.setCenter(endPoint);
                map.invalidate();

                final Road road = roadManager.getRoad(waypoints);
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (road.mStatus != Road.STATUS_OK) {
                            //handle error... warn the user, etc.
                        }

                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, activity);
                        map.getOverlays().add(roadOverlay);

                        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
                        for (int i=0; i<road.mNodes.size(); i++){
                            RoadNode node = road.mNodes.get(i);
                            Marker nodeMarker = new Marker(map);
                            nodeMarker.setPosition(node.mLocation);
                            nodeMarker.setIcon(nodeIcon);
                            nodeMarker.setTitle("Step " + i);
                            nodeMarker.setSnippet(node.mInstructions);
                            nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));

                           //function to add route turn by turn icon.
                            Resources res = getResources();
                            TypedArray icons = res.obtainTypedArray(R.array.turn_imgs);
                            Drawable icon = icons.getDrawable(node.mManeuverType);

                            //Set turn by turn image to popup bubble
                            nodeMarker.setImage(icon);

                            map.getOverlays().add(nodeMarker);

                        }
                        map.invalidate();
                    }
                });
            }
        }).start();
    }
    protected void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        }
    private void updateLoc(Location loc){
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        mapController.setCenter(locGeoPoint);

        setOverlayLoc(loc);

        map.invalidate();
    }

    private void setOverlayLoc(Location overlayloc){
        //GeoPoint overlocGeoPoint = new GeoPoint(overlayloc);
        //---
       // overlayItemArray.clear();

      //  OverlayItem newMyLocationItem = new OverlayItem(
      //          "My Location", "My Location", overlocGeoPoint);
      //overlayItemArray.add(newMyLocationItem);

      // TEST CODE
       GeoPoint beginPoint = new GeoPoint(overlayloc);
        waypoints.add(beginPoint);

        Marker beginMarker = new Marker(map);
        map.getOverlays().remove(beginMarker);
        beginMarker.setPosition(beginPoint);
        beginMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        beginMarker.setIcon(getResources().getDrawable(R.drawable.ic_menu_mylocation));
        beginMarker.setTitle("Begin point");
        map.getOverlays().add(beginMarker);
        mapController.setCenter(beginPoint);
        map.invalidate();


        //---
    }

    private LocationListener myLocationListener
            = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
        //    updateLoc(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }


       // @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }



    };
}
