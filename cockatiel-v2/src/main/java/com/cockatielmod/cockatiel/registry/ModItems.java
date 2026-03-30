package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import com.cockatielmod.cockatiel.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CockatielMod.MOD_ID);

    // ── Feeding ──────────────────────────────────────────────────────────────
    public static final DeferredItem<Item> MILLET_SEEDS = ITEMS.register("millet_seeds",
            () -> new MilletSeedsItem(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).build())));

    public static final DeferredItem<Item> MILLET_BUNCH = ITEMS.register("millet_bunch",
            () -> new MilletBunchItem(new Item.Properties()));

    public static final DeferredItem<Item> COCKATIEL_TREAT = ITEMS.register("cockatiel_treat",
            () -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())));

    // ── Loot drops ───────────────────────────────────────────────────────────
    public static final DeferredItem<Item> COCKATIEL_FEATHER = ITEMS.register("cockatiel_feather",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> YELLOW_COCKATIEL_FEATHER = ITEMS.register("yellow_cockatiel_feather",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WHITE_COCKATIEL_FEATHER = ITEMS.register("white_cockatiel_feather",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COCKATIEL_POTION = ITEMS.register("cockatiel_potion",
            () -> new CockatielPotionItem(new Item.Properties().stacksTo(16)));

    // ── Tools ────────────────────────────────────────────────────────────────
    public static final DeferredItem<Item> COCKATIEL_WHISTLE = ITEMS.register("cockatiel_whistle",
            () -> new CockatielWhistleItem(new Item.Properties().stacksTo(1)));

    // ── Spawn egg ────────────────────────────────────────────────────────────
    public static final DeferredItem<Item> COCKATIEL_SPAWN_EGG = ITEMS.register("cockatiel_spawn_egg",
            () -> new CockatielSpawnEggItem(new Item.Properties()));
}
