# 🇬🇦 GabChat — Messagerie Gabonaise

Application de messagerie instantanée Android, développée en Java avec Firebase.

---

## 📋 Fonctionnalités

- ✅ Inscription (Email / Google / SMS OTP)
- ✅ Messagerie instantanée (texte, images, vidéos, audio)
- ✅ Indicateurs lu/non-lu (✓ ✓✓ 🔵)
- ✅ Indicateur "en train d'écrire..."
- ✅ Chats de groupe (création, gestion)
- ✅ Appels vocaux et vidéo (UI complète)
- ✅ Notifications push (Firebase FCM)
- ✅ Statut en ligne / hors ligne / dernière vue
- ✅ Personnalisation du profil (photo, statut, bio)
- ✅ Paramètres (notifications, mode sombre)
- ✅ Cryptage Firebase (HTTPS + règles de sécurité)

---

## ⚙️ Configuration Firebase

### 1. Créer le projet Firebase

1. Aller sur [console.firebase.google.com](https://console.firebase.google.com)
2. Créer un projet : **GabChat**
3. Activer les services suivants :

### 2. Authentication
- **Email/Mot de passe** → Activer
- **Google** → Activer (récupérer le Web Client ID)
- **Téléphone** → Activer

### 3. Realtime Database
- Créer la base en mode **Production**
- Coller les règles de `firebase_database_rules.json`

### 4. Storage
- Créer le bucket
- Coller les règles de `firebase_storage_rules.txt`

### 5. Cloud Messaging (FCM)
- Activer automatiquement avec Firebase

### 6. Télécharger google-services.json
- Dans les paramètres du projet → **google-services.json**
- Le copier dans : `app/google-services.json`

---

## 🔑 Configuration dans le code

### strings.xml
Remplacer dans `app/src/main/res/values/strings.xml` :
```xml
<string name="Id_client">VOTRE_WEB_CLIENT_ID_ICI</string>
```
Le Web Client ID se trouve dans Firebase Console → Authentication → Google → Web SDK configuration.

---

## 📦 Dépendances principales

| Bibliothèque | Usage |
|---|---|
| Firebase Auth | Authentification |
| Firebase Realtime DB | Stockage messages |
| Firebase Storage | Médias (photos, vidéos, audio) |
| Firebase Messaging | Notifications push |
| Glide | Chargement d'images |
| CircleImageView | Avatars ronds |
| CountryCodePicker | Sélection indicatif pays |
| Material Components | UI moderne |

---

## 🚀 Installation Android Studio

```bash
# 1. Cloner / ouvrir le projet
File > Open > GabChat/

# 2. Sync Gradle
File > Sync Project with Gradle Files

# 3. Ajouter google-services.json dans app/

# 4. Build & Run
Run > Run 'app'
```

### Prérequis
- Android Studio Hedgehog (2023.1+)
- JDK 8+
- Android SDK 34
- Appareil/émulateur Android 7.0+ (API 24+)

---

## 📁 Structure du projet

```
GabChat/
├── app/
│   ├── src/main/
│   │   ├── java/com/gabon/gabchat/
│   │   │   ├── activities/          # 13 écrans
│   │   │   │   ├── SplashActivity
│   │   │   │   ├── LoginActivity
│   │   │   │   ├── RegisterActivity
│   │   │   │   ├── PhoneAuthActivity
│   │   │   │   ├── OtpVerificationActivity
│   │   │   │   ├── MainActivity
│   │   │   │   ├── ChatActivity
│   │   │   │   ├── GroupChatActivity
│   │   │   │   ├── CallActivity
│   │   │   │   ├── EditProfileActivity
│   │   │   │   ├── SettingsActivity
│   │   │   │   ├── NewConversationActivity
│   │   │   │   ├── CreateGroupActivity
│   │   │   │   └── MediaViewerActivity
│   │   │   ├── adapters/
│   │   │   │   ├── MessageAdapter
│   │   │   │   ├── ConversationAdapter
│   │   │   │   └── UserAdapter
│   │   │   ├── fragments/
│   │   │   │   ├── ChatsFragment
│   │   │   │   ├── ContactsFragment
│   │   │   │   ├── ProfileFragment
│   │   │   │   └── StatusFragment
│   │   │   ├── models/
│   │   │   │   ├── User
│   │   │   │   ├── Message
│   │   │   │   ├── Conversation
│   │   │   │   └── Group
│   │   │   ├── utils/
│   │   │   │   ├── FirebaseHelper
│   │   │   │   ├── TimeUtils
│   │   │   │   └── GabChatFirebaseMessagingService
│   │   │   └── GabChatApp.java
│   │   ├── res/
│   │   │   ├── layout/              # 21 layouts XML
│   │   │   ├── drawable/            # 50 drawables vectoriels
│   │   │   ├── values/              # colors, strings, themes, dimens
│   │   │   ├── menu/                # menus toolbar + bottom nav
│   │   │   ├── color/               # sélecteurs de couleur
│   │   │   └── xml/                 # file_paths, backup_rules
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json        ← À ajouter !
├── build.gradle
├── settings.gradle
├── firebase_database_rules.json
├── firebase_storage_rules.txt
└── README.md
```


## 👨‍💻 Développé pour le Gabon 🇬🇦

*Mon but était de Connecter les Gabonais, partout dans le monde. Du couage n'hésiter pas à m'ecrire si vous êtes bloqués de votre coté dans l'intégration.*
