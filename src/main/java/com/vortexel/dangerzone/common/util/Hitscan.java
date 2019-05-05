package com.vortexel.dangerzone.common.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Hitscan {

    private final World world;
    private final Vec3d start;
    private final Vec3d end;
    private final Predicate<? super Entity> filter;
    private final double rayRadius;

    private final Vec3d motion;
    private final List<Entity> entities;
    private Vec3d pos;
    private AxisAlignedBB endBox;
    private final double rayRadiusSq;

    public Hitscan(World world, Vec3d start, Vec3d end, double rayRadius, Predicate<? super Entity> filter) {
        this.world = world;
        this.start = start;
        this.end = end;
        this.filter = filter;
        this.rayRadius = rayRadius;

        this.motion = end.subtract(start).normalize();
        this.entities = Lists.newArrayList();
        this.pos = start;
        this.endBox = new AxisAlignedBB(end, end.add(motion.scale(2)));
        this.rayRadiusSq = rayRadius * rayRadius;
    }

    @Nullable
    public Entity findFirst() {
        return findFirstNot(null);
    }

    @Nullable
    public Entity findFirstNot(Entity entity) {
        while (entities.size() == 0 && !isDone()) {
            iterate(entity);
        }
        if (entities.size() == 0) {
            return null;
        } else {
            return entities.get(0);
        }
    }

    public List<Entity> findAll() {
        while (!isDone()) {
            iterate(null);
        }
        return entities;
    }

    public boolean isDone() {
        return endBox.contains(pos);
    }

    private void iterate(@Nullable Entity ignore) {
        // Don't iterate once we're done, otherwise our position will escape the end box,
        // then we'll continue on forever and crash the game.
        if (isDone()) {
            return;
        }

        final Vec3d nextPos = pos.add(motion);
        final AxisAlignedBB rayBox = new AxisAlignedBB(pos, nextPos).grow(0.3);
        List<Entity> entitiesInBox = world.getEntitiesInAABBexcluding(ignore, rayBox, filter);
        for (Entity entity : entitiesInBox) {
            if (entity.canBeCollidedWith() && !entity.noClip) {
                AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(0.30000001192092896D);
                RayTraceResult rayResult = aabb.calculateIntercept(pos, nextPos);
                if (rayResult != null) {
                    double distSq = pos.squareDistanceTo(rayResult.hitVec);
                    if (distSq <= rayRadiusSq) {
                        entities.add(entity);
                    }
                }
            }
        }
        // Update our position vector
        pos = nextPos;
    }
}
