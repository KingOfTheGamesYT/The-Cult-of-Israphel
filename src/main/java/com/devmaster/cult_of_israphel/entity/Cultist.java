package com.devmaster.cult_of_israphel.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

public class Cultist extends Monster implements RangedAttackMob {

    private static final double MELEE_RANGE = 6.0D;

    private ItemStack sword = ItemStack.EMPTY;
    private ItemStack bow = ItemStack.EMPTY;

    public Cultist(EntityType<? extends Monster> type, Level level) {
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
        this.goalSelector.addGoal(5, new AdeptPatrolFollowGoal(this, 1.15D, 8.0F, 4.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
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

        private final Cultist mob;

        public WeaponSwapGoal(Cultist mob) {
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

    private static class AdeptPatrolFollowGoal extends Goal {

        private final Cultist mob;
        private final double speedModifier;
        private final float startDistance;
        private final float stopDistance;
        private Mob leader;

        public AdeptPatrolFollowGoal(Cultist mob, double speedModifier, float startDistance, float stopDistance)
        {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.startDistance = startDistance;
            this.stopDistance = stopDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {

            if (!this.mob.getPersistentData().hasUUID("AdeptPatrolLeaderUUID")) {
                return false;
            }

            UUID leaderUUID = this.mob.getPersistentData().getUUID("AdeptPatrolLeaderUUID");

            if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
                return false;
            }

            Entity entity = serverLevel.getEntity(leaderUUID);

            if (!(entity instanceof Mob mobLeader) || !mobLeader.isAlive()) {
                return false;
            }

            this.leader = mobLeader;

            return this.mob.distanceToSqr(this.leader) > this.startDistance * this.startDistance;
        }

        @Override
        public boolean canContinueToUse() {

            return this.leader != null && this.leader.isAlive() && this.mob.distanceToSqr(this.leader) > this.stopDistance * this.stopDistance;
        }

        @Override
        public void tick() {

            if (this.leader == null) {
                return;
            }

            if (this.mob.getNavigation().isDone()) {
                this.mob.getNavigation().moveTo(this.leader, this.speedModifier);
            }
        }

        @Override
        public void stop() {

            this.leader = null;
            this.mob.getNavigation().stop();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {

        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
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
            tag.put("CultistSword", this.sword.save(new CompoundTag()));
        }

        if (!this.bow.isEmpty()) {
            tag.put("CultistBow", this.bow.save(new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {

        super.readAdditionalSaveData(tag);

        if (tag.contains("CultistSword")) {
            this.sword = ItemStack.of(tag.getCompound("CultistSword"));
        }

        if (tag.contains("CultistBow")) {
            this.bow = ItemStack.of(tag.getCompound("CultistBow"));
        }
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType)
    {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
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