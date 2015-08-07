package com.cycloon_marc.osmbonus;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private MapView map;
    private IMapController mapController;

    LocationManager locationManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Activity activity = this;

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        final GeoPoint startPoint = new GeoPoint(52.976009, 6.558900);
        mapController = map.getController();
        mapController.setZoom(14);
        mapController.setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        startMarker.setIcon(getResources().getDrawable(R.drawable.marker_departure));
        startMarker.setTitle("Start point");

        map.getOverlays().add(startMarker);
       map.invalidate();

        new Thread(new Runnable() {
            public void run() {
                RoadManager roadManager = new OSRMRoadManager();
                ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                waypoints.add(startPoint);
                GeoPoint endPoint = new GeoPoint(53.065346, 6.326113);
                waypoints.add(endPoint);

                Marker endMarker = new Marker(map);
                endMarker.setPosition(endPoint);
                endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
                endMarker.setTitle("End point");
                map.getOverlays().add(endMarker);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
