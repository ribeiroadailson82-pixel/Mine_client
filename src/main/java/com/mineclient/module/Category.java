package com.mineclient.module;

public enum Category {
    COMBAT("Combate"),
    PLAYER("Jogador"),
    RENDER("Visual");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
