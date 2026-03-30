package com.cockatielmod.cockatiel.blockentity;

import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModBlockEntities;
import com.cockatielmod.cockatiel.registry.ModEntities;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CockatielCageBlockEntity extends BlockEntity {

    private static final String TAG_COCKATIEL_NBT = "StoredCockatielData";
    private static final String TAG_HAS_BIRD = "HasBird";

    @Nullable
    private CompoundTag storedCockatielNBT = null;
    private boolean hasCockatiel = false;

    // Ambient sound timer
    private int soundTimer = 0;

    public CockatielCageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COCKATIEL_CAGE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CockatielCageBlockEntity entity) {
        if (!level.isClientSide && entity.hasCockatiel) {
            entity.soundTimer++;
            // Play ambient sound every ~10 seconds randomly
            if (entity.soundTimer > 200 && level.random.nextInt(100) == 0) {
                level.playSound(null, pos, ModSounds.COCKATIEL_AMBIENT.get(), SoundSource.NEUTRAL, 0.6f, 1.0f);
                entity.soundTimer = 0;
            }

            // Morning song trigger
            long dayTime = level.getDayTime() % 24000;
            if (dayTime >= 0 && dayTime < 50 && level.random.nextInt(20) == 0) {
                level.playSound(null, pos, ModSounds.COCKATIEL_SONG.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
    }

    public void storeCockatiel(CockatielEntity cockatiel) {
        storedCockatielNBT = new CompoundTag();
        cockatiel.save(storedCockatielNBT);
        hasCockatiel = true;
        cockatiel.discard();
        setChanged();
    }

    public void releaseCockatiel(Level level, BlockPos pos) {
        if (storedCockatielNBT != null && level instanceof ServerLevel serverLevel) {
            CockatielEntity bird = ModEntities.COCKATIEL.get().create(serverLevel);
            if (bird != null) {
                bird.load(storedCockatielNBT);
                bird.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                serverLevel.addFreshEntity(bird);
            }
            storedCockatielNBT = null;
            hasCockatiel = false;
            setChanged();

            // Update block state
            BlockState state = level.getBlockState(pos);
            level.setBlock(pos, state.setValue(
                    com.cockatielmod.cockatiel.block.CockatielCageBlock.OCCUPIED, false), 3);
        }
    }

    public boolean hasCockatiel() {
        return hasCockatiel;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean(TAG_HAS_BIRD, hasCockatiel);
        if (storedCockatielNBT != null) {
            tag.put(TAG_COCKATIEL_NBT, storedCockatielNBT);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        hasCockatiel = tag.getBoolean(TAG_HAS_BIRD);
        if (tag.contains(TAG_COCKATIEL_NBT)) {
            storedCockatielNBT = tag.getCompound(TAG_COCKATIEL_NBT);
        }
    }
}
