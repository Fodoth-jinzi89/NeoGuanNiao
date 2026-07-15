package net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.goal;

import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.AbstractColumbidEntity;
import net.fodoth.skina.neoguanniao.content.bird.impl.old.columbid.ColumbidBehaviorState;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ColumbidPairBondGoal extends Goal {
    private final AbstractColumbidEntity columbid;

    public ColumbidPairBondGoal(AbstractColumbidEntity columbid) {
        this.columbid = columbid;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.columbid.supportsPairBond() && !this.columbid.isTame()
                && this.columbid.canStartGroundSocialGoal() && this.columbid.pairScanCooldown <= 0;
    }

    @Override
    public void start() {
        this.columbid.pairScanCooldown = 180 + this.columbid.getRandom().nextInt(160);

        Optional<AbstractColumbidEntity> current = this.columbid.pairPartner();
        if (current.isEmpty() && this.columbid.pairPartnerUUID != null) {
            this.columbid.pairPartnerUUID = null;
            this.columbid.pairLostTicks = 0;
        }

        if (current.isPresent()) {
            AbstractColumbidEntity partner = current.get();
            if (!this.columbid.hasReciprocalPairWith(partner)) {
                this.columbid.pairPartnerUUID = null;
                this.columbid.pairLostTicks = 0;
            } else if (this.columbid.distanceToSqr(partner) > 625.0) {
                if (++this.columbid.pairLostTicks > 8) {
                    this.columbid.pairPartnerUUID = null;
                    this.columbid.pairLostTicks = 0;
                }
            } else {
                this.columbid.pairLostTicks = 0;
            }

            if (this.columbid.pairPartnerUUID != null) {
                return;
            }
        }

        List<AbstractColumbidEntity> nearby = this.columbid.level().getEntitiesOfClass(
                AbstractColumbidEntity.class,
                this.columbid.getBoundingBox().inflate(16.0),
                other -> other.getClass() == this.columbid.getClass() && other != this.columbid
                        && other.isAlive() && !other.isTame() && other.pairPartnerUUID == null
        );

        if (!nearby.isEmpty()) {
            AbstractColumbidEntity partner = nearby.get(this.columbid.getRandom().nextInt(nearby.size()));
            if (this.columbid.pairPartnerUUID == null && partner.pairPartnerUUID == null) {
                this.columbid.pairPartnerUUID = partner.getUUID();
                partner.pairPartnerUUID = this.columbid.getUUID();
                partner.pairScanCooldown = Math.max(partner.pairScanCooldown, 180);
                this.columbid.setBehaviorStateFor(ColumbidBehaviorState.COURTING, 45);
                partner.setBehaviorStateFor(ColumbidBehaviorState.COURTING, 45);
                this.columbid.spawnCourtshipParticles(3);
                partner.spawnCourtshipParticles(3);
            }
        }
    }
}