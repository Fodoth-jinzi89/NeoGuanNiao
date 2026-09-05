package net.fodoth.skina.neoguanniao.client.guide;

import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoEntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public record BirdGuideEntry(String id, List<String> sections) {

    public Component title() {
        return Component.translatable("gui.neoguanniao.bird_guide.entry." + this.id + ".title");
    }

    public Component subtitle() {
        return Component.translatable("gui.neoguanniao.bird_guide.entry." + this.id + ".subtitle");
    }

    public EntityType<? extends LivingEntity> entityType() {
        return switch (this.id) {
            case "budgerigar" -> NeoGuanNiaoEntityTypes.NEO_BUDGERIGAR.get();
            case "sparrow" -> NeoGuanNiaoEntityTypes.NEO_SPARROW.get();
            case "spotted_dove" -> NeoGuanNiaoEntityTypes.NEO_DOVE.get();
            case "pigeon" -> NeoGuanNiaoEntityTypes.NEO_PIGEON.get();
            case "kiwi" -> NeoGuanNiaoEntityTypes.NEO_KIWI.get();
            case "myna" -> NeoGuanNiaoEntityTypes.NEO_MYNA.get();
            default -> NeoGuanNiaoEntityTypes.NEO_NIGHT_HERON.get();
        };
    }
}
