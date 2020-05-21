---
title: Movie Score
author:
- Jeremy Favre
- Steven Liatti
papersize : a4
fontsize : 12pt
geometry : margin = 3cm
---

# Buts
Les buts de ce projet sont de déterminer les scores de films selon leur ratio revenu sur budget, de déterminer l'influence qu'ils ont sur les "peoples" (acteurs et membres de l'équipe de réalisation) qui ont participé aux-dits films et montrer les genres de films les plus populaires.

# Contexte et objectifs
Ce projet se déroule dans le cadre du cours de Web Mining du master MSE HES-SO. L'objectif principal du projet est de visualiser, sous forme graphe, les relations entre les films, leurs genres et les peoples. Nous appliquons des algorithmes choisis pour déterminer les noeuds du graphe les plus importants, trouver les plus courts chemins entre peoples et former des communautés de films, peoples ou genres.

# Données
Nous utilisons les données provenant de [The Movie Database (TMDb)](https://www.themoviedb.org/) à l'aide de son [API](https://developers.themoviedb.org/3/getting-started). À partir des données d'un film, nous avons tous les éléments nécessaires pour déterminer son score et ses relations aux peoples et genres associés. Ci-dessous un exemple de données retournées par l'API pour le film *Pirates of the Caribbean: The Curse of the Black Pearl* :

```json
{
    "budget": 140000000,
    "revenue": 655011224,
    "genres": [
        { "id": 12, "name": "Adventure" },...
    ],
    "id": 22,
    "title": "Pirates of the Caribbean: The Curse of the Black Pearl",
    "original_language": "en",
    "overview": "Jack Sparrow, a freewheeling ...",
    "release_date": "2003-07-09",
    "runtime": 143,
    "spoken_languages": [
        { "iso_639_1": "en", "name": "English"}
    ],
    "status": "Released",
    "popularity": 54.847,
    "vote_average": 7.7,
    "vote_count": 14020,
    "credits": {
        "cast": [
            {
                "cast_id": 12,
                "character": "Captain Jack Sparrow",
                "id": 85,
                "name": "Johnny Depp",
                "order": 0
            },
            {
                "cast_id": 5,
                "character": "Captain Hector Barbossa",
                "id": 118,
                "name": "Geoffrey Rush",
                "order": 1
            },...
        ],
        "crew": [
            {
                "department": "Directing",
                "id": 1704,
                "job": "Director",
                "name": "Gore Verbinski"
            },...
        ]
    },
    "keywords": {
        "keywords": [
            { "id": 911, "name": "exotic island" },
            { "id": 1318, "name": "blacksmith" },
            { "id": 1321, "name": "gold" },...
        ]
    },
    "similar": {
        "results": [
            {
                "id": 58,
                "title": "Pirates of the Caribbean: Dead Man's Chest",
                "release_date": "2006-06-20",
                "original_language": "en",
                "genre_ids": [ 28, 12, 14 ],
                "overview": "Captain Jack Sparrow works ...",
                "popularity": 31.344,
                "vote_count": 10605,
                "vote_average": 7.2
            },...
        ]
    }
}
```

Nous pouvons voir que nous avons les informations relatives au budget et revenu généré par le film, ainsi que l'ordre d'importance des acteurs ayant joué dedans et la liste des membres de l'équipe de réalisation, avec pour chacun la fonction endossée pour ce film. Nous avons également la liste des genres auxquels le film appartient, une liste de mots-clés concernant ce film et une liste de films similaires (basée sur les genres et mots-clés). Cette information peut être récupérée sur une URL de ce genre :

```bash
https://api.themoviedb.org/3/movie/$id?api_key=$key&language=en-US&append_to_response=credits%2Ckeywords%2Csimilar
```

La première étape est d'obtenir tous les films disponibles sur TMDb. Pour cela, l'API offre un export journalier de quelques informations basiques concernant les films, à savoir son id dans TMDb, son titre original, la popularité au sein du site et des indications si présence de vidéos et/ou contenu adulte, un exemple est visible ci-dessous. Le seul champs qui nous intéresse est l'id, qui nous permet de récuperer plus d'informations sur les films telles que présentées ci-dessus.

```json
{
    "adult": false,
    "id": 20,
    "original_title": "My Life Without Me",
    "popularity": 9.134,
    "video": false
}
{
    "adult": false,
    "id": 24,
    "original_title": "Kill Bill: Vol. 1",
    "popularity": 24.601,
    "video": false
}
{
    "adult": false,
    "id": 25,
    "original_title": "Jarhead",
    "popularity": 14.574,
    "video": false
}
```

Nous devons donc parcourir ce fichier, comportant plus de 500'000 films et récupérer les informations au format JSON.



# Architecture

![Architecture](report/architecture.svg)

Tout d'abord, un programme est dédié à la récupération de données proprement dites, à partir de l'API fournissant les données en JSON, enregistrant les données pour les films ayant les champs revenu et budget valides. Ensuite, ces données seront manipulées et insérées dans la base de donnée choisie de manière cohérente et selon les besoins de l'interface. Finalement, une interface graphique sera implémentée pour visualiser les graphes obtenus et exécuter des requêtes à la base de données.

## Uses cases
Voici la liste des principaux *uses cases* du système : 

- Visualiser l'interconnexion (via un graphe) entre les différentes entités représentées dans la base de données (films, acteurs, genres)
- Modifier l'affichage de ce graphe en spécifiant certains critères de recherche
- Visualiser (à l'aide du graphe) l'importance des films en fonction de leurs score (revenu/budget)
- Lister les différents films répertoriés dans notre base
- Rechercher films, *peoples* ou genres
- Visualiser les communautés d'acteurs, de genre ou de films qui ont des critères communs
- Visualiser le plus court chemin entre deux entités

# Technologies

Les langages et technologies envisagés sont Scala et/ou Rust pour la partie développement, pour la justesse, l'efficacité et/ou la performance, et OrientDB, ArangoDB ou Neo4j pour la base de données, avec leur propres langages de requêtes. Différents outils pour le frontend / visualisation sont également comparés.


## Bases de données

Nous pensons que le modèle de base de données orienté graphe est adapté à notre cas de figure, les différentes entités peuvent être représentées comme des noeuds d'un graphe et les liens les composants peuvent être vus comme des arcs ou arêtes du graphe.

### OrientDB
OrientDB est une base de données écrite en Java, multi paradigme, stockant ses données sous forme de documents, pair clé-valeur ou graphe. Elle met en avant sa scalabilité horizontale, avec la possibilité de déployer une base OrientDB sur plusieurs machines. Elle utilise un langage de requête optimisé pour les requêtes dans un graphe, nommé Gremlin.

### ArangoDB
ArangoDB offre à peu près les mêmes fonctionnalités qu'OrientDB, c'est une base de données écrite en C++, multi paradigme, stockant ses données sous forme de documents, pair clé-valeur ou graphe. Elle met en avant des fonctionnalités comme un support pour données géographiques (GeoJSON) ou ArangoML, pipeline pour machine learning. Elle utilise un langage de requête optimisé pour les requêtes dans un graphe, nommé AQL.

### Neo4j
Noeo4j est une base de données écrite en Java/Scala stockant ses données sous forme de graphe, avec le contenu des données comme noeud ou sommet du graphe et les relations entre ces données comme arête ou arc du graphe. Elle utilise un langage de requête optimisé pour les requêtes dans un graphe, nommé Cypher.

### Bilan des bases de données
Après avoir comparé ces trois technologies, nous pensons utiliser Neo4j. Bien qu'elle soit moins scalable que les deux autres et apparemment moins performante sur des énormes jeux de données, elle offre nativement d'une part de nombreuses requêtes implémentant des algorithmes de centralité des noeuds de graphes, comme PageRank, ArticleRank ou de plus court chemin et d'autre part des méthodes de visualisation plus faciles à prendre en main. Étant donné le volume de données que nous allons traiter (sur les 500'000 films recensés sur TMDb, nous pensons qu'au moins quelques milliers ou dizaines de milliers ont les informations qui nous intéressent), les défauts apparents de Neo4j sont comblés par ces deux *killer features* qui comblent nos besoins.

## Frontend

Nous envisageons de réaliser l'interface sous forme de page web montrant le graphe des films, *peoples* et genres et avec des contrôles permettant d'exécuter des requêtes. Nous avons trouvé différentes librairies Javascript qui facilitent le dessin de graphes dans le navigateur, telles que [D3.js](https://d3js.org/), [Vis.js](http://visjs.org/), [Sigma.js](http://sigmajs.org/) ou encore [Cytoscape.js](http://js.cytoscape.org/). Toutes proposent une API ou les noeuds et arêtes du graphe peuvent être personnalisés, cela va du choix des couleurs à la taille des noeuds ou arêtes en passant par la position ou manipulation à la souris (zoom, déplacement, etc.). Dans le même sens de simplicité et de facilité d'intégration avec Neo4j, nos choix se portent sur deux autres librairies : [neovis.js](https://github.com/neo4j-contrib/neovis.js) ou [NeoSig](https://github.com/sim51/neosig). Ce sont deux wrappers se basant respectivement sur Vis.js et Sigma.js mais qui facilitent l'intégration avec des données provenant directement de Neo4j.


# Implémentation

## Features
Voici la liste des différentes fonctionnalités que nous allons réaliser dans le cadre de ce projet :

- Backend :

    - Récupération des données sur les films mises à disposition par TMDb
    - Traitement de ces données pour sélectionner uniquement ce dont nous avons besoin
    - Calcul d'un score pour les différents films
    - Insertion des données dans une base de données orientée graphe

- Frontend : 
  
    - Visualisation des données sous forme d'interface graphique représentant un graphe
    - Regroupement des données en fonction de certains critères (genre, film etc.)
    - Regroupement des acteurs en fonction des genres des films dans lesquels ils ont joué
    - Création de communautés
    - Recherche spécifique (film, acteur etc.)
    - Présentation des films en fonction de leurs scores respectifs

## Collecteur de données

Nous avions plusieurs stratégies disponibles pour réaliser notre collecteur de données. La première était de réutiliser notre laboratoire n°1 sur Solr, nous avions déjà crawlé le site TMDb directement. Mais les données qu'il récoltait n'était pas autant complètes que ce que l'API pouvait fournir, il aurait donc fallu l'améliorer. Nous n'étions également pas sûrs de pouvoir exporter les données brutes au format JSON par exemple pour ensuite pouvoir les insérer dans la base de données choisie. Finalement, nous n'étions pas certains de pouvoir correctement le paralléliser pour obtenir rapidement toutes les données (voir la suite). Nous voulions donc interroger l'API TMDb directement.

### Approche naïve avec bash

Pour rappel, comme décrit dans la section Données, nous disposons d'un fichier d'environ 500'000 lignes, contenant tous les ids des films disponibles, comme l'illustre un extrait ci-dessous :

```json
{"id":3924,"original_title":"Blondie","popularity":2.569,"video":false}
{"id":2,"original_title":"Ariel","popularity":12.697,"video":false}
{"id":5,"original_title":"Four Rooms","popularity":13.013,"video":false}
```

Pour rapidement tester l'automatisation de la récupération de toutes les données des films, nous avons concocté le script bash suivant, qui lit ce fichier d'ids, pour chaque id effectue une requête HTTP à l'API TMDb avec `curl`, puis écrit chaque réponse dans le même fichier, un film/réponse par ligne :

```bash
#!/usr/bin/env bash

$api_key="..."

for id in $(cat movie_ids.json | jq '.id'); do
    curl -s "https://api.themoviedb.org/3/movie/$id
        ?api_key=$api_key&language=en-US&append_to_response
        =credits%2Ckeywords%2Csimilar" >> movies.json
        echo "" >> movies.json
done
```

Constatant que ce script ne prenait pas en compte les requêtes avec erreur et surtout qu'il récupérait que quelques films par seconde (entre 2 et 3), il nous aurait fallu entre 2 et 3 semaines pour faire les 500'000 requêtes. Ce qui est beaucoup trop long.

### Approche robuste et efficace : programme Rust "distribué"

Jusqu'à fin 2019, l'API TMDb avait une limite d'utilisation à 40 requêtes sur 10 secondes, ou 4 requêtes par seconde. Mais depuis le début de l'année, il n'y a plus de limite. Nous pouvons donc "bombarder" l'API pour récupérer les données aussi vite que désiré. Pour ce faire, nous avons réalisé un `crawler` multi threads en Rust. Le choix de Rust a été fait pour ses performances, la justesse du code obtenu et pour pratiquer le langage également. Nous nous sommes servis de différents *crates* Rust, comme [reqwest](https://crates.io/crates/reqwest) pour les requêtes HTTP, [serde](https://crates.io/crates/serde) et [serde_json](https://crates.io/crates/serde_json) pour la sérialisation du JSON et de la librairie standard Rust pour la gestion de la concurrence (via les [channels](https://doc.rust-lang.org/rust-by-example/std_misc/channels.html)). Chaque film récupéré est désérialisé et les conditions suivantes sont testées :

- champs "budget" et "revenu" supérieurs à 1000 (seuil défini arbitrairement)
- liste de genres non vide
- liste des *peoples* non vide
- liste des films similaires non vide
- liste des films recommandés non vide

Si ces conditions sont remplies, le film est gardé pour être écrit dans un fichier. Nous avions donc une version améliorée du script bash, plus rapide, robuste et multi core.

Pour accélérer davantage la récupération, nous avions à disposition via `ssh` quelques 70 machines de bureau (Intel 4 cores / 8 threads, 32 Go de RAM, SSD) reliées à internet par des interfaces de 100 Mb/s ou 1 Gb/s. A l'aide d'un deuxième petit programme Rust, le `splitter`, et de divers commandes Linux comme `parallel-ssh`, nous avons pu séparer le fichier d'ids des films en autant de parts égales que de machines disponibles. Chaque machine a lancé le crawler sur 20 threads avec un fichier d'ids réduit, différent pour chaque machine. Nous avions donc environ l'équivalent de 70 * 20 = 1400 scripts bash initiaux qui récupéraient les informations voulues sur les 500'000 films de TMDb. En quelques minutes, les crawlers terminaient leur job. Avec un dernier script `reduce.sh` et de [`cloudsend.sh`](https://github.com/tavinus/cloudsend.sh), nous avons pu récupérer tous les fichiers produits contenant les informations complètes d'un film, un film par ligne, sur un compte nextcloud en notre possession.

Pour reproduire cette récupération, vous devez disposer de machines GNU/Linux connectées à internet, que vous pouvez contrôler par `ssh`, dont le répertoire `home` est synchronisé sur chacune et remplir le fichier `collector/.env` d'une manière analogue à la suivante :

```conf
IPS=ips.txt ; La liste des IPs des machines, une par ligne
REMOTE_USER=user ; Utilisateur SSH
REMOTE_HOST=192.168.1.2 ; IP de la machine distante principale, pour init
REMOTE_WORKING_DIR=working_dir ; Le répertoire de travail courant
TMDB_API_KEY=1a2b3c4d5e6f7g8h9i0j ; Une clé API pour TMDb
NEXTCLOUD_UPLOAD=https://your.nextcloud.com/qwertz ; Le répertoire Nextcloud
```

Un `makefile` est disponible dans `collector` pour exécuter chaque étape de la récupération.

# Résultats attendus
Nous nous attendons à pouvoir comparer les scores des films entre eux, trouver des communautés d'acteurs/films/genres, ou de voir les genres de films les plus populaires.

## Test de validation du projet
En ce qui concerne la phase de test, nous avons prévu d'effectuer des tests unitaires au niveau des méthodes critiques et complexes, notament celles visant à interroger la base de données.
Pour la partie frontend, nous avons prévu d'effectuer une sorte d'audit, en faisant tester l'app à utilisateur externe au projet, afin d'avoir un retour sur l'expérience utilisateur de l'interface graphique proposée.
Une fois l'app développée, nous avons prévu une liste (ci-dessous) avec les principales fonctionnalités de notre application. Elles seront testées une à une, et, pour chaque fonctionnalité testée, une colonne correspondante sera renseignée si cette fonctionnalité a été validée ou non avec la possibilité de laisser un commentaire (3ème colonne).

Voir exemple ci-dessous :

| Feature   | Validation(OK/KO) | Comment           |
| --------- | ----------------- | ----------------- |
| Search    | OK                | Request are fast  |
| Zoom      | OK                | With the mouse    |
|           |                   |                   |

# Planning envisagé

![Planning](report/planning.svg)

| Étape                  | Délivrables / Workpackage                                                 |
|------------------------|---------------------------------------------------------------------------|
| Crawling des données   | Programme récupérant les données de TMDb et produisant des données brutes |
| Études des technos DB  | Choix final de la base de données                                         |
| Parsing + insertion DB | Base de données remplie avec des données cohérentes                       |
| Analyse algos graphes  | Choix final des algorithmes de graphes                                    |
| Frontend               | GUI disponible pour l'utilisateur final, avec les contrôles désirés       |
| Rapport                | Version définitive du rapport                                             |
