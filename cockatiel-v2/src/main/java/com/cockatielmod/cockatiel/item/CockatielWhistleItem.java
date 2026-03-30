package com.cockatielmod.cockatiel.item;

import com.cockatielmod.cockatiel.CockatielConfig;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class CockatielWhistleItem extends Item {

    public CockatielWhistleItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int range = CockatielConfig.WHISTLE_RANGE.get();
            level.playSound(null, player.blockPosition(),
                    ModSounds.WHISTLE_BLOW.get(), SoundSource.PLAYERS, 2.0f, 1.0f);

            var birds = ((ServerLevel) level).getEntitiesOfClass(
                    CockatielEntity.class,
                    new AABB(player.blockPosition()).inflate(range),
                    b -> b.isTamed() && b.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false));

            if (birds.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.cockatiel.whistle_none"), true);
            } else {
                birds.forEach(b -> b.teleportToOwner(player));
                player.displayClientMessage(
                        Component.translatable("message.cockatiel.whistle_called", birds.size()), true);
            }
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
