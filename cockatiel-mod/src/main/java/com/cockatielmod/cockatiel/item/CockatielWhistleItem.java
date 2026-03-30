package com.cockatielmod.cockatiel.item;

import com.cockatielmod.cockatiel.CockatielConfig;
import com.cockatielmod.cockatiel.entity.CockatielEntity;
import com.cockatielmod.cockatiel.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class CockatielWhistleItem extends Item {

    public CockatielWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            int range = CockatielConfig.WHISTLE_RANGE.get();

            // Play whistle sound
            level.playSound(null, player.blockPosition(), ModSounds.WHISTLE_BLOW.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 1.0f);

            // Find owned cockatiels in range
            UUID playerUUID = player.getUUID();
            List<CockatielEntity> cockatiels = serverLevel.getEntitiesOfClass(
                    CockatielEntity.class,
                    new AABB(player.blockPosition()).inflate(range),
                    entity -> entity.isTamed() && entity.getOwnerUUID().map(playerUUID::equals).orElse(false)
            );

            if (cockatiels.isEmpty()) {
                player.displayClientMessage(
                        Component.translatable("message.cockatiel.whistle_no_cockatiel"),
                        true
                );
            } else {
                // Teleport all owned cockatiels to player
                for (CockatielEntity cockatiel : cockatiels) {
                    cockatiel.teleportToOwner(player);
                }
                player.displayClientMessage(
                        Component.translatable("message.cockatiel.whistle_called",
                                cockatiels.size()),
                        true
                );
            }

            // Add cooldown
            player.getCooldowns().addCooldown(this, 40); // 2 second cooldown
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
