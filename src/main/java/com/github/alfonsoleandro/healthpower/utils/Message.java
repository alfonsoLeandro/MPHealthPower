package com.github.alfonsoleandro.healthpower.utils;

import com.github.alfonsoleandro.mputils.message.MessageEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum Message implements MessageEnum {
    NO("messages.no", "&cNo"),
    YES("messages.yes", "&aYes"),
    YOURSELF("messages.yourself", "yourself"),

    NO_PERMISSION("messages.no permission", "&cNo permission"),
    CANNOT_SEND_CONSOLE("&cThat command can only be sent by a player."),
    PLAYER_NOT_ONLINE("messages.not online", "&cThat player is not online"),
    COMMAND_USE_HP_MODIFY("messages.modify use", "&cUse: &f/%command% %what% (player) (HP)"),
    COMMAND_USE_CONSUMABLE("messages.consumable use", "&cUse: &f/%command% consumable (give) (player) (consumable_name) &cOR &f/%command% consumable (set) (consumable_name) (add/set) (amount)"),
    INVALID_CONSUMABLE("messages.invalid consumable", "&cInvalid consumable %name%. No \"set\" or \"add\" value found."),
    INVALID_CONSUMABLE_ITEM("messages.invalid consumable item", "&cInvalid consumable item for %name%. Must be edible or a potion."),
    HP_MUST_BE_NUMBER("messages.must be a number", "&cHP must be a decimal number (ie: 1.0)"),
    UNKNOWN_COMMAND("messages.unknown command", "&cUnknown Command, try &e/%command% help"),
    CONSUMABLE_GIVEN("messages.consumable given", "&aA consumable has been given to you"),
    IN_HAND_NOT_CONSUMABLE("messages.consumable not in hand", "&cYou must be holding the consumable"),
    GUI_DISABLED("&cThe HP shop GUI is disabled in this server"),
    GUI_ERROR("messages.error opening gui", "&cThere was an error while trying to open de buy hp menu, talk to an admin or check console"),
    GUI_OPENING("messages.opening gui", "&fOpening buy hp menu!"),
    YOU_SET_HP("messages.you set HP", "&fYou just set the HP of &6%player% &fto &c%HP% HP"),
    YOU_ADD_HP("messages.you add HP", "&fYou just added &6%player% &c%HP% HP"),
    SET_HP("messages.set HP", "&9%player% &fjust set your HP to &c%HP%"),
    ADD_HP("messages.add HP", "&9%player% &fjust added you &c%HP% HP"),
    PLAYER_HP_ABOVE_CAP("messages.player HP above cap", "&cThat player''s resulting HP would be above the HP cap. HP was not modified."),
    YOUR_HP_ABOVE_CAP("messages.HP above cap", "&cYour resulting HP would be above the HP cap. Your HP was not modified"),
    HP_AUTOMATIC_SET("messages.hp automatic set", "&aYour &cHP &ahas been set to &c%HP%"),
    CONSUMABLES_DISABLED("messages.consumables disabled", "&cConsumables are disabled in this server!"),
    NOT_ENOUGH_MONEY("messages.no money", "&cYou do not have enough money. You need &f%price%&c, and you have &f%balance%"),
    NOT_ENOUGH_HP("messages.not enough hp", "&cYou do not have &f%hp% &cspare hp"),
    GUI_ADD_HP("messages.gui add HP", "&c%value% HP &fhave been &aadded &fto your total health (%HP%)"),
    GUI_SET_HP("messages.gui set HP", "&fYour HP is now &c%HP%"),
    CANNOT_SET_HP_UNDER_0("messages.hp cannot be 0", "&cHP cannot be 0 or lower than 0"),
    GUI_REMOVE_HP("messages.gui remove HP", "&c%value% HP &fhave been &cremoved &ffrom your total health (%HP%)"),
    PLAYER_CLEARED("messages.hp cleared", "&fHP of player %player% cleared!"),
    PLAYERS_CLEARED("messages.hp cleared all", "&fEvery player''s HP has been cleared from the file!"),
    PLAYER_CHECKED("messages.hp checked", "&aChecked and corrected HP for &f%player%"),
    PLAYERS_CHECKED("messages.hp checked all", "&aChecked and corrected HP for every player online!"),
    INV_FULL("messages.your inventory is full", "&cYour inventory is full."),
    DEFAULT_CONSUMABLE_MSG("messages.default consumable message", "&fYour HP is now %HP%");

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
