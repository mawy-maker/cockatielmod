package com.cockatielmod.cockatiel.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MilletBunchItem extends Item {

    public MilletBunchItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.cockatiel.millet_bunch"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
