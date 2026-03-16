# Intégration n8n — Candidatures, onboarding et stagiaires

Ce document résume tout ce qui a été mis en place dans le backend Spring Boot pour supporter les workflows n8n, depuis le changement de statut d’une candidature jusqu’à la création d’un compte stagiaire et de l’entité métier `Stagiaire`.

---

## 1. Objectif global

L’objectif était de brancher le backend avec n8n pour automatiser plusieurs étapes du cycle de vie d’une candidature :

1. **Quand une candidature est créée**
   - envoyer un webhook vers n8n pour l’email de confirmation.

2. **Quand le statut d’une candidature change**
   - si `PRESELECTIONNE` → n8n crée un Google Meet avec `dateEntretien` et envoie l’email.
   - si `REJETE` → n8n envoie un email de refus.
   - si `ACCEPTE` → n8n envoie un email d’acceptation avec les informations du stage.

3. **Après acceptation**
   - n8n envoie un `POST` vers le backend pour créer un `PendingAccount`.
   - un admin ou un lien direct confirme ce pending account.
   - le backend crée le compte de connexion `Utilisateur` avec le rôle `STAGIAIRE`.
   - le backend crée ensuite l’entité métier `Stagiaire`.

---

## 2. Partie candidatures → n8n

### 2.1. Endpoint dédié au changement de statut

Un endpoint a été ajouté pour changer uniquement le statut d’une candidature :

- `PATCH /api/candidatures/{id}/statut`

Ce endpoint reçoit un DTO dédié :

- `StatutCandidatureRequest`

### Champs du DTO

- `statut`
- `dateEntretien` → obligatoire si `PRESELECTIONNE`
- `dateDebut` → obligatoire si `ACCEPTE`

### Règles métier mises en place

- si `statut = PRESELECTIONNE` et `dateEntretien == null` → erreur.
- si `statut = ACCEPTE` et `dateDebut == null` → erreur.

---

## 3. Webhook n8n sur changement de statut

Le service `CandidatureService` envoie un webhook enrichi à n8n quand le statut change.

### Webhook utilisé

- `app.n8n.webhook.statut-candidature`

### Champs envoyés au workflow n8n

#### Métadonnées candidature
- `candidatureId`
- `typeStage`
- `nouveauStatut`
- `n8nAction`
- `estBinome`

#### Dates
- `dateEntretien`
- `dateEntretienDate`
- `dateEntretienHeure`
- `dateDebut`
- `dateDebutFormatee`

#### Candidat principal
- `candidat1Nom`
- `candidat1Prenom`
- `candidat1Email`
- `candidat1Tel`
- `universite`

#### Binôme éventuel
- `candidat2Nom`
- `candidat2Prenom`
- `candidat2Email`
- `candidat2Tel`

#### Informations stage / PFE
- `departement`
- `pfeSujetTitre`
- `pfeSpecialite`
- `pfeDuree`

### Valeurs de `n8nAction`

- `ENVOYER_EMAIL_PRESELECTION_AVEC_MEET`
- `ENVOYER_EMAIL_ACCEPTATION`
- `ENVOYER_EMAIL_REFUS`

Cela permet à n8n d’utiliser un `Switch` pour router vers la bonne branche.

---

## 4. Ajout de `dateDebut` dans `Candidature`

Pour les candidatures acceptées, il fallait conserver la date de début du stage.

### Ce qui a été fait

- ajout du champ `dateDebut` dans l’entité `Candidature`
- ajout du champ `dateDebut` dans :
  - `StatutCandidatureRequest`
  - `CandidatureResponse`
- mapping ajouté dans `CandidatureService`

### Correction importante

Un problème empêchait parfois les candidatures de se charger :

- `data.sql` supprimait la colonne `date_debut`
- cela cassait les `SELECT` côté backend
- le front Angular ne recevait plus les données

### Correctif appliqué

- suppression des `DROP COLUMN date_debut`
- `dateDebut` explicitement nullable dans l’entité
- `ALTER COLUMN date_debut DROP NOT NULL` dans `data.sql`

---

## 5. Email d’acceptation envoyé par n8n

Le design du mail d��acceptation a été retravaillé pour être plus :

- professionnel
- premium
- formel
- cohérent avec Ooredoo

### Améliorations apportées

- utilisation de la couleur officielle `#ED1C24`
- header/footer plus élégants
- logo Ooredoo intégré dans le template
- meilleure lisibilité web + mobile
- mise en valeur des informations stage
- design plus sobre, moins “email automatisé basique”

### Fichier de template généré

- `email_acceptation_stage.html`

Ce fichier a été gardé à la racine pour ne pas impacter le fonctionnement du backend.

---

## 6. Workflow WF-06 — Pending accounts

Ensuite, la partie onboarding a été ajoutée.

Le but : après acceptation, n8n prépare les données de création de compte et appelle le backend.

### 6.1. Nouvel endpoint n8n

- `POST /api/n8n/pending-accounts`

### Sécurité

Cet endpoint n’utilise pas le JWT.
Il est protégé par un header :

- `X-N8N-Token`

Le backend compare cette valeur avec :

- `app.n8n.api-token`

### Payload reçu

- `candidatureId`
- `prenom`
- `nom`
- `email`
- `username`
- `tempPassword`
- `departement`
- `encadrantId`
- `dateDebut`
- `dateFin`
- `typeStage`

---

## 7. Entité `PendingAccount`

Une nouvelle entité a été créée :

- `PendingAccount`

### Table

- `pending_accounts`

### Champs principaux

- `id`
- `candidatureId`
- `prenom`
- `nom`
- `email`
- `username`
- `tempPasswordHash`
- `departement`
- `encadrantId`
- `dateDebut`
- `dateFin`
- `typeStage`
- `token`
- `statut`
- `expiresAt`
- `createdAt`
- `stagiaireId`

### Statuts possibles

via `PendingAccountStatus` :

- `EN_ATTENTE`
- `CONFIRME`
- `EXPIRE`

### Règles métier

À la création d’un pending account :

- `token = UUID`
- `statut = EN_ATTENTE`
- `expiresAt = now + 48h`
- le mot de passe temporaire est hashé en bcrypt
- le mot de passe en clair n’est renvoyé qu’une seule fois dans la réponse du POST

---

## 8. Endpoints admin / confirmation

### 8.1. Consultation d’un pending account

- `GET /api/admin/pending-accounts/{token}`

### Sécurité

- JWT requis
- rôle `ADMIN`

### Comportement

- retourne les infos si le token est valide
- retourne `410 Gone` si expiré
- retourne `409 Conflict` si déjà utilisé

---

### 8.2. Confirmation manuelle par admin

- `PUT /api/admin/pending-accounts/{token}/confirm`

### Sécurité

- JWT requis
- rôle `ADMIN`

### Comportement

- crée le compte `Utilisateur` avec rôle `STAGIAIRE`
- confirme le pending account
- crée ensuite l’entité métier `Stagiaire`

---

### 8.3. Confirmation directe via lien email

- `GET /api/admin/confirm-direct/{token}`

### Sécurité

- endpoint public (sans JWT)

### Comportement

- effectue la même confirmation que l’endpoint admin
- redirige ensuite vers le frontend

### Redirections prévues

- succès → `/login?account-confirmed=true&user=...`
- expiré → `/login?error=token-expired`
- déjà confirmé → `/login?error=already-confirmed`

Le frontend de redirection est configurable via :

- `app.frontend.url`

---

## 9. Expiration automatique des tokens

Un job planifié a été ajouté avec `@Scheduled`.

### Fréquence

- toutes les heures

### Action

- tous les `PendingAccount` en `EN_ATTENTE` dont `expiresAt < now`
  passent à `EXPIRE`

### Activation

La classe principale a été mise à jour avec :

- `@EnableScheduling`

---

## 10. Création de l’entité `Stagiaire`

La dernière partie ajoutée concerne l’entité métier réelle du stagiaire.

### Table

- `stagiaires`

### Champs créés

- `id` (`Long`, auto-increment)
- `user` → relation vers `Utilisateur`
- `candidat` → relation vers `Candidat`
- `candidature` → relation vers `Candidature`
- `prenom`
- `nom`
- `email`
- `telephone`
- `universite` → relation vers `Universite`
- `typeStage`
- `departement` → relation vers `Departement`
- `encadrant` → relation vers `Encadrant`
- `dateDebut`
- `dateFin`
- `statut`
- `createdAt`
- `updatedAt`

### Enum associée

- `StatutStagiaire`
  - `ACTIF`
  - `TERMINE`
  - `ABANDONNE`

### Important

Au début, `universite` et `departement` avaient été stockés en `String`.
Cela a été **corrigé** pour respecter l’existant du projet :

- `Universite` est maintenant une vraie relation JPA
- `Departement` est maintenant une vraie relation JPA

---

## 11. Alimentation de `Stagiaire` lors du confirm

Lors de `PendingAccountService.confirm(...)`, le backend fait maintenant :

1. charge le `PendingAccount`
2. vérifie qu’il n’est ni expiré ni déjà confirmé
3. vérifie qu’aucun `Stagiaire` n’existe déjà pour la candidature
4. charge la `Candidature`
5. récupère le `Candidat` principal
6. charge l’`Encadrant` si fourni
7. résout le `Departement` :
   - via `encadrant.getDepartement()` si disponible
   - sinon via `departementRepository.findByNom(...)`
8. crée le `Utilisateur` (`role = STAGIAIRE`)
9. crée le `Stagiaire`
10. marque le `PendingAccount` comme `CONFIRME`

---

## 12. API admin de lecture des stagiaires

Pour exposer les stagiaires créés, on a ajouté :

### DTO
- `StagiaireResponse`

### Service
- `StagiaireService`

### Controller
- `StagiaireController`

### Endpoints

#### Lister les stagiaires
- `GET /api/admin/stagiaires`
- rôles : `ADMIN`, `AGENT_RH`

#### Détail d’un stagiaire
- `GET /api/admin/stagiaires/{id}`
- rôles : `ADMIN`, `AGENT_RH`

### Champs exposés dans `StagiaireResponse`

- `id`
- `userId`
- `candidatId`
- `candidatureId`
- `prenom`
- `nom`
- `email`
- `telephone`
- `universiteId`
- `universiteNom`
- `typeStage`
- `departementId`
- `departementNom`
- `encadrantId`
- `encadrantNomComplet`
- `dateDebut`
- `dateFin`
- `statut`
- `createdAt`
- `updatedAt`

---

## 13. Sécurité mise en place

### Endpoints publics
- `/api/candidatures/**`
- `/api/public/**`
- `/api/n8n/**` → protégé par header `X-N8N-Token`
- `/api/admin/confirm-direct/**` → lien email public

### Endpoints protégés JWT
- `/api/admin/pending-accounts/**` → `ADMIN`
- `/api/admin/stagiaires/**` → `ADMIN`, `AGENT_RH`

---

## 14. Configuration ajoutée

Dans `application.properties` :

```properties
app.n8n.api-token=ooredoo-n8n-secret-2026
app.frontend.url=http://localhost:4200
```

### Remarques

- `app.n8n.api-token` doit être changé en production
- `app.frontend.url` doit pointer vers l’URL réelle du frontend

---

## 15. Validation technique effectuée

À chaque étape importante :

- les fichiers Java ont été validés par l’analyse IDE
- le projet a été compilé avec Maven
- les erreurs de compilation ont été corrigées immédiatement

### Résultat final

- **Build : PASS**
- **Intégration JPA : PASS**
- **Sécurité Spring : PASS**
- **Workflow n8n backend : PASS**

---

## 16. Résumé des principaux fichiers créés / modifiés

### Candidatures / statut / n8n
- `dto/StatutCandidatureRequest.java`
- `entity/Candidature.java`
- `dto/CandidatureResponse.java`
- `service/CandidatureService.java`
- `controller/CandidatureController.java`

### Pending accounts / onboarding
- `enums/PendingAccountStatus.java`
- `entity/PendingAccount.java`
- `repository/PendingAccountRepository.java`
- `dto/PendingAccountRequest.java`
- `dto/PendingAccountResponse.java`
- `service/PendingAccountService.java`
- `controller/N8nPendingAccountController.java`
- `controller/PendingAccountAdminController.java`
- `controller/ConfirmDirectController.java`

### Stagiaires
- `enums/StatutStagiaire.java`
- `entity/Stagiaire.java`
- `repository/StagiaireRepository.java`
- `dto/StagiaireResponse.java`
- `service/StagiaireService.java`
- `controller/StagiaireController.java`

### Config / infra
- `config/SecurityConfig.java`
- `GestionDesStagiairesApplication.java`
- `resources/application.properties`
- `resources/data.sql`
- `email_acceptation_stage.html`

---

## 17. Ce qu’il reste possible à faire ensuite

Améliorations possibles pour la suite :

1. ajouter `PATCH /api/admin/stagiaires/{id}/statut`
   - passer `ACTIF -> TERMINE`
   - passer `ACTIF -> ABANDONNE`

2. ajouter un endpoint de recherche/filtrage des stagiaires
   - par statut
   - par département
   - par encadrant
   - par type de stage

3. créer un tableau admin frontend pour visualiser les stagiaires créés

4. stocker aussi le mot de passe temporaire de façon plus traçable côté onboarding
   - ou le régénérer si besoin

5. ajouter des tests unitaires / intégration pour :
   - `PendingAccountService.confirm(...)`
   - expiration planifiée
   - contrôleurs admin

---

## 18. Conclusion

À ce stade, le backend permet maintenant un vrai workflow d’onboarding automatisé :

- changement de statut candidature → webhook n8n
- email d’acceptation → génération des infos de compte
- création d’un `PendingAccount`
- validation admin ou directe via lien email
- création du compte `Utilisateur` stagiaire
- création de l’entité métier `Stagiaire`
- consultation admin des stagiaires créés

Le système est donc prêt pour une intégration n8n plus avancée côté création de compte, email final d’identifiants, et suivi du stagiaire dans la plateforme.

