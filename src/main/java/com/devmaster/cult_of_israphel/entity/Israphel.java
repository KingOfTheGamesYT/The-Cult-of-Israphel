package com.devmaster.cult_of_israphel.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import javax.annotation.Nullable;

public class Israphel extends Monster implements RangedAttackMob {

    private static final double MELEE_RANGE = 6.0D;
    private int teleportCooldown = 0;

    private ItemStack sword = ItemStack.EMPTY;
    private ItemStack bow = ItemStack.EMPTY;

    public Israphel(EntityType<? extends Monster> type, Level level) {
        super(type, level);

        this.setCanPickUpLoot(false);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WeaponSwapGoal(this));
        this.goalSelector.addGoal(3, new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();

        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        if (target != null && teleportCooldown == 0) {
            if (this.random.nextInt(60) == 0) {
                teleportTowards(target);
                teleportCooldown = 100;
            }
        }
    }

    //(enderman like)
    private void teleportTowards(LivingEntity target) {

        double x = target.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
        double y = target.getY();
        double z = target.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;

        BlockPos pos = BlockPos.containing(x, y, z);

        while (pos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(pos).blocksMotion()) {
            pos = pos.below();
        }

        BlockState state = this.level().getBlockState(pos);

        if (state.blocksMotion() && !this.level().getBlockState(pos.above()).blocksMotion()) {
            this.teleportTo(x, pos.getY() + 1, z);
            this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    private void initWeapons() {

        if (!this.sword.isEmpty() && !this.bow.isEmpty()) {
            return;
        }

        if (this.sword.isEmpty()) {

            this.sword = new ItemStack(Items.GOLDEN_SWORD);

            if (this.random.nextFloat() < 0.35F) {
                this.sword.enchant(Enchantments.KNOCKBACK, 1);
            }
        }

        if (this.bow.isEmpty()) {

            this.bow = new ItemStack(Items.BOW);

            if (this.random.nextFloat() < 0.35F) {
                this.bow.enchant(Enchantments.POWER_ARROWS, 1);
            }
        }
    }

    private ItemStack getDesiredWeapon() {

        LivingEntity target = this.getTarget();

        if (target == null) {
            return this.bow;
        }

        double dist = this.distanceTo(target);

        return dist <= MELEE_RANGE ? this.sword : this.bow;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {

        ItemStack bow = this.getMainHandItem();

        if (!(bow.getItem() instanceof BowItem)) {
            return;
        }

        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, bow, velocity);

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();

        arrow.shoot(dx, dy, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);

        this.level().addFreshEntity(arrow);
    }

    private static class WeaponSwapGoal extends Goal {

        private final Israphel mob;

        public WeaponSwapGoal(Israphel mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return this.mob.getTarget() != null;
        }

        @Override
        public void tick() {

            this.mob.initWeapons();

            ItemStack desired = this.mob.getDesiredWeapon();
            ItemStack current = this.mob.getMainHandItem();

            if (current.getItem() != desired.getItem()) {
                this.mob.setItemSlot(EquipmentSlot.MAINHAND, desired);
            }
        }
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {

        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);

        this.initWeapons();
        this.setItemSlot(EquipmentSlot.MAINHAND, this.bow);

        return spawnData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {

        super.addAdditionalSaveData(tag);

        if (!this.sword.isEmpty()) {
            tag.put("IsraphelSword", this.sword.save(new CompoundTag()));
        }

        if (!this.bow.isEmpty()) {
            tag.put("IsraphelBow", this.bow.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {

        super.readAdditionalSaveData(tag);

        if (tag.contains("IsraphelSword")) {
            this.sword = ItemStack.of(tag.getCompound("IsraphelSword"));
        }

        if (tag.contains("IsraphelBow")) {
            this.bow = ItemStack.of(tag.getCompound("IsraphelBow"));
        }
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType)
    {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }
}