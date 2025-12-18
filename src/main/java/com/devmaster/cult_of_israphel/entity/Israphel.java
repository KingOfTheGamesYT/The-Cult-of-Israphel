package com.devmaster.cult_of_israphel.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Israphel extends Monster implements RangedAttackMob {

    private static final double MELEE_RANGE = 6.0D;

    public Israphel(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (target != null) {
            double distance = this.distanceTo(target);

            if (distance <= MELEE_RANGE) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }

            if (this.random.nextInt(20) == 0) {
                teleportTowards(target);
            }
        }
    }

    private void teleportTowards(LivingEntity target) {
        RandomSource rand = this.random;
        double x = target.getX() + (rand.nextDouble() - 0.5D) * 8.0D;
        double y = target.getY();
        double z = target.getZ() + (rand.nextDouble() - 0.5D) * 8.0D;
        attemptTeleport(x, y, z);
    }

    protected boolean attemptTeleport(double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        while (pos.getY() > this.level().getMinBuildHeight()
                && !this.level().getBlockState(pos).blocksMotion()) {
            pos.move(Direction.DOWN);
        }

        BlockState state = this.level().getBlockState(pos);
        boolean success = state.blocksMotion()
                && !this.level().getBlockState(pos.above()).blocksMotion();

        if (success) {
            this.teleportTo(x, pos.getY() + 1, z);
            this.level().playSound(null, this.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    this.getSoundSource(), 1.0F, 1.0F);
        }
        return success;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack bow = this.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) return;

        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, bow, velocity);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        arrow.shoot(dx, dy, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
        this.level().addFreshEntity(arrow);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_PROJECTILE);
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }


    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }
}