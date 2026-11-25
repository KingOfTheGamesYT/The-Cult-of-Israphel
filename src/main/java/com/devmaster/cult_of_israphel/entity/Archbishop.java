package com.devmaster.cult_of_israphel.entity;

import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class Archbishop extends Monster {
    private int dashCooldown;
    private int dashTicks;
    private Vec3 dashVelocity = Vec3.ZERO;
    private int meleeCooldown;
    private int fireballCooldown;
    private int fangCooldown;
    private int strafeTime;
    private float strafeDirection = 1.0F;

    public Archbishop(EntityType<? extends Archbishop> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new ArchbishopCombatGoal(this));
        this.goalSelector.addGoal(7, new FloatingWanderGoal(this));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.initWeapons();
        }
    }

    private void initWeapons() {
        ItemStack currentWeapon = this.getItemBySlot(EquipmentSlot.MAINHAND);

        if (currentWeapon.isEmpty()) {
            ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);

            if (this.random.nextFloat() < 0.35F) {
                sword.enchant(Enchantments.KNOCKBACK, 1);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, sword);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    private void performFireballAttack(LivingEntity target) {
        if (this.level().isClientSide) {
            return;
        }
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getEyeY();
        double dz = target.getZ() - this.getZ();
        AdeptFireball fireball = new AdeptFireball(RegistryHandler.ARCHBISHOP_FIREBALL.get(), this.level(), this, dx, dy, dz);

        this.level().addFreshEntity(fireball);
    }

    private void summonFangs(LivingEntity target) {
        if (this.level().isClientSide) {
            return;
        }

        int pattern = this.random.nextInt(3);

        switch (pattern) {
            case 0:
                summonFangLine(target);
                break;

            case 1:
                summonFangCircle(target);
                break;

            case 2:
                summonFangX(target);
                break;
        }
    }

    private void summonFangLine(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        float angle = (float) Math.atan2(dz, dx);

        for (int i = 1; i <= 5; i++) {
            double distance = i * 1.35D;
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;

            spawnFang(x, target.getY(), z, angle, i * 2);
        }
    }

    private void summonFangX(LivingEntity target) {
        double centerX = target.getX();
        double centerZ = target.getZ();
        double spacing = 1.35D;

        for (int i = -3; i <= 3; i++) {
            if (i == 0) {
                continue;
            }

            double offset = i * spacing;
            double x1 = centerX + offset;
            double z1 = centerZ + offset;
            double x2 = centerX + offset;
            double z2 = centerZ - offset;

            spawnFang(x1, target.getY(), z1, 0.0F, Math.abs(i) * 2);
            spawnFang(x2, target.getY(), z2, 0.0F, Math.abs(i) * 2);
        }
    }

    private void summonFangCircle(LivingEntity target) {
        double centerX = target.getX();
        double centerZ = target.getZ();
        double radius = 2.5D;

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0D * i) / 8.0D;
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;

            spawnFang(x,target.getY(),z,(float) angle, i * 2);
        }
    }

    private void spawnFang(double x, double y, double z, float angle, int warmup)
    {
        BlockPos pos = BlockPos.containing(x, y, z);

        if (!this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty())
        {
            pos = pos.above();
            y = pos.getY();
        }

        EvokerFangs fang = new EvokerFangs(this.level(),x,y,z,angle,warmup, this);
        this.level().addFreshEntity(fang);
    }


    private static class ArchbishopCombatGoal extends Goal {

        private final Archbishop archbishop;
        private LivingEntity target;

        private ArchbishopCombatGoal(Archbishop archbishop) {
            this.archbishop = archbishop;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.archbishop.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.archbishop.getTarget();
            return target != null && target.isAlive() && this.archbishop.distanceToSqr(target) < 32.0D * 32.0D;
        }

        @Override
        public void start() {
            this.target = this.archbishop.getTarget();
            this.archbishop.meleeCooldown = 0;
            this.archbishop.fireballCooldown = 30;
            this.archbishop.fangCooldown = 100;
            this.archbishop.strafeTime = 0;
        }

        @Override
        public void stop() {
            this.target = null;
            this.archbishop.getNavigation().stop();
            this.archbishop.getMoveControl().strafe(0.0F, 0.0F);
        }

        private void faceTarget(LivingEntity target) {
            Archbishop archbishop = this.archbishop;
            double dx = target.getX() - archbishop.getX();
            double dz = target.getZ() - archbishop.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            archbishop.setYRot(yaw);
            archbishop.setYBodyRot(yaw);
            archbishop.setYHeadRot(yaw);
        }

        @Override
        public void tick() {
            this.target = this.archbishop.getTarget();

            if (this.target == null || !this.target.isAlive()) {
                return;
            }

            Archbishop archbishop = this.archbishop;

            double distanceSqr = archbishop.distanceToSqr(this.target);

            this.faceTarget(this.target);

            if (archbishop.meleeCooldown > 0) {
                archbishop.meleeCooldown--;
            }

            if (archbishop.fireballCooldown > 0) {
                archbishop.fireballCooldown--;
            }

            if (archbishop.fangCooldown > 0) {
                archbishop.fangCooldown--;
            }

            if (archbishop.dashCooldown > 0) {
                archbishop.dashCooldown--;
            }

            if (archbishop.dashTicks > 0) {
                archbishop.dashTicks--;
                archbishop.setDeltaMovement(archbishop.dashVelocity);
                archbishop.hasImpulse = true;
                return;
            }

            this.updateLevitation(this.target);

            if (distanceSqr <= 2.8D * 2.8D) {
                archbishop.getNavigation().stop();
                Vec3 velocity = archbishop.getDeltaMovement();
                archbishop.setDeltaMovement(velocity.x * 0.5D, velocity.y, velocity.z * 0.5D);

                if (archbishop.meleeCooldown <= 0) {
                    archbishop.doHurtTarget(this.target);
                    archbishop.meleeCooldown = 20;
                }

                return;
            }

            if (distanceSqr > 4.0D * 4.0D && archbishop.dashCooldown <= 0) {
                this.performDash(this.target);
                return;
            }

            archbishop.strafeTime++;

            if (archbishop.strafeTime >= 35) {
                archbishop.strafeTime = 0;
                archbishop.strafeDirection = archbishop.random.nextBoolean() ? 1.0F : -1.0F;
            }

            if (archbishop.fireballCooldown <= 0 && distanceSqr > 5.0D * 5.0D) {
                archbishop.performFireballAttack(this.target);
                archbishop.fireballCooldown = 45 + archbishop.random.nextInt(26);
                archbishop.strafeDirection = archbishop.random.nextBoolean() ? 1.0F : -1.0F;
                archbishop.strafeTime = 0;
            }

            if (archbishop.fangCooldown <= 0 && distanceSqr <= 12.0D * 12.0D) {
                archbishop.summonFangs(this.target);
                archbishop.fangCooldown = 120 + archbishop.random.nextInt(81);
            }
        }

        private void performDash(LivingEntity target) {
            Archbishop archbishop = this.archbishop;
            double dx = target.getX() - archbishop.getX();
            double dy = target.getY(0.5D) - archbishop.getY(0.5D);
            double dz = target.getZ() - archbishop.getZ();
            Vec3 direction = new Vec3(dx, dy, dz).normalize();
            double verticalBoost = archbishop.random.nextBoolean() ? 0.65D : -0.35D;
            archbishop.dashVelocity = new Vec3(direction.x * 1.15D, direction.y * 0.45D + verticalBoost, direction.z * 1.15D);
            archbishop.dashTicks = 8;
            archbishop.dashCooldown = 65 + archbishop.random.nextInt(46);
            archbishop.setDeltaMovement(archbishop.dashVelocity);
            archbishop.hasImpulse = true;
        }

        private void updateLevitation(LivingEntity target) {
            Archbishop archbishop = this.archbishop;
            double dx = target.getX() - archbishop.getX();
            double dz = target.getZ() - archbishop.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance < 0.001D) {
                return;
            }

            double nx = dx / distance;
            double nz = dz / distance;
            double sideX = -nz * archbishop.strafeDirection;
            double sideZ = nx * archbishop.strafeDirection;
            double forwardSpeed = distance > 9.0D ? 0.18D : -0.04D;
            double strafeSpeed = 0.13D;
            double desiredX = nx * forwardSpeed + sideX * strafeSpeed;
            double desiredZ = nz * forwardSpeed + sideZ * strafeSpeed;
            double hoverHeight = 2.5D + Math.sin(archbishop.tickCount * 0.07D) * 0.6D;
            double desiredY = target.getY() + hoverHeight;
            double verticalDifference = desiredY - archbishop.getY();
            double verticalSpeed = Mth.clamp(verticalDifference * 0.08D, -0.18D, 0.18D);
            Vec3 velocity = archbishop.getDeltaMovement();
            archbishop.setDeltaMovement(velocity.x + (desiredX - velocity.x) * 0.12D, velocity.y + (verticalSpeed - velocity.y) * 0.12D, velocity.z + (desiredZ - velocity.z) * 0.12D);
            archbishop.hasImpulse = true;
        }
    }

    private static class FloatingWanderGoal extends Goal {

        private final Archbishop mob;
        private double targetX;
        private double targetY;
        private double targetZ;
        private int moveDuration;

        private FloatingWanderGoal(Archbishop mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() == null && this.mob.getNavigation().isDone() && this.mob.random.nextInt(50) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.getTarget() == null && this.moveDuration > 0 && this.mob.distanceToSqr(this.targetX, this.targetY, this.targetZ) > 1.5D;
        }

        @Override
        public void start() {
            this.moveDuration = 60 + this.mob.random.nextInt(60);
            this.chooseDestination();
        }

        private void chooseDestination() {
            double angle = this.mob.random.nextDouble() * Math.PI * 2.0D;
            double distance = 3.0D + this.mob.random.nextDouble() * 5.0D;
            this.targetX = this.mob.getX() + Math.cos(angle) * distance;
            this.targetZ = this.mob.getZ() + Math.sin(angle) * distance;
            this.targetY = this.mob.getY() + (this.mob.random.nextDouble() * 3.0D - 1.0D);
            this.targetY = Mth.clamp(this.targetY, this.mob.level().getMinBuildHeight() + 2, this.mob.level().getMaxBuildHeight() - 3);
        }

        @Override
        public void tick() {
            this.moveDuration--;
            double dx = this.targetX - this.mob.getX();
            double dy = this.targetY - this.mob.getY();
            double dz = this.targetZ - this.mob.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance < 1.5D || this.moveDuration <= 0) {
                this.chooseDestination();
                return;
            }

            double speed = 0.12D;

            Vec3 desiredVelocity = new Vec3(dx / distance * speed, dy / distance * speed, dz / distance * speed);
            double bobbing = Math.sin(this.mob.tickCount * 0.08D) * 0.025D;
            Vec3 currentVelocity = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(currentVelocity.x + (desiredVelocity.x - currentVelocity.x) * 0.08D, currentVelocity.y + (desiredVelocity.y + bobbing - currentVelocity.y) * 0.08D, currentVelocity.z + (desiredVelocity.z - currentVelocity.z) * 0.08D);
            this.mob.hasImpulse = true;

            if (Math.abs(dx) > 0.1D || Math.abs(dz) > 0.1D) {
                float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                this.mob.setYRot(yaw);
                this.mob.setYBodyRot(yaw);
                this.mob.setYHeadRot(yaw);
            }
        }

        @Override
        public void stop() {
            this.moveDuration = 0;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}