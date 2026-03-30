package com.cockatielmod.cockatiel.item;

import net.minecraft.world.food.FoodProperties;

public class ModFoodValues {
    public static final FoodProperties MILLET = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1f)
            .build();

    public static final FoodProperties TREAT = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3f)
            .build();
}
