package net.fodoth.skina.neoguanniao.content.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fodoth.skina.neoguanniao.content.bird.impl.NightHeronEntity;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SpyglassAtBirdTrigger
        extends SimpleCriterionTrigger<SpyglassAtBirdTrigger.TriggerInstance> {


    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }


    public void trigger(
            ServerPlayer player,
            NightHeronEntity heron
    ) {

        this.trigger(player,
                instance -> instance.matches(player, heron)
        );
    }


    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            SpyglassTargetType type
    ) implements SimpleInstance {


        public static final Codec<TriggerInstance> CODEC =
                RecordCodecBuilder.create(instance ->
                        instance.group(

                                EntityPredicate.ADVANCEMENT_CODEC
                                        .optionalFieldOf("player")
                                        .forGetter(TriggerInstance::player),

                                SpyglassTargetType.CODEC
                                        .fieldOf("type")
                                        .forGetter(TriggerInstance::type)

                        ).apply(instance, TriggerInstance::new)
                );


        public boolean matches(
                ServerPlayer player,
                NightHeronEntity heron
        ) {

            return switch (type) {

                case NIGHT_HERON_RIVER ->
                        isRiverBiome(player);


                case NIGHT_HERON_SNOW ->
                        isSnowBiome(player);


                case NIGHT_HERON_SPECIAL ->
                        "night_heron_2".equals(
                                heron.getSkin()
                                        .id()
                                        .getPath()
                        );
            };
        }


        private boolean isRiverBiome(
                ServerPlayer player
        ) {

            return player.level()
                    .getBiome(player.blockPosition())
                    .is(BiomeTags.IS_RIVER);
        }


        private boolean isSnowBiome(
                ServerPlayer player
        ) {

            var biome = player.level()
                    .getBiome(player.blockPosition())
                    .value();

            return biome.coldEnoughToSnow(
                    player.blockPosition()
            );
        }
    }
}
