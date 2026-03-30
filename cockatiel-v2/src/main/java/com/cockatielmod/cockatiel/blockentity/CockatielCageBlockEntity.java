package com.cockatielmod.cockatiel.blockentity;

import com.cockatielmod.cockatiel.block.CockatielCageBlock;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModBlockEntities;
import com.cockatielmod.cockatiel.registry.ModEntities;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CockatielCageBlockEntity extends BlockEntity {

    @Nullable private CompoundTag storedBirdNbt = null;
    private boolean hasBird = false;
    private int soundTimer  = 0;

    public CockatielCageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COCKATIEL_CAGE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CockatielCageBlockEntity be) {
        if (level.isClientSide || !be.hasBird) return;
        be.soundTimer++;
        // Ambient chirp every ~15 s randomly
        if (be.soundTimer > 300 && level.random.nextInt(60) == 0) {
            level.playSound(null, pos, ModSounds.COCKATIEL_AMBIENT.get(),
                    SoundSource.NEUTRAL, 0.5f, 0.9f + level.random.nextFloat() * 0.2f);
            be.soundTimer = 0;
        }
        // Morning song
        long dayTime = level.getDayTime() % 24000;
        if (dayTime < 100 && level.random.nextInt(40) == 0)
            level.playSound(null, pos, ModSounds.COCKATIEL_SONG.get(), SoundSource.NEUTRAL, 0.9f, 1.0f);
    }

    public void storeCockatiel(CockatielEntity bird) {
        storedBirdNbt = new CompoundTag();
        bird.save(storedBirdNbt);
        hasBird = true;
        bird.discard();
        setChanged();
    }

    public void releaseCockatiel(Level level, BlockPos pos) {
        if (storedBirdNbt == null || !(level instanceof ServerLevel sl)) return;
        CockatielEntity bird = ModEntities.COCKATIEL.get().create(sl);
        if (bird != null) {
            bird.load(storedBirdNbt);
            bird.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            sl.addFreshEntity(bird);
        }
        storedBirdNbt = null;
        hasBird = false;
        // Update occupied state
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(CockatielCageBlock.OCCUPIED))
            level.setBlock(pos, state.setValue(CockatielCageBlock.OCCUPIED, false), 3);
        setChanged();
    }

    public boolean hasCockatiel() { return hasBird; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("HasBird", hasBird);
        if (storedBirdNbt != null) tag.put("BirdData", storedBirdNbt);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        hasBird = tag.getBoolean("HasBird");
        storedBirdNbt = tag.contains("BirdData") ? tag.getCompound("BirdData") : null;
    }
}
