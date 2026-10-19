# Quiet TV design and implementation

Quiet TV presents installed apps over a wallpaper without promotional feeds. It is a native Android Activity, not a web app or operating-system fork. Six columns, a translucent dock, explicit remote-focus links, and two primary viewing states form the core interface.

## Current visual behavior

The initial view shows the dock near the bottom. Moving down shifts the content by one row stride and reveals the next row near the bottom, with the dock above it. Future rows are hidden to avoid peeking at the bottom edge. The clock, Sleep and Settings remain in the header.

Cards preserve a 5:3 banner ratio. Focus scales to 1.20 with a 2dp lift over 220ms; release takes 180ms. There are no visible app names or focus shadows. Accessibility descriptions retain app names. The horizontal inset is 12dp, vertical card padding is 10dp, and the inter-row margin is 10dp. These measurements are converted through display density.

Opening an app expands a card surface for 420ms. The centered rounded logo is counter-scaled to avoid stretching a small source banner over the display. Its backdrop uses edge colors sampled from the artwork. A short system handoff fade follows; the external app controls its own subsequent loading screen. Return to the launcher uses a subtle scale restoration.

Wallpaper changes combine a 1.4-second crossfade with 2.5% opposing horizontal drift and up to 6% overscan zoom. An opaque outgoing frame prevents a black midpoint. A hold of 8.6 seconds plus decode/transition gives an approximately 10-second cadence. Motion is optional; fixed wallpapers and three generated gradients are available.

## Rendering and lifecycle

App cards use hardware layers, cached artwork and reusable focus animators. Backgrounds decode on a single worker executor. Current and incoming frames are cached; generation tokens reject stale loads after view detachment. Pause/detach cancels rotation, and launch overlays are cleared on lifecycle transitions or cancellation.

Avoid expensive per-frame blur or image decoding. Two full-screen images crossfading are GPU-intensive on a Fire TV Stick; visual approval does not establish a universal performance guarantee. Do not present past local timing samples as reproducible benchmark claims.

## Boundaries

Home redirection belongs to an optional third-party helper. Sleep belongs to a small local accessibility service and does not read screen content. No app installation, debloating, system animation settings, or device updates are performed by the launcher or build script.
