# GDELT Event Analysis — Hadoop MapReduce sur Docker

**Auteurs :** Ibrahima Fall – Aicha Dalloba Savané  
**Filière :** M1SRT — 2024-2025  
**Atelier :** TP2 Big Data

---

## Description

Application Big Data qui analyse les événements mondiaux de la plateforme [GDELT](https://www.gdeltproject.org/) via un job **MapReduce** sur un cluster **Hadoop** déployé avec **Docker**.

L'objectif est de compter le nombre d'événements pour chaque combinaison **pays × type d'événement** à partir des fichiers CSV exportés par GDELT.

---

## Architecture du projet

```
gdelt-hadoop-mapreduce/
├── docker-compose.yml                         # Cluster Hadoop (1 NameNode + 9 DataNodes)
├── pom.xml                                    # Configuration Maven
├── README.md
└── src/
    └── main/
        └── java/
            └── gdelt/
                └── mapreduce/
                    ├── EventDriver.java       # Point d'entrée du job MapReduce
                    ├── EventMapper.java       # Phase Map : extraction pays + type événement
                    └── EventReducer.java      # Phase Reduce : agrégation des comptages
```

---

## Technologies utilisées

| Outil       | Version               | Rôle                                      |
|-------------|-----------------------|-------------------------------------------|
| Hadoop      | 3.2.1                 | Framework de traitement distribué         |
| Docker      | —                     | Déploiement du cluster Hadoop             |
| Java        | 8                     | Développement des programmes MapReduce    |
| Maven       | 3.x                   | Compilation et packaging                  |
| GDELT       | Export CSV quotidien  | Source de données d'événements mondiaux   |

---

## Prérequis

- Docker & Docker Compose installés
- Java 8 (JDK)
- Maven 3.x
- Fichier CSV GDELT (ex : `20241228.export.CSV`) téléchargé depuis [gdeltproject.org](https://www.gdeltproject.org/data.html)

---

## Mise en place

### 1. Démarrer le cluster Hadoop

```bash
docker-compose up -d
docker ps   # Vérifier que namenode + 9 datanodes sont bien démarrés
```

### 2. Compiler le projet

```bash
mvn package
# Génère : target/event-analysis-1.0-SNAPSHOT.jar
```

### 3. Charger les données dans HDFS

```bash
# Copier le CSV dans le conteneur namenode
docker cp 20241228.export.CSV namenode:/root/20241228.export.CSV

# Créer le répertoire HDFS et charger le fichier
docker exec -it namenode bash
hdfs dfs -mkdir -p /gdelt/input
hdfs dfs -put /root/20241228.export.CSV /gdelt/input
hdfs dfs -ls /gdelt/input   # Vérification
```

### 4. Copier et exécuter le JAR

```bash
# Copier le JAR dans le conteneur namenode
docker cp target/event-analysis-1.0-SNAPSHOT.jar namenode:/root/event-analysis.jar

# Lancer le job MapReduce
docker exec -it namenode bash
hadoop jar /root/event-analysis.jar gdelt.mapreduce.EventDriver /gdelt/input /gdelt/output
```

### 5. Consulter les résultats

```bash
hdfs dfs -ls /gdelt/output
hdfs dfs -cat /gdelt/output/part-r-00000
```

---

## Format des résultats

Le fichier `part-r-00000` contient une ligne par combinaison pays/type d'événement :

```
[CODE_PAYS]    [CODE_TYPE_ÉVÉNEMENT]    [NOMBRE_TOTAL]
```

**Exemples :**
```
        010    5718
        011     191
        012    1247
CTH     010       6
CTH     042       3
PRO     036       5
```

- `010 → 5718` : 5718 événements de type 010 sans code pays précis
- `CTH → 010 → 6` : 6 événements de type 010 pour le groupe CTH

---

## Interfaces web disponibles

| Service          | URL                       |
|------------------|---------------------------|
| HDFS NameNode    | http://localhost:9870      |
| YARN ResourceMgr | http://localhost:8089      |

---

## Arrêter le cluster

```bash
docker-compose down
```
