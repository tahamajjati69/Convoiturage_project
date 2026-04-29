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
CREATE DATABASE co_voiturage;

USE co_voiturage;

CREATE TABLE utilisateurs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nom VARCHAR(100),
  email VARCHAR(100),
  role ENUM('conducteur', 'passager', 'admin') DEFAULT 'passager'
);

CREATE TABLE trajets (
  id INT PRIMARY KEY AUTO_INCREMENT,
  conducteur_id INT,
  ville_depart VARCHAR(100),
  ville_arrivee VARCHAR(100),
  prix DECIMAL(10,2),
  places_disponibles INT,
  FOREIGN KEY (conducteur_id) REFERENCES utilisateurs(id)
);
```
##  **Video Demonstration :**







CREATE TABLE reservations (
  id INT PRIMARY KEY AUTO_INCREMENT,
  passager_id INT,
  trajet_id INT,
  statut ENUM('acceptée', 'en attente', 'refusée'),
  FOREIGN KEY (passager_id) REFERENCES utilisateurs(id),
  FOREIGN KEY (trajet_id) REFERENCES trajets(id)
);
