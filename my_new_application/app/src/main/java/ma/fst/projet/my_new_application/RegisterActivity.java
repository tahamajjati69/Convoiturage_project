package ma.fst.projet.my_new_application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.*;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etPhone;
    Button   btnRegister, btnGoLogin;
    TextView tvError;
    RadioButton rbDriver, rbPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        etPhone     = findViewById(R.id.etPhone);
        rbDriver    = findViewById(R.id.rbDriver);
        rbPassenger = findViewById(R.id.rbPassenger);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoLogin  = findViewById(R.id.btnGoLogin);
        tvError     = findViewById(R.id.tvError);

        btnRegister.setOnClickListener(v -> sInscrire());
        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void sInscrire() {
        String name     = etName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone    = etPhone.getText().toString().trim();
        String role     = rbDriver.isChecked() ? "driver" : "passenger";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Nom, email et mot de passe sont obligatoires");
            return;
        }
        if (password.length() < 4) {
            showError("Mot de passe trop court (4 caractères minimum)");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Inscription...");
        tvError.setVisibility(View.GONE);

        JSONObject body = new JSONObject();
        try {
            body.put("name",     name);
            body.put("email",    email);
            body.put("password", password);
            body.put("phone",    phone);
            body.put("role",     role);
        } catch (JSONException e) { e.printStackTrace(); return; }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                "http://10.0.2.2:5000/register",
                body,
                response -> {
                    Toast.makeText(this,
                            "✅ Compte créé ! Connectez-vous.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                },
                error -> {
                    String msg = "Erreur lors de l'inscription";
                    if (error.networkResponse != null) {
                        if (error.networkResponse.statusCode == 409)
                            msg = "Cet email est déjà utilisé";
                        else if (error.networkResponse.statusCode == 400)
                            msg = "Données invalides";
                    } else {
                        msg = "Serveur inaccessible";
                    }
                    showError(msg);
                    btnRegister.setEnabled(true);
                    btnRegister.setText("Créer mon compte");
                }
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}