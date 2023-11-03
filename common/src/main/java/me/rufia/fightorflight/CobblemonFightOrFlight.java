package me.rufia.fightorflight;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import me.rufia.fightorflight.config.FightOrFlightCommonConfigModel;
import me.rufia.fightorflight.goals.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonFightOrFlight {
	public static final String MODID = "fightorflight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final float AUTO_AGGRO_THRESHOLD = 50.0f;
	private static FightOrFlightCommonConfigModel config;
	private static TriConsumer<PokemonEntity, Integer, Goal> goalAdder;

	public static FightOrFlightCommonConfigModel config() {
		return config;
	}

	public static void init(TriConsumer<PokemonEntity, Integer, Goal> goalAdder) {
		CobblemonFightOrFlight.goalAdder = goalAdder;
		AutoConfig.register(FightOrFlightCommonConfigModel.class, JanksonConfigSerializer::new);
		config = AutoConfig.getConfigHolder(FightOrFlightCommonConfigModel.class).getConfig();

		CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, event -> {
			addPokemonGoal(event.getEntity());
			return Unit.INSTANCE;
		});

		CobblemonEvents.POKEMON_ENTITY_LOAD.subscribe(Priority.HIGHEST, event -> {
			addPokemonGoal(event.getPokemonEntity());
			return Unit.INSTANCE;
		});

		CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.HIGHEST, event -> {
			addPokemonGoal(event.getPokemonEntity());
			return Unit.INSTANCE;
		});
	}

	public static void addPokemonGoal(PokemonEntity pokemonEntity) {
		float fleeSpeed = 1.5f;
		float pursuitSpeed = 1.2f;

		goalAdder.accept(pokemonEntity, 3, new PokemonAvoidGoal(pokemonEntity, 48.0f, 1.0f, fleeSpeed));
		goalAdder.accept(pokemonEntity, 3, new PokemonMeleeAttackGoal(pokemonEntity, pursuitSpeed, true));
		goalAdder.accept(pokemonEntity, 4, new PokemonPanicGoal(pokemonEntity, fleeSpeed));

		goalAdder.accept(pokemonEntity, 1, new PokemonOwnerHurtByTargetGoal(pokemonEntity));
		goalAdder.accept(pokemonEntity, 2, new PokemonOwnerHurtTargetGoal(pokemonEntity));
		goalAdder.accept(pokemonEntity, 3, new HurtByTargetGoal(pokemonEntity));
		goalAdder.accept(pokemonEntity, 4, new CaughtByTargetGoal(pokemonEntity));
		goalAdder.accept(pokemonEntity, 5, new PokemonNearestAttackableTargetGoal<>(pokemonEntity, Player.class, 48.0f, true, true));
	}

	public static double getFightOrFlightCoefficient(PokemonEntity pokemonEntity){
		if (!CobblemonFightOrFlight.config().do_pokemon_attack) { return -100; }

		Pokemon pokemon = pokemonEntity.getPokemon();
		double pkmnLevel = pokemon.getLevel();
		//double levelAggressionCoefficient = (pokemon.getLevel() - 20);
		double lowStatPenalty = (pkmnLevel * 1.5)+30;
		double levelAggressionCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - lowStatPenalty;
		double atkDefRatioCoefficient = (pokemon.getAttack() + pokemon.getSpecialAttack()) - (pokemon.getDefence() + pokemon.getSpecialDefence());
		double natureAggressionCoefficient = 0;
		switch (pokemon.getNature().getDisplayName().toLowerCase()){
			case "cobblemon.nature.docile":
			case "cobblemon.nature.timid":
			case "cobblemon.nature.gentle":
			case "cobblemon.nature.careful":
				natureAggressionCoefficient = -2;
				break;
			case "cobblemon.nature.relaxed":
			case "cobblemon.nature.lax":
			case "cobblemon.nature.quiet":
			case "cobblemon.nature.bashful":
			case "cobblemon.nature.calm":
				natureAggressionCoefficient = -1;
				break;
			case "cobblemon.nature.sassy":
			case "cobblemon.nature.hardy":
			case "cobblemon.nature.bold":
			case "cobblemon.nature.impish":
			case "cobblemon.nature.hasty":
				natureAggressionCoefficient = 1;
				break;
			case "cobblemon.nature.brave":
			case "cobblemon.nature.rash":
			case "cobblemon.nature.adamant":
			case "cobblemon.nature.naughty":
				natureAggressionCoefficient = 2;
				break;
			default:
				natureAggressionCoefficient = 0;
				break;
		}

		//Weights and Clamps:
		levelAggressionCoefficient = Math.max(-(pkmnLevel + 5), Math.min(pkmnLevel, 1.5d * levelAggressionCoefficient));//5.0d * levelAggressionCoefficient;
		atkDefRatioCoefficient = Math.max(-pkmnLevel, 1.0d * atkDefRatioCoefficient);
		natureAggressionCoefficient = (pkmnLevel * 0.5) * natureAggressionCoefficient;//25.0d * natureAggressionCoefficient;

		double finalResult = levelAggressionCoefficient + atkDefRatioCoefficient + natureAggressionCoefficient;


//        var pkmnString = "[" + pokemon.getSpecies().getName() + "]";
//        LOGGER.info(pkmnString + " levelAggressionCoefficient: " + levelAggressionCoefficient);
//        LOGGER.info(pkmnString + " atkDefRatioCoefficient: " + atkDefRatioCoefficient);
//        LOGGER.info(pkmnString + " natureAggressionCoefficient: " + natureAggressionCoefficient
//                + " (" + pokemon.getNature().getDisplayName().toLowerCase() + ")");
//
//        LOGGER.info("final FightOrFlightCoefficient: "
//                + levelAggressionCoefficient + "+" + atkDefRatioCoefficient + "+" + natureAggressionCoefficient
//                + " = " + finalResult);
		return finalResult;
	}

	public static void PokemonEmoteAngry(Mob mob){
		double particleSpeed = Math.random();
		double particleAngle = Math.random() * 2 * Math.PI;
		double particleXSpeed = Math.cos(particleAngle) * particleSpeed;
		double particleYSpeed = Math.sin(particleAngle) * particleSpeed;

		if (mob.level() instanceof ServerLevel level){
			level.sendParticles(ParticleTypes.ANGRY_VILLAGER,
					mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
					1, //Amount?
					particleXSpeed,0.5d, particleYSpeed,
					1.0f); //Scale?
		}
		else{
			mob.level().addParticle(ParticleTypes.ANGRY_VILLAGER,
					mob.position().x, mob.getBoundingBox().maxY, mob.position().z,
					particleXSpeed,0.5d, particleYSpeed);
		}
	}
}