package com.birdcamera.registry;

import java.util.HashMap;
import java.util.Map;
import com.birdcamera.content.bird.core.model.BirdModel;
import net.minecraft.resources.ResourceLocation;

public class BirdCameraBirdModels {
   private static final Map<ResourceLocation, BirdModel> MODELS = new HashMap<>();

   public static void register(BirdModel model) {
      MODELS.put(model.id(), model);
   }

   public static BirdModel get(ResourceLocation id) {
      return MODELS.get(id);
   }
}
