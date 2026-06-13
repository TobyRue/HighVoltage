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

# 2. Effects

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

| Field    | Type    | Description                                                                                                     |
| -------- | ------- | --------------------------------------------------------------------------------------------------------------- |
| `chance` | Integer | Lower values result in more frequent activation. The exact interpretation depends on the effect implementation. |

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
Or

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

| Field    | Type    | Required | Description                      |
| -------- | ------- | -------- | -------------------------------- |
| `radius` | Integer | Yes      | Search radius around the player. |
| `chance` | Integer | Yes      | Lightning strike chance.         |

Example:

```json
{
  "type": "high_voltage:player_bonus_lightning",
  "radius": 32,
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
  "run": "say Hello World!",
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