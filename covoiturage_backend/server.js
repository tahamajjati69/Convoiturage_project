const express = require("express");
const cors = require("cors");
const mysql = require("mysql2");

const app = express();

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "",
  database: "convoiturae_ville"
});

db.connect(err => {
  if (err) console.error("Erreur connexion MySQL :", err);
  else console.log("Connecté à MySQL");
});

app.get("/", (req, res) => res.send("API covoiturage fonctionne"));

// ══════════════════════════════════
//  AUTH — LOGIN
// ══════════════════════════════════
app.post("/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password)
    return res.status(400).json({ message: "Email et mot de passe obligatoires" });

  db.query(
    "SELECT id, name, email, role FROM users WHERE email = ? AND password = ?",
    [email, password],
    (err, results) => {
      if (err) return res.status(500).json(err);
      if (results.length === 0)
        return res.status(401).json({ message: "Email ou mot de passe incorrect" });
      const u = results[0];
      res.json({ id: u.id, name: u.name, email: u.email, role: u.role });
    }
  );
});

// ══════════════════════════════════
//  AUTH — REGISTER ✅ NOUVEAU
// ══════════════════════════════════
app.post("/register", (req, res) => {
  const { name, email, password, phone, role } = req.body;

  if (!name || !email || !password || !role)
    return res.status(400).json({ message: "Nom, email, mot de passe et rôle sont obligatoires" });

  if (!["driver", "passenger"].includes(role))
    return res.status(400).json({ message: "Rôle invalide" });

  // Vérifier si email déjà utilisé
  db.query("SELECT id FROM users WHERE email = ?", [email], (err, rows) => {
    if (err) return res.status(500).json(err);
    if (rows.length > 0)
      return res.status(409).json({ message: "Cet email est déjà utilisé" });

    const sql = `INSERT INTO users (name, email, password, phone, role, is_active)
                 VALUES (?, ?, ?, ?, ?, 1)`;
    db.query(sql, [name, email, password, phone || null, role], (err2, result) => {
      if (err2) return res.status(500).json(err2);
      res.json({ message: "Compte créé avec succès", id: result.insertId });
    });
  });
});

// ══════════════════════════════════
//  RIDES
// ══════════════════════════════════
app.get("/rides", (req, res) => {
  db.query("SELECT * FROM rides", (err, result) => {
    if (err) return res.status(500).json(err);
    res.json(result);
  });
});

app.post("/rides", (req, res) => {
  const { driver_id, departure_city, arrival_city, ride_date, ride_time, available_seats, price } = req.body;
  const sql = `INSERT INTO rides (driver_id, departure_city, arrival_city, ride_date, ride_time, available_seats, price)
               VALUES (?, ?, ?, ?, ?, ?, ?)`;
  db.query(sql, [driver_id, departure_city, arrival_city, ride_date, ride_time, available_seats, price], err => {
    if (err) { console.error("Erreur ajout trajet :", err); return res.status(500).json(err); }
    res.json({ message: "Trajet ajouté" });
  });
});

// ══════════════════════════════════
//  BOOKINGS ✅ CORRIGÉ (anti-doublon)
// ══════════════════════════════════
app.post("/bookings", (req, res) => {
  const { ride_id, passenger_id } = req.body;
  if (!ride_id || !passenger_id)
    return res.status(400).json({ message: "ride_id et passenger_id sont obligatoires" });

  // Vérifier si déjà réservé
  db.query(
    "SELECT id FROM ride_bookings WHERE ride_id = ? AND passenger_id = ?",
    [ride_id, passenger_id],
    (err, rows) => {
      if (err) return res.status(500).json(err);
      if (rows.length > 0)
        return res.status(409).json({ message: "Vous avez déjà réservé ce trajet" });

      const sql = `INSERT INTO ride_bookings (ride_id, passenger_id, seats_reserved, booking_status)
                   VALUES (?, ?, 1, 'pending')`;
      db.query(sql, [ride_id, passenger_id], (err2, result) => {
        if (err2) { console.error("Erreur réservation :", err2); return res.status(500).json(err2); }
        res.json({ message: "Réservation ajoutée", id: result.insertId });
      });
    }
  );
});

app.get("/bookings/user/:userId", (req, res) => {
  const sql = `
    SELECT rb.id, r.id AS ride_id,
           r.departure_city, r.arrival_city, r.price,
           rb.booking_status AS statut
    FROM ride_bookings rb
    JOIN rides r ON rb.ride_id = r.id
    WHERE rb.passenger_id = ?
    ORDER BY rb.id DESC`;
  db.query(sql, [req.params.userId], (err, result) => {
    if (err) { console.error("Erreur historique :", err); return res.status(500).json(err); }
    res.json(result);
  });
});

// ══════════════════════════════════
//  ADMIN — BOOKINGS
// ══════════════════════════════════
app.get("/admin/bookings", (req, res) => {
  const sql = `
    SELECT rb.id, u.name AS passager,
           r.departure_city AS ville_depart,
           r.arrival_city AS ville_arrivee,
           rb.booking_status AS statut
    FROM ride_bookings rb
    JOIN users u ON rb.passenger_id = u.id
    JOIN rides r ON rb.ride_id = r.id
    ORDER BY rb.id DESC`;
  db.query(sql, (err, result) => {
    if (err) return res.status(500).json(err);
    res.json(result);
  });
});

app.put("/admin/bookings/:id", (req, res) => {
  const { statut } = req.body;
  db.query(
    "UPDATE ride_bookings SET booking_status = ? WHERE id = ?",
    [statut, req.params.id],
    err => {
      if (err) { console.error("Erreur modification statut :", err); return res.status(500).json(err); }
      res.json({ message: "Statut mis à jour" });
    }
  );
});

// ══════════════════════════════════
//  REVIEWS
// ══════════════════════════════════
app.post("/reviews", (req, res) => {
  const { ride_id, passenger_id, rating, comment } = req.body;

  if (!ride_id || !passenger_id || !rating)
    return res.status(400).json({ message: "ride_id, passenger_id et rating sont obligatoires" });

  if (rating < 1 || rating > 5)
    return res.status(400).json({ message: "La note doit être entre 1 et 5" });

  // Règle R2 : vérifier participation
  db.query(
    "SELECT id FROM ride_bookings WHERE ride_id = ? AND passenger_id = ? AND booking_status = 'accepted' LIMIT 1",
    [ride_id, passenger_id],
    (err, rows) => {
      if (err) return res.status(500).json(err);
      if (rows.length === 0)
        return res.status(403).json({ message: "Vous devez avoir participé à ce trajet pour le noter" });

      // Vérifier doublon avis
      db.query(
        "SELECT id FROM ride_reviews WHERE ride_id = ? AND passenger_id = ? LIMIT 1",
        [ride_id, passenger_id],
        (err2, existing) => {
          if (err2) return res.status(500).json(err2);
          if (existing.length > 0)
            return res.status(409).json({ message: "Vous avez déjà noté ce trajet" });

          db.query(
            "INSERT INTO ride_reviews (ride_id, passenger_id, rating, comment) VALUES (?, ?, ?, ?)",
            [ride_id, passenger_id, rating, comment || null],
            (err3, result) => {
              if (err3) return res.status(500).json(err3);
              res.json({ message: "Avis ajouté avec succès", id: result.insertId });
            }
          );
        }
      );
    }
  );
});

app.get("/admin/reviews", (req, res) => {
  const sql = `
    SELECT rr.id, rr.rating, rr.comment, rr.created_at,
           u.name AS passager,
           r.departure_city, r.arrival_city
    FROM ride_reviews rr
    JOIN users u ON rr.passenger_id = u.id
    JOIN rides r ON rr.ride_id = r.id
    ORDER BY rr.id DESC`;
  db.query(sql, (err, result) => {
    if (err) return res.status(500).json(err);
    res.json(result);
  });
});

app.delete("/admin/reviews/:id", (req, res) => {
  db.query("DELETE FROM ride_reviews WHERE id = ?", [req.params.id], err => {
    if (err) return res.status(500).json(err);
    res.json({ message: "Avis supprimé" });
  });
});

// ══════════════════════════════════
//  ADMIN — STATS
// ══════════════════════════════════
app.get("/admin/stats", (req, res) => {
  const sql = `
    SELECT
      (SELECT COUNT(*) FROM rides)                                           AS total_rides,
      (SELECT COUNT(*) FROM ride_bookings)                                   AS total_bookings,
      (SELECT COUNT(*) FROM ride_bookings WHERE booking_status = 'accepted') AS accepted_bookings,
      (SELECT COUNT(*) FROM ride_bookings WHERE booking_status = 'pending')  AS pending_bookings,
      (SELECT ROUND(AVG(rating), 1) FROM ride_reviews)                       AS avg_rating,
      (SELECT COUNT(*) FROM ride_reviews)                                    AS total_reviews`;
  db.query(sql, (err, result) => {
    if (err) return res.status(500).json(err);
    res.json(result[0]);
  });
});

// ══════════════════════════════════
//  REVIEWS PAR TRAJET — Android
// ══════════════════════════════════
app.get("/reviews/ride/:rideId", (req, res) => {
  const sql = `
    SELECT rr.id, rr.rating, rr.comment, rr.created_at, u.name AS passager
    FROM ride_reviews rr
    JOIN users u ON rr.passenger_id = u.id
    WHERE rr.ride_id = ?
    ORDER BY rr.id DESC`;
  db.query(sql, [req.params.rideId], (err, result) => {
    if (err) return res.status(500).json(err);
    res.json(result);
  });
});

// ══════════════════════════════════
app.listen(5000, () => console.log("Serveur lancé sur http://localhost:5000"));