package com.cockatielmod.cockatiel.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CockatielPotionItem extends Item {

    public CockatielPotionItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP,          1200, 1));  // 1 min Jump II
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0)); // 1 min Speed I
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,   600, 0));  // 30s Slow Fall
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,   200, 0));  // 10s Regen
            player.displayClientMessage(Component.translatable("message.cockatiel.potion_effect"), true);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.cockatiel.cockatiel_potion"));
    }
}
