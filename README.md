# IOT-architecture-project

## Fonctionnalités
Le projet est donc séparé en quatre parties distinctes : 

### Application Android
L’application Android développée possède plusieurs fonctionnalités : 
- L’affichage des données d’un capteur de luminosité et de température
- Le changement de l’ordre des données affichées sur une Micro:bit distantes

L’interface minimale permet, dans un premier temps, de renseigner une adresse IP et un port d’un serveur cible. 
Une fois les champs remplies, cliquer sur le bouton “Initialiser le réseau” permet de lancer deux threads qui vont tourner en continu : 
Le premier envoie le message “getValues()” toutes les 10 secondes. Cela va permettre à la cible de savoir à qui envoyer les données en retour.
Le second reçoit simplement les données. Une fois reçu, le thread retourne la donnée reçue et la traite pour l’afficher dans l’activité principale.
Les données des capteurs sont donc mise à jour sur l’interface de l’application.

Il est également possible de changer l’ordre des capteurs affichés sur une interface distante. Pour cela, un simple bouton “Echanger” envoie un message contenant le nouvel ordre via un thread.

### Passerelle Serveur
Le serveur à un rôle central dans le fonctionnement de l’architecture du projet. Son objectif est de faire le lien entre les Micro Bits et l’application Android.

#### Lien avec les Micro:bit
Ici, l’idée est de stocker des données qui sont reçues par la passerelle. Il va donc lire sur son port UART les informations qui lui seront transmises, et les stocker en fonction de leur timestamp dans une base de données SQLite afin de pouvoir, si besoin, interrogé les données pour constater (ou non) une évolution de ces dernières à travers le temps (évolution possible).
Dans un second temps, le serveur va aussi stocker la configuration de l’affichage pour l’écran de la Micro:bit ( pour savoir si la température sera affichée avant ou après la luminosité ). Cette configuration est disponible dans la base de données SQLite et est accessible à tout moment à la demande des cartes.

#### Lien avec l’application Android 
Deux fonctionnalités sont disponibles pour le lien entre le serveur et l’application Android. La première est l'envoi des données stockées par le serveur (données de température et de luminosité). L’application peut en faire la demande en envoyant “getValues()” au serveur via packet UDP, la réponse sera ensuite envoyée à l'adresse IP via laquelle le message aura été envoyé.
La deuxième fonctionnalité est la gestion de l’inversion de l’affichage des données. L’application à la possibilité d’envoyer au serveur l’ordre dans lequel elle souhaite afficher les données sur l’écran de la Micro:bit météo. Cette demande va être traitée par le serveur en 2 temps : Le serveur va stocker l’information d’affichage en base de donnée, puis va transmettre cette nouvelle configuration à la passerelle pour qu’elle puisse être transmise aux autres Micro:bit.

### Micro:bit
#### Passerelle Micro:bit
Pour que notre architecture fonctionne et que la communication se fasse correctement, nous avons une Micro:bit qui joue le rôle de passerelle. En effet, elle reçoit des messages de la part du serveur python et renvoie l’information à l’autre Micro:bit en prenant soin de chiffrer le message pour que celui-ci ne soit pas compromis.

Le principe est de s’assurer que les messages transitent correctement, ce qui implique de lire les informations que le serveur envoie sur le port série et de les retranscrire via un message radio chiffré. La passerelle doit également assurer le chemin inverse, à savoir de recevoir des messages par radio, de les déchiffrer puis de les envoyer au serveur via le port série.

#### Micro:bit Station Météo
Au sein de notre architecture, la Micro:bit station météo correspond au microcontrôleur programmé pour collecter des données à partir de ses capteurs et afficher des informations sur un écran OLED.

Elle agit en tant qu’acteur interfacé avec une puce développée par Techno-Innov regroupant divers capteurs. Nous nous sommes intéressés tout particulièrement à deux d’entre eux : 
Le TLS2561 recueillant des informations quant à la lumière visible et infra-rouge
Le BME280 récupérant la température, l’humidité ainsi que la pression environnante

La connexion entre ces deux éléments est réalisée via l’interface I2C du module sensors de la Micro:bit. Les informations relevées, dans notre cas, la luminosité ainsi que la température ambiante, sont affichées sur l’écran OLED connecté à la Micro:bit via l’interface I2C également. Ces dernières sont également envoyées à intervalles réguliers de 10 secondes à la Micro:bit passerelle qui se chargera de transmettre le message.

La disposition des données sur l’écran est réglable depuis l’application android citée plus tôt. Les messages indiquant cette fameuse disposition parviennent à la station météo par radio depuis la Micro:bit passerelle.

## Protocole de communication
### Changement de l’ordre d’affichage des capteurs
Les messages envoyés par l’application Android peuvent être “L;T~” ou “T;L~”.
“L” correspond à la luminosité et “T” à la température. L'ordre des lettres correspond donc à l’ordre d’affichage des données. Le “;” permet de séparer distinctement les données et le “~” montre la fin du message pour qu’il puisse être traiter dans sa globalité par les appareils cibles.

À l’avenir, on peut penser étendre ce principe avec d’autres capteurs, par exemple l’humidité et les messages seraient ressemblant à: “T;L;H~”.
Le message est structuré de manière à être comme un dérivé de fichier CSV, permettant ainsi de futur ajouts. 
### Chiffrement des messages radio inter-Micro:bit
Les messages transmis par le canal radio sont sensibles. C’est pourquoi nous avons mis en place un chiffrement utilisant l’algorithme AES ECB via la librairie ‘TinyAES’ (https://github.com/kokke/tiny-AES-c). Nous mettons en œuvre celui-ci avec une clé de 128 bits directement présente dans les fichiers C++.
