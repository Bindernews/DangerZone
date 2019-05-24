\[Insert Logo Image Here\]

# Danger Zone
Danger Zone is a mod about adding danger and excitement to Minecraft. I kept playing modpacks, getting awesome
armor and weaponry, but having nothing to kill with it. This mod changes the difficulty of enemies, making them
a real challenge again, even if you have cool mod armor. But wait! With great risk comes great reward, and the
rewards are certainly great: extra mob drops based on the mob's level, and **Loot Coins**!

When you kill mobs they drop **Loot Coins**. You can summon a trader villager who will sell you things in
exchange for you loot coins. You can also build a shotgun and fire money at your enemies! Maybe money can't buy
happiness but it **can** buy you the sweet, sweet sound of a shotgun blast. 

# F.A.Q.
### May I use this in a modpack?
You may use this in your modpack as long as you list the author(s) and include a link back to either the
[GitHub] page or ~~the Curse page~~ (once I get the project page up).

### Can I change the level of an entity?
Entities are modified when they're spawned. If you use `/summon` to summon an entity,
its level may be modified by adding `ForgeCaps:{"dangerzone:level":<LEVEL>}` to your data tag.
Replace `<LEVEL>` with the desired danger level of the entity.

### Why are enemies so tough by default?
This mod is intended to make enemies tough enough to be fun when playing with other mods.
If you're trying to play this mod with Vanilla, you should probably change the settings.

### How do I change enemy difficulty?
See the relevant [Configuration](#Configuration) section.

### How do I change what the merchant is selling?
See the relevant [Configuration](#Trader Merchandise) section.

### It's not working in the Nether / End / etc.


# Configuration
This mod is extremely configurable and we encourage you to play with the settings. Each modpack
will have different limits, and each player will find different modifications harder.

## Entity Difficulty
In your Minecraft directory there should be a directory `config/dangerzone`.
Create a file named `entity_config.json` in that directory. I *highly* recommend looking at the example
below before reading on, because the rest of this will make more sense.

### Format
An entity configuration file is a JSON file. Each key must be a **resource location** (see below).
If the string is a valid entity identifier (e.g. "minecraft:horse") then it will be used when
modifying entities of that type. Otherwise it can be used for inheritance. Each **resource location**
maps to an **Entity Config** object.
> **Note:** Entities can be used for inheritance as well, but we recommend against it because it could get confusing.

> A **resource location** is a string with a in the format `group:resource_name`. <br/>
> Usually the "group" is a modid (e.g. "minecraft", "dangerzone", "twilightforest", etc.)
> but it doesn't have to be. <br/>
> Examples: `minecraft:horse`, `dangerzone:trader_villager`, `minecraft:diamond`


#### Entity Config

#### `inherits`
**Valid Values:** Either a single *resource location* or an array of them, each referencing another
  *Entity Config*. <br/>
**Purpose:** Inherit the properties of the given *Entity Config*s, with values from later inherited
  configs overriding the ones from earlier, and values from the current *Entity Config* overriding
  anything else. <br/>
  For example if you have  `"inherits": ["a:a", "b:b"]` and "a:a" and "b:b" both specify `move_speed`
  then "b:b"'s `move_speed` will be used. Then, if the current *Entity Config* specifies `move_speed`
  as well, that will override the `move_speed` from "b:b". <br/>
**Optional:** yes <br/> 

#### `<modifier>`
Replace `<modifier>` with one of the modifiers from the [List of Modifiers](#List_of_Modifiers). <br/>
**Valid Values:** `false` will disable this modifier from being applied to this *Entity Config*.
If the value isn't `false` then it must be an *object* with the following fields.

| Field | Values | Details
|-------|--------|:--|
| range | `[min, max]` \| `max` | The range of the adjustments, the default minimum value is 0. |
| enabled | `true` \| `false` | `true` by default, setting this to `false` will prevent this modifier from being applied. |
| chance | `[min, max]` | The chance this modifier will be applied. The chance scales between `min` and `max` based on the entity's level. |
| min_level | `level` | The minimum level at which this modifier will be applied. At this level the minimum chance will be applied. |
| max_level | `level` | The level at which chance will max out. Any level above this will have the maximum chance. | 
| danger_scale | `number (0.0, 1.0]` | A measure of how much more difficult this modifier makes an entity. At 0.5 this modifier will be applied twice as much. | 

#### List of Modifiers
- max_health
- regeneration
- attack_damage
- attack_speed
- move_speed
- fly_speed
- armor
- armor_toughness
- wither
- explosion_radius

### Example
Here is an example of what your file might look like. This includes inheritance, and modifying horses and zombies.
In this example the zombie inherits from "generic:hostile", then inherits from "generic:regen", and finally
adds its own modifiers.
```json
{
  "generic:regen": {
    "regeneration": {
      "range": [1, 3],
      "chance": [0, 0.6],
      "min_level": 50
    }
  },
  "minecraft:horse": {
    "inherits": "generic:animal", 
    "move_speed": {
      "range": [1, 2],
      "chance": [0.2, 0.8],
      "min_level": 30
    }
  },
  "minecraft:zombie": {
    "inherits": ["generic:hostile", "generic:regen"],
    "max_health": {
      "range": [1, 50],
      "danger_scale": 0.75,
      "min_level": 10
    }
  }
}
```

## Trader Merchandise
In the Danger Zone configuration directory (`<minecraft_dir>/config/dangerzone`) there's a file named
`merchandise.json`. This contains all the default merchandise for Danger Zone and Minecraft. Feel
free to modify this file as appropriate for your modpack.

The file is a JSON array of *Offer*s.

| Field | Type | Example | Required | Description
|---|---|---|---|:--|
| item | string | "minecraft:diamond" | yes | The item the player will buy |
| cost | number | 100 | yes | How many loot coins this costs |
| count | number | 1 | no (default: 1) | The stack size of `item` |
| damage | number | 0 | no (default: 0) | The damage value of the item(s) |
| tag | any | { "CustomName": "Dinnerbone" } | no (default: empty) | Custom NBT tag for the item. |

# Status
This mod is currently in Alpha. It should work, but there may be bugs and it definitely needs balance changes.

# Copying
If you're looking at this mod because you want to learn how to make Minecraft mods, read on. This mod
is licensed under the GPLv3, meaning if you want to copy it then your code must be GPLv3 as well.

If you learn how to do something from reading our code, then make it work in your own code, that is fine.
You can license your own code however you want.

If you flat out copy our code and all you do is rename some variables, that's not okay, unless you
also release your code as GPLv3.

# Contributing
By submitting code or assets to Danger Zone, you agree to release your contributions under the terms
of the GNU Lesser General Public License (version 3).
 
 
[GitHub]: https://github.com/Bindernews/DangerZone
