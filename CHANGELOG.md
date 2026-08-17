# Changelog

## v0.1 (MVP, no-root)
Fitur awal TheWatcher:
- Daftar device terhubung ke hotspot (SoftAp API31+ / ARP fallback).
- Total konsumsi data hotspot via TrafficStats.
- Estimasi per-device (durasi proporsional).
- History per sesi hotspot (Room).
- GitHub Actions build + unit test.

Batasan: per-device = estimasi (Android non-root tidak ekspos byte per-MAC).
