package com.cockatielmod.cockatiel.item;

import com.cockatielmod.cockatiel.registry.ModBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MilletSeedsItem extends Item {

    public MilletSeedsItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        var pos   = ctx.getClickedPos();
        var above = pos.above();

        // Plant on farmland if the block above is air
        if (level.getBlockState(pos).is(Blocks.FARMLAND)
                && level.getBlockState(above).isAir()) {
            if (!level.isClientSide) {
                level.setBlock(above, ModBlocks.MILLET_CROP.get().defaultBlockState(), 3);
                if (!ctx.getPlayer().getAbilities().instabuild)
                    ctx.getItemInHand().shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.cockatiel.millet_seeds"));
    }
}
