package ma.fst.projet.my_new_application;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView map;
    private EditText etDepart, etArrivee;
    private TextView tvDepart, tvArrivee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map);

        etDepart  = findViewById(R.id.etDepart);
        etArrivee = findViewById(R.id.etArrivee);
        tvDepart  = findViewById(R.id.tvDepart);
        tvArrivee = findViewById(R.id.tvArrivee);
        map       = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(6.0);
        map.getController().setCenter(new GeoPoint(31.7917, -7.0926));

        Button btnRetour = findViewById(R.id.btnRetour);
        btnRetour.setOnClickListener(v -> finish());

        String depVille = getIntent().getStringExtra("departure_city");
        String arrVille = getIntent().getStringExtra("arrival_city");
        if (depVille != null) etDepart.setText(depVille);
        if (arrVille != null) etArrivee.setText(arrVille);

        Button btnRechercher = findViewById(R.id.btnRechercher);
        btnRechercher.setOnClickListener(v -> rechercherEtAfficher());

        if (depVille != null && arrVille != null) {
            rechercherEtAfficher();
        }
    }

    private void rechercherEtAfficher() {
        String villeDepart  = etDepart.getText().toString().trim();
        String villeArrivee = etArrivee.getText().toString().trim();

        if (villeDepart.isEmpty() || villeArrivee.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer les deux villes", Toast.LENGTH_SHORT).show();
            return;
        }

        GeoPoint departure = geocoder(villeDepart + ", Maroc");
        GeoPoint arrival   = geocoder(villeArrivee + ", Maroc");

        if (departure == null) {
            Toast.makeText(this, "Ville introuvable : " + villeDepart, Toast.LENGTH_SHORT).show();
            return;
        }
        if (arrival == null) {
            Toast.makeText(this, "Ville introuvable : " + villeArrivee, Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher les noms des villes en haut ✅
        tvDepart.setText("🟢 " + villeDepart);
        tvArrivee.setText("🔴 " + villeArrivee);

        map.getOverlays().clear();

        // Ligne bleue entre les deux points ✅
        Polyline ligne = new Polyline();
        List<GeoPoint> points = new ArrayList<>();
        points.add(departure);
        points.add(arrival);
        ligne.setPoints(points);
        ligne.getOutlinePaint().setColor(android.graphics.Color.parseColor("#6C63FF"));
        ligne.getOutlinePaint().setStrokeWidth(8f);
        map.getOverlays().add(ligne);

        // Marqueur DÉPART 🟢
        Marker markerDep = new Marker(map);
        markerDep.setPosition(departure);
        markerDep.setTitle("🟢 Départ : " + villeDepart);
        markerDep.setSnippet("Point de départ");
        markerDep.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerDep);
        markerDep.showInfoWindow();

        // Marqueur ARRIVÉE 🔴
        Marker markerArr = new Marker(map);
        markerArr.setPosition(arrival);
        markerArr.setTitle("🔴 Arrivée : " + villeArrivee);
        markerArr.setSnippet("Point d'arrivée");
        markerArr.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(markerArr);
        markerArr.showInfoWindow();

        // Zoom automatique
        BoundingBox box = new BoundingBox(
                Math.max(departure.getLatitude(), arrival.getLatitude()) + 0.5,
                Math.max(departure.getLongitude(), arrival.getLongitude()) + 0.5,
                Math.min(departure.getLatitude(), arrival.getLatitude()) - 0.5,
                Math.min(departure.getLongitude(), arrival.getLongitude()) - 0.5
        );
        map.zoomToBoundingBox(box, true);
        map.invalidate();
    }

    private GeoPoint geocoder(String ville) {
        try {
            String query = ville.replace(" ", "+");
            String urlStr = "https://nominatim.openstreetmap.org/search?q="
                    + query + "&format=json&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", getPackageName());
            conn.setConnectTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONArray results = new JSONArray(sb.toString());
            if (results.length() > 0) {
                JSONObject obj = results.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new GeoPoint(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() { super.onResume(); map.onResume(); }

    @Override
    protected void onPause() { super.onPause(); map.onPause(); }
}