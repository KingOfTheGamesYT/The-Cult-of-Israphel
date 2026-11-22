package com.devmaster.cult_of_israphel.entity;

import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
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

import java.util.EnumSet;

public class Adept extends Monster {

    private int meleeCooldown;
    private int fireballCooldown;
    private int fangCooldown;
    private int strafeTime;
    private float strafeDirection = 1.0F;

    public Adept(EntityType<? extends Adept> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new AdeptCombatGoal(this));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8D));
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
        AdeptFireball fireball = new AdeptFireball(RegistryHandler.ADEPT_FIREBALL.get(), this.level(), this, dx, dy, dz);

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

    private static class AdeptCombatGoal extends Goal {

        private final Adept adept;
        private LivingEntity target;

        private AdeptCombatGoal(Adept adept) {
            this.adept = adept;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.adept.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.adept.getTarget();
            return target != null && target.isAlive() && this.adept.distanceToSqr(target) < 32.0D * 32.0D;
        }

        @Override
        public void start() {
            this.target = this.adept.getTarget();
            this.adept.meleeCooldown = 0;
            this.adept.fireballCooldown = 30;
            this.adept.fangCooldown = 100;
            this.adept.strafeTime = 0;
        }

        @Override
        public void stop() {
            this.target = null;
            this.adept.getNavigation().stop();
            this.adept.getMoveControl().strafe(0.0F, 0.0F);
        }

        private void faceTarget(LivingEntity target) {
            Adept adept = this.adept;
            double dx = target.getX() - adept.getX();
            double dz = target.getZ() - adept.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            adept.setYRot(yaw);
            adept.setYBodyRot(yaw);
            adept.setYHeadRot(yaw);
        }

        @Override
        public void tick() {
            this.target = this.adept.getTarget();

            if (this.target == null || !this.target.isAlive()) {
                return;
            }

            Adept adept = this.adept;
            double distanceSqr = adept.distanceToSqr(this.target);
            this.faceTarget(this.target);

            if (adept.meleeCooldown > 0) {
                adept.meleeCooldown--;
            }

            if (adept.fireballCooldown > 0) {
                adept.fireballCooldown--;
            }

            if (adept.fangCooldown > 0) {
                adept.fangCooldown--;
            }

            if (distanceSqr <= 2.8D * 2.8D) {
                adept.getNavigation().stop();
                adept.getMoveControl().strafe(0.0F, 0.0F);

                if (adept.meleeCooldown <= 0) {
                    adept.doHurtTarget(this.target);
                    adept.meleeCooldown = 20;
                }

                return;
            }

            if (distanceSqr > 5.0D * 5.0D) {
                adept.getNavigation().stop();

                adept.strafeTime++;

                if (adept.strafeTime >= 20) {
                    adept.strafeTime = 0;
                    adept.strafeDirection = adept.random.nextBoolean() ? 1.0F : -1.0F;
                }

                adept.getMoveControl().strafe(distanceSqr > 12.0D * 12.0D ? 0.0F : 0.15F, adept.strafeDirection * 0.65F);

                if (adept.fireballCooldown <= 0) {
                    adept.performFireballAttack(this.target);
                    adept.fireballCooldown = 45 + adept.random.nextInt(26);

                    adept.strafeDirection = adept.random.nextBoolean() ? 1.0F : -1.0F;
                    adept.strafeTime = 0;
                }
            } else {
                adept.getMoveControl().strafe(0.0F, 0.0F);
                adept.getNavigation().moveTo(this.target, 1.15D);
            }

            if (adept.fangCooldown <= 0 && distanceSqr <= 12.0D * 12.0D) {
                adept.summonFangs(this.target);
                adept.fangCooldown = 120 + adept.random.nextInt(81);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITCH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WITCH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITCH_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.9F;
    }
}