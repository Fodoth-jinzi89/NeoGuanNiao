package com.birdcamera.content.bird.core.controller.tick.ticker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.birdcamera.BirdCameraMod;
import com.birdcamera.content.bird.core.AbstractBirdEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class DebugLoopTicker<T extends AbstractBirdEntity<T>> extends AbstractBirdTicker<T> {
   public DebugLoopTicker() {
      super(true, true, true);
   }

   @Override
   protected void reset() {
      super.reset();
      this.setTicks(100);
   }

   @Override
   protected void onReset() {
      if (this.bird().level() instanceof ServerLevel level) {
         List<ServerPlayer> players = level.players();
         if (!players.isEmpty()) {
            ServerPlayer player = players.get(level.random.nextInt(players.size()));
            AbstractBirdEntity<?> bird = level.getEntitiesOfClass(AbstractBirdEntity.class, player.getBoundingBox().inflate(256.0))
               .stream()
               .min(Comparator.comparingDouble(player::distanceToSqr))
               .orElse(null);
            if (bird == null) {
               BirdCameraMod.LOGGER.info("[Ticker] No bird found near player {}", player.getName().getString());
            } else {
               BirdCameraMod.LOGGER.info("[Ticker] Debug bird: {} ({})", bird.getId(), bird.getDisplayName().getString());
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
               BirdCameraMod.LOGGER.info("=========== State ===========");
               BirdCameraMod.LOGGER
                  .info(
                     "State: {} (cached: {})", bird.getBehaviorStateController().getBehaviorState(), bird.getBehaviorStateController().getCachedBehaviorState()
                  );
               BirdCameraMod.LOGGER.info("========== Tickers ==========");
               BirdCameraMod.LOGGER.info("Running ({})", running.size());
               running.forEach(t -> BirdCameraMod.LOGGER.info("{}", t.debugLine()));
               BirdCameraMod.LOGGER.info("");
               BirdCameraMod.LOGGER.info("Idle ({})", idle.size());
               idle.forEach(t -> BirdCameraMod.LOGGER.info("{}", t.debugLine()));
               BirdCameraMod.LOGGER.info("");
               BirdCameraMod.LOGGER.info("Frozen ({})", frozen.size());
               frozen.forEach(t -> BirdCameraMod.LOGGER.info("{}", t.debugLine()));
            }
         }
      }
   }

   public void debugGoalStart(String... debugMessage) {
      BirdCameraMod.LOGGER
         .info("[Goal] Bird: {} ({}) Starting Goal: {}", new Object[]{this.bird().getId(), this.bird().getDisplayName().getString(), debugMessage});
   }

   public void debugGoalStop(String... debugMessage) {
      BirdCameraMod.LOGGER
         .info("[Goal] Bird: {} ({}) Stopping Goal: {}", new Object[]{this.bird().getId(), this.bird().getDisplayName().getString(), debugMessage});
   }
}
