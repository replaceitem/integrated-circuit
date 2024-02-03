# Integrated Circuit

[<img alt="Available for fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">](https://fabricmc.net/)
[<img alt="Requires fabric api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">](https://modrinth.com/mod/fabric-api)
[<img alt="Works best with cloth config" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@2/assets/cozy/requires/cloth-config-api_vector.svg">](https://modrinth.com/mod/cloth-config)
[<img alt="Available on Modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg">](https://modrinth.com/mod/integrated-circuit)
[<img alt="Chat on Discord" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-singular_vector.svg">](https://discord.gg/etTDQAVSgt)

A Minecraft mod that adds an integrated circuit redstone component.
Each integrated circuit block has a 15x15 grid where you can place two-dimensional
redstone components. Each side of the circuit can be used as an input or output for your circuit.

![The integrated circuit block](/block.png)

![Example circuit](https://i.imgur.com/QbySfvI.gif)

## How to use

To craft an integrated circuit, you need 2 redstone dust, 1 quartz, 1 black terracotta, and 3 concrete or stone arranged like so:

![Crafting recipe](/recipe.gif)

When you place one down and click on it, it will open the circuit editor screen.
You can select a block on the left, and place it in the 15x15 grid as you normally would in minecraft.
To rotate a component, you use the scroll wheel.
Adjusting repeater delay or the comparator mode is done by right clicking on a component.

The four ports are labeled using four colors to associate them with the sides of the integrated circuit block.
Each port can be an input or output, which can be changed by right clicking it in the editor.

You can also copy circuits by putting an integrated circuit in the crafting grid,
along with empty integrated circuits, similar to how books are copied, or re-dye them a different color
by crafting them with a dye item, like with wool or shulker boxes.

## Config

When having [cloth-config](https://modrinth.com/mod/cloth-config) and [ModMenu](https://modrinth.com/mod/modmenu) installed,
a config can be accessed from the Mod's modmenu page.
Without cloth-config installed, the mod will use the vanilla keybindings and default config options.

The config allows to customize the three main keybindings for placing, destroying and picking components.
You can also change the scroll behaviour (rotate component, scroll palette) and inverse the scroll direction.

## Credits

Credits for the tweaked redstone component textures (levers, observers):

Vanilla Tweaks: h