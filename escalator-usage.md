# Escalators

## How it works

- Each escalator has a top location and a bottom location.
- Direction decides which way it runs:
  - `up` means it starts at bottom and ends at top
  - `down` means it starts at top and ends at bottom
- Speed is in blocks per second.
- Optional sound is played while moving if a sound key is set.

When you walk into the start area, the plugin starts moving you along the escalator path.

## Commands

All commands start with:

`/sylv escalator`

### Create

Create a new escalator:

`/sylv escalator create <id> <topX> <topY> <topZ> <bottomX> <bottomY> <bottomZ> <up|down> <speed> [sound]`

Notes:

- `<id>` is case-insensitive, but it will show using the original casing you typed.
- Top and bottom must be in the same world.
- Speed must be `0` or higher.

### List

List all escalators:

`/sylv escalator list`

The id in the list is clickable and teleports you to the escalator's top location.
Hover the id to see quick info.

### Info

Show full info and action buttons:

`/sylv escalator info <id>`

This view includes:

- Teleport, show, delete buttons
- Edit buttons next to fields like speed, direction, upLoc, bottomLoc, sound

Most edit buttons use command suggestions so you can tweak values before running them.

### Teleport

Teleport to an escalator's top location:

`/sylv escalator tp <id>`

Alias:

`/sylv escalator teleport <id>`

### Show (debug view)

Toggle the show view for an escalator:

`/sylv escalator show <id>`

Useful if you want to visually confirm the path while testing.

### Edit

Edit fields on an escalator:

`/sylv escalator edit <id> upLoc <x> <y> <z>`

`/sylv escalator edit <id> bottomLoc <x> <y> <z>`

`/sylv escalator edit <id> speed <value>`

`/sylv escalator edit <id> direction <up|down>`

`/sylv escalator edit <id> sound <key>`

Remove the sound:

`/sylv escalator edit <id> sound none`

### Delete

Delete an escalator:

`/sylv escalator delete <id>`

Alias:

`/sylv escalator remove <id>`