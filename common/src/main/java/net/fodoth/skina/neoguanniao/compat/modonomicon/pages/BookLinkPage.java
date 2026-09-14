package net.fodoth.skina.neoguanniao.compat.modonomicon.pages;

import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.page.BookTextPage;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.BookPageJsonLoader;
import com.klikli_dev.modonomicon.data.LoaderRegistry;
import com.klikli_dev.modonomicon.util.BookGsonHelper;
import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * 观鸟手册的链接页：普通文本页 + 一个可点击的链接。
 * 行为与 {"type": "spectrum:link"} 一致（复刻自 Spectrum 的 BookLinkPage）。
 * <p>
 * JSON 字段：{@code title}、{@code text}、{@code link_text}、{@code url}（其余与 modonomicon:text 相同）。
 */
public class BookLinkPage extends BookTextPage {

    public static final ResourceLocation TYPE = NeoGuanNiao.resource("link");

    private final String url;
    private BookTextHolder linkText;

    public BookLinkPage(BookTextHolder title, BookTextHolder text, boolean useMarkdownInTitle, boolean showTitleSeparator,
                        String anchor, BookCondition condition, String url, BookTextHolder linkText) {
        super(title, text, useMarkdownInTitle, showTitleSeparator, anchor, condition);
        this.url = url;
        this.linkText = linkText;
    }

    /** 注册页面类型（数据侧，需在数据包加载前调用）。 */
    public static void register() {
        BookPageJsonLoader<BookLinkPage> jsonLoader = BookLinkPage::fromJson;
        LoaderRegistry.registerPageLoader(TYPE, jsonLoader, BookLinkPage::fromNetwork);
    }

    public static BookLinkPage fromJson(ResourceLocation entryId, JsonObject json, HolderLookup.Provider provider) {
        BookTextHolder title = BookGsonHelper.getAsBookTextHolder(json, "title", BookTextHolder.EMPTY, provider);
        boolean useMarkdownInTitle = GsonHelper.getAsBoolean(json, "use_markdown_title", false);
        boolean showTitleSeparator = GsonHelper.getAsBoolean(json, "show_title_separator", true);
        BookTextHolder text = BookGsonHelper.getAsBookTextHolder(json, "text", BookTextHolder.EMPTY, provider);
        String anchor = GsonHelper.getAsString(json, "anchor", "");
        BookCondition condition = json.has("condition")
                ? BookCondition.fromJson(entryId, json.getAsJsonObject("condition"), provider)
                : new BookNoneCondition();
        String url = GsonHelper.getAsString(json, "url", "");
        BookTextHolder linkText = BookGsonHelper.getAsBookTextHolder(json, "link_text", BookTextHolder.EMPTY, provider);
        return new BookLinkPage(title, text, useMarkdownInTitle, showTitleSeparator, anchor, condition, url, linkText);
    }

    public static BookLinkPage fromNetwork(RegistryFriendlyByteBuf buffer) {
        BookTextHolder title = BookTextHolder.fromNetwork(buffer);
        boolean useMarkdownInTitle = buffer.readBoolean();
        boolean showTitleSeparator = buffer.readBoolean();
        BookTextHolder text = BookTextHolder.fromNetwork(buffer);
        String anchor = buffer.readUtf();
        BookCondition condition = BookCondition.fromNetwork(buffer);
        String url = buffer.readUtf();
        BookTextHolder linkText = BookTextHolder.fromNetwork(buffer);
        return new BookLinkPage(title, text, useMarkdownInTitle, showTitleSeparator, anchor, condition, url, linkText);
    }

    public BookTextHolder getLinkText() {
        return this.linkText;
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    /** 把链接文字包成带点击/悬停事件的组件，点击由 BookEntryScreen 分发。 */
    @Override
    public void prerenderMarkdown(BookTextRenderer textRenderer) {
        super.prerenderMarkdown(textRenderer);
        if (!this.linkText.hasComponent()) {
            MutableComponent link = Component.translatable(this.linkText.getKey());
            Style style = Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, this.url))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty(this.url)));
            this.linkText = new BookTextHolder(link.withStyle(style));
        }
    }

    @Override
    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        super.toNetwork(buffer);
        buffer.writeUtf(this.url);
        this.linkText.toNetwork(buffer);
    }

    @Override
    public boolean matchesQuery(String query) {
        return super.matchesQuery(query)
                || this.url.toLowerCase().contains(query)
                || this.linkText.getString().toLowerCase().contains(query);
    }
}
