package ma.fst.projet.my_new_application;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;

public class ReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private TextView tvTitle;
    private int rideId;
    private int passengerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        rideId      = getIntent().getIntExtra("ride_id", -1);
        passengerId = getIntent().getIntExtra("passenger_id", -1);
        String route = getIntent().getStringExtra("route");

        ratingBar  = findViewById(R.id.ratingBar);
        etComment  = findViewById(R.id.etComment);
        btnSubmit  = findViewById(R.id.btnSubmitReview);
        tvTitle    = findViewById(R.id.tvReviewTitle);

        if (route != null) tvTitle.setText("Noter : " + route);

        btnSubmit.setOnClickListener(v -> soumettrAvis());
    }

    private void soumettrAvis() {
        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Veuillez donner une note", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        String url = "http://10.0.2.2:5000/reviews";

        JSONObject body = new JSONObject();
        try {
            body.put("ride_id", rideId);
            body.put("passenger_id", passengerId);
            body.put("rating", rating);
            if (!comment.isEmpty()) body.put("comment", comment);
        } catch (Exception e) { e.printStackTrace(); return; }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> {
                    Toast.makeText(this, "Avis envoyé ! Merci", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    String msg = "Erreur lors de l'envoi";
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 403)
                            msg = "Vous devez avoir participé à ce trajet";
                        else if (error.networkResponse.statusCode == 409)
                            msg = "Vous avez déjà noté ce trajet";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                }
        );

        Volley.newRequestQueue(this).add(req);
    }
}