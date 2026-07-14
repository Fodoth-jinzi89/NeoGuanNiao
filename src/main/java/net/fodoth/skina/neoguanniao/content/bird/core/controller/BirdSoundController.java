package net.fodoth.skina.neoguanniao.content.bird.core.controller;

import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.fodoth.skina.neoguanniao.content.bird.core.data.BirdData;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdMiscDatum;
import net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSoundDatum;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

/**
 * 鸟类声音控制器
 * <p>
 * 负责管理鸟实体的声音相关逻辑，包括：
 * <ul>
 *     <li>环境音（Ambient Sound）</li>
 *     <li>受伤音效（Hurt Sound）</li>
 *     <li>死亡音效（Death Sound）</li>
 *     <li>声音音调（Voice Pitch）</li>
 *     <li>环境音播放间隔（Ambient Sound Interval）</li>
 *     <li>交互声音（Interaction Sound）</li>
 * </ul>
 * 具体声音资源由 {@link net.fodoth.skina.neoguanniao.content.bird.core.data.datum.BirdSoundDatum}
 * 提供，使不同鸟种能够拥有独立的声音配置。
 * </p>
 *
 * @param bird 当前控制的鸟实体
 */
public record BirdSoundController(AbstractBirdEntity<?> bird) {

    /**
     * 获取鸟的环境声音
     * <p>
     * 该声音通常用于实体随机鸣叫等自然行为。
     * 返回 {@code null} 表示该鸟不会播放环境音。
     * </p>
     *
     * @return 环境声音事件
     */
    public @Nullable SoundEvent getAmbientSound() {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.ambientSound();
    }

    /**
     * 获取鸟受到伤害时播放的声音
     *
     * @param source 造成伤害的来源
     * @return 受伤声音事件
     */
    public @Nullable SoundEvent getHurtSound(DamageSource source) {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.hurtSound();
    }

    /**
     * 获取鸟死亡时播放的声音
     *
     * @return 死亡声音事件
     */
    public @Nullable SoundEvent getDeathSound() {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.deathSound();
    }

    /**
     * 获取鸟声音播放时的音调倍率
     * <p>
     * 用于调整同一种声音在不同鸟个体之间的音高差异。
     * </p>
     *
     * @return 声音音调倍率
     */
    public float getVoicePitch() {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.voicePitch();
    }

    /**
     * 获取鸟环境声音的播放间隔
     * <p>
     * 单位为 Tick。
     * 该值通常用于控制鸟随机鸣叫的频率。
     * </p>
     *
     * @return 环境声音播放间隔
     */
    public int getAmbientSoundInterval() {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.ambientSoundInterval();
    }

    /**
     * 播放鸟与玩家或环境交互时的声音
     * <p>
     * 用于处理例如喂食、抚摸、驯服等交互行为产生的声音效果。
     * 具体声音类型由鸟种实现决定。
     * </p>
     *
     * @return 交互声音事件
     */
    public @Nullable SoundEvent getInteractionSound() {
        BirdData birdData = bird.getBirdData();
        BirdSoundDatum soundDatum = birdData.sound();
        return soundDatum.interactionSound();
    }
}