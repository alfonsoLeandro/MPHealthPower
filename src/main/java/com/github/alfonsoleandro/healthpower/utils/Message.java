package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.mputils.misc.MessageEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Message implements MessageEnum {
    NO("config.messages.no", "&cNo"),
    YES("config.messages.yes", "&aYes"),
    YOURSELF("config.messages.yourself", "yourself"),

    NO_PERMISSION("config.messages.no permission", "&cNo permission"),
    PLAYER_NOT_ONLINE("config.messages.not online", "&cThat player is not online"),
    COMMAND_USE("config.messages.use", "&cUse: &f/%command% %what% (player) (HP)"),
    HP_MUST_BE_NUMBER("config.messages.must be a number", "&cHP must be a double number (ie: 1.0)"),
    UNKNOWN_COMMAND("config.messages.unknown command", "&cUnknown Command, try &e/%command% help"),
    CONSUMABLE_GIVEN("config.messages.consumable given", "&aA consumable has been given to you"),
    IN_HAND_NOT_CONSUMABLE("config.messages.consumable not in hand", "&cYou must be holding the consumable"),
    GUI_ERROR("config.messages.error opening gui", "&cThere was an error while trying to open de buy hp menu, talk to an admin or check console"),
    GUI_OPENING("config.messages.opening gui", "&fOpening buy hp menu!"),
    YOU_SET_HP("config.messages.you set HP", "&fYou just set the HP of &6%player% &fto &c%HP% HP"),
    YOU_ADD_HP("config.messages.you add HP", "&fYou just added &6%player% &c%HP% HP"),
    SET_HP("config.messages.set HP", "&9%player% &fjust set your HP to &c%HP%"),
    ADD_HP("config.messages.add HP", "&9%player% &fjust added you &c%HP% HP"),
    PLAYER_HP_ABOVE_CAP("config.messages.player HP above cap", "&cThat player''s resulting HP would be above the HP cap. HP was not modified."),
    YOUR_HP_ABOVE_CAP("config.messages.HP above cap", "&cYour resulting HP would be above the HP cap. Your HP was not modified"),
    HP_AUTOMATIC_SET("config.messages.hp automatic set", "&aYour &cHP &ahas been set to &c%HP%"),
    CONSUMABLES_DISABLED("config.messages.consumables disabled", "&cConsumables are disabled in this server!"),
    NOT_ENOUGH_MONEY("config.messages.no money", "&cYou do not have enough money. You need &f%price%&c, and you have &f%balance%"),
    NOT_ENOUGH_HP("config.messages.not enough hp", "&cYou do not have &f%hp% &cspare hp"),
    GUI_ADD_HP("config.messages.gui add HP", "&c%value% HP &fhave been &aadded &fto your total health (%HP%)"),
    GUI_SET_HP("config.messages.gui set HP", "&fYour HP is now &c%HP%"),
    CANNOT_SET_HP_UNDER_0("config.messages.hp cannot be 0", "&cHP cannot be 0 or lower than 0"),
    GUI_REMOVE_HP("config.messages.gui remove HP", "&c%value% HP &fhave been &cremoved &ffrom your total health (%HP%)"),
    PLAYER_CLEARED("config.messages.hp cleared", "&fHP of player %player% cleared!"),
    PLAYERS_CLEARED("config.messages.hp cleared all", "&fEvery player''s HP has been cleared from the file!"),
    PLAYER_CHECKED("config.messages.hp checked", "&aChecked and corrected HP for &f%player%"),
    PLAYERS_CHECKED("config.messages.hp checked all", "&aChecked and corrected HP for every player online!"),
    INV_FULL("config.messages.your inventory is full", "&cYour inventory is full."),
    DEFAULT_CONSUMABLE_MSG("config.messages.default consumable message", "&fYour HP is now %HP%");

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
                "config.messages."+this.toString().toLowerCase(Locale.ROOT).replace("_", " ")
                :
                this.path;
    }

    @NotNull
    @Override
    public String getDefault() {
        return this.dflt;
    }
}
