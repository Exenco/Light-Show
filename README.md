# Light Show

Server-side Minecraft plugin for creating dynamic in-game light shows. It works by simulating real world light fixtures
which are then controlled via DMX512 protocol over Art-Net. Thus, allowing to utilize professional lighting control software.

## Showcase
[Currently left blank]

## Setup
1. Download and install the latest version of the plugin.
2. Start server once to generate configuration files.
3. Configure the plugin by editing the `config.yml` file.
4. Register your light fixtures in the `fixtures.json` file.
5. Restart the server to apply changes.
6. Setup your lighting control software to send DMX data via Art-Net to the server's IP address.
7. Start Art-Net receiver in the plugin using `/ls artnet start`.

## Troubleshooting
For troubleshooting and support, please visit join the [Discord](https://discord.gg/cDzyUUuwaH) or create an issue.

## Credits
This project contains no third-party libraries or external code.
Thank you to the authors of the many resources that offered inspiration and help over the years.
Because the project has evolved for so long, I can no longer credit individual sources as precisely as before, but I’m sincerely grateful for the knowledge shared by the community.