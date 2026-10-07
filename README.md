# RSPSi

RSPSi is a desktop map editor for RuneScape private servers. It loads a game cache, renders the world in 3D and lets
you edit the terrain and the objects on it: paint floors, raise and lower the ground, place and remove objects, set
tile flags, and save the result back to your cache or to map files.

This repository is a fork with additional features, most notably the **Path**, **Coast**, **Mountain** and **Bridge**
tools (see below).

## Features

### Map editing
- **Tile selection** with rectangle select, add-to-selection and single tile toggling.
- **Paint overlays and underlays** with an adjustable brush (size and shape: rectangle, circle, checker), all 13
  overlay shapes and rotation. Hold the modifier keys to remove an overlay or to only repaint the overlay colour.
- **Path tool**: draw a path by clicking points and let RSPSi lay the overlay with matching shapes (details below).
- **Coast tool**: draw a waterline and get a sea overlay with a beach or cliff along it (details below).
- **Mountain tool**: raise a hill or a whole mountain range, optionally with a cliff edge (details below).
- **Bridge tool**: draw a bridge over a river or ravine, flat, sloping or arched, with a live preview (details below).
- **Modify heights** with a brush, with optional smoothing, relative or absolute height, and multiple height levels.
- **Tile flags**: set unwalkable, bridge, remove roof, render on z - 1 and disable render flags, with overlays to
  visualise them.
- **Objects**: select, spawn and delete objects, with rotation, an object filter and an object preview window.
- **Undo and redo** for tiles and objects.

### Working with tiles
- Copy, paste and delete tile selections, with options for what to include.
- Copy flags, heights, overlays or underlays from one tile to a selection.
- Set heights, flags, overlays and underlays on a whole selection in one action.
- Import a prefab and export selected tiles to reuse them elsewhere.
- Reset tile heights and force a map update.

### Files and tools
- Open a map by coordinates, by hash, from `.dat`/`.gz` files, from `.pack` files or from your cache; save back to the
  same formats.
- A **region editor** and a **map index editor** (under Tools).
- A **remapper** and a **landscape converter** (under Tools).
- Full map view, minimap, object view, adjustable view distance and debug overlays (overlay ids, underlay ids, tile
  heights, FPS and tile information).
- Swatches for overlays, underlays and objects, with reloading of swatches and models.
- Plugin support; the included `OSRSPlugin` adds loaders for Old School RuneScape caches.

## Path tool

The Path tool draws a path for you. Select the **P_P** button in the toolbar, pick an overlay in the Overlay tab, and
click the points of your path in the scene. A preview follows your mouse.

- **Enter** applies the path, **Esc** cancels it.
- RSPSi chooses for every tile the overlay shape and rotation that best matches the path, so the pieces fit together,
  including the curved parts.
- Settings are in the **Path** tab on the right:
  - **Width**: how wide the path is, in tiles.
  - **Smooth curve**: let the path flow in a smooth curve through your points, or use straight lines.
  - **Edge smoothing**: how strongly the shapes of neighbouring tiles are made to line up.
  - **Smooth terrain height** (optional): evens out the terrain under the path so it has no steep steps.
    - **Height smoothing**: number of smoothing passes (1 - 250).
    - **Blend distance**: how many tiles around the path are blended into the surrounding terrain. Use a larger
      distance on steep hillsides.
- Painting the overlay and smoothing the height are separate undo steps.

The width of the right hand panel can be changed by dragging its left edge.

## Coast tool

The Coast tool makes a shoreline: it lays a sea overlay and shapes the terrain along a line you draw. Pick the sea
overlay in the Overlay tab first, then select the **CST** button in the toolbar; the settings are in the **Coast**
tab on the right.

1. Click points along the waterline in the scene. The preview shows the sea strip.
2. Press **Enter** to apply it, or **Esc** to cancel.

- **Sea side**: the sea lies on the left of the drawing direction. Untick *Sea on the left* if the sea ends up on
  the wrong side.
- **Beach**: the land slopes down smoothly to sea level (height 0) over *Beach width* tiles. Use a larger width on
  high terrain, otherwise the slope stays steep.
- **Cliff**: the land rises steeply from the waterline. *Cliff height* is how high the rock face climbs, *Cliff
  width* how many tiles it spans (smaller is steeper). The extra height stays for *Plateau depth* tiles and then
  fades back into the terrain.
- **Sea width**: how far the sea overlay and the flat sea bed (height 0) reach from the waterline.
- **Natural shoreline**: *Irregularity* makes the line meander with bays and headlands (0 follows your line
  exactly), *Edge smoothing* makes the overlay edge flow better, and *New shoreline shape* picks another random
  shape. The same settings always give the same shoreline.
- The overlay and the heights are two separate undo steps. Heights are applied to the selected height level.

## Mountain tool

The Mountain tool raises the terrain into a hill, mountain or mountain range. Select the **MTN** button in the toolbar;
the settings are in the **Mountain** tab on the right. The mountain is added on top of the existing terrain and fades
out into it. The overlays and objects are not changed.

- **Hill / mountain**: the **Footprint** grid in the tab shows the shape of the mountain. It starts with an uneven
  suggestion; click or drag to colour squares, drag from a coloured square to erase (**New suggestion**, **Clear** and
  **Fill** are next to it). The drawing is stretched over the radius. Click the centre of the mountain in the scene
  to place it. With tiles selected, press **Enter** to raise the selected
  area instead (highest in the middle).
- **Mountain range**: click points along the ridge, **Enter** applies it, **Esc** cancels it.
- Settings: height, radius (or width of a range), steepness, foot blend, irregularity of the outline, roughness,
  peak variation along a range and a seed. The same seed and settings give the same mountain.
- **Cliff edge** (optional): a steep rock face along the edge, with a gentler slope above it. *Cliff height* is how much
  of the total height the face climbs, *Cliff width* how many tiles it spans sideways (smaller is steeper).
- One undo step per mountain.

## Modify Height tool

Select the **H_M** button in the toolbar and hold the left mouse button while moving over the terrain. The brush
(size and shape from the brush settings) changes the height of every tile it touches, on the height level that is
currently selected. What it does depends on the key you hold:

| Keys            | Effect                                                                                   |
|-----------------|------------------------------------------------------------------------------------------|
| *(none)*        | **Raise** the terrain a little every time the brush updates; hold the button to keep raising. |
| `Shift`         | **Lower** the terrain in the same way.                                                   |
| `Ctrl`          | **Smooth**: every tile becomes the average of its 3x3 neighbours. Use it to even out bumps and steps. |
| `Alt`           | **Set** the tile to the value of the *Tile Height* slider (see below).                   |
| `Shift` + `Alt` | **Set** the tile to the *Tile Height* slider value, always measured from the ground (0). |

Tips:
- To flatten an area, use `Alt` (or `Shift` + `Alt`) with the same slider value on the whole area, then use `Ctrl`
  on the edges.
- To blend two regions of a different height, paint over the border between them with `Ctrl`. Every pass moves the
  tiles closer to their neighbours, so move over the border several times, or use a bigger brush, for a gentler slope.
- Ground can never go below 0; heights higher than the tile below are corrected for you.

Details:
- **Tile Height slider** (right hand panel): the height used by `Alt` and `Shift` + `Alt`.
- **Absolute** checkbox: with `Alt` on a height level above 0, the slider value is added to the tile on the level
  below (relative, the default) or measured from 0 (absolute). `Shift` + `Alt` is always absolute.
- Changes apply to the selected height level and to all levels above it, so objects and floors stay aligned. The
  levels above are also kept from dipping below the level underneath.
- Every stroke can be undone with undo (`Ctrl` + `Z`).
- Other height actions are in the menu: *Copy heights from tile*, *Set heights to tiles* (for a whole selection),
  *Set Tile Height*.

## Bridge tool

Builds a bridge over a stretch of the map, for example over a river or a ravine. The bridge is built on the level
above the current height level, so the current level can be at most 2.

**Drawing a bridge (`BRG` button)**
1. Pick the height level the bridge crosses and an overlay for the deck (Overlay tab).
2. Click points along the bridge. The preview shows the deck and, in a different colour, the ramp around it.
3. Press `Enter` to build it or `Esc` to cancel. The deck stops at the first and last point you clicked.
4. Settings are in the Bridge tab:
   - **Deck width**: how wide the bridge is.
   - **Deck height**: automatic (the deck starts and ends at the height of the ground at your first and last point, so
     it connects to the banks) or one fixed height for the whole deck.
   - **Arch height**: how far the middle lies above a straight line between the ends. `0` gives a straight or sloping
     bridge, a higher value gives an arched bridge.
   - **Ramp width**: how many corners around the deck the terrain slopes down to the ground; `0` gives a hard edge.
   - **Bridge flag**: sets the bridge tile flag on the deck.

Heights, overlays and flags are changed in one go, so a single `Ctrl` + `Z` undoes the whole bridge.

## Building

RSPSi is a Gradle project and needs a JDK 21.

```
./gradlew build
./gradlew :Editor:run
```

On Windows use `gradlew.bat`. The project consists of the `Client` (renderer and cache handling), the `Editor` (the
JavaFX user interface) and the `Plugins` (such as `OSRSPlugin`).

## Credits

RSPSi was created by the RSPSi project and a lot of its code and ideas come from the work of others. Thank you to:

- **[RSPSi/RSPSi](https://github.com/RSPSi/RSPSi)**, the original RSPSi, which this project builds on.
- **[blurite/RSPSi](https://github.com/blurite/RSPSi)**, the continued version of RSPSi that this fork is based on.

## License

RSPSi is released under the MIT License, see [LICENSE](LICENSE).
