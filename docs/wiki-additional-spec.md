# Additional Specification

## Non-Functional Requirements

### Performance
- Cold start time < 2 s on mid-range device (Snapdragon 665 / 4 GB RAM).
- All Room queries run on `Dispatchers.IO`; UI thread never blocked.
- Lists rendered with `LazyColumn` / `LazyVerticalGrid` for efficient recycling.

### Reliability
- WorkManager retries failed currency refreshes with exponential back-off (max 3 attempts).
- Room database version managed via migrations; `fallbackToDestructiveMigration` used only in debug.

### Security
- No sensitive user data transmitted externally (only anonymous currency rate requests).
- NBRB API does not require authentication.
- Release APK signed with a private keystore stored as a GitHub Actions secret (never committed).

### Compatibility
- Min SDK 26 (Android 8.0) — covers > 95 % of active devices.
- Target SDK 35 (Android 15).
- Edge-to-edge display support (`enableEdgeToEdge()`).

### Accessibility
- All interactive elements have `contentDescription` for TalkBack.
- Color-blind safe: amounts differentiated by +/− prefix and green/red color together.
- Touch targets ≥ 48 dp per Material Design guidelines.

### Internationalisation
- All user-visible strings in `res/values/strings.xml`.
- Dates formatted via `DateTimeFormatter` respecting locale.
- Currency amount formatted via `String.format("%.2f")` (no hard-coded locale).
