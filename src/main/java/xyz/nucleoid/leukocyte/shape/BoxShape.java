package xyz.nucleoid.leukocyte.shape;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import xyz.nucleoid.leukocyte.visualiser.VisualisableShape;
import xyz.nucleoid.stimuli.filter.EventFilter;

import java.util.List;

public final class BoxShape implements ProtectionShape, VisualisableShape {
    public static final Codec<BoxShape> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.xmap(id -> RegistryKey.of(RegistryKeys.WORLD, id), RegistryKey::getValue).fieldOf("dimension").forGetter(scope -> scope.dimension),
                BlockPos.CODEC.fieldOf("min").forGetter(scope -> scope.min),
                BlockPos.CODEC.fieldOf("max").forGetter(scope -> scope.max)
        ).apply(instance, BoxShape::new);
    });

    private final RegistryKey<World> dimension;
    private final BlockPos min;
    private final BlockPos max;

    private final EventFilter eventFilter;

    public BoxShape(RegistryKey<World> dimension, BlockPos min, BlockPos max) {
        this.dimension = dimension;
        this.min = min;
        this.max = max;

        this.eventFilter = EventFilter.box(dimension, min, max);
    }

    @Override
    public EventFilter asEventFilter() {
        return this.eventFilter;
    }

    @Override
    public Codec<? extends ProtectionShape> getCodec() {
        return CODEC;
    }

    @Override
    public MutableText display() {
        return Text.literal("[")
                .append(this.displayPos(this.min).formatted(Formatting.AQUA))
                .append("; ")
                .append(this.displayPos(this.max).formatted(Formatting.AQUA))
                .append("] in ")
                .append(Text.literal(this.dimension.getValue().toString()).formatted(Formatting.YELLOW))
                .formatted(Formatting.GRAY);
    }

    @Override
    public MutableText displayShort() {
        return this.display();
    }

    private MutableText displayPos(BlockPos pos) {
        return Text.literal("(" + pos.getX() + "; " + pos.getY() + "; " + pos.getZ() + ")");
    }

    @Override
    public List<Pair<GlobalPos, GlobalPos>> edges() {
        int minX = this.min.getX();
        int minY = this.min.getY();
        int minZ = this.min.getZ();
        int maxX = this.max.getX();
        int maxY = this.max.getY();
        int maxZ = this.max.getZ();
        return List.of(
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, minY, minZ)), GlobalPos.create(this.dimension, new BlockPos(minX, minY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, minY, minZ)), GlobalPos.create(this.dimension, new BlockPos(minX, maxY, minZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, minY, minZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, minY, minZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, minZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, minY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(minX, maxY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, maxY, minZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, minZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, maxY, minZ)), GlobalPos.create(this.dimension, new BlockPos(minX, maxY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(minX, maxY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(minX, minY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, maxY, minZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, minY, minZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, minY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(minX, minY, maxZ))),
                Pair.of(GlobalPos.create(this.dimension, new BlockPos(maxX, minY, maxZ)), GlobalPos.create(this.dimension, new BlockPos(maxX, minY, minZ)))
        );
    }
}
