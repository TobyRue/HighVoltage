# High Voltage — Weather Profile Configuration Guide

Weather Profiles are data-driven JSON sets that control atmospheric properties, customs rendering behaviors, overlays, and custom logic behaviors across groups of biomes.

## File Location
Profiles must be placed inside a datapack using the following structure:
```text
data/<namespace>/weather_profiles/<file_name>.json
```

---

## 1. Top-Level Profile Structure

Every Weather Profile file uses a root object containing the following configuration fields:

# High Voltage — Weather Profile Configuration Guide

Weather Profiles are data-driven JSON sets that control atmospheric properties, custom rendering behaviors, overlays, lightning frequency, and weather effects across groups of biomes.

---

Every Weather Profile file uses a root object containing the following fields:

| Field                   | Type                    | Required / Default     | Description                                                                                       |
| :---------------------- | :---------------------- |:-----------------------| :------------------------------------------------------------------------------------------------ |
| `biomes`                | Biome Tag or Biome List | **Required**           | Determines which biomes use this weather profile. Supports biome tags and explicit biome entries. |
| `precipitation`         | Object                  | Optional (`none`)      | Controls precipitation textures, tinting, movement, particles, and sounds.                        |
| `fog`                   | Object                  | Optional (`none`)      | Controls custom fog coloring and density distances.                                               |
| `effects`               | Array                   | Optional (`[]`)        | Collection of weather effects executed while the profile is active.                               |
| `base_lightning_chance` | Integer                 | Optional (`10000`)     | Base lightning frequency. Lower numbers produce more lightning strikes.                           |
| `foliage_color`         | Hex Color (`#AARRGGBB`) | Optional (`#00FFFFFF`) | Color overlay applied to foliage. Supports alpha transparency.                                    |

---

## Biomes

The `biomes` field uses Minecraft's biome holder set codec.

### Biome Tag Example

```json
{
  "biomes": "#minecraft:is_forest"
}
```

### Explicit Biome List Example

```json
{
  "biomes": [
    "minecraft:plains",
    "minecraft:meadow",
    "minecraft:sunflower_plains"
  ]
}
```

---

## Foliage Color

Foliage colors use hexadecimal color notation:

### RGB

```json
"#44AA55"
```

Automatically interpreted as:

```text
#FF44AA55
```

(full opacity)

### ARGB

```json
"#8044AA55"
```

| Component | Value |
| --------- | ----- |
| Alpha     | 80    |
| Red       | 44    |
| Green     | AA    |
| Blue      | 55    |

The alpha channel controls overlay strength.

### Default

```json
"#00FFFFFF"
```

This effectively disables foliage tinting.

---

# 2. Precipitation

The `precipitation` object controls the visual appearance and behavior of falling weather particles.

## Structure

| Field            | Type               | Required / Default                                      | Description                                                                            |
| :--------------- | :----------------- |:--------------------------------------------------------| :------------------------------------------------------------------------------------- |
| `texture`        | Resource Location  | Optional (`high_voltage:textures/environment/none.png`) | Texture rendered as falling precipitation.                                             |
| `tint`           | String (Hex Color) | Optional (`#FFFFFF`)                                    | Color multiplier applied to the precipitation texture.                                 |
| `vx`             | Float              | **Required**                                            | Horizontal movement velocity.                                                          |
| `vy`             | Float              | **Required**                                            | Vertical movement velocity. Negative values fall downward.                             |
| `acts_like_rain` | Boolean            | Optional (`true`)                                       | Determines whether the precipitation behaves as rain for vanilla weather interactions. |
| `land_particle`  | Particle Type      | Optional (`none`)                                         | Particle spawned when precipitation lands on surfaces.                                 |
| `land_sound`     | Boolean            | Optional (`false`)                                      | Enables vanilla-style precipitation landing sounds.                                    |

## Color Format

`tint` accepts standard hexadecimal RGB colors:

"#FFFFFF"
"#44AAFF"
"#FF0000"

Alpha channels are not supported for precipitation tinting.

---

## Examples:

```json
{
  "precipitation": {
    "texture": "minecraft:textures/environment/rain.png",
    "tint": "#A0D8FF",
    "vx": 0.0,
    "vy": -1.0,
    "acts_like_rain": true,
    "land_particle": "minecraft:rain",
    "land_sound": true
  }
}
```

---

```json
{
  "precipitation": {
    "texture": "high_voltage:textures/environment/ash.png",
    "tint": "#555555",
    "vx": 0.02,
    "vy": -0.15,
    "acts_like_rain": false,
    "land_particle": "minecraft:ash",
    "land_sound": false
  }
}
```

---

# 3. Fog

The `fog` object controls client-side atmospheric fog rendering while the weather profile is active.

## Structure

| Field   | Type               | Required     | Description                                              |
| :------ | :----------------- | :----------- | :------------------------------------------------------- |
| `color` | String (Hex Color) | **Required** | Target fog color.                                        |
| `start` | Integer            | **Required** | Distance from the camera where fog begins.               |
| `end`   | Integer            | **Required** | Distance from the camera where fog reaches full density. |

---

## Color Format

Fog colors use standard RGB hexadecimal values:

"#FFFFFF"
"#88AAFF"
"#223344"

The fog codec expects RGB values only, with no alpha channel.

---

## Distance Behavior

The fog range is defined using `start` and `end`.

```text
Player
│
├── start
│     Fog begins
│
└── end
      Fully fogged
```

Lower values create dense fog.

Higher values create lighter atmospheric effects.

---

## Examples: Dense Blizzard Fog

```json
{
  "fog": {
    "color": "#DDEEFF",
    "start": 2,
    "end": 32
  }
}
```

---

```json
{
  "fog": {
    "color": "#2B2B2B",
    "start": 1,
    "end": 16
  }
}
```

---

```json
{
  "fog": {
    "color": "#C8D8E8",
    "start": 16,
    "end": 96
  }
}
```

---

# Complete Example

```json
{
  "biomes": "#minecraft:is_forest",

  "precipitation": {
    "texture": "minecraft:textures/environment/rain.png",
    "tint": "#A0D8FF",
    "vx": 0.0,
    "vy": -1.0,
    "acts_like_rain": true,
    "land_particle": "minecraft:rain",
    "land_sound": true
  },

  "fog": {
    "color": "#BFD8FF",
    "start": 8,
    "end": 64
  }
}
```

# 4. Effects

Effects are custom logic components that execute while a Weather Profile is active.

Effects are defined inside the `effects` array:

```json
{
  "effects": [
    {
      "type": "high_voltage:freeze",
      "freeze_ticks": 200
    }
  ]
}
```

---

## Common Fields

Many effects include a `chance` field.

| Field    | Type    | Description                                                                                                                                                      |
| -------- | ------- |------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `chance` | Integer | Lower values result in more frequent activation. The exact interpretation depends on the effect implementation. However in most cases it is a `1/chance` to happen |

Some effects also support an `entity_predicate` field:

| Field              | Type                 | Description                                                                                               |
| ------------------ | -------------------- | --------------------------------------------------------------------------------------------------------- |
| `entity_predicate` | Entity Type Tag/List | Restricts the effect to specific entity types. Accepts registry entries or tags from the entity registry. |

Example:

```json
{
  "entity_predicate": "#minecraft:skeletons"
}
```
Or (One or the other, not both)

```json
{
  "entity_predicate": ["minecraft:pig","minecraft:cow"]
}
```

---

# Bonus Lightning Effect

Creates additional lightning strikes around nearby players.

**Type ID**

```json
"high_voltage:player_bonus_lightning"
```

| Field    | Type    | Required | Description                                                       |
| -------- | ------- | -------- |-------------------------------------------------------------------|
| `radius` | Integer | Yes      | Radius around the player for it to happen, distance is in chunks. |
| `chance` | Integer | Yes      | Lightning strike chance.                                          |

Example:

```json
{
  "type": "high_voltage:player_bonus_lightning",
  "radius": 4,
  "chance": 500
}
```

---

# Command Effect

Runs a server command.

**Type ID**

```json
"high_voltage:command"
```

| Field    | Type    | Required | Description         |
| -------- | ------- | -------- | ------------------- |
| `run`    | String  | Yes      | Command to execute. |
| `chance` | Integer | Yes      | Execution chance.   |

Example:

```json
{
  "type": "high_voltage:command",
  "run": "give @a minecraft:diamond",
  "chance": 200
}
```

---

# Damage Effect

Deals damage to matching entities.

**Type ID**

```json
"high_voltage:damage"
```

| Field              | Type                 | Required | Default      |
| ------------------ | -------------------- | -------- | ------------ |
| `damage`           | Float                | Yes      | -            |
| `chance`           | Integer              | Yes      | -            |
| `entity_predicate` | Entity Type List/Tag | No       | All entities |

Example:

```json
{
  "type": "high_voltage:damage",
  "damage": 2.0,
  "chance": 100,
  "entity_predicate": "#minecraft:raiders"
}
```

---

# Freeze Effect

Applies freezing damage buildup.

**Type ID**

```json
"high_voltage:freeze"
```

| Field          | Type    | Required | Description                       |
| -------------- | ------- | -------- | --------------------------------- |
| `freeze_ticks` | Integer | Yes      | Amount of freezing time to apply. |

Example:

```json
{
  "type": "high_voltage:freeze",
  "freeze_ticks": 300
}
```

---

# Hunger Effect

Adds exhaustion to players.

**Type ID**

```json
"high_voltage:hunger"
```

| Field        | Type    | Required |
| ------------ | ------- | -------- |
| `exhaustion` | Float   | Yes      |
| `chance`     | Integer | Yes      |

Example:

```json
{
  "type": "high_voltage:hunger",
  "exhaustion": 1.5,
  "chance": 50
}
```

---

# Ignite Effect

Sets matching entities on fire.

**Type ID**

```json
"high_voltage:ignite"
```

| Field              | Type                 | Required | Default      |
| ------------------ | -------------------- | -------- | ------------ |
| `entity_predicate` | Entity Type List/Tag | No       | All entities |
| `chance`           | Integer              | No       | `1`          |
| `duration`         | Integer              | No       | `100`        |

Example:

```json
{
  "type": "high_voltage:ignite",
  "duration": 200,
  "chance": 10,
  "entity_predicate": "#minecraft:animals"
}
```

---

# Status Effect

Applies a Minecraft mob effect.

**Type ID**

```json
"high_voltage:status_effect"
```

| Field              | Type                 | Required | Default      |
| ------------------ | -------------------- | -------- | ------------ |
| `effect`           | Resource Location    | Yes      | -            |
| `duration`         | Integer              | No       | `200`        |
| `amplifier`        | Integer              | No       | `0`          |
| `ambient`          | Boolean              | No       | `true`       |
| `visible`          | Boolean              | No       | `false`      |
| `entity_predicate` | Entity Type List/Tag | No       | All entities |
| `chance`           | Integer              | No       | `1`          |

Example:

```json
{
  "type": "high_voltage:status_effect",
  "effect": "minecraft:slowness",
  "duration": 300,
  "amplifier": 1,
  "ambient": true,
  "visible": false,
  "chance": 5
}
```

---

## Full Example

```json
{
  "biomes": "#minecraft:is_overworld",
  "effects": [
    {
      "type": "high_voltage:freeze",
      "freeze_ticks": 200
    },
    {
      "type": "high_voltage:status_effect",
      "effect": "minecraft:slowness",
      "duration": 100,
      "amplifier": 0
    },
    {
      "type": "high_voltage:ignite",
      "duration": 60,
      "chance": 25
    }
  ]
}
```