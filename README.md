# High Voltage — Weather Profile Configuration Guide

Weather Profiles are data-driven JSON sets that control atmospheric properties, customs rendering behaviors, overlays, and custom logic behaviors across groups of biomes.

## File Location
Profiles must be placed inside a datapack using the following structure:
`data/<namespace>/weather_profiles/<file_name>.json`

---

## 1. Top-Level Profile Structure

Every Weather Profile file uses a root object containing the following configuration fields:

| Field | Type | Required / Default | Description |
| :--- | :--- | :--- | :--- |
| `biomes` | String / Array | **Required** | A biome tag (e.g., `#minecraft:is_ocean`) or a list of explicit biome registry IDs (e.g., `["minecraft:plains"]`). |
| `precipitation` | Object | Optional | Defines visual particles, sounds, falling textures, and velocities. |
| `fog` | Object | Optional | Dictates the rendering fog density distance ranges and HEX target coloring. |
| `effects` | Array of Objects | Optional (`[]`) | Collection of custom code execution logic components to run dynamically. |
| `base_lightning_chance` | Integer | Optional (`10000`) | The lower the number, the higher the frequency of lightning bolts during active storms. |
| `foliage_color` | String (Hex) | Optional (`#00FFFFFF`) | Overlays color onto leaves and biome maps. Supports Alpha channels (`#AARRGGBB`). If alpha is omitted, it defaults to full opacity (`#FF...`). |

### JSON Example
```json
{
  "biomes": "#minecraft:is_forest",
  "base_lightning_chance": 4500,
  "foliage_color": "#4D00FF44",
  "precipitation": { },
  "fog": { },
  "effects": [ ]
}