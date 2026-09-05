package com.birdcamera.registry;

import java.util.HashMap;
import java.util.Map;
import com.birdcamera.content.bird.core.skin.BirdSkin;
import net.minecraft.resources.ResourceLocation;

public class BirdCameraBirdSkins {
   private static final Map<ResourceLocation, BirdSkin> SKINS = new HashMap<>();

   public static void register(BirdSkin skin) {
      SKINS.put(skin.id(), skin);
   }

   public static BirdSkin get(ResourceLocation id) {
      return SKINS.get(id);
   }
}
