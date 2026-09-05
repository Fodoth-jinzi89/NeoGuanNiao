package com.birdcamera.content.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import com.birdcamera.content.bird.impl.NightHeronEntity;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;

public class SpyglassAtBirdTrigger extends SimpleCriterionTrigger<SpyglassAtBirdTrigger.TriggerInstance> {
   @NotNull
   public Codec<SpyglassAtBirdTrigger.TriggerInstance> codec() {
      return SpyglassAtBirdTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer player, NightHeronEntity heron) {
      this.trigger(player, instance -> instance.matches(player, heron));
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, SpyglassTargetType type) implements SimpleInstance {
      public static final Codec<SpyglassAtBirdTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SpyglassAtBirdTrigger.TriggerInstance::player),
                  SpyglassTargetType.CODEC.fieldOf("type").forGetter(SpyglassAtBirdTrigger.TriggerInstance::type)
               )
               .apply(instance, SpyglassAtBirdTrigger.TriggerInstance::new)
      );

      public boolean matches(ServerPlayer player, NightHeronEntity heron) {
         return switch (this.type) {
            case NIGHT_HERON_RIVER -> this.isRiverBiome(player);
            case NIGHT_HERON_SNOW -> this.isSnowBiome(player);
            case NIGHT_HERON_SPECIAL -> "night_heron_2".equals(heron.getSkin().id().getPath());
         };
      }

      private boolean isRiverBiome(ServerPlayer player) {
         return player.level().getBiome(player.blockPosition()).is(BiomeTags.IS_RIVER);
      }

      private boolean isSnowBiome(ServerPlayer player) {
         Biome biome = (Biome)player.level().getBiome(player.blockPosition()).value();
         return biome.coldEnoughToSnow(player.blockPosition());
      }
   }
}
