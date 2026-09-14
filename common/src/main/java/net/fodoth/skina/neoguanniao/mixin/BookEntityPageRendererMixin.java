package net.fodoth.skina.neoguanniao.mixin;

import com.klikli_dev.modonomicon.client.render.page.BookEntityPageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 观鸟手册的实体展示页（modonomicon:entity）里，Modonomicon 自己 create 出实体后从不 tick 它，
 * 而 geckolib 把 {@code tickCount} 当作动画时钟
 * （{@code GeoEntityRenderer.defaultRender} 中 {@code ageInTicks = animatable.tickCount + partialTick}），
 * 所以书里的鸟只会冻结在动画第 0 帧。
 *
 * <p>这里每帧把 {@code tickCount} 对齐到客户端 20Hz 的游戏时间，让展示的实体正常播放 idle 等动画。
 * 仅在客户端应用（配置中放在 client 列表），且 Modonomicon 是软依赖（配置 required=false）。
 */
@Mixin(BookEntityPageRenderer.class)
public abstract class BookEntityPageRendererMixin {

    @Shadow
    private Entity entity;

    @Inject(method = "render", at = @At("HEAD"))
    private void neoguanniao$tickDisplayedEntity(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                                 float partialTick, CallbackInfo ci) {
        if (this.entity == null) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            this.entity.tickCount = (int) level.getGameTime();
        }
    }
}
