package com.killeffect.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.killeffect.effect.KillEffectType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Stores which effect is selected and the speed multiplier for each effect.
 * Saved to <gamedir>/config/killeffect.json
 */
public class KillEffectConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("killeffect.json");

    public KillEffectType selectedEffect = KillEffectType.TOTEM;
    public Map<KillEffectType, Float> speeds = new EnumMap<>(KillEffectType.class);

    private static KillEffectConfig instance;

    public static KillEffectConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        KillEffectConfig loaded = null;
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    loaded = GSON.fromJson(reader, KillEffectConfig.class);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Fall back to defaults below if the file is missing/corrupt.
        }
        instance = (loaded != null) ? loaded : new KillEffectConfig();
        if (instance.selectedEffect == null) instance.selectedEffect = KillEffectType.TOTEM;
        if (instance.speeds == null) instance.speeds = new EnumMap<>(KillEffectType.class);
        for (KillEffectType type : KillEffectType.values()) {
            instance.speeds.putIfAbsent(type, 1.0f);
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public float getSpeed(KillEffectType type) {
        return speeds.getOrDefault(type, 1.0f);
    }

    public void setSpeed(KillEffectType type, float speed) {
        speeds.put(type, speed);
        save();
    }

    public void setSelected(KillEffectType type) {
        selectedEffect = type;
        save();
    }
}
