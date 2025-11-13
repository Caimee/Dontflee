package org.sample.fleeonsight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class Fleeonsight implements ModInitializer {
    public static final String MOD_ID = "sheepflee";
    private static final double DETECTION_RANGE = 8.0;
    private static final double FLEE_SPEED = 0.25;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        System.out.println("Sheep Flee Mod initialized!");
    }

    private void onWorldTick(ServerWorld world) {

        List<ServerPlayerEntity> players = world.getPlayers();

        for (PlayerEntity player : players) {
            Box detectionBox = new Box(
                    player.getX() - DETECTION_RANGE,
                    player.getY() - DETECTION_RANGE,
                    player.getZ() - DETECTION_RANGE,
                    player.getX() + DETECTION_RANGE,
                    player.getY() + DETECTION_RANGE,
                    player.getZ() + DETECTION_RANGE
            );

            List<SheepEntity> nearbyShee = world.getEntitiesByClass(
                    SheepEntity.class,
                    detectionBox,
                    sheep -> sheep.isAlive()
            );

            for (SheepEntity sheep : nearbyShee) {
                fleeSheepFromPlayer(sheep, player);
            }
        }
    }

    private void fleeSheepFromPlayer(SheepEntity sheep, PlayerEntity player) {
        Vec3d sheepPos = sheep.getPos();
        Vec3d playerPos = player.getPos();

        double dx = sheepPos.x - playerPos.x;
        double dz = sheepPos.z - playerPos.z;
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0 && distance < DETECTION_RANGE) {
            double normalizedX = (dx / distance) * FLEE_SPEED;
            double normalizedZ = (dz / distance) * FLEE_SPEED;
            sheep.setVelocity(normalizedX, sheep.getVelocity().y, normalizedZ);
            sheep.velocityModified = true;
            float yaw = (float) (Math.atan2(normalizedZ, normalizedX) * 180.0 / Math.PI) - 90.0F;
            sheep.setYaw(yaw);
            sheep.setBodyYaw(yaw);
            sheep.setHeadYaw(yaw);
        }
    }
}