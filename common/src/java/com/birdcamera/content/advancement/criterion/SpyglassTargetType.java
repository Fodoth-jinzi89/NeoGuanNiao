package com.birdcamera.content.advancement.criterion;

import com.mojang.serialization.Codec;

public enum SpyglassTargetType {
   NIGHT_HERON_RIVER,
   NIGHT_HERON_SNOW,
   NIGHT_HERON_SPECIAL;

   public static final Codec<SpyglassTargetType> CODEC = Codec.STRING.xmap(SpyglassTargetType::valueOf, Enum::name);
}
