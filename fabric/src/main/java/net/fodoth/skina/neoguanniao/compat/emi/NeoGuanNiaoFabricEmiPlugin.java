package net.fodoth.skina.neoguanniao.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.compat.PhotoRecipeDisplays;
import net.fodoth.skina.neoguanniao.registry.NeoGuanNiaoItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class NeoGuanNiaoFabricEmiPlugin implements EmiPlugin {
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
        for (PhotoRecipeDisplays.Display display : PhotoRecipeDisplays.all()) {
            List<EmiIngredient> displayInputs = new ArrayList<>(display.inputs().size());
            for (List<ItemStack> slot : display.inputs()) {
                List<EmiStack> stacks = new ArrayList<>(slot.size());
                for (ItemStack stack : slot) {
                    stacks.add(EmiStack.of(stack));
                }
                displayInputs.add(EmiIngredient.of(stacks));
            }
            registry.addRecipe(new EmiCraftingRecipe(displayInputs, EmiStack.of(display.output()), display.id(), true));
        }
    }
}
