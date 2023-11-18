# Documentation de l'Application Android IoT

## Introduction

Cette documentation fournit une vue d'ensemble du fonctionnement de l'application Android IoT.
L'application est conçue pour interagir avec un réseau via des sockets UDP, permettant ainsi d'échanger des éléments d'interface utilisateur et de recevoir des données de capteurs.

## Fonctionnalités Principales

### Configuration du Réseau

L'application permet à l'utilisateur de configurer l'adresse IP et le port pour établir une connexion réseau via une interface utilisateur simple.
Une fois les informations réseau complétées, l'utilisateur appuie sur le bouton pour initialiser le réseau.
L'application initialise alors une nouvelle socket UDP et configure les paramètres réseau nécessaires.

### Réception de Données de Capteurs

L'application ANdroid est conçu pour recevoir des données de capteurs.
Actuellement, elle peut uniquement recevoir des données de lumière et de température.

Le thread `AskValueThread` envoie en continu des requêtes demandant l'envoi de données.
Cela peremet à la cible d'envoyer les données de capteurs en réponse à la requête.
Un thread d'écoute (`ListenThread`) est constamment en cours d'exécution, écoutant les données entrantes sur la socket UDP.
Lorsqu'une nouvelle donnée est reçue, elle est traitée et affichée à l'utilisateur.

### Échange d'Éléments d'Interface Utilisateur

L'utilisateur peut échanger les éléments d'interface utilisateur en appuyant sur le bouton d'échange.
Cette opération échange le texte affiché dans deux zones de texte spécifiques à l'interface utilisateur.
Echanger les deux données envoie également une requête via `TalkThread` indiquant le nouvel ordre.

## Structure du Code

L'application est structurée en plusieurs classes qui interagissent pour réaliser ses fonctionnalités.
Les classes principales comprennent :

- `MainActivity`: La classe principale qui gère l'interface utilisateur et coordonne les opérations réseau.
- `AskValueThread`: Un thread qui envoie périodiquement une demande de valeurs de capteurs sur le réseau.
- `ListenThread`: Un thread qui écoute continuellement les données de capteurs entrantes sur la socket UDP.
- `TalkThread`: Un thread qui envoie des messages sur le réseau.

## Utilisation du Code

Le code est commenté pour fournir des explications sur chaque section.
Il utilise des `ExecutorService` pour gérer les threads de manière efficace et utilise des constantes pour rendre le code lisible et éviter les fautes de frappe.

## Gestion des Ressources

La gestion des ressources réseau, telle que la fermeture propre de la socket, est assurée pour éviter les fuites de ressources et garantir un fonctionnement fiable de l'application.
