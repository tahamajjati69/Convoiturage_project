package ma.fst.projet.my_new_application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText etDepart, etArrivee, etPrix, etPlaces;
    Button btnPublier, btnPrixPlus, btnPrixMoins, btnPlacesPlus, btnPlacesMoins;
    Button btnVoirTrajets, btnMesReservations, btnLogout;
    TextView tvWelcome;

    int prixValue   = 10;
    int placesValue = 1;
    int driverId    = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Lire la session depuis "user"
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        int    userId   = prefs.getInt("id", -1);
        String userName = prefs.getString("name", "Conducteur");
        String userRole = prefs.getString("role", "");

        // ✅ Si pas connecté → Login
        if (userId == -1 || userRole.isEmpty()) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        // ✅ Si passager arrive ici par erreur → RidesList
        if ("passenger".equals(userRole)) {
            Intent i = new Intent(this, RidesListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        driverId = userId;
        setContentView(R.layout.activity_main);

        // ── VIEWS ──
        tvWelcome          = findViewById(R.id.tvWelcome);
        etDepart           = findViewById(R.id.etDepart);
        etArrivee          = findViewById(R.id.etArrivee);
        etPrix             = findViewById(R.id.etPrix);
        etPlaces           = findViewById(R.id.etPlaces);
        btnPublier         = findViewById(R.id.btnPublier);
        btnPrixPlus        = findViewById(R.id.btnPrixPlus);
        btnPrixMoins       = findViewById(R.id.btnPrixMoins);
        btnPlacesPlus      = findViewById(R.id.btnPlacesPlus);
        btnPlacesMoins     = findViewById(R.id.btnPlacesMoins);
        btnVoirTrajets     = findViewById(R.id.btnVoirTrajets);
        btnMesReservations = findViewById(R.id.btnMesReservations);
        btnLogout          = findViewById(R.id.btnLogout);

        // ✅ Message de bienvenue
        tvWelcome.setText("Bonjour " + userName + " — Conducteur 🚗");

        // ✅ Section publier visible pour driver
        android.view.View sectionPublier = findViewById(R.id.sectionPublier);
        sectionPublier.setVisibility(android.view.View.VISIBLE);

        // ── PRIX ──
        etPrix.setText(String.valueOf(prixValue));
        etPlaces.setText(String.valueOf(placesValue));

        btnPrixPlus.setOnClickListener(v -> {
            prixValue += 5;
            etPrix.setText(String.valueOf(prixValue));
        });
        btnPrixMoins.setOnClickListener(v -> {
            if (prixValue > 5) {
                prixValue -= 5;
                etPrix.setText(String.valueOf(prixValue));
            }
        });
        btnPlacesPlus.setOnClickListener(v -> {
            placesValue++;
            etPlaces.setText(String.valueOf(placesValue));
        });
        btnPlacesMoins.setOnClickListener(v -> {
            if (placesValue > 1) {
                placesValue--;
                etPlaces.setText(String.valueOf(placesValue));
            }
        });

        // ── PUBLIER ──
        btnPublier.setOnClickListener(v -> publierTrajet());

        // ── VOIR TRAJETS ──
        btnVoirTrajets.setOnClickListener(v ->
                startActivity(new Intent(this, RidesListActivity.class)));

        // ── MES RÉSERVATIONS ──
        btnMesReservations.setOnClickListener(v ->
                startActivity(new Intent(this, BookingsActivity.class)));

        // ✅ DÉCONNEXION
        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void publierTrajet() {
        String depart  = etDepart.getText().toString().trim();
        String arrivee = etArrivee.getText().toString().trim();

        if (depart.isEmpty() || arrivee.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir départ et arrivée", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:5000/rides";

        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "✅ Trajet publié !", Toast.LENGTH_SHORT).show();
                    etDepart.setText("");
                    etArrivee.setText("");
                    prixValue   = 10;
                    placesValue = 1;
                    etPrix.setText(String.valueOf(prixValue));
                    etPlaces.setText(String.valueOf(placesValue));
                },
                error -> Toast.makeText(this, "❌ Erreur connexion API", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("driver_id",       String.valueOf(driverId));
                params.put("departure_city",  etDepart.getText().toString().trim());
                params.put("arrival_city",    etArrivee.getText().toString().trim());
                params.put("ride_date",       "2026-04-25");
                params.put("ride_time",       "08:00:00");
                params.put("available_seats", String.valueOf(placesValue));
                params.put("price",           String.valueOf(prixValue));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}