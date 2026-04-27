package ma.fst.projet.my_new_application;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class BookingsActivity extends AppCompatActivity {

    LinearLayout bookingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);
        bookingsContainer = findViewById(R.id.bookingsContainer);
        chargerHistorique();
    }

    private void chargerHistorique() {
        int passagerId = getSharedPreferences("user", MODE_PRIVATE).getInt("id", 4);
        String url = "http://10.0.2.2:5000/bookings/user/" + passagerId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    bookingsContainer.removeAllViews();

                    // Titre
                    TextView title = new TextView(this);
                    title.setText("Mes réservations");
                    title.setTextSize(26);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setTextColor(Color.WHITE);
                    title.setGravity(android.view.Gravity.CENTER);
                    title.setPadding(0, 20, 0, 24);
                    bookingsContainer.addView(title);

                    if (response.length() == 0) {
                        TextView tv = new TextView(this);
                        tv.setText("Aucune réservation trouvée.");
                        tv.setTextColor(Color.parseColor("#64748B"));
                        tv.setTextSize(15);
                        tv.setGravity(android.view.Gravity.CENTER);
                        tv.setPadding(20, 40, 20, 20);
                        bookingsContainer.addView(tv);
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject b   = response.getJSONObject(i);
                            String depart  = b.getString("departure_city");
                            String arrivee = b.getString("arrival_city");
                            String prix    = b.getString("price");
                            String statut  = b.getString("statut");
                            int    rideId  = b.getInt("ride_id");
                            ajouterCarte(depart, arrivee, prix, statut, rideId);
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                },
                error -> Toast.makeText(this, "Erreur chargement", Toast.LENGTH_LONG).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void ajouterCarte(String depart, String arrivee, String prix, String statut, int rideId) {

        // Carte sombre
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(40, 32, 40, 32);
        card.setBackgroundResource(R.drawable.card_dark);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);

        // Trajet
        TextView tvRoute = new TextView(this);
        tvRoute.setText(depart + " → " + arrivee);
        tvRoute.setTextSize(17);
        tvRoute.setTextColor(Color.WHITE);
        tvRoute.setTypeface(null, Typeface.BOLD);
        tvRoute.setPadding(0, 0, 0, 6);

        // Prix
        TextView tvPrix = new TextView(this);
        tvPrix.setText("💰 " + prix + " DH");
        tvPrix.setTextSize(14);
        tvPrix.setTextColor(Color.parseColor("#94A3B8"));
        tvPrix.setPadding(0, 0, 0, 14);

        // Badge statut
        TextView tvStatut = new TextView(this);
        tvStatut.setText(traduireStatut(statut));
        tvStatut.setTextSize(13);
        tvStatut.setPadding(24, 10, 24, 10);
        tvStatut.setTypeface(null, Typeface.BOLD);

        switch (statut) {
            case "accepted":
                tvStatut.setBackgroundColor(Color.parseColor("#064E3B"));
                tvStatut.setTextColor(Color.parseColor("#6EE7B7"));
                break;
            case "rejected":
                tvStatut.setBackgroundColor(Color.parseColor("#7F1D1D"));
                tvStatut.setTextColor(Color.parseColor("#FCA5A5"));
                break;
            default:
                tvStatut.setBackgroundColor(Color.parseColor("#78350F"));
                tvStatut.setTextColor(Color.parseColor("#FCD34D"));
                break;
        }

        card.addView(tvRoute);
        card.addView(tvPrix);
        card.addView(tvStatut);

        // Bouton Avis (si acceptée)
        if ("accepted".equals(statut)) {
            Button btnAvis = new Button(this);
            btnAvis.setText("⭐ Laisser un avis");
            btnAvis.setTextColor(Color.parseColor("#6C63FF"));
            btnAvis.setBackgroundColor(Color.parseColor("#1E1B4B"));
            btnAvis.setTextSize(14);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 16, 0, 0);
            btnAvis.setLayoutParams(btnParams);

            btnAvis.setOnClickListener(v -> {
                int passagerId = getSharedPreferences("user", MODE_PRIVATE).getInt("id", 4);
                Intent intent = new Intent(this, ReviewActivity.class);
                intent.putExtra("ride_id",      rideId);
                intent.putExtra("passenger_id", passagerId);
                intent.putExtra("route",        depart + " → " + arrivee);
                startActivity(intent);
            });

            card.addView(btnAvis);
        }

        bookingsContainer.addView(card);
    }

    private String traduireStatut(String s) {
        switch (s) {
            case "pending":   return " En attente";
            case "accepted":  return " Acceptée";
            case "rejected":  return " Refusée";
            case "cancelled": return " Annulée";
            default:          return s;
        }
    }
}