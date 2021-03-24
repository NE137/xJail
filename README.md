# xJail

## Commands
* /jail jail \<user> \<terms> `Jail a <user> for <terms> amount of jail terms`
* /jail free \<user> `Release a user from jail`
* /jail getstick `Get a jailstick`

* /jail admin add  `Add your current location as a cell`
* /jail admin release `Set the release location to your location`
* /jail admin list  `Get a list of all of the cells.`

## Permissions 

xjail.free - `Allows you to use /jail free`

xjail.jail - `Allows you to use /jail jail`

xjail.getstick - `Allows you to use /jail getstick`

xjail.admin - `Allows you to use admin commands`

## Config.yml
```
minutesPerKill: 10 #How many minutes extra jail time do you get per kill?
defaultTime: 10 #What is the default jail sentence time?
jailstickHitsRequired: 3 #How many hits do you need to do with a jailstick to jail someone
region: prison #Name of the region of the prison, if a person who is jailed leaves this region, the escape commands will be cast and their sentence will be over.
escapeCommands: #Commands to call once a player has left the prison
- give %player% diamond 1
- broadcast %player% has just escaped from prison!
jailLocation: #Locations of the cells, set by using /jail admin add
  '0':
    x: 682
    y: 66
    z: -86
    world: world
  '1':
    x: 687
    y: 66
    z: -82
    world: world
jailReleaseLocation: #Location of the release point, set using /jail admin release
  world: RDR
  x: 596
  y: 66
  z: 52
allowedCommands: # List of commands you can use in jail
- /jail
- /help
```
