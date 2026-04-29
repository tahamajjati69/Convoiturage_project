#  **Projet Covoiturage Personnel (Ville)**

## 1. 📜 **Objectif du Projet**
Développer une plateforme de **covoiturage urbain** permettant aux **conducteurs** de partager leurs trajets quotidiens, et aux **passagers** de réserver des places disponibles pour leurs trajets. Cette application inclut aussi une interface d'administration pour la gestion des trajets et des réservations.

---

## 2. 🔑 **Fonctionnalités Principales**

### 🚘 **Gestion des Trajets**
-  **Publier un trajet** (ville de départ, d'arrivée, prix, places disponibles).
-  **Rechercher un trajet** en fonction de la ville de départ et de l'arrivée.
-  **Annuler un trajet** (en tant que conducteur).

### 🛣️ **Gestion des Réservations**
-  **Réserver une place** pour un trajet.
-  **Visualiser les réservations** effectuées.
-  **Annuler une réservation**.

### ⭐ **Gestion des Avis**
-  **Évaluer un trajet** après l'avoir effectué.
-  **Ajouter un commentaire** optionnel sur le trajet.

---

## 3. 🏗️ **Architecture du Projet**
Le projet est composé de plusieurs parties : le **frontend**, le **backend**, et la gestion des **réservations** et des **trajets**. Le modèle relationnel et les bases de données sont structurés comme suit :

![Modèle relationnel](path/to/database_diagram.png)

---

## 4. 📊 **Modèle relationnel du système de gestion des livraisons**
Le système utilise une base de données relationnelle pour gérer les utilisateurs, les trajets et les réservations. Voici le diagramme de la structure de la base de données :

![Base de données](path/to/database_schema.png)

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

CREATE TABLE reservations (
  id INT PRIMARY KEY AUTO_INCREMENT,
  passager_id INT,
  trajet_id INT,
  statut ENUM('acceptée', 'en attente', 'refusée'),
  FOREIGN KEY (passager_id) REFERENCES utilisateurs(id),
  FOREIGN KEY (trajet_id) REFERENCES trajets(id)
);
