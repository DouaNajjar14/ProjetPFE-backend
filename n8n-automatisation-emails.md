# 📧 Automatisation des Emails avec n8n — Guide Complet

> Ce guide explique **pas à pas** comment automatiser l'envoi d'emails après certaines actions dans l'application de gestion des stagiaires, en utilisant **n8n**.  
> Aucune modification du code n'est requise pour l'instant — tout se fait côté n8n.

---

## 📌 Table des matières

1. [Qu'est-ce que n8n ?](#1-quest-ce-que-n8n-)
2. [Installation de n8n](#2-installation-de-n8n)
3. [Les 3 cas d'automatisation à implémenter](#3-les-3-cas-dautomatisation-à-implémenter)
4. [Stratégie choisie : Webhook depuis le backend Spring Boot](#4-stratégie-choisie--webhook-depuis-le-backend-spring-boot)
5. [Étape 1 — Configurer un compte email dans n8n](#5-étape-1--configurer-un-compte-email-dans-n8n)
6. [Workflow 1 — Email après soumission d'une candidature](#6-workflow-1--email-après-soumission-dune-candidature)
7. [Workflow 2 — Email après changement de statut](#7-workflow-2--email-après-changement-de-statut)
8. [Modifications nécessaires dans le backend Spring Boot](#8-modifications-nécessaires-dans-le-backend-spring-boot)
9. [Résumé des endpoints Webhook n8n](#9-résumé-des-endpoints-webhook-n8n)
10. [Alternative : Polling (sans modifier le backend)](#10-alternative--polling-sans-modifier-le-backend)
11. [Tests et débogage](#11-tests-et-débogage)

---

## 1. Qu'est-ce que n8n ?

**n8n** est un outil d'automatisation de workflows (comme Zapier ou Make) mais **open-source** et que tu peux héberger toi-même.

Il fonctionne avec des **nœuds** (nodes) connectés entre eux :
- Un nœud **déclencheur** (trigger) qui démarre le workflow
- Des nœuds **d'action** qui font quelque chose (envoyer un email, transformer des données, etc.)

---

## 2. Installation de n8n

### Option A — En local avec npm (recommandé pour le développement)

```bash
npm install -g n8n
n8n start
```

Puis ouvre ton navigateur sur : **http://localhost:5678**

### Option B — Avec Docker

```bash
docker run -it --rm \
  --name n8n \
  -p 5678:5678 \
  -v ~/.n8n:/home/node/.n8n \
  n8nio/n8n
```

### Option C — n8n Cloud (sans installation)
Crée un compte gratuit sur **https://n8n.io** (limite de workflows sur la version gratuite).

---

## 3. Les 3 cas d'automatisation à implémenter

| # | Événement | Destinataire | Email envoyé |
|---|-----------|-------------|-------------|
| 1 | Le candidat **soumet sa candidature** | Candidat principal (+ binôme si applicable) | Confirmation de réception |
| 2 | Le statut passe à **PRESELECTIONNE** | Candidat(s) | Félicitations, informations entretien |
| 3 | Le statut passe à **ACCEPTE** | Candidat(s) | Acceptation officielle |
| 4 | Le statut passe à **REJETE** | Candidat(s) | Refus avec message poli |

---

## 4. Stratégie choisie : Webhook depuis le backend Spring Boot

La meilleure approche pour une réactivité **en temps réel** :

```
Action dans l'app (soumettre candidature / changer statut)
        ↓
Spring Boot envoie une requête HTTP POST au Webhook n8n
        ↓
n8n reçoit les données (email, nom, statut...)
        ↓
n8n envoie l'email automatiquement
```

> ⚠️ Cette approche nécessite **une petite modification du backend** (voir section 8).  
> Une **alternative sans modifier le backend** est décrite en section 10.

---

## 5. Étape 1 — Configurer un compte email dans n8n

### 5.1 — Ouvrir n8n et créer des Credentials

1. Ouvre **http://localhost:5678**
2. Dans le menu de gauche, clique sur **"Credentials"**
3. Clique sur **"Add Credential"**
4. Cherche **"SMTP"** ou **"Gmail"**

### 5.2 — Configuration Gmail (recommandé)

1. Choisis **"Gmail"** dans la liste
2. Clique sur **"Connect with OAuth2"** OU utilise un **App Password**

**Pour utiliser un App Password Gmail :**
1. Va sur **https://myaccount.google.com/apppasswords**
2. Génère un mot de passe pour "Mail"
3. Dans n8n, choisis **"SMTP"** et remplis :
   - **Host** : `smtp.gmail.com`
   - **Port** : `465`
   - **SSL** : activé
   - **User** : ton adresse Gmail
   - **Password** : le App Password généré (16 caractères)

### 5.3 — Configuration avec un autre service SMTP

| Service | Host | Port |
|---------|------|------|
| Gmail | smtp.gmail.com | 465 |
| Outlook/Hotmail | smtp.office365.com | 587 |
| Mailtrap (tests) | sandbox.smtp.mailtrap.io | 2525 |

> 💡 **Conseil** : Pour les tests, utilise **Mailtrap** (https://mailtrap.io) — gratuit, capture les emails sans les envoyer vraiment.

---

## 6. Workflow 1 — Email après soumission d'une candidature

### 6.1 — Créer le workflow

1. Dans n8n, clique sur **"New Workflow"**
2. Donne-lui le nom : `Email - Confirmation Candidature`

### 6.2 — Ajouter le nœud Webhook (Trigger)

1. Clique sur **"Add first step"** → cherche **"Webhook"**
2. Configure-le :
   - **HTTP Method** : `POST`
   - **Path** : `confirmation-candidature`
   - **Authentication** : None (ou Header Auth pour plus de sécurité)
3. Clique sur **"Listen for test event"** — n8n attendra une requête
4. Note l'URL générée, exemple :
   ```
   http://localhost:5678/webhook/confirmation-candidature
   ```

### 6.3 — Tester le Webhook avec un outil

Tu peux tester avec **Postman** ou **curl** en envoyant :

```json
POST http://localhost:5678/webhook/confirmation-candidature
Content-Type: application/json

{
  "candidatNom": "Ahmed",
  "candidatPrenom": "Yassin",
  "candidatEmail": "ahmed@exemple.com",
  "typeStage": "PFE",
  "dateDepot": "2026-03-11T10:30:00",
  "candidatureId": "abc123",
  "estBinome": false,
  "candidat2Nom": null,
  "candidat2Prenom": null,
  "candidat2Email": null
}
```

### 6.4 — Ajouter un nœud "Send Email"

1. Clique sur le **"+"** après le nœud Webhook
2. Cherche **"Send Email"** (ou **"Gmail"** si tu utilises Gmail)
3. Configure-le :

**To (destinataire) :**
```
{{ $json.candidatEmail }}
```

**Subject (objet) :**
```
✅ Confirmation de votre candidature — {{ $json.typeStage }}
```

**Email Body (HTML activé) :**
```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
  <h2 style="color: #2c3e50;">Bonjour {{ $json.candidatPrenom }} {{ $json.candidatNom }},</h2>
  
  <p>Nous avons bien reçu votre candidature pour un stage de type <strong>{{ $json.typeStage }}</strong>.</p>
  
  <div style="background: #f0f4f8; padding: 15px; border-radius: 8px; margin: 20px 0;">
    <p><strong>📅 Date de dépôt :</strong> {{ $json.dateDepot }}</p>
    <p><strong>🔖 Référence :</strong> {{ $json.candidatureId }}</p>
    <p><strong>📋 Statut actuel :</strong> En attente de traitement</p>
  </div>
  
  <p>Notre équipe va examiner votre dossier et vous contactera prochainement.</p>
  
  <p style="color: #7f8c8d; font-size: 12px;">
    Cet email est automatique, merci de ne pas y répondre.
  </p>
</div>
```

### 6.5 — Gérer le cas Binôme (email au candidat 2)

Pour envoyer aussi un email au candidat 2 quand `estBinome = true` :

1. Ajoute un nœud **"IF"** après le Webhook
2. Configure la condition :
   - **Value 1** : `{{ $json.estBinome }}`
   - **Operation** : `Equal`
   - **Value 2** : `true`
3. Sur la branche **"true"**, ajoute un autre nœud **"Send Email"** pour `candidat2Email`

### 6.6 — Activer le workflow

1. Clique sur **"Save"** (en haut à droite)
2. Clique sur le toggle pour **activer** le workflow (il passe en vert)
3. L'URL de production sera :
   ```
   http://localhost:5678/webhook/confirmation-candidature
   ```
   *(sans `/test/` dans l'URL)*

---

## 7. Workflow 2 — Email après changement de statut

### 7.1 — Créer un nouveau workflow

Nom : `Email - Changement Statut Candidature`

### 7.2 — Nœud Webhook

- **HTTP Method** : `POST`
- **Path** : `statut-candidature`

Le backend enverra ce type de données :

```json
{
  "candidatNom": "Ahmed",
  "candidatPrenom": "Yassin", 
  "candidatEmail": "ahmed@exemple.com",
  "nouveauStatut": "ACCEPTE",
  "typeStage": "PFE",
  "dateEntretien": "2026-03-20T14:00:00",
  "candidatureId": "abc123",
  "estBinome": false,
  "candidat2Nom": null,
  "candidat2Prenom": null,
  "candidat2Email": null
}
```

### 7.3 — Nœud Switch (pour différencier les statuts)

1. Ajoute un nœud **"Switch"** après le Webhook
2. Configure :
   - **Mode** : `Rules`
   - **Value** : `{{ $json.nouveauStatut }}`
3. Ajoute 3 règles :
   - Règle 1 : `Equal` → `PRESELECTIONNE` → output 1
   - Règle 2 : `Equal` → `ACCEPTE` → output 2
   - Règle 3 : `Equal` → `REJETE` → output 3

### 7.4 — Email pour PRESELECTIONNE

Connecte le output 1 du Switch à un nœud **"Send Email"** :

**Subject :**
```
🎯 Votre candidature a été présélectionnée !
```

**Body HTML :**
```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
  <h2 style="color: #27ae60;">Félicitations {{ $json.candidatPrenom }} {{ $json.candidatNom }} !</h2>
  
  <p>Nous avons le plaisir de vous informer que votre candidature pour un stage <strong>{{ $json.typeStage }}</strong> a été <strong style="color: #27ae60;">présélectionnée</strong>.</p>
  
  <div style="background: #eafaf1; padding: 15px; border-radius: 8px; margin: 20px 0;">
    <p>📅 <strong>Date d'entretien prévue :</strong> {{ $json.dateEntretien }}</p>
    <p>🔖 <strong>Référence candidature :</strong> {{ $json.candidatureId }}</p>
  </div>
  
  <p>Préparez-vous bien pour votre entretien. Bonne chance !</p>
  
  <p style="color: #7f8c8d; font-size: 12px;">Cet email est automatique.</p>
</div>
```

### 7.5 — Email pour ACCEPTE

Connecte le output 2 du Switch à un nœud **"Send Email"** :

**Subject :**
```
🎉 Votre candidature a été acceptée — Bienvenue !
```

**Body HTML :**
```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
  <h2 style="color: #2980b9;">Félicitations {{ $json.candidatPrenom }} {{ $json.candidatNom }} !</h2>
  
  <p>Nous sommes ravis de vous informer que votre candidature pour un stage <strong>{{ $json.typeStage }}</strong> a été <strong style="color: #2980b9;">officiellement acceptée</strong>.</p>
  
  <p>Notre équipe vous contactera très prochainement pour vous communiquer les détails pratiques (dates, lieu, encadrant...).</p>
  
  <p>Encore toutes nos félicitations et bienvenue dans notre équipe !</p>
  
  <p style="color: #7f8c8d; font-size: 12px;">Cet email est automatique.</p>
</div>
```

### 7.6 — Email pour REJETE

Connecte le output 3 du Switch à un nœud **"Send Email"** :

**Subject :**
```
Réponse concernant votre candidature
```

**Body HTML :**
```html
<div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
  <h2 style="color: #2c3e50;">Bonjour {{ $json.candidatPrenom }} {{ $json.candidatNom }},</h2>
  
  <p>Nous avons bien examiné votre candidature pour un stage de type <strong>{{ $json.typeStage }}</strong>.</p>
  
  <p>Après étude attentive de votre dossier, nous sommes au regret de vous informer que nous ne pouvons pas donner suite à votre candidature cette fois-ci.</p>
  
  <p>Nous vous encourageons à postuler de nouveau lors de nos prochaines campagnes de recrutement.</p>
  
  <p>Nous vous souhaitons bonne chance dans vos recherches.</p>
  
  <p style="color: #7f8c8d; font-size: 12px;">Cet email est automatique, merci de ne pas y répondre.</p>
</div>
```

---

## 8. Modifications nécessaires dans le backend Spring Boot

Pour que les workflows n8n se déclenchent automatiquement, il faut que le backend **appelle les webhooks n8n** après chaque action concernée.

### 8.1 — Ce qu'il faut ajouter dans `CandidatureService.java`

Voici la logique à ajouter (sans toucher au code maintenant — juste pour comprendre) :

**Après `candidatureRepository.save(candidature)` dans la méthode `creer()` :**
```java
// Appel HTTP POST vers n8n webhook
notifierN8n("http://localhost:5678/webhook/confirmation-candidature", Map.of(
    "candidatNom", candidat1.getNom(),
    "candidatPrenom", candidat1.getPrenom(),
    "candidatEmail", candidat1.getEmail(),
    "typeStage", saved.getTypeStage().name(),
    "dateDepot", saved.getDateDepot().toString(),
    "candidatureId", saved.getId().toString(),
    "estBinome", saved.getEstBinome(),
    "candidat2Email", candidat2 != null ? candidat2.getEmail() : null
));
```

**Après `candidatureRepository.save(updated)` dans la méthode `modifier()` :**
```java
// Appel HTTP POST vers n8n webhook
notifierN8n("http://localhost:5678/webhook/statut-candidature", Map.of(
    "candidatNom", updated.getCandidat1().getNom(),
    "candidatPrenom", updated.getCandidat1().getPrenom(),
    "candidatEmail", updated.getCandidat1().getEmail(),
    "nouveauStatut", updated.getStatut().name(),
    "typeStage", updated.getTypeStage().name(),
    "dateEntretien", updated.getDateEntretien() != null ? updated.getDateEntretien().toString() : "",
    "candidatureId", updated.getId().toString(),
    "estBinome", updated.getEstBinome()
));
```

**Méthode utilitaire `notifierN8n()` :**
```java
private void notifierN8n(String url, Map<String, Object> payload) {
    try {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(url, payload, String.class);
    } catch (Exception e) {
        // Log l'erreur mais ne bloque pas l'opération principale
        System.err.println("Erreur notification n8n : " + e.getMessage());
    }
}
```

> ⚠️ Ces modifications seront implémentées dans le code plus tard. Cette section sert uniquement à comprendre la logique.

---

## 9. Résumé des endpoints Webhook n8n

| Workflow | URL Webhook n8n | Déclenché par |
|----------|----------------|---------------|
| Confirmation candidature | `POST http://localhost:5678/webhook/confirmation-candidature` | `CandidatureService.creer()` |
| Changement de statut | `POST http://localhost:5678/webhook/statut-candidature` | `CandidatureService.modifier()` |

---

## 10. Alternative : Polling (sans modifier le backend)

Si tu ne veux **pas modifier le backend** pour l'instant, n8n peut **interroger la base de données ou l'API** régulièrement pour détecter les changements.

### 10.1 — Polling via l'API REST

1. Crée un workflow avec le nœud **"Schedule Trigger"**
   - Fréquence : toutes les minutes (ou 5 minutes)
2. Ajoute un nœud **"HTTP Request"**
   - Method : `GET`
   - URL : `http://localhost:8086/api/candidatures`
3. Ajoute un nœud **"Compare Datasets"** ou utilise un nœud **"IF"** pour détecter les nouveaux enregistrements ou changements de statut.

> ⚠️ Cette approche est moins précise et plus lourde que les webhooks. Elle est conseillée uniquement si tu ne peux pas modifier le backend.

### 10.2 — Polling direct sur la base PostgreSQL

1. Ajoute le nœud **"Postgres"** dans n8n
2. Configure les credentials :
   - **Host** : `localhost`
   - **Port** : `5432`
   - **Database** : `gestion_stagiaires`
   - **User** : `ahmed`
   - **Password** : `ahmed`
3. Requête SQL pour détecter les nouvelles candidatures des dernières minutes :

```sql
SELECT 
    c.id,
    c.statut,
    c.type_stage,
    c.date_depot,
    c.date_entretien,
    ca1.nom AS candidat1_nom,
    ca1.prenom AS candidat1_prenom,
    ca1.email AS candidat1_email
FROM candidatures c
JOIN candidats ca1 ON c.candidat1_id = ca1.id
WHERE c.date_depot >= NOW() - INTERVAL '1 minute'
```

---

## 11. Tests et débogage

### 11.1 — Tester un Workflow manuellement

1. Dans n8n, ouvre le workflow
2. Clique sur **"Test Workflow"** (le bouton ▶️)
3. Utilise Postman pour envoyer une requête à l'URL de test :
   ```
   http://localhost:5678/webhook-test/confirmation-candidature
   ```
   *(Note : en mode test, l'URL contient `/webhook-test/`)*

### 11.2 — Voir les logs d'exécution

1. Dans le menu de gauche, clique sur **"Executions"**
2. Tu verras toutes les exécutions avec leur statut (✅ succès / ❌ erreur)
3. Clique sur une exécution pour voir **les données à chaque nœud**

### 11.3 — Problèmes courants

| Problème | Solution |
|----------|---------|
| n8n ne reçoit pas le webhook | Vérifie que le workflow est **activé** (toggle vert) |
| Email non envoyé | Vérifie les credentials SMTP dans n8n |
| Erreur CORS ou connexion refusée | Vérifie que Spring Boot tourne sur le port 8086 |
| Gmail bloque l'envoi | Active "App Password" dans les paramètres Google |
| Workflow s'exécute mais email non reçu | Vérifie les spams ou utilise Mailtrap pour les tests |

### 11.4 — Variables dynamiques dans n8n

Dans n8n, pour accéder aux données reçues par le Webhook, utilise la syntaxe :
```
{{ $json.nomDuChamp }}
```

Par exemple :
- `{{ $json.candidatEmail }}` → l'email du candidat
- `{{ $json.nouveauStatut }}` → le nouveau statut
- `{{ $json.typeStage }}` → le type de stage (PFE, INITIATION, etc.)

---

## 📋 Checklist de mise en place

- [ ] n8n installé et accessible sur `http://localhost:5678`
- [ ] Credentials SMTP configurés dans n8n
- [ ] Workflow 1 "Confirmation Candidature" créé et activé
- [ ] Workflow 2 "Changement Statut" créé et activé avec nœud Switch
- [ ] Backend Spring Boot modifié pour appeler les webhooks n8n
- [ ] Tests effectués avec Postman
- [ ] Vérification des emails reçus (ou dans Mailtrap)

---

## 🔗 Ressources utiles

- Documentation n8n : https://docs.n8n.io
- Nœud Webhook n8n : https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.webhook/
- Nœud Send Email : https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.sendemail/
- Nœud Switch : https://docs.n8n.io/integrations/builtin/core-nodes/n8n-nodes-base.switch/
- Mailtrap (tests email) : https://mailtrap.io
- App Passwords Gmail : https://myaccount.google.com/apppasswords

