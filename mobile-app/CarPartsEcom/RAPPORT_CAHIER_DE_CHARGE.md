# 📋 RAPPORT ET CAHIER DE CHARGE
## Application Mobile E-Commerce - Pièces Automobiles
### AutoParts Pro - Frontend Android

---

## 📑 TABLE DES MATIÈRES

1. [Présentation du Projet](#1-présentation-du-projet)
2. [Spécifications Techniques](#2-spécifications-techniques)
3. [Architecture de l'Application](#3-architecture-de-lapplication)
4. [Modules Fonctionnels](#4-modules-fonctionnels)
5. [Modèle de Données](#5-modèle-de-données)
6. [Interfaces Utilisateur](#6-interfaces-utilisateur)
7. [Services et API](#7-services-et-api)
8. [Sécurité](#8-sécurité)
9. [Intégrations Tierces](#9-intégrations-tierces)
10. [Tests et Validation](#10-tests-et-validation)
11. [Déploiement](#11-déploiement)
12. [Annexes](#12-annexes)

---

## 1. PRÉSENTATION DU PROJET

### 1.1 Contexte
AutoParts Pro est une application mobile e-commerce native Android dédiée à la vente de pièces automobiles. Elle offre une expérience utilisateur complète depuis la navigation des produits jusqu'au paiement et suivi des commandes.

### 1.2 Objectifs
| Objectif | Description |
|----------|-------------|
| **Vente en ligne** | Permettre l'achat de pièces automobiles via mobile |
| **Assistance IA** | Fournir un assistant virtuel pour le diagnostic automobile |
| **Paiement sécurisé** | Intégration Stripe pour paiements par carte |
| **Géolocalisation** | Sélection d'adresse de livraison via carte |
| **Multi-authentification** | Connexion classique et Google Sign-In |

### 1.3 Public Cible
- Propriétaires de véhicules
- Mécaniciens amateurs
- Professionnels de l'automobile
- Utilisateurs recherchant des conseils de diagnostic

### 1.4 Informations Projet
| Élément | Détail |
|---------|--------|
| **Nom de l'application** | AutoParts Pro |
| **Package** | `com.excit.car_parts_ecom` |
| **Version** | 1.0 |
| **Plateforme** | Android |
| **Langage** | Kotlin |
| **Date** | Janvier 2026 |

---

## 2. SPÉCIFICATIONS TECHNIQUES

### 2.1 Configuration Minimale
| Paramètre | Valeur |
|-----------|--------|
| **SDK Minimum** | API 26 (Android 8.0 Oreo) |
| **SDK Cible** | API 36 (Android 15) |
| **SDK Compilation** | API 36 |
| **Version JVM** | Java 17 |
| **Version Kotlin** | 1.9+ |

### 2.2 Dépendances Principales

#### 2.2.1 Android Jetpack
| Bibliothèque | Utilisation |
|--------------|-------------|
| `androidx.core.ktx` | Extensions Kotlin pour Android |
| `androidx.appcompat` | Compatibilité rétroactive |
| `androidx.constraintlayout` | Layouts responsifs |
| `androidx.lifecycle.viewmodel.ktx` | Architecture MVVM |
| `androidx.lifecycle.livedata.ktx` | Données observables |
| `androidx.room` | Base de données locale |
| `androidx.swiperefreshlayout` | Pull-to-refresh |

#### 2.2.2 Réseau et API
| Bibliothèque | Version | Utilisation |
|--------------|---------|-------------|
| `retrofit` | 2.x | Client HTTP REST |
| `retrofit.converter.gson` | 2.x | Sérialisation JSON |
| `kotlinx.coroutines` | 1.x | Programmation asynchrone |

#### 2.2.3 Services Tiers
| Bibliothèque | Version | Utilisation |
|--------------|---------|-------------|
| `stripe-android` | 20.48.0 | Paiement sécurisé |
| `osmdroid-android` | 6.1.18 | Cartes OpenStreetMap |
| `play-services-auth` | 21.0.0 | Google Sign-In |
| `glide` | 4.x | Chargement d'images |

### 2.3 Permissions Android
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

---

## 3. ARCHITECTURE DE L'APPLICATION

### 3.1 Pattern Architectural
L'application suit le pattern **MVVM (Model-View-ViewModel)** avec **Repository Pattern** pour la séparation des préoccupations.

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Fragments  │  │  Adapters   │  │     Activities      │  │
│  └──────┬──────┘  └─────────────┘  └──────────┬──────────┘  │
│         │                                      │            │
│         ▼                                      ▼            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    ViewModels                        │    │
│  │  (AuthVM, ProductVM, CartVM, OrderVM, ChatVM, etc.) │    │
│  └──────────────────────┬──────────────────────────────┘    │
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                    DATA LAYER                               │
│                         ▼                                   │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                   Repositories                       │    │
│  │  (Auth, Product, Cart, Order, Payment, Claim, User) │    │
│  └───────────┬─────────────────────────┬───────────────┘    │
│              │                         │                    │
│              ▼                         ▼                    │
│  ┌───────────────────────┐  ┌───────────────────────────┐   │
│  │    Local (Room DB)    │  │    Remote (Retrofit)      │   │
│  │  ┌─────────────────┐  │  │  ┌─────────────────────┐  │   │
│  │  │      DAOs       │  │  │  │    API Services     │  │   │
│  │  │  - UserDao      │  │  │  │  - AuthService      │  │   │
│  │  │  - ProductDao   │  │  │  │  - ProductService   │  │   │
│  │  │  - CartDao      │  │  │  │  - CartService      │  │   │
│  │  │  - OrderDao     │  │  │  │  - OrderService     │  │   │
│  │  │  - ClaimDao     │  │  │  │  - PaymentService   │  │   │
│  │  │  - ChatDao      │  │  │  │  - ClaimService     │  │   │
│  │  └─────────────────┘  │  │  │  - UserService      │  │   │
│  │                       │  │  │  - GroqApiService   │  │   │
│  │  ┌─────────────────┐  │  │  └─────────────────────┘  │   │
│  │  │    Entities     │  │  │                           │   │
│  │  │  - UserEntity   │  │  │  ┌─────────────────────┐  │   │
│  │  │  - ProductEntity│  │  │  │       DTOs          │  │   │
│  │  │  - CartItem     │  │  │  │  - AuthDto          │  │   │
│  │  │  - OrderEntity  │  │  │  │  - ProductDto       │  │   │
│  │  │  - ClaimEntity  │  │  │  │  - OrderDto         │  │   │
│  │  │  - ChatMessage  │  │  │  │  - PaymentDto       │  │   │
│  │  └─────────────────┘  │  │  └─────────────────────┘  │   │
│  └───────────────────────┘  └───────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Structure des Fichiers
```
app/src/main/java/com/example/carpartsecom/
│
├── MainActivity.kt                    # Point d'entrée, navigation, DI
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt            # Configuration Room (v7)
│   │   ├── dao/                      # 7 Data Access Objects
│   │   │   ├── CartDao.kt
│   │   │   ├── ChatDao.kt
│   │   │   ├── ClaimDao.kt
│   │   │   ├── OrderDao.kt
│   │   │   ├── OrderItemDao.kt
│   │   │   ├── ProductDao.kt
│   │   │   └── UserDao.kt
│   │   └── entities/                 # 9 Entités Room
│   │       ├── CartItemEntity.kt
│   │       ├── CartItemWithProduct.kt
│   │       ├── ChatMessageEntity.kt
│   │       ├── ClaimEntity.kt
│   │       ├── OrderEntity.kt
│   │       ├── OrderItemEntity.kt
│   │       ├── OtpCodeEntity.kt
│   │       ├── ProductEntity.kt
│   │       └── UserEntity.kt
│   │
│   ├── remote/
│   │   ├── RetrofitClient.kt         # Configuration HTTP
│   │   ├── api/                      # 8 Services API
│   │   │   ├── AuthService.kt
│   │   │   ├── CartService.kt
│   │   │   ├── ClaimService.kt
│   │   │   ├── GroqApiService.kt
│   │   │   ├── OrderService.kt
│   │   │   ├── PaymentService.kt
│   │   │   ├── ProductService.kt
│   │   │   └── UserService.kt
│   │   └── dto/                      # 6 fichiers DTO
│   │       ├── AuthDto.kt
│   │       ├── CartDto.kt
│   │       ├── ClaimDto.kt
│   │       ├── OrderDto.kt
│   │       ├── PaymentDto.kt
│   │       └── ProductDto.kt
│   │
│   └── repository/                   # 8 Repositories
│       ├── AiChatRepository.kt
│       ├── AuthRepository.kt
│       ├── CartRepository.kt
│       ├── ClaimRepository.kt
│       ├── OrderRepository.kt
│       ├── PaymentRepository.kt
│       ├── ProductRepository.kt
│       └── UserRepository.kt
│
├── ui/
│   ├── fragment/                     # 14 Fragments (écrans)
│   │   ├── AssistantFragment.kt
│   │   ├── CartFragment.kt
│   │   ├── CheckoutFragment.kt
│   │   ├── ClaimFragment.kt
│   │   ├── ForgotPasswordFragment.kt
│   │   ├── LoginFragment.kt
│   │   ├── MapPickerFragment.kt
│   │   ├── OrderDetailsFragment.kt
│   │   ├── OrderListFragment.kt
│   │   ├── OTPVerificationFragment.kt
│   │   ├── ProductDetailFragment.kt
│   │   ├── ProductListFragment.kt
│   │   ├── ProfileFragment.kt
│   │   └── RegisterFragment.kt
│   │
│   ├── adapter/                      # 5 Adapters RecyclerView
│   │   ├── CartAdapter.kt
│   │   ├── ChatAdapter.kt
│   │   ├── OrderAdapter.kt
│   │   ├── OrderItemAdapter.kt
│   │   └── ProductAdapter.kt
│   │
│   └── viewmodel/                    # 9 ViewModels
│       ├── AuthViewModel.kt
│       ├── CartViewModel.kt
│       ├── ChatViewModel.kt
│       ├── CheckoutViewModel.kt
│       ├── ClaimViewModel.kt
│       ├── OrderViewModel.kt
│       ├── ProductViewModel.kt
│       ├── ProfileViewModel.kt
│       └── ViewModelFactory.kt
│
└── util/                             # 8 Utilitaires
    ├── CarAssistant.kt               # Assistant IA local (fallback)
    ├── Constants.kt                  # Configuration (URLs, clés)
    ├── GoogleSignInHelper.kt         # Authentification Google
    ├── NetworkErrorHandler.kt        # Gestion des erreurs réseau
    ├── SingleOrListDeserializer.kt   # Désérialiseur JSON
    ├── StripePaymentHelper.kt        # Intégration paiement
    ├── TokenManager.kt               # Gestion JWT
    └── ValidationUtils.kt            # Validation des données
```

### 3.3 Ressources Layout
```
app/src/main/res/layout/              # 22 fichiers XML
├── activity_main.xml                 # Conteneur principal + bottom nav
├── dialog_change_password.xml        # Dialogue changement mot de passe
├── fragment_assistant.xml            # Chat IA
├── fragment_cart.xml                 # Panier
├── fragment_checkout.xml             # Formulaire commande
├── fragment_claim.xml                # Réclamations
├── fragment_forgot_password.xml      # Mot de passe oublié
├── fragment_login.xml                # Connexion
├── fragment_map_picker.xml           # Sélecteur de carte
├── fragment_order_details.xml        # Détails commande
├── fragment_order_list.xml           # Liste commandes
├── fragment_otp_verification.xml     # Vérification OTP
├── fragment_product_detail.xml       # Détails produit
├── fragment_product_list.xml         # Liste produits
├── fragment_profile.xml              # Profil utilisateur
├── fragment_register.xml             # Inscription
├── item_cart.xml                     # Item panier
├── item_chat_assistant.xml           # Message assistant
├── item_chat_user.xml                # Message utilisateur
├── item_order.xml                    # Item commande
├── item_order_detail_product.xml     # Produit dans commande
└── item_product.xml                  # Carte produit
```

---

## 4. MODULES FONCTIONNELS

### 4.1 Module Authentification

#### 4.1.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Inscription** | Création de compte avec email/mot de passe |
| **Vérification OTP** | Validation email par code à 6 chiffres |
| **Connexion classique** | Email + mot de passe |
| **Google Sign-In** | Authentification OAuth2 Google |
| **Mot de passe oublié** | Réinitialisation via OTP email |
| **Déconnexion** | Nettoyage session locale et Google |

#### 4.1.2 Flux d'Inscription
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Register   │────▶│   API       │────▶│  OTP Sent   │────▶│  Verify     │
│  Form       │     │  /register  │     │  to Email   │     │  OTP Code   │
└─────────────┘     └─────────────┘     └─────────────┘     └──────┬──────┘
                                                                    │
                                                                    ▼
                                                           ┌─────────────┐
                                                           │   Login     │
                                                           │   Screen    │
                                                           └─────────────┘
```

#### 4.1.3 Validations
| Champ | Règles de Validation |
|-------|---------------------|
| Prénom | Min 2 caractères, lettres uniquement |
| Nom | Min 2 caractères, lettres uniquement |
| Email | Format email valide (RFC 5322) |
| Mot de passe | Min 6 caractères |
| Confirmation | Identique au mot de passe |
| OTP | Exactement 6 chiffres |

### 4.2 Module Produits

#### 4.2.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Liste produits** | Affichage grille/liste des produits |
| **Recherche** | Recherche par nom/description |
| **Tri** | Par prix (asc/desc), note, nom |
| **Filtrage** | Par catégorie |
| **Détail produit** | Vue complète avec description |
| **Pull-to-refresh** | Actualisation manuelle |

#### 4.2.2 Catégories de Produits
| Catégorie | Exemples |
|-----------|----------|
| **Brakes** | Plaquettes de frein |
| **Engine** | Filtres à huile |
| **Ignition** | Bougies d'allumage |
| **Electrical** | Batteries |

#### 4.2.3 Modèle Produit
```kotlin
data class ProductEntity(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String,
    val description: String,
    val stockQuantity: Int,
    val rating: Double,
    val imageUrl: String
)
```

### 4.3 Module Panier

#### 4.3.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Ajout au panier** | Depuis détail produit |
| **Modification quantité** | +/- avec limites |
| **Suppression article** | Swipe ou bouton |
| **Calcul total** | Automatique en temps réel |
| **Vider le panier** | Suppression globale |
| **Persistance** | Synchronisé avec backend |

#### 4.3.2 Flux Panier → Commande
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Panier    │────▶│  Checkout   │────▶│  Paiement   │────▶│  Commande   │
│   (Cart)    │     │   Form      │     │  (Stripe)   │     │  Créée      │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

### 4.4 Module Commandes

#### 4.4.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Création commande** | Cash ou Carte |
| **Liste commandes** | Historique personnel |
| **Détails commande** | Informations complètes |
| **Annulation** | Commandes PENDING uniquement |
| **Statuts** | PENDING, COMPLETED, CANCELLED |

#### 4.4.2 Modèle Commande
```kotlin
data class OrderEntity(
    val id: Long,
    val userId: Long?,
    val totalAmount: Double?,
    val status: String?,           // PENDING, COMPLETED, CANCELLED
    val paymentMethod: String?,    // cash, card
    val paymentIntentId: String?,  // Stripe ID si paiement carte
    val deliveryLatitude: Double?,
    val deliveryLongitude: Double?,
    val deliveryAddress: String?,
    val contactPhone: String?,
    val deliveryNotes: String?,
    val createdAt: String?
)
```

#### 4.4.3 Informations de Livraison
| Champ | Validation |
|-------|------------|
| Adresse | Min 5 caractères |
| Téléphone | 8-15 chiffres |
| Latitude | -90 à 90 |
| Longitude | -180 à 180 |
| Notes | Optionnel, max 1000 caractères |

### 4.5 Module Paiement

#### 4.5.1 Méthodes de Paiement
| Méthode | Description |
|---------|-------------|
| **Cash on Delivery** | Paiement à la livraison |
| **Card (Stripe)** | Paiement sécurisé par carte |

#### 4.5.2 Flux Stripe
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Create     │────▶│  Payment    │────▶│  Stripe     │────▶│  Confirm    │
│  Intent     │     │  Sheet      │     │  Confirm    │     │  Order      │
│  (Backend)  │     │  (UI)       │     │  (API)      │     │  (Backend)  │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

#### 4.5.3 Cartes de Test Stripe
| Numéro | Résultat |
|--------|----------|
| `4242 4242 4242 4242` | Succès |
| `4000 0000 0000 0002` | Refusée |
| `4000 0000 0000 9995` | Fonds insuffisants |

### 4.6 Module Assistant IA

#### 4.6.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Chat conversationnel** | Interface de messagerie |
| **Diagnostic automobile** | Analyse des symptômes |
| **Recommandations produits** | Suggestions contextuelles |
| **Réponses rapides** | Chips cliquables |
| **Historique** | Persisté localement |

#### 4.6.2 Architecture IA
```
┌─────────────────────────────────────────────────────────────┐
│                    Message Utilisateur                       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                     ChatViewModel                            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              AiChatRepository                        │    │
│  │  ┌───────────────────┐  ┌─────────────────────────┐ │    │
│  │  │   Groq LLM API    │  │   CarAssistant Local    │ │    │
│  │  │   (LLaMA 3.1)     │◀─│   (Fallback)            │ │    │
│  │  │   - 30 req/min    │  │   - Pattern matching    │ │    │
│  │  │   - Context aware │  │   - 500+ règles         │ │    │
│  │  └───────────────────┘  └─────────────────────────┘ │    │
│  └─────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│  - Réponse formatée (Markdown)                              │
│  - Recommandations produits (ProductEntity[])               │
│  - Chips de réponse rapide (String[])                       │
│  - Alerte mécanicien si nécessaire                          │
└─────────────────────────────────────────────────────────────┘
```

#### 4.6.3 Sujets Supportés
| Catégorie | Exemples de Questions |
|-----------|----------------------|
| **Freins** | Grincement, vibration, pédale molle |
| **Moteur** | Surchauffe, fumée, calage |
| **Batterie** | Démarrage difficile, clic |
| **Voyants** | Check engine, huile, batterie |
| **Bruits** | Cognement, sifflement, claquement |
| **Transmission** | Patinage, à-coups |
| **Climatisation** | Pas de froid, bruit |
| **Échappement** | Odeur, bruit |

### 4.7 Module Réclamations

#### 4.7.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Créer réclamation** | Liée à une commande |
| **Liste réclamations** | Historique personnel |
| **Suivi statut** | PENDING, RESOLVED |

#### 4.7.2 Modèle Réclamation
```kotlin
data class ClaimEntity(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String
)
```

### 4.8 Module Profil

#### 4.8.1 Fonctionnalités
| Fonctionnalité | Description |
|----------------|-------------|
| **Affichage profil** | Avatar, nom, email, statut |
| **Modification profil** | Prénom, nom, téléphone |
| **Changement mot de passe** | Ancien + nouveau |
| **Déconnexion** | Nettoyage complet session |
| **Accès réclamations** | Navigation vers module Claims |

---

## 5. MODÈLE DE DONNÉES

### 5.1 Schéma Base de Données Locale (Room)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Room Database v7                               │
│                           "carparts_db"                                  │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│     users       │     │    products     │     │   cart_items    │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ PK id: Long     │     │ PK id: Long     │     │ PK id: Long     │
│ email: String   │     │ name: String    │     │ productId: Long │
│ token: String   │     │ price: Double   │     │ userId: Long    │
│ isVerified: Bool│     │ category: String│     │ quantity: Int   │
│ firstName: Str? │     │ description: Str│     │ productName: Str│
│ lastName: Str?  │     │ stockQuantity   │     │ productPrice    │
│ phoneNumber:Str?│     │ rating: Double  │     │ addedAt: String │
│ googleId: Str?  │     │ imageUrl: String│     └─────────────────┘
│ createdAt: Str? │     └─────────────────┘
└─────────────────┘
                        ┌─────────────────┐     ┌─────────────────┐
┌─────────────────┐     │     orders      │     │   order_items   │
│     claims      │     ├─────────────────┤     ├─────────────────┤
├─────────────────┤     │ PK id: Long     │     │ PK id: Long     │
│ PK id: Long     │     │ userId: Long?   │     │ orderId: Long   │
│ orderId: Long   │     │ totalAmount: Dbl│     │ productId: Long │
│ userId: Long    │     │ status: String? │     │ productName: Str│
│ subject: String │     │ paymentMethod   │     │ quantity: Int   │
│ description: Str│     │ paymentIntentId │     │ priceAtPurchase │
│ status: String  │     │ deliveryLat:Dbl?│     └─────────────────┘
│ createdAt: Str  │     │ deliveryLng:Dbl?│
└─────────────────┘     │ deliveryAddress │     ┌─────────────────┐
                        │ contactPhone    │     │  chat_messages  │
                        │ deliveryNotes   │     ├─────────────────┤
                        │ createdAt: Str? │     │ PK id: Long     │
                        └─────────────────┘     │ message: String │
                                                │ isFromUser: Bool│
                                                │ timestamp: Long │
                                                │ productRecs:Str?│
                                                └─────────────────┘
```

### 5.2 Relations et Contraintes
| Relation | Type | Description |
|----------|------|-------------|
| User → Orders | 1:N | Un utilisateur a plusieurs commandes |
| User → CartItems | 1:N | Un utilisateur a plusieurs articles panier |
| User → Claims | 1:N | Un utilisateur a plusieurs réclamations |
| Order → OrderItems | 1:N | Une commande a plusieurs articles |
| Order → Claims | 1:N | Une commande peut avoir des réclamations |
| Product → CartItems | 1:N | Un produit peut être dans plusieurs paniers |
| Product → OrderItems | 1:N | Un produit peut être dans plusieurs commandes |

---

## 6. INTERFACES UTILISATEUR

### 6.1 Navigation Principale
```
┌─────────────────────────────────────────────────────────────┐
│                    Bottom Navigation Bar                     │
├─────────────┬─────────────┬─────────────┬─────────┬─────────┤
│  Products   │    Cart     │  Assistant  │  Orders │ Profile │
│     🛒      │     🛍️      │     🤖      │    📦   │    👤   │
└─────────────┴─────────────┴─────────────┴─────────┴─────────┘
```

### 6.2 Flux de Navigation
```
                                    ┌─────────────┐
                                    │   Launch    │
                                    └──────┬──────┘
                                           │
                              ┌────────────┴────────────┐
                              │      Has Token?         │
                              └────────────┬────────────┘
                                    │             │
                                   Yes           No
                                    │             │
                                    ▼             ▼
                        ┌───────────────┐  ┌───────────────┐
                        │   Products    │  │    Login      │
                        │    Screen     │  │    Screen     │
                        └───────┬───────┘  └───────┬───────┘
                                │                   │
        ┌───────────────────────┼───────────────────┼────────────────────┐
        │                       │                   │                    │
        ▼                       ▼                   ▼                    ▼
┌─────────────┐         ┌─────────────┐     ┌─────────────┐      ┌─────────────┐
│  Product    │         │    Cart     │     │  Register   │      │  Forgot     │
│  Detail     │         │             │     │             │      │  Password   │
└─────────────┘         └──────┬──────┘     └──────┬──────┘      └─────────────┘
                               │                   │
                               ▼                   ▼
                        ┌─────────────┐     ┌─────────────┐
                        │  Checkout   │     │  OTP        │
                        │             │     │  Verify     │
                        └──────┬──────┘     └─────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
            ┌─────────────┐       ┌─────────────┐
            │ Map Picker  │       │   Stripe    │
            │             │       │   Payment   │
            └─────────────┘       └─────────────┘
```

### 6.3 Écrans Détaillés

#### 6.3.1 Login Screen
| Élément | Type | Description |
|---------|------|-------------|
| Logo | ImageView | Logo application |
| Email | TextInputEditText | Champ email |
| Password | TextInputEditText | Champ mot de passe (masqué) |
| Login Button | MaterialButton | Connexion |
| Google Sign-In | MaterialButton | OAuth Google |
| Register Link | Button | Navigation inscription |
| Forgot Password | Button | Navigation récupération |

#### 6.3.2 Products Screen
| Élément | Type | Description |
|---------|------|-------------|
| Search Bar | SearchView | Recherche produits |
| Category Chips | ChipGroup | Filtrage par catégorie |
| Sort Button | ImageButton | Options de tri |
| Products Grid | RecyclerView | Liste produits (GridLayout) |
| Product Card | MaterialCard | Image, nom, prix, note |
| SwipeRefresh | SwipeRefreshLayout | Pull-to-refresh |

#### 6.3.3 Cart Screen
| Élément | Type | Description |
|---------|------|-------------|
| Cart Items | RecyclerView | Liste articles |
| Item Row | MaterialCard | Produit, quantité, prix |
| Quantity Controls | +/- Buttons | Modifier quantité |
| Remove Button | ImageButton | Supprimer article |
| Subtotal | TextView | Total calculé |
| Checkout Button | MaterialButton | Vers paiement |
| Empty State | LinearLayout | Message panier vide |

#### 6.3.4 Checkout Screen
| Élément | Type | Description |
|---------|------|-------------|
| Address | TextInputEditText | Adresse livraison |
| Phone | TextInputEditText | Téléphone contact |
| Notes | TextInputEditText | Instructions livraison |
| Map Button | MaterialButton | Ouvrir carte |
| Lat/Lng | TextInputEditText | Coordonnées GPS |
| Payment Radio | RadioGroup | Cash / Card |
| Place Order | MaterialButton | Confirmer commande |

#### 6.3.5 Assistant Screen
| Élément | Type | Description |
|---------|------|-------------|
| Messages | RecyclerView | Historique chat |
| User Bubble | MaterialCard | Message utilisateur (droite) |
| Bot Bubble | MaterialCard | Réponse assistant (gauche) |
| Quick Replies | ChipGroup | Suggestions cliquables |
| Product Cards | HorizontalRecyclerView | Recommandations |
| Input Field | TextInputEditText | Saisie message |
| Send Button | MaterialButton | Envoyer |
| Clear Button | ImageButton | Effacer historique |

---

## 7. SERVICES ET API

### 7.1 Configuration Réseau
```kotlin
object Constants {
    const val BASE_URL = "http://10.0.2.2:8080/"  // Émulateur
    // ou IP locale pour device physique
}
```

### 7.2 Endpoints API Backend

#### 7.2.1 Authentification (`/api/auth`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/register` | Non | Inscription |
| POST | `/verify-email` | Non | Vérification OTP |
| POST | `/login` | Non | Connexion |
| POST | `/google-signin` | Non | Auth Google |
| POST | `/forgot-password` | Non | Demande reset |
| POST | `/reset-password` | Non | Reset avec OTP |

#### 7.2.2 Utilisateur (`/api/user`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/profile` | Oui | Obtenir profil |
| PUT | `/profile` | Oui | Modifier profil |
| PUT | `/change-password` | Oui | Changer mot de passe |

#### 7.2.3 Produits (`/api/products`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/` | Non | Liste produits |
| GET | `/{id}` | Non | Détail produit |
| GET | `/search?query=` | Non | Recherche |
| GET | `/sort?by=` | Non | Tri (price_asc, price_desc, rating_desc, name_asc) |
| GET | `/category/{cat}` | Non | Filtrage catégorie |

#### 7.2.4 Panier (`/api/cart`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/` | Oui | Obtenir panier |
| POST | `/add` | Oui | Ajouter article |
| PUT | `/update` | Oui | Modifier quantité |
| DELETE | `/remove/{id}` | Oui | Supprimer article |
| DELETE | `/clear` | Oui | Vider panier |

#### 7.2.5 Paiement (`/api/payment`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/create-intent` | Oui | Créer intention Stripe |
| GET | `/verify/{id}` | Oui | Vérifier paiement |

#### 7.2.6 Commandes (`/api/orders`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/` | Oui | Liste commandes utilisateur |
| POST | `/` | Oui | Créer commande |
| POST | `/{id}/cancel` | Oui | Annuler commande |
| PUT | `/{id}/status` | Admin | Modifier statut |

#### 7.2.7 Réclamations (`/api/claims`)
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/` | Oui | Liste réclamations |
| POST | `/` | Oui | Créer réclamation |
| GET | `/order/{id}` | Oui | Réclamations par commande |

### 7.3 API Externe - Groq (IA)
| Paramètre | Valeur |
|-----------|--------|
| **URL** | `https://api.groq.com/openai/v1/chat/completions` |
| **Modèle** | `llama-3.1-8b-instant` |
| **Rate Limit** | 30 req/min, 6000 req/jour |
| **Auth** | Bearer Token |

---

## 8. SÉCURITÉ

### 8.1 Authentification
| Mécanisme | Implémentation |
|-----------|----------------|
| **JWT Tokens** | Stockés dans SharedPreferences |
| **Token Manager** | Gestion centralisée |
| **Auto-logout** | Token expiré → redirection login |
| **Google OAuth** | ID Token vérifié backend |

### 8.2 Validation des Données
```kotlin
object ValidationUtils {
    // Email: Format RFC 5322
    // Mot de passe: Min 6 caractères
    // Téléphone: 8-15 chiffres
    // Coordonnées: Lat [-90,90], Lng [-180,180]
}
```

### 8.3 Sécurité Réseau
| Mesure | Description |
|--------|-------------|
| **HTTPS** | Recommandé en production |
| **Cleartext** | Autorisé dev uniquement |
| **Error Handling** | Messages génériques utilisateur |

### 8.4 Stockage Local
| Donnée | Protection |
|--------|------------|
| JWT Token | SharedPreferences (privé) |
| Données utilisateur | Room Database (privé) |
| Clés API | Constants.kt (à sécuriser en prod) |

---

## 9. INTÉGRATIONS TIERCES

### 9.1 Stripe (Paiements)
| Élément | Détail |
|---------|--------|
| **SDK** | `stripe-android:20.48.0` |
| **Mode** | Test (pk_test_...) |
| **Fonctionnalités** | Payment Sheet, 3D Secure |
| **Flow** | Backend crée Intent → App affiche Sheet |

### 9.2 OpenStreetMap (Cartes)
| Élément | Détail |
|---------|--------|
| **SDK** | `osmdroid-android:6.1.18` |
| **Licence** | Gratuit, open source |
| **Fonctionnalités** | MapView, markers, touch events |
| **Usage** | Sélection adresse livraison |

### 9.3 Google Sign-In
| Élément | Détail |
|---------|--------|
| **SDK** | `play-services-auth:21.0.0` |
| **Client ID** | Web Application OAuth |
| **Flow** | App → Google → ID Token → Backend |

### 9.4 Groq AI
| Élément | Détail |
|---------|--------|
| **API** | REST (OpenAI compatible) |
| **Modèle** | LLaMA 3.1 8B Instant |
| **Coût** | Gratuit (rate limited) |
| **Fallback** | CarAssistant local |

---

## 10. TESTS ET VALIDATION

### 10.1 Scénarios de Test

#### Test 1: Inscription et Connexion
- [ ] Inscription avec données valides
- [ ] Validation des erreurs de formulaire
- [ ] Vérification OTP
- [ ] Connexion email/mot de passe
- [ ] Connexion Google
- [ ] Déconnexion

#### Test 2: Navigation Produits
- [ ] Chargement liste
- [ ] Recherche
- [ ] Tri (prix, note, nom)
- [ ] Filtrage catégorie
- [ ] Détail produit

#### Test 3: Panier
- [ ] Ajout au panier
- [ ] Modification quantité
- [ ] Suppression article
- [ ] Calcul total

#### Test 4: Commande
- [ ] Validation formulaire
- [ ] Sélection carte
- [ ] Paiement cash
- [ ] Paiement Stripe
- [ ] Création commande

#### Test 5: Suivi Commandes
- [ ] Liste commandes
- [ ] Détails commande
- [ ] Annulation
- [ ] Affichage GPS

#### Test 6: Assistant IA
- [ ] Réponses diagnostic
- [ ] Recommandations produits
- [ ] Quick replies
- [ ] Fallback local

### 10.2 Cartes Test Stripe
| Numéro | Scénario |
|--------|----------|
| `4242 4242 4242 4242` | Succès |
| `4000 0000 0000 0002` | Refusé |
| `4000 0000 0000 9995` | Fonds insuffisants |

---

## 11. DÉPLOIEMENT

### 11.1 Configuration Production
| Paramètre | Développement | Production |
|-----------|---------------|------------|
| BASE_URL | `http://10.0.2.2:8080/` | `https://api.example.com/` |
| Stripe Key | `pk_test_...` | `pk_live_...` |
| Groq Key | Variable | Sécurisé |
| Cleartext | Autorisé | Désactivé |

### 11.2 Build Release
```bash
./gradlew assembleRelease
```

### 11.3 Signing
- Keystore sécurisé
- Signature APK/AAB
- Play Store ready

---

## 12. ANNEXES

### 12.1 Statistiques du Projet
| Métrique | Valeur |
|----------|--------|
| **Fichiers Kotlin** | 55+ |
| **Fichiers XML Layout** | 22 |
| **Entités Room** | 9 |
| **API Services** | 8 |
| **Repositories** | 8 |
| **ViewModels** | 9 |
| **Fragments** | 14 |
| **Adapters** | 5 |
| **Utilitaires** | 8 |
| **Version DB** | 7 |

### 12.2 Dépendances Complètes
```kotlin
// AndroidX Core
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.material)
implementation(libs.androidx.constraintlayout)
implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

// Architecture
implementation(libs.androidx.lifecycle.viewmodel.ktx)
implementation(libs.androidx.lifecycle.livedata.ktx)

// Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

// Network
implementation(libs.retrofit)
implementation(libs.retrofit.converter.gson)
implementation(libs.kotlinx.coroutines.android)

// Images
implementation(libs.glide)

// Payment
implementation("com.stripe:stripe-android:20.48.0")

// Maps
implementation("org.osmdroid:osmdroid-android:6.1.18")

// Auth
implementation("com.google.android.gms:play-services-auth:21.0.0")
```

### 12.3 Contacts et Support
| Rôle | Contact |
|------|---------|
| Développement | [À compléter] |
| Support technique | [À compléter] |
| Rapport de bugs | [À compléter] |

---

## 📝 Historique des Versions

| Version | Date | Changements |
|---------|------|-------------|
| 1.0 | Janvier 2026 | Version initiale |

---

**Document généré le:** 6 Janvier 2026  
**Application:** AutoParts Pro v1.0  
**Plateforme:** Android (Kotlin)

