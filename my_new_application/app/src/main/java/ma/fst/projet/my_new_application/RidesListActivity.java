package ma.fst.projet.my_new_application;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RidesListActivity extends AppCompatActivity {

    LinearLayout ridesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_list);

        ridesContainer = findViewById(R.id.ridesContainer);

        // ✅ Nom utilisateur dans le header
        String userName = getSharedPreferences("user", MODE_PRIVATE)
                .getString("name", "Passager");
        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText("👤 " + userName);

        // ✅ Bouton déconnexion
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            getSharedPreferences("user", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // ✅ Navbar — Mes réservations
        findViewById(R.id.btnNavReservations).setOnClickListener(v ->
                startActivity(new Intent(this, BookingsActivity.class)));

        // ✅ Navbar — Trajets (page actuelle)
        findViewById(R.id.btnNavTrajets).setOnClickListener(v ->
                Toast.makeText(this, "Vous êtes déjà sur les trajets", Toast.LENGTH_SHORT).show());

        chargerTrajets();
    }

    private void chargerTrajets() {
        String url = "http://10.0.2.2:5000/rides";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    ridesContainer.removeAllViews();

                    // Titre
                    TextView title = new TextView(this);
                    title.setText("Liste des trajets");
                    title.setTextSize(26);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setTextColor(Color.WHITE);
                    title.setGravity(android.view.Gravity.CENTER);
                    title.setPadding(0, 10, 0, 24);
                    ridesContainer.addView(title);

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject ride = response.getJSONObject(i);
                            int    rideId  = ride.getInt("id");
                            String depart  = ride.getString("departure_city");
                            String arrivee = ride.getString("arrival_city");
                            String prix    = ride.getString("price");
                            String places  = ride.getString("available_seats");
                            ajouterCarteTrajet(rideId, depart, arrivee, prix, places);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                },
                error -> Toast.makeText(this, "Erreur chargement trajets", Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void ajouterCarteTrajet(int rideId, String depart, String arrivee, String prix, String places) {

        // Carte sombre
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(40, 32, 40, 32);
        card.setBackgroundResource(R.drawable.card_dark);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);
        card.setElevation(4);

        // Trajet
        TextView tvTrajet = new TextView(this);
        tvTrajet.setText(depart + "  →  " + arrivee);
        tvTrajet.setTextSize(18);
        tvTrajet.setTypeface(null, Typeface.BOLD);
        tvTrajet.setTextColor(Color.WHITE);
        tvTrajet.setPadding(0, 0, 0, 8);

        // Détails
        TextView tvDetails = new TextView(this);
        tvDetails.setText("💰 " + prix + " DH   •   💺 " + places + " places");
        tvDetails.setTextSize(14);
        tvDetails.setTextColor(Color.parseColor("#94A3B8"));
        tvDetails.setPadding(0, 0, 0, 20);

        // Bouton Réserver
        AppCompatButton btnReserver = new AppCompatButton(this);
        btnReserver.setText("Réserver");
        btnReserver.setAllCaps(false);
        btnReserver.setTextColor(Color.WHITE);
        btnReserver.setTypeface(null, Typeface.BOLD);
        btnReserver.setTextSize(15);
        btnReserver.setBackgroundResource(R.drawable.button_bg);
        btnReserver.setOnClickListener(v -> reserverTrajet(rideId));

        // Bouton Carte
        AppCompatButton btnCarte = new AppCompatButton(this);
        btnCarte.setText("🗺️ Voir sur la carte");
        btnCarte.setAllCaps(false);
        btnCarte.setTextColor(Color.parseColor("#6C63FF"));
        btnCarte.setTypeface(null, Typeface.BOLD);
        btnCarte.setTextSize(14);
        btnCarte.setBackgroundColor(Color.parseColor("#1E293B"));

        LinearLayout.LayoutParams btnCarteParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnCarteParams.setMargins(0, 10, 0, 0);
        btnCarte.setLayoutParams(btnCarteParams);

        btnCarte.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("departure_city", depart);
            intent.putExtra("arrival_city",   arrivee);
            startActivity(intent);
        });

        card.addView(tvTrajet);
        card.addView(tvDetails);
        card.addView(btnReserver);
        card.addView(btnCarte);
        ridesContainer.addView(card);
    }

    private void reserverTrajet(int rideId) {
        // ✅ Vrai ID depuis la session
        int passagerId = getSharedPreferences("user", MODE_PRIVATE).getInt("id", -1);

        if (passagerId == -1) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:5000/bookings";

        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> Toast.makeText(this, "✅ Réservation envoyée !", Toast.LENGTH_SHORT).show(),
                error -> {
                    String msg = "Erreur réservation";
                    if (error.networkResponse != null)
                        msg += " (code " + error.networkResponse.statusCode + ")";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("passenger_id", String.valueOf(passagerId));
                params.put("ride_id",      String.valueOf(rideId));
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}