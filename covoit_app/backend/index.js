const express = require('express');
const cors = require('cors');
const db = require('./config/db');

const app = express();
app.use(cors());
app.use(express.json());

// Voir les réservations
app.get('/admin/bookings', async (req, res) => {
    try {
        const sql = `
            SELECT b.id, u.nom AS passager, r.ville_depart, r.ville_arrivee, b.statut 
            FROM ride_bookings b
            JOIN users u ON b.id_passager = u.id
            JOIN rides r ON b.id_ride = r.id
            ORDER BY b.id DESC
        `;
        const [rows] = await db.query(sql);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Mettre à jour le statut (On utilise des mots sans accents pour la sécurité)
app.put('/admin/bookings/:id', async (req, res) => {
    const { id } = req.params;
    const { statut } = req.body; 
    try {
        const sql = "UPDATE ride_bookings SET statut = ? WHERE id = ?";
        await db.query(sql, [statut, id]);
        console.log(`✅ Réservation ${id} mise à jour : ${statut}`);
        res.json({ message: "Succès" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.get('/rides', async (req, res) => {
    const [rows] = await db.query('SELECT * FROM rides');
    res.json(rows);
});

app.listen(3000, () => console.log("🚀 Serveur sur port 3000"));