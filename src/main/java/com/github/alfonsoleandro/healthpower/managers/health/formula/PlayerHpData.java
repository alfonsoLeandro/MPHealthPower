package com.github.alfonsoleandro.healthpower.managers.health.formula;

public record PlayerHpData(Double baseHp, Double groupHp,  Double permissionHp, Double shopHp) {

    public boolean hasBaseHp() {
        return this.baseHp != null;
    }

    public boolean hasGroupHp() {
        return this.groupHp != null;
    }

    public boolean hasPermissionHp() {
        return this.permissionHp != null;
    }

    public boolean hasShopHp() {
        return this.shopHp != null;
    }

}
