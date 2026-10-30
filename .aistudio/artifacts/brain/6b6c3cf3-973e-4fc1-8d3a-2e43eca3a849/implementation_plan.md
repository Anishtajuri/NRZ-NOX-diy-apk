# LiveSnap: Real-Time Live Photo Exchange

A peer-to-peer real-time Android camera app where friends can request and exchange live front-camera photos on demand with explicit mutual consent and instant ephemeral viewing.

### User Review & Critical Decisions

> [!IMPORTANT]
> Please review the architecture and UX decisions below. Your feedback from Phase 1 has been integrated.

- **Confirmed Decision (Friend Discovery)**: Searchable Usernames & Friends. Users choose a unique username handle (e.g. `@jordan`) on sign-in, search friends by username, and add them to their direct-request friend circle.
- **Photo Request & Consent Flow**: When user A requests a photo from user B, user B receives an immediate real-time incoming request dialog ("@alex wants a live photo! Allow or Decline?"). If declined, user A receives an instant notification. If allowed, user B's front camera opens with a 3-second countdown, snaps the photo, and transmits it directly to user A.
- **Viewing Experience & Ephemerality**: The photo appears full-screen for user A with an ephemeral 10-second countdown before vanishing, preventing stale or saved captures.
- **Backend Infrastructure**: Real-time Firestore sync ensures zero-delay notification pop-ups and live image exchange between two Android devices.

---

### 1. Overview & Core Concept

- **What It Does**: Connects two users in real-time. A requester can ping a friend asking for a live photo of what they are doing right now. The friend receives a prompt popup; if they allow, the front camera captures their current face/reaction and sends it back immediately.
- **Target Audience / Persona**: Close friends, couples, and family members who want spontaneous, authentic "where are you / what are you up to right now" moments without staged photo uploads.
- **Key Value**: 100% authentic, real-time presence with complete recipient consent and privacy control.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Onboarding & Handle Creation**:
   - Google Sign-In with standard authentication gate.
   - Profile creation: pick a unique username handle (e.g. `@chris`) and avatar style.
2. **Friends & Search Hub**:
   - Search bar to look up friends by username.
   - Friends list showing online status, last exchange, and a prominent "Request Live Photo" button.
3. **Incoming Live Request (Recipient Screen)**:
   - When a request arrives, a high-priority bottom sheet / modal card animates up:
     - Avatar and name of requester: *"@sam is asking for a live face photo right now."*
     - Clear dual actions: **Decline** (dismisses and notifies sender) and **Allow & Snap** (launches camera flow).
4. **Camera Capture Experience**:
   - Clean, full-bleed front camera viewfinder.
   - 3-second pulse countdown ("3... 2... 1... 📸") with auto-capture, or manual instant snap toggle.
   - Quick 2-second review card: "Send this photo to @sam?" with **Confirm Send** or **Retake**.
5. **Live Photo Viewer (Requester Screen)**:
   - Requester's screen switches to "Waiting for @friend to accept..." with an animated radar pulse.
   - As soon as uploaded, photo reveals with a smooth spring transition.
   - Circular countdown timer (10s) at the top right, followed by a playful vanishing smoke transition.

#### Visual Identity & Theme
- **Vibe**: Cyber-Clean & Spontaneous (high-contrast dark mode with neon emerald & electric violet accents).
- **Color Palette**:
  - Background: Deep obsidian (`#0F1117`) and slate card surfaces (`#1A1D27`).
  - Primary Accent: Electric Emerald (`#10B981`) for camera actions and confirmation.
  - Secondary Accent: Neon Violet (`#8B5CF6`) for request badges and radar rings.
  - Surface Tones: Soft cool grays (`#94A3B8`) for secondary copy.
- **Typography**: Clean modern sans-serif typography with high legibility and bold display headers.
- **Micro-Interactions**: Haptic feedback on capture, pulsing radar waves during waiting state, animated countdown bar during viewing.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Real-Time Firestore Sync vs WebRTC**
  - *Chosen Approach*: Firestore real-time snapshot listeners for request handshakes and base64/compressed JPEG image exchange.
  - *Why*: WebRTC peer-to-peer video streaming requires complex STUN/TURN traversal servers and is prone to connection drops between mobile cellular networks. Firestore document listeners provide sub-second handshake latency, guaranteed delivery, and offline fallback.
- **Decision 2: CameraX vs System Intent**
  - *Chosen Approach*: Embedded CameraX front-camera preview with CameraSelector.LENS_FACING_FRONT.
  - *Why*: Launching the native system camera app via Intent interrupts the in-app experience, hides incoming countdown animations, and does not allow instant automated face capture with consent. Embedded CameraX offers smooth in-app transitions.
- **Decision 3: Multi-Device Testability & Solo Simulation Mode**
  - *Chosen Approach*: Real cloud database communication between separate phones, plus an instant "Simulate Partner" switch in the developer header so the user can immediately test both the sender and recipient sides within a single emulator or device.

---

### 4. Technical Architecture & Data Strategy

#### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Jetpack Compose UI Layer                    │
├───────────────────┬──────────────────────┬──────────────────────┤
│   Auth & Profile  │   Friends & Search   │  Request & Camera    │
│    (Google Auth   │   (User Directory    │  (Incoming Popup,    │
│    + Handle Set)  │    & Friend List)    │   CameraX Viewfinder,│
│                   │                      │   Ephemeral Viewer)  │
└─────────┬─────────┴──────────┬───────────┴──────────┬───────────┘
          │                    │                      │
          ▼                    ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              LiveSnapViewModel & UI State Holders               │
│   • currentUserProfile         • incomingLiveRequests Flow      │
│   • searchResultsState         • activeSessionExchange State    │
│   • cameraCaptureState         • countdownTimerState            │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Firestore Realtime Repository                   │
│   • /users/{uid}             -> Profiles & usernames            │
│   • /requests/{requestId}    -> Status (pending/accepted/etc)   │
│   • /exchanges/{exchangeId}  -> Ephemeral payload + image data  │
└─────────────────────────────────────────────────────────────────┘
```

#### Firestore Data Model

1. **`users/{uid}`**:
   - `uid: String`
   - `username: String` (lowercased for indexing, e.g. "alex")
   - `displayName: String`
   - `photoUrl: String?`
   - `updatedAt: Timestamp`

2. **`photo_requests/{requestId}`**:
   - `requestId: String`
   - `requesterId: String`
   - `requesterName: String`
   - `targetUserId: String`
   - `status: String` (`"PENDING"`, `"ACCEPTED"`, `"DECLINED"`, `"EXPIRED"`)
   - `photoPayload: String?` (compressed image string)
   - `createdAt: Timestamp`
   - `expiresAt: Timestamp`

#### Interactive Component & State Mapping
- `SearchFriendsBar`: Reactive search flow querying `/users` where `username >= query` with debounced input.
- `IncomingRequestDialog`: Animated bottom sheet triggered by real-time listener on `photo_requests` where `targetUserId == currentUserId && status == "PENDING"`.
- `CameraCaptureView`: CameraX front camera preview with `ImageCapture.takePicture`, auto-scaling, and byte compression for real-time Firestore transport.
- `EphemeralPhotoViewer`: Full-screen dialog with `LaunchedEffect` timer (10s countdown) automatically setting request status to `"EXPIRED"` upon completion.
