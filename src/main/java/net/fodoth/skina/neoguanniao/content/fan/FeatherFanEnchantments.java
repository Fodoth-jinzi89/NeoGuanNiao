package net.fodoth.skina.neoguanniao.content.fan;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
public final class FeatherFanEnchantments {
 private FeatherFanEnchantments(){}
 public static boolean hasBurialPlume(ItemStack s){return has(s,"burial_plume");}
 public static boolean hasRivenPlume(ItemStack s){return has(s,"riven_plume");}
 public static boolean hasHuntingReturn(ItemStack s){return has(s,"hunting_return");}
 public static String mode(ItemStack s){if(hasHuntingReturn(s))return "hunting";if(hasRivenPlume(s))return "riven";if(hasBurialPlume(s))return "burial";return "";}
 private static boolean has(ItemStack s,String id){var e=s.get(DataComponents.ENCHANTMENTS);return e!=null&&e.keySet().stream().anyMatch(h->h.unwrapKey().map(k->k.location().getNamespace().equals("neoguanniao")&&k.location().getPath().equals(id)).orElse(false));}
}
