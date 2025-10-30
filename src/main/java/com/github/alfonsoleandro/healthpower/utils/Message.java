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
    COMMAND_USE_HP_MODIFY("messages.modify use", "&cUse: &f/%command% modify (set/add) (player) (HP)"),
    COMMAND_USE_CONSUMABLE("messages.consumable use", "&cUse: &f/%command% consumable (give) (player) (consumable_name) &cOR &f/%command% consumable (set) (consumable_name) (add/set) (amount)"),
    COMMAND_USE_CLEAR("messages.clear use", "&cUse: &f/%command% clear (player)"),
    COMMAND_USE_CHECK("messages.check use", "&cUse: &f/%command% check (player)"),
    COMMAND_USE_GROUP_SET("messages.group set use", "&cUse: &f/%command% group set (group) (HP)"),
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
    FORMULA_ADD_ENTER_NEW_STRING("&fEnter the new formula for world \"&e%world%&f\" before &e%seconds% seconds&f. Type &c&lCANCEL &fto cancel."),
    FORMULA_ADD_ENTER_NEW_ORDER("&fEnter the order for your new formula for world \"&e%world%&f\" before &e%seconds% seconds&f. Type &c&lCANCEL &fto cancel."),
    FORMULA_CANNOT_SAVE("&cPlease enter a valid formula value before saving."),
    FORMULA_CANNOT_SAVE_INVALID("&cCould not save Formula: invalid formula"),
    FORMULA_CANNOT_SAVE_INVALID_ORDER("&cCould not save Formula: invalid order"),
    FORMULA_CONFIRM_DELETE("&fAre you sure you want to &c&ldelete &fformula \"&e%formula%&f\" in world \"&e%world%&f\"? Type &a&lYES &fbefore &e%seconds% seconds &fto confirm."),
    FORMULA_DELETED("&aFormula \"&e%formula%&a\" for world \"&e%world%&a\" successfully deleted."),
    FORMULA_DELETE_UNKNOWN_MESSAGE("&cMessage not recognized. &fType &a&lYes &fto confirm formula deletion."),
    FORMULA_DELETE_ERROR("&cThe formula could not be deleted. Please try again."),
    FORMULA_EDITING_BLOCKED_COMMANDS("&cPlease finish editing formulas before sending new HP commands."),
    FORMULA_ENTER_NEW_ORDER("&fEnter new order for the formula \"&e%formula%&f\" in world \"&e%world%&f\" before &e%seconds% seconds&f. Type &c&lCANCEL &fto cancel."),
    FORMULA_INVALID_ORDER("&cInvalid input for new formula order. Must be a number between &e%min% &cand &e%max%&c."),
    FORMULA_INPUT_INVALID("&cInvalid input for new formula. Must be a valid mathematical expression. Available variables: &fbase&c, &fpermission&c, &fgroup &cand &fshop"),
    FORMULA_ORDER_CHANGED("&aFormula order successfully changed. New order is &e%order%&a."),
    FORMULA_ORDER_SET("&aFormula order successfully set. New order is &e%order%&a."),
    FORMULA_SAVED("&aNew formula \"&e%formula%&a\" for world \"&e%world%&a\" in order \"&e%order%&a\" successfully saved."),
    FORMULA_VALID("&cDebug:&a Formula \"&7%formula%&a\" for world \"&7%world%&a\" is valid"),
    FORMULA_VALID_SHOP("&cDebug:&a Formula \"&7%formula%&a\" for shop item in slot \"&7%slot%&a\" is valid"),
    FORMULA_VALUE_SET("&aFormula value successfully set. New value is \"&e%formula%&a\""),
    FORMULA_INVALID("&cFormula \"&7%formula%&c\" for world \"&7%world%&c\" is invalid! Please check your config"),
    FORMULA_INVALID_SHOP("&cFormula \"&7%formula%&c\" for shop item in slot \"&7%slot%&c\" is invalid! Please check your gui file"),
    FORMULAS_FOR_WORLD("&fFormulas for world \"&e%world%&f\":"),
    FORMULA_LIST_ELEMENT("&f&e%order%&f- \"&a%formula%&f\""),
    FORMULA_LIST_GLOBAL("&a&lGlobal"),
    FORMULA_LIST_GLOBAL_WORLD_NAME("&aGlobal"),
    INVALID_GUI_ITEM_PRICE("&cGUI item in slot \"&7%slot%&c\" has no price or a valid formula. Item will not be added to the shop"),
    INVALID_WORLD("&cWorld \"&7%world%&c\" does not exist."),
    YOUR_HP_ABOVE_CAP("messages.HP above cap", "&cYour resulting HP would be above the HP cap. Your HP was not modified"),
    YOUR_HP_BELOW_FLOOR("messages.HP below floor", "&cYour resulting HP would be below the HP floor. Your HP was not modified"),
    HP_AUTOMATIC_SET("&aYour &cHP &ahas been set to &c%HP%"),
    CONSUMABLES_DISABLED("&cConsumables are disabled in this server!"),
    NOT_ENOUGH_MONEY("messages.no money", "&cYou do not have enough money. You need &f%price%&c, and you have &f%balance%"),
    NOT_ENOUGH_HP("&cYou do not have &f%hp% &cspare hp"),
    PLAYER_CLEARED("messages.hp cleared", "&fHP of player %player% cleared!"),
    YOUR_HP_CLEARED("&cYour HP has been cleared by an admin."),
    PLAYERS_CLEARED("messages.hp cleared all", "&fEvery player''s HP has been cleared from the file!"),
    PLAYER_CHECKED("messages.hp checked", "&aChecked and corrected HP for &f%player%"),
    PLAYERS_CHECKED("messages.hp checked all", "&aChecked and corrected HP for every player online!"),
    PLAYER_INV_FULL("messages.player inventory is full", "&c%player%'s inventory is full."),
    DEFAULT_CONSUMABLE_MSG("messages.default consumable message", "&fYour HP is now %HP%"),
    INVALID_GUI_ITEM("&cInvalid item for GUI item in slot %slot%. Please check your configuration file."),
    PERMISSIONS_SYSTEM_DISABLED("&cThe permissions system is disabled in config. Enable \"&ause permissions system&c\" in config to enable this feature."),
    SHOP_DISABLED("&cThe HP shop is disabled in this server"),
    SHOP_ITEM_INVALID_REQUIREMENT("&cInvalid requirement for GUI item in slot %slot% for hp type \"&a%type%&c\", received value \"&f%received%&c\"."),
    SHOP_ITEM_NO_PERMISSION("&cYou have no permission to access this item."),
    SHOP_ITEM_REQUIREMENTS_NOT_MET("&cYou do not meet the requirements for this item."),
    GROUP_NOT_FOUND("&cNo group with the name \"&f%group%&c\" found."),
    GROUP_HP_SET("&aGroup HP set to &c%HP% &afor group &f%group%. HP reload recommended."),
    PLAYER_HP_INFO("&e%player%&7's HP info on world &e%world%&7\nFormula: \"&e%formula%&7\"\nBase HP: &a%base%&7\nGroup HP: &a%group%&7\nPermission HP: &a%permission%&7\nShop HP: &a%shop%"),
    WORLD_HAS_NO_FORMULAS("&cWorld \"&7%world%&c\" has no formulas."),
    YOU_ADD_HP("&7You just added &e%player% &c%amount% HP &7to their &e%type% &7HP"),
    YOU_SET_HP("&7You just set &e%player%&f's &e%type% &7HP to &c%amount%"),
    YOUR_HP_ADDED("&aYou have been added &c%amount% &aHP to your &e%type% &aHP"),
    YOUR_HP_SET("&7Your &e%type% &7HP has been set to &c%amount%"),
    SHOP_ADD_HP("&c%amount% &fHP has been added to your shop HP"),
    SHOP_SET_HP("&fYour shop HP has been set to %amount%"),
    SHOP_REMOVE_HP("&c%amount% &fHP has been removed from your HP")
    ;

    private final String path;
    private final String dflt;

    Message(String path, String dflt) {
        this.path = path;
        this.dflt = dflt;
    }

    Message(String dflt) {
        this(null, dflt);
    }


    @NotNull
    @Override
    public String getPath() {
        return this.path == null ?
                "messages." + this.toString().toLowerCase(Locale.ROOT).replace("_", " ")
                :
                this.path;
    }

    @NotNull
    @Override
    public String getDefault() {
        return this.dflt;
    }
}
