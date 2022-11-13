package xyz.nucleoid.leukocyte.visualiser;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public final class Visualiser implements ServerTickEvents.StartTick {
    public static final Visualiser INSTANCE = new Visualiser();

    public final Map<UUID, Set<DisplayedShape>> shapesToDisplay = new HashMap<>();

    private Visualiser() {
        ServerTickEvents.START_SERVER_TICK.register(this);
    }

    public void addEdges(ServerPlayerEntity player, Collection<Pair<GlobalPos, GlobalPos>> edges) {
        this.shapesToDisplay.compute(player.getUuid(), (u, s) -> {
            if (s == null) {
                var set = new HashSet<DisplayedShape>();
                set.add(new DisplayedShape(player.server.getTicks() + 30 * 20, edges));
                return set;
            } else {
                s.add(new DisplayedShape(player.server.getTicks() + 30 * 20, edges));
                return s;
            }
        });
    }

    @Override
    public void onStartTick(MinecraftServer server) {
        for (var entry : this.shapesToDisplay.entrySet()) {
            var player = server.getPlayerManager().getPlayer(entry.getKey());

            for (var itr = entry.getValue().iterator(); itr.hasNext();) {
                var shape = itr.next();
                if (shape.timeToRemove <= server.getTicks()) {
                    itr.remove();
                }

                if (player == null) {
                    continue;
                }

                for (var edge : shape.edges) {
                    if (edge.getFirst().getDimension() != player.getWorld().getRegistryKey()) {
                        continue;
                    }

                    var pos = edge.getFirst().getPos();
                    var end = edge.getSecond().getPos();

                    var dist = Math.sqrt(pos.getSquaredDistance(end));
                    var offset = Vec3d.of(end.subtract(pos)).normalize();

                    double x = pos.getX();
                    double y = pos.getY();
                    double z = pos.getZ();

                    for (int i = 0; i <= dist; i++) {
                        player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.SMALL_FLAME, true, x, y, z, 0, 0, 0, 0, 1));

                        x += offset.getX();
                        y += offset.getY();
                        z += offset.getZ();
                    }
                }
            }
        }
    }

    private record DisplayedShape(long timeToRemove, Set<Pair<GlobalPos, GlobalPos>> edges) {
        public DisplayedShape(long timeToRemove, Collection<Pair<GlobalPos, GlobalPos>> edges) {
            this(timeToRemove, Util.make(new HashSet<>(), s -> s.addAll(edges)));
        }
    }
}
