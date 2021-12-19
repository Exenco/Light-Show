# Light-Show
Spigot Plugin for creating light shows/concerts in Minecraft. It works by simulating ingame effects like guardian and beacon beams.

## Demo
Here you can see a snippet of a light-show I made:
[![Light-Show-Demo](https://raw.githubusercontent.com/Exenco/Light-Show/1.18/Demo.gif)](https://www.reddit.com/r/admincraft/comments/rgiwvv/ive_created_a_plugin_which_allows_you_to_make/) \
Click on the Demo to view the full video on Reddit.

## How it works
A surface level description would be: It uses the internet to receive signals form a lighting control software which can be translated into Minecraft. Going more into detail it receives [DMX512](https://wikipedia.org/wiki/DMX512) signals via the protocol [Art-Net](https://wikipedia.org/wiki/Art-Net) to filter out values for ingame [fixtures](https://github.com/Exenco/Light-Show/wiki/Fixtures).

## How one can use it
It might seem complex at first, but it really isn't once you get the hang of it:
1. Install plugin on your server.
2. Setup [config.json](https://github.com/Exenco/Light-Show/wiki/Plugin#config) and depending on what you want [fixture configs](https://github.com/Exenco/Light-Show/wiki/Fixtures).
3. Register fixtures by adding them as [DmxEntries](https://github.com/Exenco/Light-Show/wiki/Plugin#dmxentries).
4. Install a lighting control software ([MagicQ](https://chamsyslighting.com/products/magicq), [Dot2](https://www.malighting.com/downloads/products/dot2/), [GrandMA2](https://www.malighting.com/downloads/products/grandma2/), [GrandMA3](https://www.malighting.com/grandma3/), etc.).
5. Learn installed lighting control software ([ChamSys has tutorials for MagicQ](https://www.youtube.com/watch?v=h1UGn-naAzk)).
6. Setup fixtures in your software. See [Fixtures](https://github.com/Exenco/Light-Show/wiki/Fixtures) for coding info.
6. Connect lighting control software to Minecraft via [Art-Net](https://github.com/Exenco/Light-Show/wiki/Art-Net). 
7. Start Art-Net connectivity by using command `/show start`
8. Light away!

## Troubleshooting
For questions and ideas please join the [Discord Server](https://discord.gg/cDzyUUuwaH).

## Credits
The reason for not having any dependencies is that I made everything myself. This, however, was supported by checking out how others implemented what I was trying to do.

Plugin:\
`Java Art-Net` - [cansik's Art-Net library](https://github.com/cansik/artnet4j) \
`Lots of inspiration` - [Rushmead's Theatrical Mod](https://github.com/theatricalmod/theatrical-forge) (Additionally ORGG Studios for explanation videos)\
`Packet usage` - [SkytAsul](https://github.com/SkytAsul/) \
Video:\
`Arena Map` - TheHolder, PreFXDesigns, CraftCrusader\
`Stage` - MinerBuilder\
`Concert` - [Alan Walker](https://www.youtube.com/channel/UCJrOtniJ0-NWz37R30urifQ): [Untold Festival](https://www.youtube.com/watch?v=dYsTiW8skv0), [Neversea Festival](https://www.youtube.com/watch?v=AVVWVcIA1mw), [Parookaville Festival](https://www.youtube.com/watch?v=sANlCvgOZF0)
