# Android Offline First

An Android music app that demonstrates **offline-first architecture** using the **Deezer** public API.

## Architecture

Clean Architecture with the following layers:

- **UI** — Jetpack Compose
- **ViewModel** — StateFlow + UiState
- **Repository** — Single source of truth, offline-first logic
- **Local** — Room + Flow
- **Remote** — Retrofit + Deezer API

## Offline-First Flow
```
Room emits local data immediately
      ↓
UI displays data (no internet required)
      ↓
API syncs in background
      ↓
Room updates → UI reacts automatically
```

## Features

- 🎵 Real music data from Deezer chart API
- 📦 Offline support — works without internet
- 🔍 Search by title or artist
- 🔄 Pull to refresh
- 📡 Live connectivity indicator
- 🖼️ Album art with Coil
- 🎯 Song detail screen with Deezer link

## Preview

<img src="preview.gif" width="320" alt="Demo"/>


## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose |
| State | StateFlow / UiState |
| DI | Hilt |
| Local DB | Room |
| Networking | Retrofit + Gson |
| Images | Coil |
| Navigation | Navigation Compose |
| Testing | JUnit4 + Mockk + Turbine |

## Project Structure
```
com.mauro.offlinefirst/
├── data/
│   ├── local/        # Room, DAO, Entities
│   ├── remote/       # Retrofit, DTOs
│   ├── mapper/       # Layer converters
│   └── repository/   # Offline-first implementation
├── domain/
│   ├── model/        # Domain models
│   └── repository/   # Repository interface
├── presentation/
│   ├── songlist/     # List screen + ViewModel
│   ├── songdetail/   # Detail screen + ViewModel
│   └── navigation/   # NavGraph + Routes
└── di/               # Hilt modules
```

## Tests

- `SongMapperTest` — mapping and field conversion
- `SongRepositoryImplTest` — offline-first data flow
- `SongListViewModelTest` — UI state management

## API

This app uses the [Deezer API](https://developers.deezer.com/api) — no authentication required.
```
GET https://api.deezer.com/chart/0/tracks
```

## Author

[Mauro Cosentino](https://github.com/maurocosentino)
