package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.mputils.message.MessageEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Message implements MessageEnum {
    NO("&cNo"),
    YES("&aYes"),
    YOURSELF("yourself"),
    UNDEFINED("&cundefined"),

    NO_PERMISSION("&cNo permission"),
    CANNOT_SEND_CONSOLE("&cThat command can only be sent by a player."),
    PLAYER_NOT_ONLINE("messages.not online", "&cThat player is not online"),
    COMMAND_USE_HP_MODIFY("messages.modify use", "&cUse: &f/%command% %what% (player) (HP)"),
    COMMAND_USE_CONSUMABLE("messages.consumable use", "&cUse: &f/%command% consumable (give) (player) (consumable_name) &cOR &f/%command% consumable (set) (consumable_name) (add/set) (amount)"),
    COMMAND_USE_CLEAR("messages.clear use", "&cUse: &f/%command% clear (player)"),
    COMMAND_USE_CHECK("messages.check use", "&cUse: &f/%command% check (player)"),
    COMMAND_USE_GROUP_SET("messages.group set use", "&cUse: &f/%command% group set (group) (HP)" ),
    COMMAND_USE_INFO("messages.info use", "&cUse: &f/%command% info (player) <world>"),
    COMMAND_USE_FORMULAS("messages.formulas use", "&cUse: &f/%command% formulas <world>"),
    INVALID_CONSUMABLE("&cInvalid consumable %name%. No \"set\" or \"add\" value found."),
    INVALID_CONSUMABLE_ITEM("&cInvalid consumable item for %name%. Must be edible or a potion."),
    CONSUMABLE_NOT_EXIST("&cA consumable with that name does not exist."),
    HP_MUST_BE_NUMBER("messages.must be a number", "&cHP must be a decimal number (ie: 1.0)"),
    UNKNOWN_COMMAND("&cUnknown Command, try &e/%command% help"),
    CONSUMABLE_GIVEN("&a%player% has been given a consumable"),
    CONSUMABLE_RECEIVED("&aA consumable has been given to you"),
    IN_HAND_NOT_CONSUMABLE("messages.consumable not in hand", "&cYou must be holding a valid consumable"),
    CONSUMABLE_CREATED("&aSaved consumable with name &f%name%&a, mode &f%mode%&a and value &f%value%&a!"),
    ECONOMY_DISABLED("&cEconomy is not enabled in this server"),
    FORMULA_ACTION_TIMER_RAN_OUT("&cYou ran out of time. Action canceled."),
    FORMULA_ACTION_CANCELED("&cAction canceled."),
    FORMULA_CONFIRM_DELETE( "&fAre you sure you want to &c&ldelete &fformula \"&e%formula%&f\" in world \"&e%world%&f\"? Type &a&lYES &fbefore &e10 seconds &fto confirm."),
    FORMULA_DELETED("&aFormula \"&e%formula%&a\" for world \"&e%world%&a\" successfully deleted."),
    FORMULA_DELETE_UNKNOWN_MESSAGE("&cMessage not recognized. &fType &a&lYes &fto confirm formula deletion."),
    FORMULA_DELETE_ERROR("&cThe formula could not be deleted. Please try again."),
    FORMULA_ENTER_NEW_ORDER("&fEnter new order for the formula \"&e%formula%&f\" in world \"&e%world%&f\" before &e10 seconds&f. Type &c&lCANCEL &fto cancel."),
    FORMULA_INVALID_ORDER("&cInvalid input for new formula. Must be a number between &e%min% &cand &e%max%&c."),
    FORMULA_ORDER_CHANGED("&aFormula order successfully changed. New order is &e%order%&a."),
    FORMULA_VALID("&cDebug:&a Formula \"&7%formula%&a\" for world \"&7%world%&a\" is valid"),
    FORMULA_INVALID("&cFormula \"&7%formula%&c\" for world \"&7%world%&c\" is invalid! Please check your config"),
    FORMULAS_FOR_WORLD("&fFormulas for world \"&e%world%&f\":"),
    FORMULA_LIST_ELEMENT("&f&e%order%&f- \"&a%formula%&f\""),
    GUI_DISABLED("&cThe HP shop GUI is disabled in this server"),
    GUI_ERROR("messages.error opening gui", "&cThere was an error while trying to open de buy hp menu, talk to an admin or check console"),
    INVALID_WORLD( "&cWorld \"&7%world%&c\" does not exist."),
    GUI_OPENING("messages.opening gui", "&fOpening buy hp menu!"),
    YOU_SET_HP("&fYou just set the HP of &6%player% &fto &c%HP% HP"),
    YOU_ADD_HP("&fYou just added &6%player% &c%HP% HP"),
    SET_HP("&9%player% &fjust set your HP to &c%HP%"),
    ADD_HP("&9%player% &fjust added you &c%HP% HP"),
    PLAYER_HP_ABOVE_CAP("&cThat player''s resulting HP would be above the HP cap. HP was not modified."),
    YOUR_HP_ABOVE_CAP("messages.HP above cap", "&cYour resulting HP would be above the HP cap. Your HP was not modified"),
    HP_AUTOMATIC_SET("&aYour &cHP &ahas been set to &c%HP%"),
    CONSUMABLES_DISABLED( "&cConsumables are disabled in this server!"),
    NOT_ENOUGH_MONEY("messages.no money", "&cYou do not have enough money. You need &f%price%&c, and you have &f%balance%"),
    NOT_ENOUGH_HP("&cYou do not have &f%hp% &cspare hp"),
    GUI_ADD_HP("&c%value% HP &fhave been &aadded &fto your total health (%HP%)"),
    GUI_SET_HP("&fYour HP is now &c%HP%"),
    CANNOT_SET_HP_UNDER_0("messages.hp cannot be 0", "&cHP cannot be 0 or lower than 0"),
    GUI_REMOVE_HP("&c%value% HP &fhave been &cremoved &ffrom your total health (%HP%)"),
    CLEAR_USE("messages.hp clear use", "&cUse: &f/%command% clear (player)"),
    PLAYER_CLEARED("messages.hp cleared", "&fHP of player %player% cleared!"),
    YOUR_HP_CLEARED("&cYour HP has been cleared by an admin."),
    PLAYERS_CLEARED("messages.hp cleared all", "&fEvery player''s HP has been cleared from the file!"),
    PLAYER_CHECKED("messages.hp checked", "&aChecked and corrected HP for &f%player%"),
    PLAYERS_CHECKED("messages.hp checked all", "&aChecked and corrected HP for every player online!"),
    INV_FULL("messages.your inventory is full", "&cYour inventory is full."),
    PLAYER_INV_FULL("messages.player inventory is full", "&c%player%'s inventory is full."),
    DEFAULT_CONSUMABLE_MSG("messages.default consumable message", "&fYour HP is now %HP%"),
    INVALID_GUI_ITEM( "&cInvalid item for GUI in slot %slot%. Please check your configuration file."),
    PERMISSIONS_SYSTEM_DISABLED("&cThe permissions system is disabled in config. Enable \"&ause permissions system&c\" in config to enable this feature."),
    GROUP_NOT_FOUND("&cNo group with the name \"&f%group%&c\" found."),
    GROUP_HP_SET("&aGroup HP set to &c%HP% &afor group &f%group%. HP reload recommended."),
    GROUP_HP_UNDER_MINIMUM("&e&lWARNING:&c Group \"%group%\"'s HP (%HP%) is below the minimum (%minimum%)"),
    CANNOT_SET_GROUP_HP_UNDER_MINIMUM("&cA group's HP (%HP%) cannot be under the minimum (%minimum%)"),
    PLAYER_HP_INFO("&e%player%&7's HP info on world &e%world%&7\nFormula: \"&e%formula%&7\"\nBase HP: &a%base%&7\nGroup HP: &a%group%&7\nPermission HP: &a%permission%&7\nShop HP: &a%shop%"),
    WORLD_HAS_NO_FORMULAS("&cWorld \"&7%world%&c\" has no formulas.")
    ;

    private final String path;
    private final String dflt;

    Message(String path, String dflt){
        this.path = path;
        this.dflt = dflt;
    }

    Message(String dflt){
        this(null, dflt);
    }


    @NotNull
    @Override
    public String getPath() {
        return this.path == null ?
                "messages."+this.toString().toLowerCase(Locale.ROOT).replace("_", " ")
                :
                this.path;
    }

    @NotNull
    @Override
    public String getDefault() {
        return this.dflt;
    }
}
