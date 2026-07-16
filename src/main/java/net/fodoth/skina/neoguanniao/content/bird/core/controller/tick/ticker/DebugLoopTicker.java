package net.fodoth.skina.neoguanniao.content.bird.core.controller.tick.ticker;

import net.fodoth.skina.neoguanniao.NeoGuanNiao;
import net.fodoth.skina.neoguanniao.content.bird.core.AbstractBirdEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DebugLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {

    public DebugLoopTicker() {
        super(true, true, true);
    }

    @Override
    protected void reset() {
        super.reset();
        setTicks(100);
    }

    @Override
    protected void onReset() {

        if (!(bird().level() instanceof ServerLevel level)) {
            return;
        }

        List<ServerPlayer> players = level.players();

        if (players.isEmpty()) {
            return;
        }

        ServerPlayer player = players.get(level.random.nextInt(players.size()));

        AbstractBirdEntity<?> bird = level.getEntitiesOfClass(
                AbstractBirdEntity.class,
                player.getBoundingBox().inflate(256.0)
        ).stream().min(
                Comparator.comparingDouble(player::distanceToSqr)
        ).orElse(null);

        if (bird == null) {
            NeoGuanNiao.LOGGER.info("[Ticker] No bird found near player {}", player.getName().getString());
            return;
        }

        NeoGuanNiao.LOGGER.info(
                "[Ticker] Debug bird: {} ({})",
                bird.getId(),
                bird.getDisplayName().getString()
        );

        List<AbstractBirdTicker<?>> running = new ArrayList<>();
        List<AbstractBirdTicker<?>> idle = new ArrayList<>();
        List<AbstractBirdTicker<?>> frozen = new ArrayList<>();

        bird.getTickController().forEachTicker(t -> {
            if (t.isFrozen()) {
                frozen.add(t);
            } else if (t.isRunning()) {
                running.add(t);
            } else {
                idle.add(t);
            }
        });

        NeoGuanNiao.LOGGER.info("=========== State ===========");
        NeoGuanNiao.LOGGER.info(
                "State: {} (cached: {})",
                bird.getBehaviorStateController().getBehaviorState(),
                bird.getBehaviorStateController().getCachedBehaviorState()
        );


        NeoGuanNiao.LOGGER.info("========== Tickers ==========");
        NeoGuanNiao.LOGGER.info("Running ({})", running.size());
        running.forEach(t -> NeoGuanNiao.LOGGER.info("{}", t.debugLine()));

        NeoGuanNiao.LOGGER.info("");
        NeoGuanNiao.LOGGER.info("Idle ({})", idle.size());
        idle.forEach(t -> NeoGuanNiao.LOGGER.info("{}", t.debugLine()));

        NeoGuanNiao.LOGGER.info("");
        NeoGuanNiao.LOGGER.info("Frozen ({})", frozen.size());
        frozen.forEach(t -> NeoGuanNiao.LOGGER.info("{}", t.debugLine()));

    }


}