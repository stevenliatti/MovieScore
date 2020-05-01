# Movie Score

## Buts
Les buts de ce projet sont de déterminer les scores de films selon leur ratio revenu sur budget, de déterminer l'influence qu'ils ont sur les "peoples" (acteurs et membres de l'équipe de réalisation) qui ont participé aux-dits films et montrer les genres de films les plus populaires.

# Contexte et objectifs
Ce projet se déroule dans le cadre du cours de Web Mining du master MSE HES-SO. L'objectif principal du projet est de visualiser, sous forme graphe, les relations entre les films, leurs genres et les peoples. Nous appliquons des algorithmes choisis pour déterminer les noeuds du graphe les plus importants, trouver les plus courts chemins entre peoples et former des communautés de films, peoples ou genres.

## Données
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



## Architecture / technos envisagées
Tout d'abord, un programme est dédié à la récupération de données proprement dites, à partir de l'API fournissant les données en JSON, enregistrant les données pour les films ayant les champs revenu et budget valides. Ensuite, ces données seront manipulées et insérées dans la base de donnée choisie de manière cohérente et selon les besoins de l'interface. Finalement, une interface graphique sera implémentée pour visualiser les graphes obtenus et exécuter des requêtes à la base de données.

Les langages et technologies envisagés sont Scala et/ou Rust pour la partie développement, pour la justesse, l'efficacité et/ou la performance, et OrientDB, ArangoDB ou Neo4j pour la base de données, avec leur propres langages de requêtes.

## Analyse envisagée
Nous appliquerons des algorithmes choisis comme PageRank, plus court chemin, ou d'autres algorithmes de centralité pour extraire une plus value intéressante sur les données récoltées.

## Résultats attendus
Nous nous attendons à pouvoir comparer les scores des films entre eux, trouver des communautés d'acteurs/films/genres, ou de voir les genres de films les plus populaires.

## Planning envisagé

![Planning](report/planning.svg)

#################################################################################################

# Workpackge

## 1) Gestion du projet
Planning

Delivrable : planning

## 2) State of the art
## Analyse de l'exsitant

### Méthodes
Explication de l'analyse de données qu'on à fait

### Analyse technologique
De nombreuses technologies sont à notre disposition et nous proposent les outils nécessaires à la réalisation de ce projet, notamment au niveau de la base de données mais également pour le front end.
Nous avons donc comparé ces différentes technologies afin de faire le choix le plus adapté au objectifs du projet et à nos domaine de compétences respectifs.


### BDD
En ce qui concerne la technologie de base de données, nous avons étudié principalement les technologies suivantes :
#### Neo4j
Steven ? peut être plus d'infos sur tes recherches ?

#### Orient DB
Steven ? peut être plus d'infos sur tes recherches ?

Après avoir comparé plus en détail ces deux technologies, nous avons fait le choix de partir sur Neo4j car elle propose une abstraction contenant de nombreuses fonctionnalités integrées à son langage de requête qui permet donc un prétraitemnet des données et par la même occasion faciliter l'utilisation de ces dernières.


## 3) Conception architecturre


### Use cases
Voici la liste des principaux uses cases:
    Un utilisaeur de l'application doit pouvoir : 
    - Visualiser l'interconnexion (via un graph) entre les différentes entités représentées dans la base de données (films, acteurs, genres)
    - Modifier l'affichage de ce graph en spécifiant certains citères de recherche
    - Visualiser (à l'aide du graph) l'importance des films en fonction de leurs score(revenu/prix)
    - Lister les différents films répértoriés dans notre base
    - Rechercher un film par son nom
    - Rechercher un film par son genre
    - Visualiser les communautés d'acteurs, de genre, de films qui ont des critères communs
    - Visualiser le plus court chermin entre les entités

Delivrable :


## 4) Features
Voici la liste des différentes fonctionnalités que nous allons réaliser dans le cadre de ce projet :
    Backend :
        - Récupération des données sur les films mises à disposition par TMDb
        - Traitement des ces données pour séléctionner uniquement ce dont nous avons besoin
        - Calcul d'un score pour les différents films
        - Insertion des données dans une base de données orientée graph
    Frontend : 
        - Visualisation des données sous forme d'interface graphique représentant un graph
        - Regroupement des données en fonction de certains critères (genre, film etc.)
        - Regroupement des acteurs en fonction des genres des films dans lesquels ils ont joué
        - Création de communautés
        - Recherche spécifique (film, acteur etc.)
        - Présentation des films en fonction de leurs scores respectifs

## 5) Test validation du projet
En ce qui concerne la phase de test, nous avons prévu d'effectuer des tests unitaires au niveau des méthodes critiques et complexe, notament celles visant à interogrer la base de données.
Pour la partie front-end, nous avons prévu d'effectuer une sorte d'audit, en faisant tester l'app à utilisateur externe au projet, afin d'avoir un retour sur l'experience utiliateur de l'interface graphique proposée.
Une fois l'app developpée, nous avons prévu une liste (ci-dessous) avec les principales fonctionnalités de notra application. Elles seront testée une à une, pour chaque fonctionnalité testée, une colonne correspondante sera renseginée si cette fonctionnalité à été validée ou non avec la possilibté de laisser un commentaire (3ème colone).

Voir exemple ci-dessous :

| Feature   | Validation(OK/KO) | Comment           |
|-----------|-------------------|-------------------|
| Recherche | OK                | Request are fast  |
|           |                   |                   |
|           |                   |                   |

Delivrable : rapport de test