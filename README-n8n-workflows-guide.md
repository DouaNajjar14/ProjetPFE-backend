# Guide n8n — Workflows complets Ooredoo Gestion des Stages

Ce document décrit **pas à pas** comment configurer les workflows n8n
pour automatiser les emails et l'onboarding des stagiaires.

> **Prérequis**
> - n8n installé et accessible sur `http://localhost:5678`
> - Backend Spring Boot démarré sur `http://localhost:8086`
> - Un compte Gmail ou SMTP configuré dans n8n (Credentials)
> - Le token partagé : `ooredoo-n8n-secret-2026`

---

## Vue d'ensemble des workflows

| # | Nom du workflow | Déclencheur | Actions |
|---|---|---|---|
| WF-01 | Confirmation de candidature | Webhook POST | Email de confirmation au candidat |
| WF-02 | Changement de statut | Webhook POST | Email présélection + Meet / refus / acceptation |
| WF-03 | Onboarding stagiaire | Suite de WF-02 (ACCEPTE) | Création PendingAccount + email identifiants |

---

## WF-01 — Email de confirmation de candidature

### Objectif

Envoyer un email de confirmation au candidat dès qu'il soumet sa candidature.

### Déclencheur

Le backend appelle ce webhook automatiquement lors de la création d'une candidature.

---

### Étapes dans n8n

#### Étape 1 — Créer le workflow

1. Ouvrir n8n → **New Workflow**
2. Nommer : `WF-01 - Confirmation Candidature`

---

#### Étape 2 — Nœud Webhook (trigger)

1. Ajouter un nœud **Webhook**
2. Configurer :
   - **HTTP Method** : `POST`
   - **Path** : `confirmation-candidature`
   - **Authentication** : None
   - **Response Mode** : `Immediately`
3. Copier l'URL générée
4. La mettre dans `application.properties` :
   ```
   app.n8n.webhook.confirmation-candidature=http://localhost:5678/webhook/confirmation-candidature
   ```

---

#### Étape 3 — Nœud Send Email

1. Ajouter un nœud **Send Email** (Gmail ou SMTP)
2. Configurer :
   - **To** : `{{ $json.body.candidatEmail }}`
   - **Subject** : `Ooredoo — Confirmation de votre candidature`
   - **Email Format** : HTML
   - **HTML Body** : coller le template HTML de confirmation
     (variables disponibles : `candidatNom`, `candidatPrenom`, `typeStage`, `candidatureId`)

---

#### Étape 4 — Activer le workflow

1. Cliquer **Save**
2. Cliquer **Active** (toggle en haut à droite)

---

---

## WF-02 — Changement de statut de candidature

### Objectif

Router vers 3 branches selon la valeur de `n8nAction` :
- `ENVOYER_EMAIL_PRESELECTION_AVEC_MEET` → créer un Google Meet + email
- `ENVOYER_EMAIL_REFUS` → email de refus
- `ENVOYER_EMAIL_ACCEPTATION` → email d'acceptation

---

### Étapes dans n8n

#### Étape 1 — Créer le workflow

1. Ouvrir n8n → **New Workflow**
2. Nommer : `WF-02 - Statut Candidature`

---

#### Étape 2 — Nœud Webhook (trigger)

1. Ajouter un nœud **Webhook**
2. Configurer :
   - **HTTP Method** : `POST`
   - **Path** : `statut-candidature`
   - **Authentication** : None
   - **Response Mode** : `Immediately`
3. Mettre dans `application.properties` :
   ```
   app.n8n.webhook.statut-candidature=http://localhost:5678/webhook/statut-candidature
   ```

---

#### Étape 3 — Nœud Switch (routage par action)

1. Ajouter un nœud **Switch**
2. **Mode** : `Rules`
3. **Value** : `{{ $json.body.n8nAction }}`
4. Ajouter 3 règles :

| Règle | Condition | Valeur |
|---|---|---|
| Output 0 | Equal | `ENVOYER_EMAIL_PRESELECTION_AVEC_MEET` |
| Output 1 | Equal | `ENVOYER_EMAIL_REFUS` |
| Output 2 | Equal | `ENVOYER_EMAIL_ACCEPTATION` |

---

### Branche 0 — Présélection avec Google Meet

#### Étape 4a — Nœud Google Calendar (créer un Meet)

1. Connecter **Output 0** du Switch
2. Ajouter un nœud **Google Calendar** → action **Create Event**
3. Configurer les credentials Google OAuth2
4. Paramètres :
   - **Calendar** : votre calendrier RH
   - **Title** : `Entretien — {{ $json.body.candidat1Prenom }} {{ $json.body.candidat1Nom }}`
   - **Start** : `{{ $json.body.dateEntretien }}`
   - **End** : calculer +1h (Expression : `{{ new Date(new Date($json.body.dateEntretien).getTime() + 60*60*1000).toISOString() }}`)
   - **Attendees** : `{{ $json.body.candidat1Email }}`
   - **Conference Data** : activer **Add Google Meet link**
   - **Description** : `Entretien de stage {{ $json.body.typeStage }} — Ooredoo Tunisie`

> ⚠️ Le lien Meet sera disponible dans la réponse du nœud sous :
> `{{ $node["Google Calendar"].json.hangoutLink }}`

---

#### Étape 4b — Nœud Send Email (présélection)

1. Ajouter un nœud **Send Email**
2. Connecter après le nœud Google Calendar
3. Configurer :
   - **To** : `{{ $json.body.candidat1Email }}`
   - **Subject** : `Ooredoo — Vous êtes présélectionné(e) !`
   - **HTML Body** :

```html
<p>Bonjour {{ $json.body.candidat1Prenom }} {{ $json.body.candidat1Nom }},</p>
<p>Nous avons le plaisir de vous informer que votre candidature a été <strong>présélectionnée</strong>.</p>
<p><strong>Date de votre entretien :</strong> {{ $json.body.dateEntretienDate }} à {{ $json.body.dateEntretienHeure }}</p>
<p><strong>Lien Google Meet :</strong>
  <a href="{{ $node["Google Calendar"].json.hangoutLink }}">
    Rejoindre l'entretien
  </a>
</p>
<p>Cordialement,<br/>L'équipe RH Ooredoo Tunisie</p>
```

> Si le candidat est en binôme (`estBinome = true`),
> envoyer aussi un email à `candidat2Email`.

---

#### Étape 4b-bis — Nœud IF (binôme ?)

1. Ajouter un nœud **IF** après le Google Calendar
2. Condition : `{{ $json.body.estBinome }}` Equal `true`
3. Branche **True** → envoyer un second email à `candidat2Email`
4. Branche **False** → envoyer uniquement à `candidat1Email`

---

### Branche 1 — Refus

#### Étape 5a — Nœud Send Email (refus)

1. Connecter **Output 1** du Switch
2. Ajouter un nœud **Send Email**
3. Configurer :
   - **To** : `{{ $json.body.candidat1Email }}`
   - **Subject** : `Ooredoo — Suite donnée à votre candidature`
   - **HTML Body** :

```html
<p>Bonjour {{ $json.body.candidat1Prenom }} {{ $json.body.candidat1Nom }},</p>
<p>Nous vous remercions de l'intérêt que vous portez à Ooredoo Tunisie.</p>
<p>Après examen attentif de votre candidature, nous sommes au regret de vous informer
   que nous ne pouvons pas y donner suite favorablement.</p>
<p>Nous vous souhaitons plein succès dans vos démarches.</p>
<p>Cordialement,<br/>L'équipe RH Ooredoo Tunisie</p>
```

---

### Branche 2 — Acceptation

#### Étape 6a — Nœud Send Email (acceptation)

1. Connecter **Output 2** du Switch
2. Ajouter un nœud **Send Email**
3. Configurer :
   - **To** : `{{ $json.body.candidat1Email }}`
   - **Subject** : `Ooredoo — Votre candidature est acceptée !`
   - **HTML Body** : coller le template `email_acceptation_stage.html`
     avec les variables :
     - `{{ $json.body.candidat1Prenom }}`
     - `{{ $json.body.candidat1Nom }}`
     - `{{ $json.body.typeStage }}`
     - `{{ $json.body.departement }}`
     - `{{ $json.body.dateDebutFormatee }}`
     - `{{ $json.body.candidatureId }}`
     - Si PFE : `{{ $json.body.pfeSujetTitre }}`, `{{ $json.body.pfeSpecialite }}`

---

#### Étape 6b — Nœud HTTP Request (créer PendingAccount)

Après l'email d'acceptation, déclencher la création du compte stagiaire.

1. Ajouter un nœud **HTTP Request**
2. Connecter après le Send Email d'acceptation
3. Configurer :
   - **Method** : `POST`
   - **URL** : `http://localhost:8086/api/n8n/pending-accounts`
   - **Headers** :
     - `Content-Type` : `application/json`
     - `X-N8N-Token` : `ooredoo-n8n-secret-2026`
   - **Body** (JSON) :

```json
{
  "candidatureId": "{{ $json.body.candidatureId }}",
  "prenom":        "{{ $json.body.candidat1Prenom }}",
  "nom":           "{{ $json.body.candidat1Nom }}",
  "email":         "{{ $json.body.candidat1Email }}",
  "username":      "{{ $json.body.candidat1Email }}",
  "tempPassword":  "{{ $now.toMillis() }}Ooredoo!",
  "departement":   "{{ $json.body.departement }}",
  "encadrantId":   null,
  "dateDebut":     "{{ $json.body.dateDebut }}",
  "dateFin":       null,
  "typeStage":     "{{ $json.body.typeStage }}"
}
```

> La réponse de ce POST contiendra `token`, `username` et `expiresAt`.
> Ces valeurs seront utilisées dans WF-03.

---

#### Étape 7 — Activer le workflow

1. Cliquer **Save**
2. Cliquer **Active**

---

---

## WF-03 — Onboarding : envoi des identifiants au stagiaire

### Objectif

Après la création du `PendingAccount` par WF-02,
le backend retourne le `token` et les informations du compte.
n8n envoie alors un email au stagiaire avec :
- le lien de confirmation de son compte
- son identifiant de connexion

Ce workflow est déclenché automatiquement **en chaîne depuis WF-02**
(suite du nœud HTTP Request de l'étape 6b).

---

### Étapes dans n8n

> Ces nœuds s'ajoutent **directement dans WF-02**,
> en continuant la chaîne après le nœud HTTP Request.

---

#### Étape 1 — Nœud Set (extraire les données de la réponse)

1. Ajouter un nœud **Set** après le HTTP Request
2. Mapper les champs utiles :

| Nom du champ | Expression |
|---|---|
| `token` | `{{ $json.token }}` |
| `username` | `{{ $json.username }}` |
| `expiresAt` | `{{ $json.expiresAt }}` |
| `prenom` | `{{ $json.prenom }}` |
| `nom` | `{{ $json.nom }}` |
| `email` | `{{ $json.email }}` |

---

#### Étape 2 — Nœud Send Email (email identifiants stagiaire)

1. Ajouter un nœud **Send Email**
2. Configurer :
   - **To** : `{{ $json.email }}`
   - **Subject** : `Ooredoo — Vos identifiants de connexion à la plateforme`
   - **HTML Body** :

```html
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"/></head>
<body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;">
  <table width="600" style="margin:auto;background:#fff;border-radius:12px;overflow:hidden;
         box-shadow:0 2px 12px rgba(0,0,0,0.08);">

    <!-- Header -->
    <tr>
      <td style="background:#ED1C24;padding:32px 40px;">
        <p style="margin:0;font-size:26px;font-weight:900;color:#fff;letter-spacing:-0.02em;">
          ooredoo<sup style="font-size:12px;">®</sup>
        </p>
        <p style="margin:8px 0 0;font-size:11px;font-weight:700;letter-spacing:0.2em;
                  text-transform:uppercase;color:rgba(255,255,255,0.75);">
          Plateforme Gestion des Stages
        </p>
      </td>
    </tr>

    <!-- Body -->
    <tr>
      <td style="padding:36px 40px;">
        <p style="font-size:17px;font-weight:700;color:#1a0505;margin:0 0 8px;">
          Bonjour {{ $json.prenom }} {{ $json.nom }}&thinsp;,
        </p>
        <p style="font-size:13.5px;color:#555;line-height:1.8;margin:0 0 28px;">
          Votre candidature a été acceptée. Voici vos informations de connexion
          à la <strong style="color:#ED1C24;">Plateforme Stagiaire Ooredoo</strong>.
        </p>

        <!-- Identifiants -->
        <table width="100%" style="border:1px solid #eed8d8;border-radius:10px;
               overflow:hidden;margin-bottom:28px;">
          <tr style="background:#fef6f5;">
            <td colspan="2" style="padding:10px 18px;border-bottom:1px solid #eed8d8;">
              <p style="margin:0;font-size:10px;font-weight:700;letter-spacing:0.18em;
                        text-transform:uppercase;color:#ED1C24;">
                🔐 Vos identifiants
              </p>
            </td>
          </tr>
          <tr>
            <td style="padding:14px 18px;border-right:1px solid #eed8d8;
                       border-bottom:1px solid rgba(238,216,216,0.5);width:40%;">
              <p style="margin:0 0 3px;font-size:10px;font-weight:700;
                        text-transform:uppercase;color:#c8a0a0;">Identifiant</p>
              <p style="margin:0;font-size:13px;font-weight:700;color:#1a0505;">
                {{ $json.username }}
              </p>
            </td>
            <td style="padding:14px 18px;border-bottom:1px solid rgba(238,216,216,0.5);">
              <p style="margin:0 0 3px;font-size:10px;font-weight:700;
                        text-transform:uppercase;color:#c8a0a0;">Mot de passe temporaire</p>
              <p style="margin:0;font-size:13px;font-weight:700;
                        color:#1a0505;letter-spacing:0.05em;font-family:monospace;">
                À récupérer via le lien ci-dessous
              </p>
            </td>
          </tr>
        </table>

        <!-- Bouton confirmation -->
        <table width="100%" style="margin-bottom:28px;">
          <tr>
            <td align="center">
              <a href="http://localhost:8086/api/admin/confirm-direct/{{ $json.token }}"
                 style="display:inline-block;padding:14px 36px;background:#ED1C24;
                        color:#fff;text-decoration:none;border-radius:8px;
                        font-size:14px;font-weight:700;letter-spacing:0.02em;">
                ✓ Activer mon compte
              </a>
            </td>
          </tr>
        </table>

        <!-- Alerte expiration -->
        <table width="100%" style="border-radius:10px;overflow:hidden;
               border:1.5px solid rgba(204,0,16,.2);background:#fffbfb;margin-bottom:24px;">
          <tr>
            <td style="width:4px;background:#ED1C24;padding:0;font-size:0;">&nbsp;</td>
            <td style="padding:14px 18px;">
              <p style="margin:0;font-size:12.5px;color:#6a4a48;line-height:1.7;">
                ⏰ Ce lien est valide <strong style="color:#cc0010;">48 heures</strong>
                jusqu'au <strong>{{ $json.expiresAt }}</strong>.<br/>
                Après activation, vous devrez changer votre mot de passe lors de
                votre première connexion.
              </p>
            </td>
          </tr>
        </table>

        <p style="margin:0;font-size:12px;color:#b09090;line-height:1.8;">
          Questions ? <a href="mailto:rh@ooredoo.tn"
          style="color:#ED1C24;font-weight:700;text-decoration:none;">rh@ooredoo.tn</a>
        </p>
      </td>
    </tr>

    <!-- Footer -->
    <tr>
      <td style="background:#ED1C24;padding:20px 40px;text-align:center;">
        <p style="margin:0;font-size:18px;font-weight:900;color:#fff;">
          ooredoo<sup style="font-size:9px;">®</sup>
        </p>
        <p style="margin:6px 0 0;font-size:10px;font-weight:700;letter-spacing:0.2em;
                  text-transform:uppercase;color:rgba(255,255,255,0.65);">
          Plateforme Intelligente • Gestion des Stages
        </p>
        <p style="margin:10px 0 0;font-size:10px;color:rgba(255,255,255,0.45);">
          © 2026 Ooredoo Tunisie • Email automatique — merci de ne pas répondre directement.
        </p>
      </td>
    </tr>

  </table>
</body>
</html>
```

---

#### Étape 3 — Activer

1. Cliquer **Save**
2. Le workflow est déjà actif (même workflow que WF-02)

---

---

## Résumé des connexions entre les nœuds

### WF-01 — Confirmation candidature

```
Webhook (POST /confirmation-candidature)
  └─► Send Email → candidat1Email
```

---

### WF-02 + WF-03 — Statut + Onboarding (un seul workflow n8n)

```
Webhook (POST /statut-candidature)
  └─► Switch (n8nAction)
        ├─ [0] PRESELECTION_AVEC_MEET
        │       └─► Google Calendar (Create Event + Meet)
        │               └─► IF estBinome
        │                     ├─ true  → Send Email candidat1 + Send Email candidat2
        │                     └─ false → Send Email candidat1
        │
        ├─ [1] REFUS
        │       └─► Send Email candidat1 (email de refus)
        │
        └─ [2] ACCEPTATION
                └─► Send Email candidat1 (email acceptation)
                        └─► HTTP Request POST /api/n8n/pending-accounts
                                └─► Set (extraire token, username, email…)
                                        └─► Send Email candidat1 (email identifiants)
```

---

## Variables disponibles dans les nœuds n8n

Toutes les variables sont accessibles via `$json.body.*` dans le webhook trigger.

### Variables candidature

| Variable | Description |
|---|---|
| `$json.body.candidatureId` | UUID de la candidature |
| `$json.body.typeStage` | `INITIATION`, `PERFECTIONNEMENT`, `ETE`, `PFE` |
| `$json.body.nouveauStatut` | Nouveau statut |
| `$json.body.n8nAction` | Clé de routage du Switch |
| `$json.body.estBinome` | `true` / `false` |

### Variables candidat principal

| Variable | Description |
|---|---|
| `$json.body.candidat1Prenom` | Prénom |
| `$json.body.candidat1Nom` | Nom |
| `$json.body.candidat1Email` | Email |
| `$json.body.candidat1Tel` | Téléphone |
| `$json.body.universite` | Université |

### Variables candidat binôme

| Variable | Description |
|---|---|
| `$json.body.candidat2Prenom` | Prénom |
| `$json.body.candidat2Nom` | Nom |
| `$json.body.candidat2Email` | Email |

### Variables dates

| Variable | Description |
|---|---|
| `$json.body.dateEntretien` | ISO 8601 complet |
| `$json.body.dateEntretienDate` | Date seule (ex: `15/03/2026`) |
| `$json.body.dateEntretienHeure` | Heure seule (ex: `10:30`) |
| `$json.body.dateDebut` | Date début stage (ISO 8601) |
| `$json.body.dateDebutFormatee` | Date formatée lisible |

### Variables stage / PFE

| Variable | Description |
|---|---|
| `$json.body.departement` | Nom du département |
| `$json.body.pfeSujetTitre` | Titre du sujet PFE (si PFE) |
| `$json.body.pfeSpecialite` | Spécialité requise (si PFE) |
| `$json.body.pfeDuree` | Durée du PFE (si PFE) |

### Variables PendingAccount (réponse du POST /api/n8n/pending-accounts)

| Variable | Description |
|---|---|
| `$json.token` | UUID token de confirmation |
| `$json.username` | Email / username du stagiaire |
| `$json.expiresAt` | Expiration du token (48h) |
| `$json.prenom` | Prénom |
| `$json.nom` | Nom |
| `$json.email` | Email |

---

## Configuration des Credentials n8n

### 1. Gmail / SMTP

1. n8n → **Settings** → **Credentials** → **New**
2. Choisir **Gmail** ou **SMTP**
3. Pour Gmail : activer l'accès application dans les paramètres Google
4. Tester la connexion
5. Nommer : `Ooredoo RH Email`

### 2. Google Calendar (pour les Google Meet)

1. n8n → **Settings** → **Credentials** → **New**
2. Choisir **Google Calendar OAuth2**
3. Configurer un projet Google Cloud Console :
   - Activer **Google Calendar API**
   - Créer des credentials OAuth2 (type : Web Application)
   - Ajouter l'URI de callback n8n : `http://localhost:5678/rest/oauth2-credential/callback`
4. Copier Client ID et Client Secret dans n8n
5. Nommer : `Ooredoo Google Calendar`

---

## Checklist de mise en production

### Backend
- [ ] Changer `app.n8n.api-token` dans `application.properties`
- [ ] Mettre à jour `app.frontend.url` avec l'URL réelle du frontend
- [ ] Mettre à jour les URLs des webhooks pour pointer vers l'URL de production de n8n

### n8n
- [ ] Remplacer `http://localhost:8086` par l'URL de production du backend
- [ ] Remplacer `http://localhost:5678` par l'URL de production de n8n
- [ ] Vérifier que le token `X-N8N-Token` correspond bien dans les deux sens
- [ ] Tester chaque branche du Switch manuellement avec des données réelles
- [ ] Activer tous les workflows

### Test de bout en bout
- [ ] Créer une candidature → vérifier email de confirmation (WF-01)
- [ ] Passer à `PRESELECTIONNE` → vérifier email + Google Meet créé
- [ ] Passer à `REJETE` → vérifier email de refus
- [ ] Passer à `ACCEPTE` → vérifier email d'acceptation + PendingAccount créé
- [ ] Cliquer sur le lien de confirmation → vérifier création compte + email identifiants
- [ ] Se connecter à la plateforme avec les identifiants reçus

---

## Erreurs courantes et solutions

| Erreur | Cause probable | Solution |
|---|---|---|
| `403 Forbidden` sur `/api/n8n/pending-accounts` | Token `X-N8N-Token` incorrect | Vérifier que le header correspond à `app.n8n.api-token` |
| `409 Conflict` sur `/api/n8n/pending-accounts` | PendingAccount déjà créé pour cette candidature | Normal si le workflow est rejoué — vérifier les logs |
| `410 Gone` sur confirm-direct | Token expiré (> 48h) | Recréer un PendingAccount depuis l'admin |
| `Google Meet non créé` | Credentials Google Calendar invalides | Vérifier OAuth2 dans n8n |
| `Email non reçu` | Credentials SMTP/Gmail invalides | Tester les credentials dans n8n |
| Webhook ne répond pas | Backend arrêté ou URL incorrecte | Vérifier que le backend tourne sur le port 8086 |

