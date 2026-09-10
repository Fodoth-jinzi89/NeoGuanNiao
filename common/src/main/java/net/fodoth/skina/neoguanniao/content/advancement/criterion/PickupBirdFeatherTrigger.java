package net.fodoth.skina.neoguanniao.content.advancement.criterion;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PickupBirdFeatherTrigger
        extends SimpleCriterionTrigger<PickupBirdFeatherTrigger.TriggerInstance> {


    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }


    public void trigger(ServerPlayer player) {

        this.trigger(player, instance -> true);
    }


    public record TriggerInstance(
            Optional<ContextAwarePredicate> player
    ) implements SimpleInstance {


        public static final Codec<TriggerInstance> CODEC =
                Codec.unit(
                        new TriggerInstance(
                                Optional.empty()
                        )
                );
    }
}