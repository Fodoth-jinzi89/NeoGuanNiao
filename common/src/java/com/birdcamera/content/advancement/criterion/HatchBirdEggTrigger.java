package com.birdcamera.content.advancement.criterion;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HatchBirdEggTrigger extends SimpleCriterionTrigger<HatchBirdEggTrigger.TriggerInstance> {
   @NotNull
   public Codec<HatchBirdEggTrigger.TriggerInstance> codec() {
      return HatchBirdEggTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, instance -> true);
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleInstance {
      public static final Codec<HatchBirdEggTrigger.TriggerInstance> CODEC = Codec.unit(new HatchBirdEggTrigger.TriggerInstance(Optional.empty()));
   }
}
