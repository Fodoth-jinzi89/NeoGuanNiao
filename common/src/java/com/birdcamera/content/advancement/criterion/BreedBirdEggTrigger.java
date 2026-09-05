package com.birdcamera.content.advancement.criterion;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class BreedBirdEggTrigger extends SimpleCriterionTrigger<BreedBirdEggTrigger.TriggerInstance> {
   @NotNull
   public Codec<BreedBirdEggTrigger.TriggerInstance> codec() {
      return BreedBirdEggTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, instance -> true);
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance {
      public static final Codec<BreedBirdEggTrigger.TriggerInstance> CODEC = Codec.unit(new BreedBirdEggTrigger.TriggerInstance(Optional.empty()));
   }
}
