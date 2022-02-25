package com.github.alfonsoleandro.healthpower.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayersOnGUIsManager {

    private static PlayersOnGUIsManager instance;
    final private List<String> players;

    private PlayersOnGUIsManager(){
        this.players = new ArrayList<>();
    }

    public static PlayersOnGUIsManager getInstance(){
        if(instance == null){
            instance = new PlayersOnGUIsManager();
        }
        return instance;
    }


    public void add(String playerName){
        this.players.add(playerName);
    }

    public boolean isInGUI(String playerName){
        return this.players.contains(playerName);
    }

    public void remove(String playerName){
        this.players.remove(playerName);
    }

    public void removeAll(){
        for(String p : this.players){
            Player player = Bukkit.getPlayer(p);
            if(player != null){
                player.closeInventory();
            }
        }
        this.players.clear();
    }





}
