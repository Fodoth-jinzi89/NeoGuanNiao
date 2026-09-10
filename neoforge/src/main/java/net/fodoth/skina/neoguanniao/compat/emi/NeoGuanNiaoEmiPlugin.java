package net.fodoth.skina.neoguanniao.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.List;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.world.item.Items;

@EmiEntrypoint
public final class NeoGuanNiaoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        List<EmiIngredient> inputs = new ArrayList<>(9);
        for (int i = 0; i < 6; i++) {
            inputs.add(EmiStack.of(NeoGuanNiaoItems.BIRD_FEATHER.get()));
        }
        inputs.add(EmiStack.of(Items.ECHO_SHARD));
        inputs.add(EmiStack.of(Items.STICK));
        inputs.add(EmiStack.of(Items.ECHO_SHARD));
        registry.addRecipe(new EmiCraftingRecipe(inputs, EmiStack.of(NeoGuanNiaoItems.WIND_FEATHER_FAN.get()),
                NeoGuanNiao.resource("wind_feather_fan")));
    }
}
