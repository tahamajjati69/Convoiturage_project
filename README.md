#  **Projet Covoiturage Personnel (Ville)**

<p align="center">
<img width="500" height="500" alt="mobile (1)" src="https://github.com/user-attachments/assets/3a281238-a9a1-46c7-89cf-16a9190c2b08" />
</p>

## 1. 📜 **Objectif du Projet**
Développer une plateforme de **covoiturage urbain** permettant aux **conducteurs** de partager leurs trajets quotidiens, et aux **passagers** de réserver des places disponibles pour leurs trajets. Cette application inclut aussi une interface d'administration pour la gestion des trajets et des réservations.

---

## 2. 🔑 **Fonctionnalités Principales**

### 🚘 **Gestion des Trajets**
-  **Publier un trajet** (ville de départ, d'arrivée, prix, places disponibles).
-  **Rechercher un trajet** en fonction de la ville de départ et de l'arrivée.

### 🛣️ **Gestion des Réservations**
-  **Réserver une place** pour un trajet.
-  **Visualiser les réservations** effectuées.
-  **Annuler une réservation**.

### ⭐ **Gestion des Avis**
-  **Évaluer un trajet** après l'avoir effectué.
-  **Ajouter un commentaire** optionnel sur le trajet.

---

## 3. 🏗️ **Architecture du Projet**
Le projet est composé de plusieurs parties : le **frontend**, le **backend**, et la gestion des **réservations** et des **trajets**.  <br>


<img width="1408" height="768" alt="projet_struct" src="https://github.com/user-attachments/assets/021074c1-4af3-4b0f-849c-da83d2b34ef4" />


---

## 4. 📊 **Diagramme d'architecture**

<img width="983" height="178" alt="image" src="https://github.com/user-attachments/assets/e60766fa-d15e-4f5f-94e4-91daa4ca24c7" />

---

## 5. 🗃️ **Base de Données (MySQL)**

Voici le script SQL pour créer la base de données du projet. Cette base de données contient les tables nécessaires pour gérer les utilisateurs, les trajets et les réservations.

```sql
CREATE DATABASE covoiturage_ville;

USE covoiturage_ville;

-- Table users
CREATE TABLE users (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('driver', 'passenger', 'admin') NOT NULL,
    city VARCHAR(100),
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table rides
CREATE TABLE rides (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    driver_id INT(11) NOT NULL,
    departure_city VARCHAR(100) NOT NULL,
    arrival_city VARCHAR(100) NOT NULL,
    departure_location VARCHAR(150),
    arrival_location VARCHAR(150),
    ride_date DATE NOT NULL,
    ride_time TIME NOT NULL,
    available_seats INT(11) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status ENUM('open', 'full', 'cancelled') DEFAULT 'open',
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES users(id)
);

-- Table ride_bookings
CREATE TABLE ride_bookings (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    ride_id INT(11) NOT NULL,
    passenger_id INT(11) NOT NULL,
    seats_reserved INT(11) NOT NULL,
    booking_status ENUM('pending', 'accepted', 'rejected', 'cancelled') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id),
    FOREIGN KEY (passenger_id) REFERENCES users(id)
);

-- Table ride_reviews
CREATE TABLE ride_reviews (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    ride_id INT(11) NOT NULL,
    passenger_id INT(11) NOT NULL,
    rating INT(11) NOT NULL,
    comment VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id),
    FOREIGN KEY (passenger_id) REFERENCES users(id)
);
```
##  **Video Demonstration :**









https://github.com/user-attachments/assets/6bc518f2-fc89-4222-b62a-99ff1f75b28e


