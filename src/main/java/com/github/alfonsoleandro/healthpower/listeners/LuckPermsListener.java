package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.HealthPower;
import com.github.alfonsoleandro.healthpower.managers.health.HPManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class LuckPermsListener {

    private final HealthPower plugin;
    private final HPManager hpManager;
    private final Map<String, BukkitTask> debouncer = new HashMap<>();

    public LuckPermsListener(HealthPower plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.hpManager = plugin.getHpManager();
        luckPerms.getEventBus().subscribe(plugin, NodeMutateEvent.class, this::onNodeMutate);
    }

    private void onNodeMutate(NodeMutateEvent nodeMutateEvent) {
        synchronized (this.debouncer) {
            PermissionHolder target = nodeMutateEvent.getTarget();
            if (this.debouncer.containsKey(getKey(target))) {
                BukkitTask awaitingTask = this.debouncer.remove(getKey(target));
                awaitingTask.cancel();
            }
            prepareCheck(target);
        }
    }

    private void prepareCheck(PermissionHolder target) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (target instanceof Group) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    this.hpManager.checkAndCorrectHP(player);
                }
            } else {
                Player player = Bukkit.getPlayerExact(target.getFriendlyName());
                if (player != null && player.isOnline()) {
                    this.hpManager.checkAndCorrectHP(player);
                }
            }
            synchronized (this.debouncer) {
                this.debouncer.remove(getKey(target));
            }
        }, 1);

        this.debouncer.put(getKey(target), task);
    }

    private String getKey(PermissionHolder holder) {
        if (holder instanceof User user) {
            return "user:" + user.getUniqueId();
        } else if (holder instanceof Group group) {
            return "group:" + group.getName().toLowerCase();
        }
        return "other";
    }


}
