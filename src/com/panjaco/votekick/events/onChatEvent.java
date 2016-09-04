package com.panjaco.votekick.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import com.panjaco.votekick.VoteKick;

import net.md_5.bungee.api.ChatColor;

public class onChatEvent implements Listener {

	private VoteKick plugin;
	
	public onChatEvent(VoteKick pl){
		plugin = pl;
	}
	
	@EventHandler
	public void onSentMessage(PlayerChatEvent event){
		
		Player sender = (Player) event.getPlayer();
		String message = event.getMessage();
		
		if(plugin.currentVote){
			if(sender instanceof Player){
				sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "Chat is currently muted for a vote kick");
				event.setCancelled(true);
			}
		}
		
		
	}
	
}
