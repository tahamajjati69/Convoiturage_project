package ma.fst.projet.my_new_application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoRegister;
    TextView tvError;
    LinearLayout btnChoixDriver, btnChoixPassenger;
    String roleChoisi = "driver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier session dans "user"
        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);
        if (userId != -1) {
            String role = prefs.getString("role", "");
            redirectByRole(role);
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        btnLogin          = findViewById(R.id.btnLogin);
        tvError           = findViewById(R.id.tvError);
        btnChoixDriver    = findViewById(R.id.btnChoixDriver);
        btnChoixPassenger = findViewById(R.id.btnChoixPassenger);
        btnGoRegister     = findViewById(R.id.btnGoRegister);

        // Clic Conducteur
        btnChoixDriver.setOnClickListener(v -> {
            roleChoisi = "driver";
            btnChoixDriver.setBackgroundResource(R.drawable.role_bg_selected);
            btnChoixPassenger.setBackgroundResource(R.drawable.role_bg_unselected);
            ((TextView) btnChoixDriver.getChildAt(1)).setTextColor(0xFFFFFFFF);
            ((TextView) btnChoixPassenger.getChildAt(1)).setTextColor(0xFF64748B);
        });

        // Clic Passager
        btnChoixPassenger.setOnClickListener(v -> {
            roleChoisi = "passenger";
            btnChoixPassenger.setBackgroundResource(R.drawable.role_bg_selected);
            btnChoixDriver.setBackgroundResource(R.drawable.role_bg_unselected);
            ((TextView) btnChoixPassenger.getChildAt(1)).setTextColor(0xFFFFFFFF);
            ((TextView) btnChoixDriver.getChildAt(1)).setTextColor(0xFF64748B);
        });

        btnLogin.setOnClickListener(v -> seConnecter());

        // ✅ AJOUT — Aller vers l'inscription
        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void seConnecter() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");
        tvError.setVisibility(View.GONE);

        JSONObject body = new JSONObject();
        try {
            body.put("email",    email);
            body.put("password", password);
        } catch (JSONException e) { e.printStackTrace(); return; }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:5000/login",
                body,
                response -> {
                    try {
                        int    userId = response.getInt("id");
                        String name   = response.getString("name");
                        String role   = response.getString("role");

                        // Vérifier que le rôle correspond au choix
                        if (!role.equals(roleChoisi)) {
                            String attendu = roleChoisi.equals("driver") ? "conducteur" : "passager";
                            afficherErreur("Ce compte n'est pas un " + attendu);
                            resetBtn();
                            return;
                        }

                        // Sauvegarder dans "user"
                        getSharedPreferences("user", MODE_PRIVATE).edit()
                                .putInt("id",      userId)
                                .putString("name", name)
                                .putString("role", role)
                                .apply();

                        Toast.makeText(this, "Bienvenue " + name + " !", Toast.LENGTH_SHORT).show();
                        redirectByRole(role);

                    } catch (JSONException e) {
                        afficherErreur("Erreur de réponse serveur");
                        resetBtn();
                    }
                },
                error -> {
                    String msg = "Email ou mot de passe incorrect";
                    if (error.networkResponse == null)
                        msg = "Serveur inaccessible";
                    afficherErreur(msg);
                    resetBtn();
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void redirectByRole(String role) {
        Intent intent;
        if ("driver".equals(role)) {
            intent = new Intent(this, MainActivity.class);
        } else if ("passenger".equals(role)) {
            intent = new Intent(this, RidesListActivity.class);
        } else {
            afficherErreur("Rôle non reconnu : " + role);
            resetBtn();
            return;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void afficherErreur(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void resetBtn() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Se connecter");
    }
}