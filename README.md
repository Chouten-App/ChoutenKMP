<div align="center">

# Chouten

### A Modular Media Platform  
Designed for Performance. Built for Freedom.

Anime · Manga · Books · Music · Video  
One Architecture. Infinite Sources.

</div>

---

## What is Chouten?

**Chouten** is a modular, cross-platform media platform built with Kotlin Multiplatform.

It is not tied to a single provider.  
It is not locked to a single media type.  
It is not architected as a monolith.

Chouten separates:

- 📊 Metadata & Tracking
- 🎞 Media Delivery
- 🧠 Core Platform Logic
- 🎨 UI & Experience

This separation enables flexibility, extensibility, and long-term scalability.

---

## Philosophy

Most media apps are:

- Provider-locked
- Hard to extend
- Inconsistent across platforms
- Architecturally rigid

Chouten is designed differently.

### Core Principles

- Modular by default  
- Cross-platform by design  
- UI-first experience  
- Clean architecture  
- Source abstraction  
- Performance-conscious  

The goal is simple:

> A smooth, immersive media experience — without sacrificing architectural integrity.

---

## Design Language

Chouten embraces a cinematic, immersive UI:

- Full-bleed artwork backgrounds
- Layered glass-style surfaces
- Elevated rounded cards
- Strong visual hierarchy
- Minimal clutter
- Content-focused layout

The interface prioritizes:

- Fluid navigation
- Visual clarity
- Responsiveness
- User satisfaction

Design is not decoration — it is structure.

---

## Architecture Overview

Chouten is built with **Kotlin Multiplatform (KMP)** and structured into modular components.

### Core Modules

- Shared business logic
- Source abstraction layer
- Feature-based modules
- Compose UI layer
- Platform-specific targets (Android, iOS, Desktop JVM)

---

## Source System

Sources are divided into two clear categories.

### 📊 TrackerSource

Provides:
- Metadata
- User progress tracking
- Ratings
- Lists

Examples:
- AniList
- TMDB
- Kitsu

TrackerSources do **not** provide media files.

---

### 🎞 MediaSource

Provides:
- Episode streams
- Chapter images
- Audio streams
- Video files

MediaSources focus purely on content delivery.

---

### Why This Matters

This separation allows:

- Mixing trackers with different media providers
- Independent source development
- Plugin-based extensibility (planned)
- Cleaner long-term maintainability
- A true platform, not just an app

---

## Planned Screens

- 🏠 Home
- 🔍 Discover
- 📦 Repo Management
- ℹ️ Media Info
- ⚙️ Settings
- 📖 EPUB / Book Reader
- 🖼 Comic Reader
- 🎵 Music Player
- 🎬 Video Player
- 👤 Profile

---

## Current Capabilities

- Kotlin Multiplatform foundation
- Compose-based UI system
- Cross-platform targets
- Multi-screen navigation
- Media detail & episode layouts
- Modular feature structure
- TrackerSource abstraction foundation
- Clean separation of UI and logic

---

## Roadmap

### Platform Core
- [ ] Plugin system for MediaSources
- [ ] Dynamic source loading
- [ ] Source sandboxing
- [ ] Background sync engine
- [ ] Offline library support
- [ ] Cloud synchronization
- [ ] Account system

---

### Tracking
- [ ] AniList OAuth
- [ ] TMDB integration
- [ ] Multi-tracker support
- [ ] Automatic progress sync
- [ ] Statistics dashboard

---

### Readers & Players
- [ ] Custom EPUB rendering engine
- [ ] Custom comic layout engine
- [ ] Infinite chapter scrolling
- [ ] Multiple reading modes
- [ ] Adaptive video streaming
- [ ] Music background playback
- [ ] Chromecast support

---

### Experience & UX
- [ ] Theme engine
- [ ] Advanced animation system
- [ ] Tablet-optimized layouts
- [ ] Desktop window modes
- [ ] Performance profiling tools
- [ ] Accessibility improvements

---

### Developer Ecosystem
- [ ] Public module API
- [ ] Source development documentation
- [ ] Mockable source interfaces
- [ ] Testing harness
- [ ] CI pipeline
- [ ] Contributor guidelines

---

## Target Platforms

- Android
- iOS
- Desktop (JVM)
- Future expansion possible

---

## Status

Chouten is under active development.

The architecture is being built carefully to support long-term extensibility and performance.

---

## Why Chouten Exists

Because users deserve:

- A smooth experience
- A clean interface
- Freedom of sources
- Cross-platform consistency

And developers deserve:

- Modular architecture
- Clear abstraction boundaries
- Extensibility
- Scalability

---

## And Much More…

Chouten is a foundation.

Readers, players, trackers, sources — these are components of something larger.

The goal is not to build another media app.

The goal is to build a platform.

