package com.cockatielmod.cockatiel.entity;

import com.cockatielmod.cockatiel.CockatielConfig;
import com.cockatielmod.cockatiel.entity.ai.CockatielFollowOwnerGoal;
import com.cockatielmod.cockatiel.entity.ai.CockatielSitOnPerchGoal;
import com.cockatielmod.cockatiel.entity.ai.CockatielStealCropsGoal;
import com.cockatielmod.cockatiel.registry.ModEntities;
import com.cockatielmod.cockatiel.registry.ModItems;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CockatielEntity extends Animal implements FlyingAnimal, GeoEntity {

    // ── GeckoLib ─────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final RawAnimation IDLE   = RawAnimation.begin().thenLoop("animation.cockatiel.idle");
    private static final RawAnimation WALK   = RawAnimation.begin().thenLoop("animation.cockatiel.walk");
    private static final RawAnimation FLY    = RawAnimation.begin().thenLoop("animation.cockatiel.fly");
    private static final RawAnimation SING   = RawAnimation.begin().thenLoop("animation.cockatiel.sing");
    private static final RawAnimation ALERT  = RawAnimation.begin().thenLoop("animation.cockatiel.alert");
    private static final RawAnimation EAT    = RawAnimation.begin().thenPlayAndHold("animation.cockatiel.eat");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, state -> {
            if (isAlerting())        return state.setAndContinue(ALERT);
            if (isSinging())         return state.setAndContinue(SING);
            if (isFlying())          return state.setAndContinue(FLY);
            if (state.isMoving())    return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
        controllers.add(new AnimationController<>(this, "eat", 2, state ->
                PlayState.STOP));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }

    // ── Synced data ──────────────────────────────────────────────────────────
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SINGING =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ALERTING =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> IS_TAMED =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int VARIANT_GREY   = 0;
    public static final int VARIANT_LUTINO = 1;
    public static final int VARIANT_ALBINO = 2;

    // ── Server-side state ────────────────────────────────────────────────────
    private long lastFedTime   = 0;
    private int milletFedCount = 0;
    private int featherPetCount = 0;
    private int singTimer      = 0;
    private int alertCooldown  = 0;
    private int cropCooldown   = 0;

    private static final int MILLET_FOR_POTION  = 10;
    private static final int PETS_FOR_FEATHER   = 5;

    public CockatielEntity(EntityType<? extends CockatielEntity> type, Level level) {
        super(type, level);
    }

    // ── Attributes ───────────────────────────────────────────────────────────
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    // ── Synced data init ─────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT,    VARIANT_GREY);
        builder.define(IS_SINGING, false);
        builder.define(IS_ALERTING,false);
        builder.define(IS_TAMED,   false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    // ── Goals ────────────────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new CockatielFollowOwnerGoal(this, 1.2f, 8.0f, 3.0f));
        goalSelector.addGoal(4, new CockatielSitOnPerchGoal(this));
        goalSelector.addGoal(5, new CockatielStealCropsGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // ── Tick ─────────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) serverTick();
    }

    private void serverTick() {
        long dayTime = level().getDayTime() % 24000;

        // Morning song (dawn ~0-200)
        if (dayTime < 200 && !isSinging()) startSinging();
        if (isSinging()) {
            if (++singTimer >= CockatielConfig.SONG_DURATION_TICKS.get()) stopSinging();
        }

        // Creeper check
        if (--alertCooldown <= 0) {
            alertCooldown = 20;
            checkCreepers();
        }

        // Crop steal
        if (--cropCooldown <= 0 && isHungry() && random.nextDouble() < 0.002) {
            trySteelCrop();
        }
    }

    private boolean isHungry() {
        if (lastFedTime == 0) return false;
        return level().getGameTime() - lastFedTime > CockatielConfig.HUNGER_THRESHOLD_TICKS.get();
    }

    private void checkCreepers() {
        int range = CockatielConfig.CREEPER_ALERT_RANGE.get();
        boolean creeperNear = !level().getEntitiesOfClass(Creeper.class,
                new AABB(blockPosition()).inflate(range)).isEmpty();

        entityData.set(IS_ALERTING, creeperNear);
        if (creeperNear) {
            playSound(ModSounds.COCKATIEL_ALERT.get(), 1.5f, 1.0f);
            level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(range + 6))
                    .forEach(p -> p.displayClientMessage(
                            Component.translatable("message.cockatiel.creeper_alert"), true));
        }
    }

    private void trySteelCrop() {
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    var pos = blockPosition().offset(dx, dy, dz);
                    if (isRipeCrop(level().getBlockState(pos))) {
                        level().destroyBlock(pos, true);
                        cropCooldown = 600;
                        playSound(ModSounds.COCKATIEL_EAT.get(), 1.0f, 1.2f);
                        level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(24))
                                .forEach(p -> p.displayClientMessage(
                                        Component.translatable("message.cockatiel.stole_crops"), true));
                        return;
                    }
                }
            }
        }
    }

    private boolean isRipeCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock cb)
            return state.getValue(CropBlock.AGE) == cb.getMaxAge();
        if (state.is(Blocks.BEETROOTS))
            return state.getValue(BeetrootBlock.AGE) == 3;
        return false;
    }

    // ── Interaction ──────────────────────────────────────────────────────────
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(ModItems.MILLET_SEEDS.get()) || stack.is(ModItems.MILLET_BUNCH.get()))
            return onFeedMillet(player, stack);

        if (stack.is(ModItems.COCKATIEL_TREAT.get()))
            return onFeedTreat(player, stack);

        // Pet with empty hand if tamed and owner
        if (stack.isEmpty() && isTamed() && isOwner(player))
            return onPet(player);

        return super.mobInteract(player, hand);
    }

    private InteractionResult onFeedMillet(Player player, ItemStack stack) {
        lastFedTime = level().getGameTime();
        milletFedCount++;
        playSound(ModSounds.COCKATIEL_EAT.get(), 1.0f, 1.1f);
        heal(2.0f);

        // Try to tame
        if (!isTamed() && random.nextFloat() < 0.25f) {
            entityData.set(IS_TAMED, true);
            entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
            playSound(SoundEvents.NOTE_BLOCK_FLUTE.value(), 1.0f, 1.5f);
            spawnHearts(8);
        } else if (!isTamed()) {
            spawnParticle(ParticleTypes.SMOKE);
        }

        // Drop feather occasionally
        if (random.nextFloat() < 0.15f) spawnAtLocation(variantFeather());

        // Give potion after enough millet
        if (milletFedCount >= MILLET_FOR_POTION) {
            milletFedCount = 0;
            spawnAtLocation(new ItemStack(ModItems.COCKATIEL_POTION.get()));
            playSound(ModSounds.COCKATIEL_HAPPY.get(), 1.0f, 1.2f);
            spawnHearts(12);
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private InteractionResult onFeedTreat(Player player, ItemStack stack) {
        lastFedTime = level().getGameTime();
        playSound(ModSounds.COCKATIEL_EAT.get(), 0.9f, 1.3f);
        heal(4.0f);
        spawnHearts(4);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private InteractionResult onPet(Player player) {
        playSound(ModSounds.COCKATIEL_HAPPY.get(), 0.8f, 0.9f + random.nextFloat() * 0.3f);
        if (++featherPetCount % PETS_FOR_FEATHER == 0) spawnAtLocation(variantFeather());
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void spawnHearts(int count) {
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(ParticleTypes.HEART, getX(), getY() + 1, getZ(),
                    count, 0.4, 0.4, 0.4, 0.0);
    }

    private void spawnParticle(net.minecraft.core.particles.SimpleParticleType type) {
        if (level() instanceof ServerLevel sl)
            sl.sendParticles(type, getX(), getY() + 0.5, getZ(), 3, 0.2, 0.2, 0.2, 0.0);
    }

    // ── Breeding ─────────────────────────────────────────────────────────────
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.MILLET_SEEDS.get()) || stack.is(ModItems.MILLET_BUNCH.get());
    }

    @Override @Nullable
    public CockatielEntity getBreedOffspring(ServerLevel level, AgeableMob other) {
        var baby = new CockatielEntity(ModEntities.COCKATIEL.get(), level);
        int pVariant = random.nextBoolean() ? getVariant() : ((CockatielEntity) other).getVariant();
        baby.setVariant(random.nextFloat() < 0.1f ? random.nextInt(3) : pVariant);
        return baby;
    }

    // ── Spawn ────────────────────────────────────────────────────────────────
    @Override @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        setVariant(random.nextInt(3));
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private ItemStack variantFeather() {
        return new ItemStack(switch (getVariant()) {
            case VARIANT_LUTINO -> ModItems.YELLOW_COCKATIEL_FEATHER.get();
            case VARIANT_ALBINO -> ModItems.WHITE_COCKATIEL_FEATHER.get();
            default             -> ModItems.COCKATIEL_FEATHER.get();
        });
    }

    private boolean isOwner(Player player) {
        return getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false);
    }

    public void teleportToOwner(Player owner) {
        teleportTo(owner.getX(), owner.getY(), owner.getZ());
    }

    private void startSinging() {
        entityData.set(IS_SINGING, true);
        singTimer = 0;
        playSound(ModSounds.COCKATIEL_SONG.get(), 0.9f, 1.0f);
    }

    private void stopSinging() {
        entityData.set(IS_SINGING, false);
        singTimer = 0;
    }

    // ── Sounds ───────────────────────────────────────────────────────────────
    @Override protected SoundEvent getAmbientSound() { return ModSounds.COCKATIEL_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.COCKATIEL_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.COCKATIEL_DEATH.get(); }
    @Override protected float getSoundVolume() { return 0.65f; }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant",    getVariant());
        tag.putLong("LastFed",   lastFedTime);
        tag.putInt("MilletCount",milletFedCount);
        tag.putInt("PetCount",   featherPetCount);
        tag.putBoolean("Tamed",  isTamed());
        getOwnerUUID().ifPresent(u -> tag.putUUID("Owner", u));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        lastFedTime    = tag.getLong("LastFed");
        milletFedCount = tag.getInt("MilletCount");
        featherPetCount= tag.getInt("PetCount");
        entityData.set(IS_TAMED, tag.getBoolean("Tamed"));
        if (tag.hasUUID("Owner")) entityData.set(OWNER_UUID, Optional.of(tag.getUUID("Owner")));
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int getVariant()        { return entityData.get(VARIANT); }
    public void setVariant(int v)  { entityData.set(VARIANT, v); }
    public boolean isSinging()     { return entityData.get(IS_SINGING); }
    public boolean isAlerting()    { return entityData.get(IS_ALERTING); }
    public boolean isTamed()       { return entityData.get(IS_TAMED); }
    public Optional<UUID> getOwnerUUID() { return entityData.get(OWNER_UUID); }
    public long getLastFedTime()   { return lastFedTime; }

    @Override public boolean isFlying() { return !onGround(); }
}
