package com.birdcamera.content.feather;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BirdFeatherData(ResourceLocation birdType, int rarity) {
   public static final Codec<BirdFeatherData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ResourceLocation.CODEC.fieldOf("bird_type").forGetter(BirdFeatherData::birdType), Codec.INT.fieldOf("rarity").forGetter(BirdFeatherData::rarity)
            )
            .apply(instance, BirdFeatherData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BirdFeatherData> STREAM_CODEC = StreamCodec.of((buf, data) -> {
      ResourceLocation.STREAM_CODEC.encode(buf, data.birdType());
      buf.writeInt(data.rarity());
   }, buf -> new BirdFeatherData((ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf), buf.readInt()));

   public static BirdFeatherData create(ResourceLocation birdType, int rarity) {
      return new BirdFeatherData(birdType, rarity);
   }

   public static BirdFeatherData createDefault(ResourceLocation birdType) {
      return new BirdFeatherData(birdType, 0);
   }

   public BirdFeatherData withRarity(int newRarity) {
      return new BirdFeatherData(this.birdType, Math.max(0, newRarity));
   }

   public BirdFeatherData addRarity(int amount) {
      return new BirdFeatherData(this.birdType, Math.max(0, this.rarity + amount));
   }

   public BirdFeatherData withBirdType(ResourceLocation newBirdType) {
      return new BirdFeatherData(newBirdType, this.rarity);
   }

   public int toTypeInt() {
      if (!this.birdType().getNamespace().equals("birdcamera")) {
         return 0;
      } else {
         String var1 = this.birdType().getPath();

         return switch (var1) {
            case "neo_budgerigar" -> 0;
            case "neo_night_heron" -> 1;
            case "neo_dove" -> 2;
            case "neo_pigeon" -> 3;
            case "neo_sparrow" -> 4;
            case "neo_cockatiel" -> 5;
            case "neo_long_tailed_tit" -> 6;
            case "neo_macaw" -> 7;
            case "neo_crow" -> 8;
            case "neo_seagull" -> 9;
            default -> -1;
         };
      }
   }
}
