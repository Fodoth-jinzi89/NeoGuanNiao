package com.birdcamera.registry;

import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.advancement.criterion.BreedBirdEggTrigger;
import com.birdcamera.content.advancement.criterion.HatchBirdEggTrigger;
import com.birdcamera.content.advancement.criterion.PickupBirdFeatherTrigger;
import com.birdcamera.content.advancement.criterion.SpyglassAtBirdTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class BirdCameraCriteriaTriggers {

    public static final BreedBirdEggTrigger BREED_BIRD_EGG = CriteriaTriggers.register("birdcamera:breed_bird_egg", new BreedBirdEggTrigger());
    public static final HatchBirdEggTrigger HATCH_BIRD_EGG = CriteriaTriggers.register("birdcamera:hatch_bird_egg", new HatchBirdEggTrigger());
    public static final PickupBirdFeatherTrigger PICKUP_BIRD_FEATHER = CriteriaTriggers.register("birdcamera:pickup_bird_feather", new PickupBirdFeatherTrigger());
    public static final SpyglassAtBirdTrigger SPYGLASS_AT_BIRD = CriteriaTriggers.register("birdcamera:spyglass_at_bird", new SpyglassAtBirdTrigger());

    public static void register() {
        BirdCameraMod.LOGGER.info("Registering criteria triggers...");
    }
}
