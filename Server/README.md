# Serveur de Communication UDP-Série

## Description

Ce programme établit un serveur UDP pour la communication entre une application Android et un microcontrôleur via une connexion série. Il permet d'envoyer des commandes au microcontrôleur et de recevoir des données de celui-ci.

## Dépendances

- Python 3
- Bibliothèque pyserial (`pip install pyserial`)

## Installation

1. Clonez le dépôt : `git clone https://github.com/votre/utilisateur/serveur-udp-serie.git`
2. Accédez au répertoire du projet : `cd serveur-udp-serie`

## Utilisation

1. Connectez le microcontrôleur au port série spécifié (`SERIALPORT`).
2. Lancez le serveur : `python serveur.py`
3. Le serveur écoutera les messages UDP sur l'adresse IP spécifiée (`HOST`) et le port (`UDP_PORT`).
4. Envoyez des commandes depuis une application Android pour contrôler le microcontrôleur.

## Configuration

- `HOST` : Adresse IP sur laquelle le serveur UDP écoute.
- `UDP_PORT` : Port UDP pour la communication.
- `MICRO_COMMANDS` : Liste des commandes reconnues par le microcontrôleur.
- `DATABASE` : Fichier de base de données SQLite pour stocker les données reçues.
- `SERIALPORT` : Port série auquel le microcontrôleur est connecté.
- `BAUDRATE` : Débit en bauds pour la communication série.

## Protocole de Communication

- Envoyez la commande "getValues()" pour récupérer les dernières valeurs reçues du microcontrôleur.
- Les autres commandes sont envoyées directement au microcontrôleur via UART.

## Base de Données

Le programme enregistre les données reçues dans une base de données SQLite (`dbiot.db`). La table `data` stocke la température, la luminosité et l'horodatage.
