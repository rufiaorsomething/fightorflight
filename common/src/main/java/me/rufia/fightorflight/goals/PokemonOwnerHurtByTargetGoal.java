package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class PokemonOwnerHurtByTargetGoal extends TargetGoal {
    private final PokemonEntity pokemonEntity;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public PokemonOwnerHurtByTargetGoal(PokemonEntity pokemonEntity) {
        super(pokemonEntity, false);
        this.pokemonEntity = pokemonEntity;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        if (!CobblemonFightOrFlight.config().do_pokemon_defend_owner) { return false; }

        LivingEntity owner = this.pokemonEntity.getOwner();

        if (owner != null && !this.pokemonEntity.isBusy()) {
            this.ownerLastHurtBy = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.timestamp &&
                    this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && this.pokemonEntity.wantsToAttack(this.ownerLastHurtBy, owner);
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.pokemonEntity.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
