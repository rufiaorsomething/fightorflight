package me.rufia.fightorflight;

import me.rufia.fightorflight.event.EntityLoadHandler;
import me.rufia.fightorflight.mixin.MobEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;



public class CobblemonFightOrFlightFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		CobblemonFightOrFlight.LOGGER.info("Hello Fabric world from Fight or Flight!");

		CobblemonFightOrFlight.init((pokemonEntity, priority, goal) -> ((MobEntityAccessor) (Object) pokemonEntity).goalSelector().addGoal(priority, goal));

		ServerEntityEvents.ENTITY_LOAD.register(new EntityLoadHandler());
	}
}