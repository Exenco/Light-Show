package net.exenco.showcontroller;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ShowController implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("showcontroller");

	private static final KeyBinding startKeyBind;
	private static final KeyBinding checkKeyBind;
	static {
		KeyBinding startKeyBinding = new KeyBinding("Start Art-Net", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, "Show Controller");
		startKeyBind = KeyBindingHelper.registerKeyBinding(startKeyBinding);
		KeyBinding checkKeyBinding = new KeyBinding("Check connection", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F10, "Show Controller");
		checkKeyBind = KeyBindingHelper.registerKeyBinding(checkKeyBinding);
	}

	@Override
	public void onInitialize() {
		ShowConfig.createConfig();
		ArtNetHandler artNetHandler = new ArtNetHandler();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			JsonObject config = ShowConfig.getConfig();
			if(config == null || client.player == null || client.getCurrentServerEntry() == null)
				return;
			while (startKeyBind.wasPressed()) {
				String address = config.has("Address") ? config.get("Address").getAsString() : "-";
				int port  = config.has("Port") ? config.get("Port").getAsInt() : 6454;

				if(address.equals("-")) {
					client.player.sendMessage(new LiteralText("§7Please enter the address!"), false);
					try {
						Runtime.getRuntime().exec("explorer.exe /select," + ShowConfig.getFile().getPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
					continue;
				}
				if (artNetHandler.isRunning()) {
					artNetHandler.stop();
					client.player.sendMessage(new LiteralText("§7Stopping clientside Art-Net..."), false);
				} else {
					String str = artNetHandler.start(new InetSocketAddress(address, port), LOGGER) ? "§7Starting clientside Art-Net..." : "§cEntered ip is invalid: " + address;
					client.player.sendMessage(new LiteralText(str), false);
				}
			}
			while (checkKeyBind.wasPressed()) {
				if (artNetHandler.confirmConnection())
					client.player.sendMessage(new LiteralText("§7Clientside Art-Net §aconnected§7!"), false);
				else
					client.player.sendMessage(new LiteralText("§7Clientside Art-Net §cnot connected§7!"), false);
			}
		});
	}
}
