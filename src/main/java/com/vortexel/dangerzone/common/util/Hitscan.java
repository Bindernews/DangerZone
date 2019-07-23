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
    private final Predicate<? super Entity> filter;
    private final double rayRadius;
    private Vec3d end;
    private Vec3d motion;
    private List<Entity> entities;
    private Vec3d pos;
    private AxisAlignedBB endBox;
    private final double rayRadiusSq;

    public Hitscan(World world, Vec3d start, Vec3d end, RayTraceResult blockRayTrace, double rayRadius,
                   Predicate<? super Entity> filter) {
        this.world = world;
        this.filter = filter;
        this.rayRadius = rayRadius;
        this.end = end;

        // If we're including blocks, then do an initial ray-trace to find the first block
        if (blockRayTrace != null) {
            this.end = blockRayTrace.hitVec;
        }

        this.motion = this.end.subtract(start).normalize();
        this.entities = Lists.newArrayList();
        this.pos = start;
        Vec3d end2 = this.end.add(motion.scale(2));
        this.endBox = new AxisAlignedBB(this.end.x, this.end.y, this.end.z, end2.x, end2.y, end2.z);
        this.rayRadiusSq = rayRadius * rayRadius;
    }

    public Vec3d getEnd() {
        return this.end;
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
