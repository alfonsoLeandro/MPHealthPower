package com.github.alfonsoleandro.healthpower.managers.health.formula;

import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formula {

    private final String rawFormulaString;
    private final Set<FormulaVariable> requiredVariables;
    private final Expression expression;
    private final Map<String, Argument> arguments;

    public Formula(String rawFormulaString) {
        this.rawFormulaString = rawFormulaString;
        // Sanitize formula string
        String parsedFormulaString = rawFormulaString.replace(" ", "").toLowerCase()
                .replace("base", "b")
                .replace("shop", "s")
                .replace("permission", "p")
                .replace("group", "g");

        // Recognize which variables are needed for this case
        Pattern pattern = Pattern.compile("([bspg])");
        Matcher matcher = pattern.matcher(parsedFormulaString);
        this.requiredVariables = new HashSet<>();
        while (matcher.find()) {
            switch (matcher.group(1).toLowerCase()) {
                case "b":
                    this.requiredVariables.add(FormulaVariable.BASE);
                    break;
                case "s":
                    this.requiredVariables.add(FormulaVariable.SHOP);
                    break;
                case "p":
                    this.requiredVariables.add(FormulaVariable.PERMISSION);
                    break;
                case "g":
                    this.requiredVariables.add(FormulaVariable.GROUP);
                    break;

            }
        }

        // Prepare expression and arguments
        this.arguments = new HashMap<>();
        Argument base = new Argument("b");
        Argument shop = new Argument("s");
        Argument permission = new Argument("p");
        Argument group = new Argument("g");
        this.arguments.put("base", base);
        this.arguments.put("shop", shop);
        this.arguments.put("permission", permission);
        this.arguments.put("group", group);
        this.expression = new Expression(parsedFormulaString, base, shop, permission, group);
    }

    /**
     * Checks if this formula is written in an acceptable format.
     * @return True if the formula makes sense.
     */
    public boolean isValid() {
        for (Argument value : this.arguments.values()) {
            value.setArgumentValue(1);
        }
        double value = this.expression.calculate();
        return !Double.isNaN(value);
    }

    /**
     * Checks whether a player can have this formula applied to them.
     * @param playerHpData The raw values from this player's HP configuration.
     * @return True, if the player has all variables available or if the ones they are missing are not needed for
     * applying this formula.
     */
    public boolean canApply(PlayerHpData playerHpData) {
        if (playerHpData.hasShopHp() && playerHpData.hasPermissionHp() && playerHpData.hasGroupHp()) {
            return true;
        }
        for (FormulaVariable requiredVariable : this.requiredVariables) {
            if (requiredVariable == FormulaVariable.SHOP && !playerHpData.hasShopHp()) {
                return false;
            } else if (requiredVariable == FormulaVariable.PERMISSION && !playerHpData.hasPermissionHp()) {
                return false;
            } else if (requiredVariable == FormulaVariable.GROUP && !playerHpData.hasGroupHp()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the resulting player's HP purely from the HP formula and the player's variables.
     * @param playerHpData The raw player's variable data.
     * @param defaultIfNotPresent Default value for variables whose value is not present.
     * @return The final value of HP for a player calculated using this Formula and the player's values.
     */
    public Double calculate(PlayerHpData playerHpData, Double defaultIfNotPresent) {
        Argument base = this.arguments.get("b");
        Argument shop = this.arguments.get("s");
        Argument permission = this.arguments.get("p");
        Argument group = this.arguments.get("g");
        base.setArgumentValue(Objects.requireNonNullElse(playerHpData.baseHp(), defaultIfNotPresent));
        shop.setArgumentValue(Objects.requireNonNullElse(playerHpData.shopHp(), defaultIfNotPresent));
        permission.setArgumentValue(Objects.requireNonNullElse(playerHpData.permissionHp(), defaultIfNotPresent));
        group.setArgumentValue(Objects.requireNonNullElse(playerHpData.groupHp(), defaultIfNotPresent));

        return this.expression.calculate();
    }

    /**
     * Gets the raw formula as a String.
     * @return The formula in its String form, as written in config.
     */
    public String getRawFormulaString() {
        return this.rawFormulaString;
    }

}
