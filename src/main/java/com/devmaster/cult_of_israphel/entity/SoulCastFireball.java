package com.devmaster.cult_of_israphel.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SoulCastFireball extends SmallFireball {

    public SoulCastFireball(EntityType<SoulCastFireball> entityType, Level level) {
        super(entityType, level);
    }

    public SoulCastFireball(EntityType<SoulCastFireball> entityType, Level level, LivingEntity shooter, double dx, double dy, double dz)
    {
        super(entityType, level);

        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        Vec3 direction = new Vec3(dx, dy, dz).normalize();
        this.xPower = direction.x * 0.1D;
        this.yPower = direction.y * 0.1D;
        this.zPower = direction.z * 0.1D;
        this.setDeltaMovement(direction.scale(0.8D));
        this.hasImpulse = true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
}
