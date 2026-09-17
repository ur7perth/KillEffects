package com.killeffect.effect;

import com.killeffect.config.KillEffectConfig;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Spawns every effect entirely inside the local ClientWorld (world.addEntity / world.addParticle
 * / playSound with the client player as the sound "source"). None of this is ever sent to the
 * server, so nothing here is visible to other players - only to the person who triggered it.
 */
public class KillEffectRenderer {
    private static final Random RANDOM = new Random();
    private static final List<ScheduledTask> SCHEDULED_TASKS = new ArrayList<>();

    /** Call once per client tick (from KillEffectClient) to run delayed effect sounds. */
    public static void tick() {
        SCHEDULED_TASKS.removeIf(task -> {
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                task.action.run();
                return true;
            }
            return false;
        });
    }

    public static void play(Vec3d pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null || client.player == null) return;

        KillEffectConfig config = KillEffectConfig.get();
        KillEffectType type = config.selectedEffect;
        float speed = Math.max(0.1f, config.getSpeed(type));

        switch (type) {
            case LIGHTNING -> spawnLightning(world, pos);
            case TNT -> spawnTnt(client, world, pos, speed);
            case TOTEM -> spawnTotem(client, world, pos);
            case FIREWORK -> spawnFirework(world, pos, speed);
            case ANVIL -> spawnAnvil(client, world, pos, speed);
        }
    }

    private static void spawnLightning(ClientWorld world, Vec3d pos) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
        if (bolt == null) return;
        bolt.refreshPositionAfterTeleport(pos.x, pos.y, pos.z);
        bolt.setCosmetic(true); // visual only: no damage, no fire, no block changes
        world.addEntity(bolt);
    }

    private static void spawnTnt(MinecraftClient client, ClientWorld world, Vec3d pos, float speed) {
        TntEntity tnt = new TntEntity(world, pos.x, pos.y, pos.z, null);
        int fuse = Math.max(5, Math.round(80 / speed));
        tnt.setFuse(fuse);
        world.addEntity(tnt);
        world.playSound(client.player, pos.x, pos.y, pos.z, SoundEvents.ENTITY_TNT_PRIMED,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private static void spawnTotem(MinecraftClient client, ClientWorld world, Vec3d pos) {
        for (int i = 0; i < 30; i++) {
            double vx = (RANDOM.nextDouble() - 0.5) * 0.6;
            double vy = RANDOM.nextDouble() * 0.6;
            double vz = (RANDOM.nextDouble() - 0.5) * 0.6;
            world.addParticle(ParticleTypes.TOTEM_OF_UNDYING, pos.x, pos.y + 1.0, pos.z, vx, vy, vz);
        }
        world.playSound(client.player, pos.x, pos.y, pos.z, SoundEvents.ITEM_TOTEM_USE,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private static void spawnFirework(ClientWorld world, Vec3d pos, float speed) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
        // 1.20.5+ item data uses components instead of raw NBT tags.
        stack.set(DataComponentTypes.FIREWORKS, new FireworksComponent(1, List.of()));

        FireworkRocketEntity rocket = new FireworkRocketEntity(world, pos.x, pos.y, pos.z, stack);
        rocket.setVelocity(0, 0.35 * speed, 0);
        world.addEntity(rocket);
    }

    private static void spawnAnvil(MinecraftClient client, ClientWorld world, Vec3d pos, float speed) {
        BlockPos spawnPos = BlockPos.ofFloored(pos.x, pos.y + 5, pos.z);
        FallingBlockEntity anvil = FallingBlockEntity.spawnFromBlock(world, spawnPos, Blocks.ANVIL.getDefaultState());
        if (anvil == null) return;
        anvil.setVelocity(0, -0.35 * speed, 0);

        int landDelay = Math.max(3, Math.round(24 / speed));
        SCHEDULED_TASKS.add(new ScheduledTask(landDelay, () ->
                world.playSound(client.player, pos.x, pos.y, pos.z, SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.PLAYERS, 1.0f, 1.0f)));
    }

    private static class ScheduledTask {
        int ticksLeft;
        final Runnable action;

        ScheduledTask(int ticksLeft, Runnable action) {
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }
}
