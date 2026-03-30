package com.cockatielmod.cockatiel;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CockatielConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue HUNGER_THRESHOLD_TICKS;
    public static final ModConfigSpec.IntValue CREEPER_ALERT_RANGE;
    public static final ModConfigSpec.IntValue WHISTLE_RANGE;
    public static final ModConfigSpec.DoubleValue CROP_STEAL_CHANCE;
    public static final ModConfigSpec.IntValue SONG_DURATION_TICKS;

    static {
        BUILDER.push("Cockatiel Settings");

        HUNGER_THRESHOLD_TICKS = BUILDER
                .comment("Ticks before a hungry cockatiel starts stealing crops (default: 24000 = 1 in-game day)")
                .defineInRange("hungerThresholdTicks", 24000, 6000, 72000);

        CREEPER_ALERT_RANGE = BUILDER
                .comment("Range in blocks for creeper detection alert")
                .defineInRange("creeperAlertRange", 16, 8, 64);

        WHISTLE_RANGE = BUILDER
                .comment("Maximum range of the cockatiel whistle in blocks")
                .defineInRange("whistleRange", 200, 50, 500);

        CROP_STEAL_CHANCE = BUILDER
                .comment("Chance (0.0 to 1.0) that a hungry cockatiel will steal a crop when nearby")
                .defineInRange("cropStealChance", 0.05, 0.0, 1.0);

        SONG_DURATION_TICKS = BUILDER
                .comment("How long the morning song lasts in ticks")
                .defineInRange("songDurationTicks", 100, 20, 400);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
