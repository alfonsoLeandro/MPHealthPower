package com.github.alfonsoleandro.healthpower.listeners;

import com.github.alfonsoleandro.healthpower.utils.Settings;
import com.github.alfonsoleandro.mputils.guis.events.GUIButtonClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NavigableGUIClickListener implements Listener {

    @EventHandler
    public void onGUIClick(GUIButtonClickEvent event) {
        // check the cause of the event is a GUI from this plugin
        if (!event.getGui().getGuiTags().startsWith(Settings.TAG_PREFIX)) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot >= event.getGui().getSize()) {
            return;
        }
        event.setCancelled(true);

        //NavBar click
        if (event.isButtonClick() && event.buttonMetCondition()) {
            String clickedButtonTags = event.getClickedButton().getButtonTags();
            if (clickedButtonTags.equals(Settings.PREVIOUS_PAGE_BUTTON_TAG)) {
                event.getGui().preparePage(((Player) event.getWhoClicked()), event.getPage() - 1);

            } else if (clickedButtonTags.equalsIgnoreCase(Settings.NEXT_PAGE_BUTTON_TAG)) {
                event.getGui().preparePage(((Player) event.getWhoClicked()), event.getPage() + 1);
            }
        }
    }

}
