package com.birdcamera.content.egg;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record BirdEggData(
   ResourceLocation birdType,
   boolean gender,
   ResourceLocation model,
   ResourceLocation skin,
   int eggCount,
   int featherCount,
   int featherInterval,
   float size,
   int hatchTime,
   boolean alive
) {
   public static final Codec<BirdEggData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               ResourceLocation.CODEC.fieldOf("bird_type").forGetter(BirdEggData::birdType),
               Codec.BOOL.fieldOf("gender").forGetter(BirdEggData::gender),
               ResourceLocation.CODEC.fieldOf("model").forGetter(BirdEggData::model),
               ResourceLocation.CODEC.fieldOf("skin").forGetter(BirdEggData::skin),
               Codec.INT.fieldOf("egg_count").forGetter(BirdEggData::eggCount),
               Codec.INT.fieldOf("feather_count").forGetter(BirdEggData::featherCount),
               Codec.INT.fieldOf("feather_interval").forGetter(BirdEggData::featherInterval),
               Codec.FLOAT.fieldOf("size").forGetter(BirdEggData::size),
               Codec.INT.fieldOf("hatch_time").forGetter(BirdEggData::hatchTime),
               Codec.BOOL.fieldOf("alive").forGetter(BirdEggData::alive)
            )
            .apply(instance, BirdEggData::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BirdEggData> STREAM_CODEC = StreamCodec.of(
      (buf, data) -> {
         ResourceLocation.STREAM_CODEC.encode(buf, data.birdType());
         buf.writeBoolean(data.gender());
         ResourceLocation.STREAM_CODEC.encode(buf, data.model());
         ResourceLocation.STREAM_CODEC.encode(buf, data.skin());
         buf.writeInt(data.eggCount());
         buf.writeInt(data.featherCount());
         buf.writeInt(data.featherInterval());
         buf.writeFloat(data.size());
         buf.writeInt(data.hatchTime());
         buf.writeBoolean(data.alive());
      },
      buf -> new BirdEggData(
            (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
            buf.readBoolean(),
            (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
            (ResourceLocation)ResourceLocation.STREAM_CODEC.decode(buf),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            buf.readBoolean()
         )
   );

   public static BirdEggData create(
      ResourceLocation birdType,
      boolean gender,
      ResourceLocation model,
      ResourceLocation skin,
      int eggCount,
      int featherCount,
      int featherInterval,
      float size,
      int hatchTime,
      boolean alive
   ) {
      return new BirdEggData(birdType, gender, model, skin, eggCount, featherCount, featherInterval, size, hatchTime, alive);
   }

   public static BirdEggData createDefault(ResourceLocation birdType, ResourceLocation model, ResourceLocation skin, float size) {
      return new BirdEggData(birdType, true, model, skin, 1, 1, 24000, size, 6000, true);
   }

   public boolean canHatch() {
      return this.alive && this.hatchTime <= 0;
   }

   public boolean isMale() {
      return this.gender;
   }

   public boolean isFemale() {
      return !this.gender;
   }

   public boolean isAlive() {
      return this.alive;
   }

   public BirdEggData tickDown() {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         this.featherInterval,
         this.size,
         Math.max(0, this.hatchTime - 1),
         this.alive
      );
   }

   public BirdEggData withHatchTime(int newHatchTime) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         this.featherInterval,
         this.size,
         Math.max(0, newHatchTime),
         this.alive
      );
   }

   public BirdEggData withAlive(boolean newAlive) {
      return new BirdEggData(
         this.birdType, this.gender, this.model, this.skin, this.eggCount, this.featherCount, this.featherInterval, this.size, this.hatchTime, newAlive
      );
   }

   public BirdEggData withEggCount(int newEggCount) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         Math.max(0, newEggCount),
         this.featherCount,
         this.featherInterval,
         this.size,
         this.hatchTime,
         this.alive
      );
   }

   public BirdEggData withGender(boolean newGender) {
      return new BirdEggData(
         this.birdType, newGender, this.model, this.skin, this.eggCount, this.featherCount, this.featherInterval, this.size, this.hatchTime, this.alive
      );
   }

   public BirdEggData withSize(float newSize) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         this.featherInterval,
         Math.max(0.1F, newSize),
         this.hatchTime,
         this.alive
      );
   }

   public BirdEggData withFeatherCount(int newFeatherCount) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         Math.max(0, newFeatherCount),
         this.featherInterval,
         this.size,
         this.hatchTime,
         this.alive
      );
   }

   public BirdEggData withFeatherInterval(int newFeatherInterval) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         Math.max(0, newFeatherInterval),
         this.size,
         this.hatchTime,
         this.alive
      );
   }

   @NotNull
   @Override
   public String toString() {
      return String.format(
         "BirdEggData{birdType=%s, gender=%s, model=%s, skin=%s, eggCount=%d, featherCount=%d, featherInterval=%d, size=%.2f, hatchTime=%d, alive=%s}",
         this.birdType,
         this.gender ? "male" : "female",
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         this.featherInterval,
         this.size,
         this.hatchTime,
         this.alive
      );
   }

   public BirdEggData tickDown(int ticks) {
      return new BirdEggData(
         this.birdType,
         this.gender,
         this.model,
         this.skin,
         this.eggCount,
         this.featherCount,
         this.featherInterval,
         this.size,
         Math.max(0, this.hatchTime - ticks),
         this.alive
      );
   }
}
