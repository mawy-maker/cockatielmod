package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.block.CockatielCageBlock;
import com.cockatielmod.cockatiel.block.MilletCropBlock;
import com.cockatielmod.cockatiel.block.PerchBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CockatielMod.MOD_ID);

    public static final DeferredBlock<MilletCropBlock> MILLET_CROP = BLOCKS.register("millet_crop",
            () -> new MilletCropBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final DeferredBlock<PerchBlock> PERCH = BLOCKS.register("perch",
            () -> new PerchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<CockatielCageBlock> COCKATIEL_CAGE = BLOCKS.register("cockatiel_cage",
            () -> new CockatielCageBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()));

    // Block items (perch and cage get block items; millet crop does not)
    static {
        ModItems.ITEMS.register("perch",
                () -> new BlockItem(PERCH.get(), new Item.Properties()));
        ModItems.ITEMS.register("cockatiel_cage",
                () -> new BlockItem(COCKATIEL_CAGE.get(), new Item.Properties()));
    }
}
