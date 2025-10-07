# 🖥️ Backend - Portfolio Nouhaila AZLAG
Backend développé avec **Spring Boot** pour gérer le chatbot IA. Il fournit les API REST pour les questions du chatbot et pour la gestion des informations stockées dans la base de données.


## ✨ Description
Le backend a pour rôle principal de :  
- Gérer les **questions posées par les utilisateurs** via le chatbot.  
- **Identifier la catégorie** de la question (`about`, `skills`, `experience`, `projects`).  
- **Récupérer les données correspondantes** dans la base de données (MySQL).  
- **Reformuler la réponse** via l’API **Google Gemini** pour donner un texte naturel et complet.  
- Fournir un système de **cache local** pour améliorer la rapidité et réduire les appels API.  
- Gérer les **salutations, remerciements et questions générales**.  



## 🛠️ Stack technique
- **Langage & Framework** : Java, Spring Boot  
- **API externe** : Google Gemini (Generative AI)  
- **Base de données** : MySQL  
- **Build & gestion dépendances** : Maven  
- **Autres** : Lombok, WebClient (Spring WebFlux), Reactor (Mono)  

## 🚀 Installation & lancement
Clone le repo backend :

```bash
git clone https://github.com/NouhailaAZ/portfolio_ChatBot.git
cd portfolio-backend
```
Installe les dépendances et build le projet :

```bash
mvn clean install
```

Lance le serveur en mode développement :

```bash
mvn spring-boot:run
```
Le backend sera accessible sur **http://localhost:8080**.

## 📌 Endpoints principaux
| Méthode | URL                          | Description                                          |
| ------- | ---------------------------- | ---------------------------------------------------- |
| POST    | `/api/portfolio/ask`         | Pose une question au chatbot et récupère la réponse. |
| GET     | `/api/portfolio/clear-cache` | Vide le cache local du chatbot.                      |


## 📂 Structure du projet
```bash
src/
 ├── main/
 │   ├── java/com/project/portfolio/
 │   │   ├── config/
 │   │   ├── controller/
 │   │   ├── model/
 │   │   ├── repository/
 │   │   ├── service/
 │   │   └── PortfolioApplication.java
 │   └── resources/
 │       └── application.properties  # Configuration (DB, API keys)
 └── test/                          # Tests unitaires et d’intégration
```

## 🔑 Configuration
Ajoute ta clé API Google Gemini dans application.properties :
```bash
google.api.key=VOTRE_CLE_API
```

Configure aussi les paramètres de ta base de données (ex : MySQL) :

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/nameDB
spring.datasource.username=root
spring.datasource.password=motdepasse
spring.jpa.hibernate.ddl-auto=update
```
## 💡 Fonctionnalités clés
- Détection automatique de la catégorie de la question.
- Réponses reformulées par l’IA pour un texte naturel.
- Gestion des questions sur Nouhaila uniquement.
- Réponses aux salutations et remerciements.
- Cache local pour accélérer les réponses.

## 🚧 Fonctionnalités à venir
**Module d’apprentissage** : mémorisation automatique des questions fréquentes et ajout de leurs réponses à la base de données pour un traitement plus rapide par le chatbot.
## 👩‍💻 Développé par
**Nouhaila AZLAG**

