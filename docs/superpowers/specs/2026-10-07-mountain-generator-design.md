# Mountain generator — ontwerp

## Doel
Snel een geloofwaardige berg, heuvel of bergketen in een map neerzetten, in plaats van met de hoogtebrush te schilderen.

## Scope
- **In scope:** alleen terreinhoogtes. Twee modi: *Heuvel/berg* en *Bergketen*. Configuratie in een nieuwe "Mountain"-tab in het rechter tabmenu. Eén undo-stap per toepassing.
- **Buiten scope (later):** overlays op basis van hoogte/helling, objecten (rotsen, bomen), erosie, live preview (alleen als `previewPath` zich er eenvoudig voor leent).

## Gedrag
- **Optellen op bestaand terrein.** De berg komt bovenop de huidige hoogtes en loopt geleidelijk uit in het omliggende terrein (blend-zone).
- **Heuvel/berg:** klik = middelpunt, straal uit de tab. De afstand wordt vervormd met lage-frequentie noise, zodat de omtrek onregelmatig is. Is er een selectie actief, dan is die het gebied; de hoogte volgt dan uit het afstandsveld naar de selectierand (midden het hoogst).
- **Bergketen:** punten klikken, Enter past toe, Esc annuleert (zoals de pad-tool). Curve via `PathOverlayFitter.smooth`, afstand via `PathOverlayFitter.distanceToPath`. Langs de rug komt hoogtevariatie (toppen en zadels).
- Profiel: gladde klokvorm (smoothstep) van top naar rand, vermenigvuldigd met fractal noise.

## Onderdelen
| Onderdeel | Plek | Verantwoordelijkheid |
|---|---|---|
| `MountainGenerator` | Client, `com.rspsi.tools` | Pure logica zonder JavaFX of scene. Input: gebied (middelpunt+straal, selectie of lijn) en parameters. Output: `Map<Integer,Integer>` hoekpunt (`x<<16\|y`) → extra hoogte. Deterministisch per seed. |
| `Options.mountain*` | `Options.java` | Properties voor alle instellingen, zoals `pathWidth`. |
| `createMountainTab()` | `MainController` | Tab met moduskeuze en instellingen; hergebruikt `section`, `hint`, `pathSlider`. Toegevoegd in `toolsTabPane` naast de Path-tab. |
| Toepassing | `SceneGraph` | Naar voorbeeld van `smoothPathHeights`: undo-state bewaren (`HeightState`, `preserveTileState`), levels erboven meeschuiven, tiles updaten, `commitChanges()`. |

## Instellingen in de tab
- Modus: Heuvel/berg of Bergketen
- Max. hoogte
- Straal (heuvel) of breedte (keten)
- Ruwheid (aantal noise-octaven)
- Vormonregelmatigheid
- Seed + knop "Nieuwe seed"
- Randblend
- Steilheid van het profiel
- Alleen keten: toppenvariatie

## Randgevallen
- Hoogtes in RS zijn negatief omhoog: de berg trekt waarden naar negatief; `Math.min(h, 0)` blijft gelden.
- Afkappen op de mapgrenzen.
- Hogere levels schuiven mee zoals bij de hoogtetool en worden daarna gecorrigeerd zodat ze niet onder het niveau eronder uitkomen.
- Lege selectie of keten met minder dan 2 punten: niets doen.

## Testen
Unit-tests op `MountainGenerator`:
- zelfde seed geeft hetzelfde resultaat;
- hoogte is nul buiten straal plus blend;
- piek ligt in het midden (heuvel);
- rand loopt geleidelijk uit (geen stap groter dan een drempel tussen buurpunten);
- geen waarde overschrijdt de maximumhoogte.

Handmatig in de editor: berg plaatsen, keten tekenen, selectie gebruiken, undo (één Ctrl+Z), randen van de map, hogere levels.

## Open punten
Geen. Preview is bewust als optioneel later onderdeel gemarkeerd.
