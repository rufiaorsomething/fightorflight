package me.rufia.fightorflight.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class FightOrFlightCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_POKEMON_ATTACK;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_POKEMON_ATTACK_UNPROVOKED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_POKEMON_ATTACK_IN_BATTLE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_POKEMON_DEFEND_OWNER;
//    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_PLAYER_POKEMON_ATTACK_OTHER_PLAYERS;
//    public static final ForgeConfigSpec.ConfigValue<Boolean> DO_PLAYER_POKEMON_ATTACK_OTHER_PLAYER_POKEMON;
    public static final ForgeConfigSpec.ConfigValue<Boolean> FORCE_WILD_BATTLE_ON_POKEMON_HURT;
//    public static final ForgeConfigSpec.ConfigValue<Boolean> FORCE_PLAYER_BATTLE_ON_POKEMON_HURT;

    public static final ForgeConfigSpec.ConfigValue<Integer> MINIMUM_ATTACK_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Integer> MINIMUM_ATTACK_UNPROVOKED_LEVEL;




    static {
        BUILDER.push("Configs for Cobblemon Fight or Flight");

        DO_POKEMON_ATTACK = BUILDER.comment("Do more aggressive Pokemon fight back when provoked?").define("do_pokemon_fight_back", true);
        DO_POKEMON_ATTACK_UNPROVOKED = BUILDER.comment("Do especially aggressive Pokemon attack unprovoked?").define("do_pokemon_attack_unprovoked", true);
        DO_POKEMON_ATTACK_IN_BATTLE = BUILDER.comment("Do aggro Pokemon wait for their target to finish any battles before attacking?").define("do_pokemon_attack_in_battle", true);

        DO_POKEMON_DEFEND_OWNER = BUILDER.comment("Do player Pokemon defend their owners when they attack or are attacked by other mobs?").define("do_pokemon_defend_owners", true);
//        DO_PLAYER_POKEMON_ATTACK_OTHER_PLAYERS = BUILDER.comment("Can player Pokemon target other players?").define("do_player_pokemon_attack_other_players", false);
//        DO_PLAYER_POKEMON_ATTACK_OTHER_PLAYER_POKEMON = BUILDER.comment("Can player Pokemon target other player's Pokemon?").define("do_player_pokemon_attack_other_player_pokemon", false);
        FORCE_WILD_BATTLE_ON_POKEMON_HURT = BUILDER.comment("When a player owned Pokemon hurts or is hurt by a wild pokemon, should a pokemon battle be started?").define("force_wild_battle_on_hurt", false);
//        FORCE_PLAYER_BATTLE_ON_POKEMON_HURT = BUILDER.comment("When a player owned Pokemon hurts or is hurt by another player's pokemon, should a pokemon battle be started?").define("force_player_battle_on_hurt",false);

        MINIMUM_ATTACK_LEVEL = BUILDER.comment("The minimum level a Pokemon needs to be to fight back when provoked.").defineInRange("min_fight_back_level", 5, 0, 100);
        MINIMUM_ATTACK_UNPROVOKED_LEVEL = BUILDER.comment("The minimum level a Pokemon needs to be to attack unprovoked.").defineInRange("min_attack_unprovoked_level", 10, 0, 100);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }


}
