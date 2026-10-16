# Live TV Player for Android TV

A lean, high-performance Android TV live streaming application that connects directly to the StreamingZone API (`https://streamingzone.up.railway.app/api/channels`), automatically starts playing the live stream immediately upon launch, and provides seamless navigation using standard TV remotes (D-pad, OK, and numeric buttons).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during Phase 1 clarification:
> - **Direct Number Channel Changing**: Uses the server's channel ID (typing remote number keys like `4`, `5`, `7` switches directly to that channel ID with on-screen TV digit feedback).
> - **Channel Guide Display**: Elegant, semi-transparent overlay drawer on the left side of the screen, allowing video playback to continue uninterrupted while browsing.
> - **Immediate Autoplay**: Automatically resumes playback of the last watched channel upon app launch, or starts playing the first available channel on initial install—with zero intervening loading screens or landing pages.

- **Confirmed Decision 1**: Channel guide slides in from the left as an overlay drawer when pressing D-pad Left, D-pad Center/OK, or Guide/Menu.
- **Confirmed Decision 2**: Direct number entry overlay displays typed digits on the top-right corner with a 1.5-second auto-commit timer (or instant switch on OK press).
- **Confirmed Decision 3**: Media3 ExoPlayer configured with HLS (`m3u8`) streaming engine and HTTP cleartext traffic enabled to reliably play live IPTV streams.

---

## 1. Overview & Core Concept

- **What It Does**: A dedicated 10-foot UI Android TV app that loads live channels from the API, streams live HLS/M3U8 feeds full-screen, and turns any Android TV or Google TV box into a traditional cable/satellite TV experience with instant remote control.
- **Target Audience**: Android TV / Google TV users who want instant live sports and entertainment channels with traditional remote control convenience.
- **Key Value**: Instant startup straight into video, zero clutter, authentic TV remote usability (channel up/down, direct digit input, and live channel guide drawer).

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Cold Launch**: App opens directly to full-screen video playback with a subtle bottom-left channel info banner (Channel Number, Name, Category, Live indicator) that fades out after 4 seconds.
2. **Channel Switching with D-Pad**:
   - `D-Pad UP` / `Channel Up`: Switches to previous channel in the list.
   - `D-Pad DOWN` / `Channel Down`: Switches to next channel in the list.
   - `D-Pad LEFT` or `OK`: Opens the Left Channel Guide Drawer.
   - `D-Pad RIGHT` / `BACK`: Closes the drawer or returns to full-screen view.
3. **Channel Switching with Remote Number Keys**:
   - Pressing `0-9` triggers a floating TV channel number banner in the upper right (e.g. `[ 7 ] SKY SPORTS FOOTBALL`).
   - Typing additional digits updates the number buffer (e.g., `5`, `0`, `1`).
   - If a matching channel ID exists, it switches immediately on `OK` or automatically after a 1.5s countdown.
4. **Channel Guide Drawer (Left Overlay)**:
   - Categorized channel list (All, Sports, Entertainment, etc.) with focused channel highlighting (glowing border, scale-up effect).
   - Shows channel logo, channel ID badge, and live stream status.
   - Pressing `OK` on a channel switches playback instantly and auto-hides the drawer.
5. **Playback Controls & HUD**:
   - Media keys (`PLAY`, `PAUSE`, `MEDIA_PLAY_PAUSE`) pause/resume the live stream.
   - Quality / aspect ratio toggle (Fit, Fill/Crop, 16:9).
   - Retry banner on connection drop with auto-reconnect logic.

### Visual Identity & Theme
- **Aesthetic Direction**: Cinematic TV Leanback Dark — pure `#0B0F19` deep space background with vivid emerald/cyan accent glows (`#00E5FF` / `#00E676`) for focused items.
- **Focus States**: High-contrast TV focus indicators with 1.05x scale elevation, glowing border ring, and distinct contrast to make D-pad focus unmistakably clear from 10 feet away.
- **Typography**: Clean, bold sans-serif display typography with high readability at distance. Channel numbers rendered in prominent monospace/tabbed numerals.
- **Atmosphere**: Frosted glassmorphism (`Blur` + dark 85% alpha tint) for the channel drawer and HUD banners so live video motion remains subtly visible beneath UI elements.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Fullscreen SurfaceView via Media3 ExoPlayer**
  - *Chosen Approach*: AndroidX Media3 ExoPlayer with `media3-exoplayer-hls` rendering to a full-screen Compose `AndroidView(PlayerView)`.
  - *Why*: Provides hardware-accelerated video decoding, low-latency live HLS playback, and robust error recovery for live m3u8 streams.
  - *Alternatives Considered*: Standard MediaPlayer (lacks proper HLS live stream manifest parsing and adaptive bitrate handling).

- **Decision 2: Global TV KeyEvent Interception**
  - *Chosen Approach*: Custom key interceptor at the Activity / Root Compose level handling `KeyEvent.KEYCODE_DPAD_*`, `KEYCODE_0`..`KEYCODE_9`, `KEYCODE_CHANNEL_UP`/`DOWN`, and `KEYCODE_MEDIA_*`.
  - *Why*: Guarantees that typing numeric remote keys works instantly anywhere without requiring an active input field or virtual on-screen keyboard.

- **Decision 3: Cleartext Traffic & Fallback Streams**
  - *Chosen Approach*: Enable `android:usesCleartextTraffic="true"` in `AndroidManifest.xml` and support `stream_url_backup` fallback in the player when primary URL encounters errors.
  - *Why*: Many IPTV streams and server feeds utilize HTTP URLs (`http://mag.tivi-one-iptv.net:80/...`); without cleartext enabled, modern Android versions block the connection.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────┐
│                      Android TV                        │
│               MainActivity & Key Dispatcher            │
└───────────────────────────┬────────────────────────────┘
                            │ (DPAD, Numbers 0-9, Media)
                            ▼
┌────────────────────────────────────────────────────────┐
│                   TvPlayerViewModel                    │
│  - Channels State (StateFlow<List<Channel>>)           │
│  - Current Channel (StateFlow<Channel?>)               │
│  - Drawer State (Visible / Hidden)                     │
│  - Number Input Buffer & Countdown Timer               │
│  - Last Channel ID Store (SharedPreferences)           │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
        (Stream URL)                (Channels API)
               ▼                          ▼
┌──────────────────────────────┐  ┌──────────────────────┐
│       Media3 ExoPlayer       │  │  StreamingZone API   │
│   - HLS / M3U8 Source        │  │  - Retrofit + Moshi  │
│   - Auto Reconnect           │  │  - Channel Models    │
│   - Aspect Ratio Handling    │  └──────────────────────┘
└──────────────┬───────────────┘
               ▼
┌────────────────────────────────────────────────────────┐
│                      Compose UI                        │
│  ├── Fullscreen Video Layer (PlayerView)               │
│  ├── Left Side Channel Guide Drawer (LazyColumn)       │
│  ├── Bottom Channel Info Overlay HUD                   │
│  └── Top-Right Quick Number Input Badge (e.g. "4")     │
└────────────────────────────────────────────────────────┘
```

### Data Model & State
- `Channel`: `id`, `name`, `category_id`, `category_name`, `stream_url`, `stream_type`, `logo_url`, `epg_channel_id`, `stream_url_backup`, `is_active`.
- `TvUiState`:
  - `channels`: List of loaded channels.
  - `currentChannel`: Active playing channel.
  - `isLoading`: Initial fetching state.
  - `error`: Network or stream error message with retry trigger.
  - `isDrawerOpen`: Whether the left channel list guide is visible.
  - `numberInput`: Current typed string (e.g., "7") and auto-switch countdown.
  - `isInfoHudVisible`: Bottom channel banner visibility with 4s auto-hide.
