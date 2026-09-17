package com.killeffect;

import com.killeffect.config.KillEffectConfig;
import com.killeffect.effect.KillEffectRenderer;
import com.killeffect.gui.KillEffectScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-only entry point.
 *
 * Kill detection works by watching vanilla's own death chat messages (e.g. "X was slain by Y").
 * If the message mentions the local player as the killer, we look up where that victim was last
 * seen (tracked every tick) and trigger the selected effect there - purely client-side, via
 * KillEffectRenderer, so nobody else ever sees it.
 */
public class KillEffectClient implements ClientModInitializer {
    private static KeyBinding openGuiKey;

    // Last known position of every player we've seen this session, keyed by their name.
    private static final Map<String, Vec3d> lastKnownPositions = new HashMap<>();

    @Override
    public void onInitializeClient() {
        KillEffectConfig.load();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.killeffect.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.killeffect"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.world == null) return;

        while (openGuiKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new KillEffectScreen());
            }
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            lastKnownPositions.put(player.getGameProfile().getName(), player.getPos());
        }

        KillEffectRenderer.tick();
    }

    private void onGameMessage(net.minecraft.text.Text message, boolean overlay) {
        if (overlay) return; // ignore the actionbar copy, only handle the chat message

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String text = message.getString();
        String myName = client.player.getGameProfile().getName();

        // Matches vanilla death message formats: "X was slain by Y", "X was shot by Y using [...]", etc.
        if (!text.contains(" by " + myName)) return;

        String victimName = extractVictimName(text);
        if (victimName == null || victimName.equals(myName)) return;

        Vec3d pos = lastKnownPositions.get(victimName);
        if (pos != null) {
            KillEffectRenderer.play(pos);
        }
    }

    private static String extractVictimName(String message) {
        int idx = message.indexOf(" was ");
        if (idx <= 0) return null;
        return message.substring(0, idx).trim();
    }
}
