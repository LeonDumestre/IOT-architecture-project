# Documentation de la MicroBit Station Météo

## Introduction

Cette documentation fournit des informations sur le code de la station météo MicroBit, cette dernière récupère des données de différents capteurs externes auxquels elle est connectée, les affiche sur un écran mono-couleur et les transfert également à la passerelle.

## Configuration

Le code initialise la MicroBit, configure les écouteurs d'événements nécessaires et paramètre le groupe radio. La boucle principale du programme assure un fonctionnement continu avec une pause de 10 secondes à chaque itération.

## Initialisation

La MicroBit est initialisé avec `uBit.init()`, et un message "INIT" est affiché sur la matrice de LEDs du MicroBit.

## Communication Radio

La radio est activée et configurée pour fonctionner dans le groupe 28.

Lorsqu'un message est reçu par radio, la fonction `onData` est lancée. Celle-ci est responsable du déchiffrement du message et de son décodage afin de réaliser l'action demandée par le message, soit l'inversion de l'ordre d'affichage des données sur l'écran mono-couleur. 

La fonction `sendData`, quant à elle, chiffre le message à envoyer puis le transmet par la radio. Cette dernière est appelée à une fréquence de 10 secondes afin de ne pas encombrer le trafic des datagrammes.

## Chiffrement

Le chiffrement est mis en œuvre à l'aide d'une clé prédéfinie. La clé est un tableau de 128 bits (16 octets) défini dans le code. Les fonctions encrypt et decrypt sont utilisées pour chiffrer et déchiffrer les messages, respectivement.

## Capteurs

Les capteurs avec lesquels est interfacée la MicroBit station météo sont regroupés au sein d'une puce développée par Techno-Innov. Ce module dispose de divers capteurs, dans notre cas, nous nous sommes intéressés aux deux capteurs suivants :
- TLS2561 : Lumière visible et infra-rouge
- BME280 : Température, Humidité, Pression

Ces derniers sont connectés à la MicroBit via l'interface I2C du module sensors et permettent la récupération des valeurs de température et de luminosité affichées sur l'écran mono-couleur.