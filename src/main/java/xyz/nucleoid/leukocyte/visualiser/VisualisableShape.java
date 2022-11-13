package xyz.nucleoid.leukocyte.visualiser;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public interface VisualisableShape {
    public List<Pair<GlobalPos, GlobalPos>> edges();
}
