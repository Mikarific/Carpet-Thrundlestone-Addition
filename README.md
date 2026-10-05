<img src="./src/main/resources/assets/carpet-thrundlestone-addition/icon.png" align="right" width="128px" />

# Carpet Thrundlestone Addition

A Carpet mod ([fabric-carpet](https://github.com/gnembon/fabric-carpet)) extension for performing and testing Thrundlestone.

# Rules
## asyncBeaconUpdates
Beacons send out async **comparator** block updates when powered.  
Sends out updates to neighboring comparators to reflect the thrundlestone method of getting an async update.  
Different from the implementation in carpetmod112, use with rule "asyncBeaconUpdatesUpdateDirectly" to act like carpetmod112.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `THRUNDLESTONE`, `CREATIVE`  

## asyncBeaconUpdatesUpdatesDirectly
Makes rule `asyncBeaconUpdates` update blocks directly, similar to carpetmod112.  
Rule `asyncBeaconUpdates` needs to be on for it to work.
* Type: `Boolean`
* Default value: `false`
* Allowed options: `true`, `false`
* Categories: `THRUNDLESTONE`, `CREATIVE`  

## commandPalette
Enables /palette command to get information about the palette.
* Type: `String`
* Default value: `ops`
* Allowed options: `true`, `false`, `ops`, `0`, `1`, `2`, `3`, `4`
* Categories: `THRUNDLESTONE`, `CREATIVE`  

## dungeonLoggerExcludeNonViable
Excludes dungeons from the dungeon logger that aren't viable for survival thrundlestone setups.
* Type: `Boolean`
* Default value: `true`
* Allowed options: `true`, `false`
* Categories: `THRUNDLESTONE`  

## maxChainedNeighborUpdates
Changes the "max-chained-neighbor-updates" value of the server.  
Set to -1 to not override the value in server settings.
* Type: `Integer`
* Default value: `-1`
* Suggested options: `-1`, `0`, `65535`, `1000000`, `2147483647`
* Categories: `THRUNDLESTONE`, `CREATIVE`  

# Loggers
## asyncBlockUpdates

`/log asyncBlockUpdates <options>`

Available options:
- `all`: Log everything below
- `thread`: Log when the block update queue starts/finishes on a thread other than the main thread.
- `skips`: Log when update skipping occurs on a thread other than the main thread.

Attributes:
- Default option: `all`
- Suggested options: `thread`, `skips`, `all`  

## dungeons

`/log dungeons <options>`

Available options:
- `all`: Log everything below
- `spawner`: Log when a dungeon spawner attempts to generate.
- `chest`: Log when a dungeon chest attempts to generate.

Attributes:
- Default option: `all`
- Suggested options: `spawner`, `chest`, `all`

By default, dungeons that are not viable for thrundlestone setups are not logged. You can specify this with rule `dungeonLoggerExcludeNonViable`.
