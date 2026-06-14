# High Voltage - Weather Profile Configuration Guide

Weather Profiles are data-driven JSON sets that control atmospheric properties, customs rendering behaviors, overlays, and custom logic behaviors across groups of biomes.

## File Location
Profiles must be placed inside a datapack using the following structure:
```text
data/<namespace>/weather_profiles/<file_name>.json
```

---

## 1. Top-Level Profile Structure

Every Weather Profile file uses a root object containing the following configuration fields:

---

Every Weather Profile file uses a root object containing the following fields:

| Field                   | Type                    | Default     | Description                                                                                       |
|:------------------------|:------------------------|:------------|:--------------------------------------------------------------------------------------------------|
| `biomes`                | Biome Tag or Biome List | -           | Determines which biomes use this weather profile. Supports biome tags and explicit biome entries. |
| `precipitation`         | Object                  | `null`      | Controls precipitation textures, tinting, movement, particles, and sounds.                        |
| `fog`                   | Object                  | `null`      | Controls custom fog coloring and density distances.                                               |
| `effects`               | Array                   | `[]`        | Collection of weather effects executed while the profile is active.                               |
| `base_lightning_chance` | Integer                 | `10000`     | Base lightning frequency. Lower numbers produce more lightning strikes.                           |
| `foliage_color`         | Hex Color (`#AARRGGBB`) | `#00FFFFFF` | Color overlay applied to foliage. Supports alpha transparency.                                    |

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

| Field            | Type               | Default                                      | Description                                                                            |
|:-----------------|:-------------------|:---------------------------------------------|:---------------------------------------------------------------------------------------|
| `texture`        | Resource Location  | `high_voltage:textures/environment/none.png` | Texture rendered as falling precipitation.                                             |
| `tint`           | String (Hex Color) | `#FFFFFF`                                    | Color multiplier applied to the precipitation texture.                                 |
| `vx`             | Float              | -                                            | Horizontal movement velocity.                                                          |
| `vy`             | Float              | -                                            | Vertical movement velocity.                                                            |
| `acts_like_rain` | Boolean            | `true`                                       | Determines whether the precipitation behaves as rain for vanilla weather interactions. |
| `land_particle`  | Particle Type      | `null`                                       | Particle spawned when precipitation lands on surfaces.                                 |
| `land_sound`     | Boolean            | `false`                                      | Enables vanilla-style precipitation landing sounds.                                    |

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

| Field   | Type               | Default | Description                                             |
|:--------|:-------------------|:--------|:--------------------------------------------------------|
| `color` | String (Hex Color) | -       | Target fog color.                                       |
| `start` | Integer            | -       | Distance from the camera where fog begins.              |
| `end`   | Integer            | -       | Distance from the camera where fog reaches full density.|

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

## Examples:

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

Some effects also support an `entity_type` field:

| Field         | Type                 | Description                                                                                               |
|---------------|----------------------|-----------------------------------------------------------------------------------------------------------|
| `entity_type` | Entity Type Tag/List | Restricts the effect to specific entity types. Accepts registry entries or tags from the entity registry. |

Example:

```json
{
  "entity_type": "#minecraft:skeletons"
}
```
Or (One or the other, not both)

```json
{
  "entity_type": ["minecraft:pig","minecraft:cow"]
}
```

---

# Bonus Lightning Effect

Creates additional lightning strikes around nearby players.

**Type ID**

```json
"high_voltage:player_bonus_lightning"
```

| Field    | Type    | Default | Description                                                       |
| -------- | ------- |---------|-------------------------------------------------------------------|
| `radius` | Integer | -       | Radius around the player for it to happen, distance is in chunks. |
| `chance` | Integer | -       | Lightning strike chance.                                          |

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

| Field    | Type    | Default | Description         |
| -------- | ------- |---------| ------------------- |
| `run`    | String  | -       | Command to execute. |
| `chance` | Integer | `1`     | Execution chance.   |

Example:

```json
{
  "type": "high_voltage:command",
  "run": "give @a minecraft:diamond",
  "chance": 200
}
```

---

# Damage Armor Effect

Damages armor worn by players and entities.

**Type ID**

```json
"high_voltage:damage_armor"
```

| Field    | Type                     | Default | Description                      |
| -------- | ------------------------ |---------| -------------------------------- |
| `slots`  | Array of Equipment Slots | -       | Armor/equipment slots to damage. |
| `damage` | Integer                  | -       | Durability damage applied.       |
| `chance` | Integer                  | -       | Chance for the effect to occur.  |

Valid slot names include:

```text
head
chest
legs
feet
mainhand
offhand
```

Example:

```json
{
  "type": "high_voltage:damage_armor",
  "slots": ["head", "chest", "legs", "feet"],
  "damage": 1,
  "chance": 50
}
```

---

# Damage Effect

Deals damage to matching entities.

**Type ID**

```json
"high_voltage:damage"
```

| Field         | Type                 | Default      |
|---------------|----------------------|--------------|
| `damage`      | Float                | -            |
| `chance`      | Integer              | -            |
| `entity_type` | Entity Type List/Tag | All entities |

Example:

```json
{
  "type": "high_voltage:damage",
  "damage": 2.0,
  "chance": 100,
  "entity_type": "#minecraft:raiders"
}
```

---

# Disable Elytra Effect

Prevents Elytra flight while the weather profile is active.

**Type ID**

```json
"high_voltage:disable_elytra"
```

This effect has no additional configuration.

Example:

```json
{
  "type": "high_voltage:disable_elytra"
}
```

---

# Disable Sprinting Effect

Prevents sprinting while the weather profile is active.

**Type ID**

```json
"high_voltage:disable_sprinting"
```

This effect has no additional configuration.

Example:

```json
{
  "type": "high_voltage:disable_sprinting"
}
```

---

# Fill Cauldron Effect

Automatically fills cauldrons exposed to the weather.

**Type ID**

```json
"high_voltage:fill_cauldron"
```

| Field          | Type    | Default | Description                               |
| -------------- | ------- |---------|-------------------------------------------|
| `fluid`        | String  | -       | Fluid type to fill with (See below).      |
| `surface_only` | Boolean | `true`  | Only affect cauldrons exposed to the sky. |
| `chance`       | Integer | -       | Fill chance.                              |

Valid fluid types are:
```text
lava
water
snow
```

Example:

```json
{
  "type": "high_voltage:fill_cauldron",
  "fluid": "water",
  "surface_only": true,
  "chance": 100
}
```

---

# Freeze Effect

Applies freezing damage buildup.

**Type ID**

```json
"high_voltage:freeze"
```

| Field          | Type    | Default | Description                       |
|----------------|---------|---------|-----------------------------------|
| `freeze_ticks` | Integer | -       | Amount of freezing time to apply. |

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

| Field        | Type    | Default |
| ------------ | ------- |---------|
| `exhaustion` | Float   | -       |
| `chance`     | Integer | -       |

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

| Field         | Type                 | Default      |
|---------------|----------------------|--------------|
| `entity_type` | Entity Type List/Tag | All entities |
| `chance`      | Integer              | `1`          |
| `duration`    | Integer              | `100`        |

Example:

```json
{
  "type": "high_voltage:ignite",
  "duration": 200,
  "chance": 10,
  "entity_type": "#minecraft:animals"
}
```

---

# Layer Effect

Places or increases layers of a configurable block during weather.

Useful for snow accumulation, ash buildup, dust storms, and similar effects.

**Type ID**

```json
"high_voltage:layer"
```

| Field          | Type              | Default | Description                                                                                |
|----------------|-------------------|---------|--------------------------------------------------------------------------------------------|
| `block`        | Resource Location | -       | Block to place or modify.                                                                  |
| `property`     | String            | -       | Integer property used as the layer count.                                                  |
| `max_level`    | Integer           | -       | Maximum layer level.                                                                       |
| `noisy`        | Boolean           | -       | Randomly chooses an inclusive value out of the `max_value` and limits the height of which. |
| `surface_only` | Boolean           | `true`  | Only affect exposed surfaces.                                                              |
| `chance`       | Integer           | -       | Placement chance.                                                                          |

Example:

```json
{
  "type": "high_voltage:layer",
  "block": "minecraft:snow",
  "property": "layers",
  "max_level": 8,
  "noisy": true,
  "surface_only": true,
  "chance": 50
}
```

---

# Modify Attribute Effect

Temporarily modifies entity attributes during weather.

**Type ID**

```json
"high_voltage:modify_attribute"
```

| Field       | Type              | Default | Description          |
|-------------|-------------------|---------|----------------------|
| `attribute` | Resource Location | -       | Attribute to modify. |
| `name`      | String            | -       | Modifier name.       |
| `value`     | Double            | -       | Modifier value.      |
| `operation` | String            | -       | Attribute operation. |
| `chance`    | Integer           | `1`     | Application chance.  |

Valid operations:

```text
addition
multiply_base
multiply_total
```

Example:

```json
{
  "type": "high_voltage:modify_attribute",
  "attribute": "minecraft:generic_movement_speed",
  "name": "Blizzard Slowdown",
  "value": -0.2,
  "operation": "addition",
  "chance": 1
}
```

---

# Play Sound Effect

Plays a sound during weather.

**Type ID**

```json
"high_voltage:play_sound"
```

| Field    | Type              | Default |
|----------|-------------------|---------|
| `sound`  | Resource Location | -       |
| `volume` | Float             | -       |
| `pitch`  | Float             | -       |
| `chance` | Integer           | -       |

Example:

```json
{
  "type": "high_voltage:play_sound",
  "sound": "minecraft:entity.lightning_bolt.thunder",
  "volume": 1.0,
  "pitch": 1.0,
  "chance": 500
}
```

---

# Ring Bell Effect

Rings nearby bells during storms.

**Type ID**

```json
"high_voltage:ring_bell"
```

| Field    | Type    | Default | Description      |
| -------- | ------- |---------|------------------|
| `radius` | Float   | -       | Radius in blocks |
| `chance` | Integer | -       | Chance to occur  |

Example:

```json
{
  "type": "high_voltage:ring_bell",
  "radius": 64,
  "chance": 1000
}
```

---

# Status Effect

Applies a Minecraft mob effect.

**Type ID**

```json
"high_voltage:status_effect"
```

| Field         | Type                 | Default      |
|---------------|----------------------|--------------|
| `effect`      | Resource Location    | -            |
| `duration`    | Integer              | `200`        |
| `amplifier`   | Integer              | `0`          |
| `ambient`     | Boolean              | `true`       |
| `visible`     | Boolean              | `false`      |
| `entity_type` | Entity Type List/Tag | All entities |
| `chance`      | Integer              | `1`          |

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

# Summon Entity Effect

Summons entities during weather events.

**Type ID**

```json
"high_voltage:summon_entity"
```

| Field    | Type        | Default | Description                                |
|----------|-------------|---------|--------------------------------------------|
| `entity` | Entity Type | -       | Entity to summon.                          |
| `data`   | NBT Object  | `{}`    | Additional NBT data applied to the entity. |
| `chance` | Integer     | -       | Spawn chance.                              |

Example:

```json
{
  "type": "high_voltage:summon_entity",
  "entity": "minecraft:lightning_bolt",
  "chance": 1000
}
```

Example with NBT:

```json
{
  "type": "high_voltage:summon_entity",
  "entity": "minecraft:zombie",
  "data": {
    "CustomName": "{\"text\":\"Storm Walker\"}"
  },
  "chance": 500
}
```

---

# Velocity Effect

Applies motion to matching entities.

Useful for wind, gusts, storms, and knockback effects.

**Type ID**

```json
"high_voltage:velocity"
```

| Field          | Type                 | Default      | Description                    |
|----------------|----------------------|--------------|--------------------------------|
| `velocity`     | Vec3                 | -            | Minimum velocity applied.      |
| `max_velocity` | Vec3                 | `velocity`   | Maximum random velocity range. |
| `entity_type`  | Entity Type List/Tag | All Entities | Target entities.               |
| `chance`       | Integer              | `1`          | Activation chance.             |

If `max_velocity` is specified, a random velocity between `velocity` and `max_velocity` is chosen for each application.

Example:

```json
{
  "type": "high_voltage:velocity",
  "velocity": {
    "x": 0.1,
    "y": 0.0,
    "z": 0.1
  },
  "max_velocity": {
    "x": 0.5,
    "y": 0.2,
    "z": 0.5
  },
  "chance": 10
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