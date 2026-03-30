package com.cockatielmod.cockatiel.entity;

import com.cockatielmod.cockatiel.CockatielConfig;
import com.cockatielmod.cockatiel.entity.ai.*;
import com.cockatielmod.cockatiel.registry.ModItems;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CockatielEntity extends Animal implements FlyingAnimal {

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_SINGING =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ALERTING =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(CockatielEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final String TAG_VARIANT = "Variant";
    private static final String TAG_LAST_FED = "LastFedTime";
    private static final String TAG_FEATHERS_GIVEN = "FeathersGiven";
    private static final String TAG_MILLET_COUNT = "MilletCount";
    private static final String TAG_OWNER = "OwnerUUID";
    private static final String TAG_IS_TAMED = "IsTamed";

    private long lastFedTime = 0;
    private int feathersGivenCount = 0;
    private int milletFedCount = 0;
    private boolean isTamed = false;
    private int singTimer = 0;
    private int alertCooldown = 0;
    private int cropStealCooldown = 0;

    public static final int VARIANT_GREY = 0;
    public static final int VARIANT_LUTINO = 1;
    public static final int VARIANT_ALBINO = 2;

    private static final int MILLET_FOR_POTION = 10;
    private static final int FEATHERS_THRESHOLD = 5;

    public CockatielEntity(EntityType<? extends CockatielEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, VARIANT_GREY);
        builder.define(IS_SINGING, false);
        builder.define(IS_ALERTING, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new CockatielFollowOwnerGoal(this, 1.2, 6.0f, 2.0f));
        this.goalSelector.addGoal(4, new CockatielSitOnPerchGoal(this));
        this.goalSelector.addGoal(5, new CockatielStealCropsGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) serverTick();
    }

    private void serverTick() {
        long currentTime = this.level().getGameTime();
        long dayTime = this.level().getDayTime() % 24000;

        if (dayTime >= 0 && dayTime < 200 && !isSinging()) startSinging();

        if (isSinging()) {
            singTimer++;
            if (singTimer >= CockatielConfig.SONG_DURATION_TICKS.get()) stopSinging();
        }

        if (alertCooldown > 0) alertCooldown--;
        if (alertCooldown == 0) checkForCreepers();

        if (cropStealCooldown > 0) cropStealCooldown--;
        long ticksSinceFed = lastFedTime > 0 ? currentTime - lastFedTime : Long.MAX_VALUE;
        if (ticksSinceFed > CockatielConfig.HUNGER_THRESHOLD_TICKS.get()
                && cropStealCooldown == 0
                && this.random.nextDouble() < CockatielConfig.CROP_STEAL_CHANCE.get() / 400.0) {
            trySealCrops();
        }
    }

    private void checkForCreepers() {
        int range = CockatielConfig.CREEPER_ALERT_RANGE.get();
        List<Creeper> creepers = this.level().getEntitiesOfClass(Creeper.class,
                new AABB(this.blockPosition()).inflate(range));
        if (!creepers.isEmpty()) {
            this.entityData.set(IS_ALERTING, true);
            this.playSound(ModSounds.COCKATIEL_ALERT.get(), 1.5f, 1.0f);
            this.level().getEntitiesOfClass(Player.class, new AABB(this.blockPosition()).inflate(range + 4))
                    .forEach(p -> p.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.cockatiel.creeper_alert"), true));
            alertCooldown = 60;
        } else {
            this.entityData.set(IS_ALERTING, false);
            alertCooldown = 20;
        }
    }

    private void trySealCrops() {
        BlockPos pos = this.blockPosition();
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockState state = this.level().getBlockState(checkPos);
                    if (isRipeCrop(state)) {
                        this.level().destroyBlock(checkPos, true);
                        this.playSound(ModSounds.COCKATIEL_EAT.get(), 1.0f, 1.0f);
                        cropStealCooldown = 400;
                        this.level().getEntitiesOfClass(Player.class, new AABB(this.blockPosition()).inflate(20))
                                .forEach(p -> p.displayClientMessage(
                                        net.minecraft.network.chat.Component.translatable("message.cockatiel.stole_crops"), true));
                        return;
                    }
                }
            }
        }
    }

    private boolean isRipeCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock cropBlock) {
            return state.getValue(CropBlock.AGE) == cropBlock.getMaxAge();
        }
        if (state.is(Blocks.BEETROOTS)) {
            return state.getValue(BeetrootBlock.AGE) == 3;
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.MILLET_BUNCH.get()) || stack.is(ModItems.MILLET_SEEDS.get()))
            return feedMillet(player, stack);
        if (stack.is(ModItems.COCKATIEL_TREAT.get()))
            return feedTreat(player, stack);
        if (stack.isEmpty() && isTamed && getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false))
            return petCockatiel(player);
        return super.mobInteract(player, hand);
    }

    private InteractionResult feedMillet(Player player, ItemStack stack) {
        lastFedTime = this.level().getGameTime();
        milletFedCount++;
        this.playSound(ModSounds.COCKATIEL_EAT.get(), 1.0f, 1.0f);
        this.heal(2.0f);

        if (!isTamed && this.random.nextFloat() < 0.3f) {
            isTamed = true;
            this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
            this.playSound(SoundEvents.NOTE_BLOCK_FLUTE.value(), 1.0f, 1.5f);
            if (!this.level().isClientSide)
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HEART,
                        getX(), getY() + 1, getZ(), 5, 0.3, 0.3, 0.3, 0.0);
        }
        if (this.random.nextFloat() < 0.2f) spawnAtLocation(getVariantFeather());
        if (milletFedCount >= MILLET_FOR_POTION) {
            milletFedCount = 0;
            spawnAtLocation(new ItemStack(ModItems.COCKATIEL_POTION.get()));
            this.playSound(ModSounds.COCKATIEL_HAPPY.get(), 1.0f, 1.2f);
            if (!this.level().isClientSide)
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        getX(), getY() + 1, getZ(), 8, 0.5, 0.5, 0.5, 0.0);
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult feedTreat(Player player, ItemStack stack) {
        lastFedTime = this.level().getGameTime();
        this.playSound(ModSounds.COCKATIEL_EAT.get(), 1.0f, 1.2f);
        this.heal(4.0f);
        if (!this.level().isClientSide)
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.HEART,
                    getX(), getY() + 1, getZ(), 3, 0.3, 0.3, 0.3, 0.0);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private InteractionResult petCockatiel(Player player) {
        this.playSound(ModSounds.COCKATIEL_HAPPY.get(), 0.8f, 1.0f + this.random.nextFloat() * 0.3f);
        if (++feathersGivenCount % FEATHERS_THRESHOLD == 0) spawnAtLocation(getVariantFeather());
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private ItemStack getVariantFeather() {
        return switch (getVariant()) {
            case VARIANT_LUTINO -> new ItemStack(ModItems.YELLOW_COCKATIEL_FEATHER.get());
            case VARIANT_ALBINO -> new ItemStack(ModItems.WHITE_COCKATIEL_FEATHER.get());
            default -> new ItemStack(ModItems.COCKATIEL_FEATHER.get());
        };
    }

    private void startSinging() {
        this.entityData.set(IS_SINGING, true);
        singTimer = 0;
        this.playSound(ModSounds.COCKATIEL_SONG.get(), 1.0f, 1.0f);
    }

    private void stopSinging() {
        this.entityData.set(IS_SINGING, false);
        singTimer = 0;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.MILLET_SEEDS.get()) || stack.is(ModItems.MILLET_BUNCH.get());
    }

    @Override
    @Nullable
    public CockatielEntity getBreedOffspring(ServerLevel level, AgeableMob other) {
        CockatielEntity baby = new CockatielEntity(
                com.cockatielmod.cockatiel.registry.ModEntities.COCKATIEL.get(), level);
        int variant = this.random.nextBoolean() ? getVariant() : ((CockatielEntity) other).getVariant();
        baby.setVariant(this.random.nextFloat() < 0.1f ? this.random.nextInt(3) : variant);
        return baby;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        setVariant(this.random.nextInt(3));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override protected SoundEvent getAmbientSound() { return ModSounds.COCKATIEL_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.COCKATIEL_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.COCKATIEL_DEATH.get(); }
    @Override protected float getSoundVolume() { return 0.7f; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(TAG_VARIANT, getVariant());
        tag.putLong(TAG_LAST_FED, lastFedTime);
        tag.putInt(TAG_FEATHERS_GIVEN, feathersGivenCount);
        tag.putInt(TAG_MILLET_COUNT, milletFedCount);
        tag.putBoolean(TAG_IS_TAMED, isTamed);
        getOwnerUUID().ifPresent(uuid -> tag.putUUID(TAG_OWNER, uuid));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt(TAG_VARIANT));
        lastFedTime = tag.getLong(TAG_LAST_FED);
        feathersGivenCount = tag.getInt(TAG_FEATHERS_GIVEN);
        milletFedCount = tag.getInt(TAG_MILLET_COUNT);
        isTamed = tag.getBoolean(TAG_IS_TAMED);
        if (tag.hasUUID(TAG_OWNER)) entityData.set(OWNER_UUID, Optional.of(tag.getUUID(TAG_OWNER)));
    }

    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int v) { entityData.set(VARIANT, v); }
    public boolean isSinging() { return entityData.get(IS_SINGING); }
    public boolean isAlerting() { return entityData.get(IS_ALERTING); }
    public boolean isTamed() { return isTamed; }
    public Optional<UUID> getOwnerUUID() { return entityData.get(OWNER_UUID); }
    public long getLastFedTime() { return lastFedTime; }
    public void teleportToOwner(Player owner) { teleportTo(owner.getX(), owner.getY(), owner.getZ()); }

    @Override public boolean isFlying() { return !this.onGround(); }
    }
}
