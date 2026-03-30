package com.cockatielmod.cockatiel.registry;

import com.cockatielmod.cockatiel.CockatielMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CockatielMod.MOD_ID);

    public static final Supplier<SoundEvent> COCKATIEL_AMBIENT =
            SOUNDS.register("cockatiel_ambient", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_ambient")));

    public static final Supplier<SoundEvent> COCKATIEL_HURT =
            SOUNDS.register("cockatiel_hurt", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_hurt")));

    public static final Supplier<SoundEvent> COCKATIEL_DEATH =
            SOUNDS.register("cockatiel_death", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_death")));

    public static final Supplier<SoundEvent> COCKATIEL_SONG =
            SOUNDS.register("cockatiel_song", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_song")));

    public static final Supplier<SoundEvent> COCKATIEL_ALERT =
            SOUNDS.register("cockatiel_alert", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_alert")));

    public static final Supplier<SoundEvent> COCKATIEL_EAT =
            SOUNDS.register("cockatiel_eat", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_eat")));

    public static final Supplier<SoundEvent> COCKATIEL_HAPPY =
            SOUNDS.register("cockatiel_happy", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("cockatiel_happy")));

    public static final Supplier<SoundEvent> WHISTLE_BLOW =
            SOUNDS.register("whistle_blow", () -> SoundEvent.createVariableRangeEvent(CockatielMod.rl("whistle_blow")));
}
