# FamilyJewels #

## Purpose ##
FamilyJewels is a plugin for Bukkit powered Minecraft servers that provides a level of protectection against X-Ray cheaters. In order to provide this protection FamilyJewels will modify the information sent to the client turning certain ore blocks into smooth stone, effectively protecting ores from prying eyes.

Unlike other anti-xray plugins FamilyJewels strives to provide an anticheat experience that does not impact a legitimate player in any way. Someone who is playing should never know that FamilyJewels is installed. See "How it works" for more information.

## How it works ##
In order to provide minimal intrusion for regular players, FamilyJewels only hides blocks that are not directly surrounded by air. 

To do this, FamilyJewels will hook the players NetServerHandler to rewrite the packets sent to the player. As a result of this hook, this plugin is mosty likely incompatible with other plugins that also do this (eg Spout).

Whenever a player punches or breaks a block, the plugin will then update the client to show any nearby ores. This is very effective as it uncovers ores only when a player is nearby and digging. 

##Issues and Feedback##
Please report all issues and feedback with FamilyJewels at http://www.github.com/Minecarts/FamilyJewels/issues

Please keep in mind that this is not a pure Bukkit plugin and this will only work on pure CraftBukkit servers. In addition, **BE VERY CAREFUL** when running this plugin after a patch. Since this plugin hooks into the internals of CraftBukkit, there is always the possibility of world corruption or otherwise weird behavior. Use this plugin on a test server before deploying it to a production environemtn.

While we personally have been running this plugin for many months, we cannot guarntee that it won't break your world. Please make backups.

##Source Code###
You can view and contribute to FamilyJewels at our github page: http://www.github.com/Minecarts/FamilyJewels/