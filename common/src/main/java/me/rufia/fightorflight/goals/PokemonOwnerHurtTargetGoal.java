package me.rufia.fightorflight.goals;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.mojang.logging.LogUtils;
import me.rufia.fightorflight.CobblemonFightOrFlight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class PokemonOwnerHurtTargetGoal extends TargetGoal {
    private final PokemonEntity pokemonEntity;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public PokemonOwnerHurtTargetGoal(PokemonEntity pokemonEntity) {
        super(pokemonEntity, false);
        this.pokemonEntity = pokemonEntity;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public boolean canUse() {
        if (!CobblemonFightOrFlight.config().do_pokemon_defend_owner) { return false; }

        LivingEntity owner = this.pokemonEntity.getOwner();
//        if (owner != null) {
//            LogUtils.getLogger().info(pokemonEntity.getPokemon().getSpecies().getName() + " owner: " + this.pokemonEntity.getPokemon().getOwnerPlayer());
//        }

        if (owner != null && !this.pokemonEntity.isBusy()) {
            this.ownerLastHurt = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp
                    && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.pokemonEntity.wantsToAttack(this.ownerLastHurt, owner);
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.pokemonEntity.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }

        super.start();
    }
}