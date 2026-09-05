package net.fodoth.skina.neoguanniao.content.bath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class BirdBathEffects {

    private BirdBathEffects() {
    }


    public static void waterAdded(
            Level level,
            BlockPos pos,
            SoundEvent sound
    ) {
        play(level, pos, sound, 0.8F, 1.0F);
        splash(level, pos, 10);
    }


    public static void foodAdded(
            Level level,
            BlockPos pos,
            BirdBathContentType type
    ) {

        SoundEvent sound = switch (type) {
            case FISH, MEAT -> SoundEvents.FISH_SWIM;
            case BREAD -> SoundEvents.ITEM_PICKUP;
            default -> SoundEvents.WOOD_HIT;
        };


        play(level, pos, sound, 0.7F, 1.0F);


        if(level instanceof ServerLevel serverLevel){

            serverLevel.sendParticles(
                    type == BirdBathContentType.FISH
                            ? ParticleTypes.SPLASH
                            : ParticleTypes.HAPPY_VILLAGER,

                    pos.getX() + 0.5,
                    pos.getY() + 0.95,
                    pos.getZ() + 0.5,

                    5,

                    0.18,
                    0.08,
                    0.18,

                    0.02
            );
        }
    }



    public static void cleaned(
            Level level,
            BlockPos pos,
            BirdBathCleanliness previous
    ){

        play(
                level,
                pos,
                SoundEvents.BUCKET_EMPTY,
                0.65F,
                1.15F
        );


        if(level instanceof ServerLevel serverLevel){

            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,

                    pos.getX()+0.5,
                    pos.getY()+0.9,
                    pos.getZ()+0.5,

                    Math.max(
                            3,
                            previous.particleIntensity()
                    ),

                    0.2,
                    0.08,
                    0.2,

                    0.015
            );
        }
    }



    public static void spoiledCleared(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.COMPOSTER_EMPTY,
                0.75F,
                0.9F
        );


        particles(
                level,
                pos,
                ParticleTypes.ASH,
                8
        );
    }



    public static void contentCleared(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.BUCKET_EMPTY,
                0.7F,
                1.1F
        );


        particles(
                level,
                pos,
                ParticleTypes.HAPPY_VILLAGER,
                4
        );
    }



    public static void evaporated(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.FIRE_EXTINGUISH,
                0.25F,
                1.7F
        );


        particles(
                level,
                pos,
                ParticleTypes.CLOUD,
                3
        );
    }



    public static void froze(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.GLASS_BREAK,
                0.55F,
                1.35F
        );


        particles(
                level,
                pos,
                ParticleTypes.SNOWFLAKE,
                6
        );
    }



    public static void melted(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.GLASS_BREAK,
                0.45F,
                1.4F
        );

        splash(level,pos,5);
    }



    public static void spoiled(
            Level level,
            BlockPos pos
    ){

        play(
                level,
                pos,
                SoundEvents.COMPOSTER_EMPTY,
                0.45F,
                0.8F
        );


        particles(
                level,
                pos,
                ParticleTypes.ASH,
                6
        );
    }



    public static void birdUsed(
            Level level,
            BlockPos pos,
            boolean water
    ){

        if(water){

            splash(level,pos,4);

        }else{

            particles(
                    level,
                    pos,
                    ParticleTypes.HAPPY_VILLAGER,
                    3
            );
        }
    }



    public static void idleDirty(
            Level level,
            BlockPos pos,
            BirdBathCleanliness cleanliness,
            boolean spoiled
    ){

        if(level instanceof ServerLevel serverLevel){

            RandomSource random =
                    serverLevel.random;


            if(spoiled && random.nextInt(8)==0){

                particles(
                        level,
                        pos,
                        ParticleTypes.ASH,
                        1
                );

            }else if(
                    cleanliness == BirdBathCleanliness.FILTHY
                            && random.nextInt(4)==0
            ){

                particles(
                        level,
                        pos,
                        ParticleTypes.CLOUD,
                        1
                );
            }
        }
    }



    private static void splash(
            Level level,
            BlockPos pos,
            int count
    ){

        particles(
                level,
                pos,
                ParticleTypes.SPLASH,
                count
        );
    }



    private static void particles(
            Level level,
            BlockPos pos,
            net.minecraft.core.particles.ParticleOptions particle,
            int count
    ){

        if(level instanceof ServerLevel serverLevel){

            serverLevel.sendParticles(
                    particle,

                    pos.getX()+0.5,
                    pos.getY()+0.9,
                    pos.getZ()+0.5,

                    count,

                    0.15,
                    0.08,
                    0.15,

                    0.01
            );
        }
    }



    private static void play(
            Level level,
            BlockPos pos,
            SoundEvent sound,
            float volume,
            float pitch
    ){

        level.playSound(
                null,
                pos,
                sound,
                SoundSource.BLOCKS,
                volume,
                pitch
        );
    }
}