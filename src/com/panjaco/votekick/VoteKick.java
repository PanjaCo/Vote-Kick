package com.panjaco.votekick;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.ShutdownHooks.Task;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.enhance.asm.commons.Method;
import com.panjaco.votekick.events.onChatEvent;

import net.md_5.bungee.api.ChatColor;

public class VoteKick extends JavaPlugin{

	public static VoteKick plugin;
	
	public static boolean currentVote = false;
	public static boolean timerActive = false;
	
	
	public static ArrayList<Player> yesVotes = new ArrayList<Player>();
	public static ArrayList<Player> noVotes = new ArrayList<Player>();
	public static ArrayList<Player> recentKicks = new ArrayList<Player>();
	public static ArrayList<Player> immunePlayers = new ArrayList<Player>();
	
	public static Player targetPlayer;
	public static String playerName;
	
	
	public void onEnable(){
		PluginDescriptionFile descFile = getDescription();
		Logger logger = getLogger();
		logger.info("[Vote Kick] Enabled");
		
		plugin = this;
		
		//Register commands
		//getCommand("votekick").setExecutor(new startKick(this));
		
		Bukkit.getPluginManager().registerEvents(new onChatEvent(this), this);
		
		
	}
	
	public void onDisable(){
		PluginDescriptionFile descFile = getDescription();
		Logger logger = getLogger();
		logger.info("[Vote Kick] Disabled");
	}
	
	public void loadConfiguration(){
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("votekick")){
			
			if(args[0].equalsIgnoreCase("start")){
				if(args.length == 2){
					
					//Start votekick
					
					
					//Bukkit.broadcastMessage("The startKick class has started");
					
					if(!VoteKick.currentVote){

						
						playerName = args[1];
						
						try{
							targetPlayer = (Player) Bukkit.getPlayer(playerName);
						}catch(Exception e){
							sender.sendMessage(ChatColor.DARK_RED + "Player " + playerName + " could not be found!");
							return true;
						}
						
						if(immunePlayers.contains(targetPlayer) || recentKicks.contains(targetPlayer)){
							sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + targetPlayer.getDisplayName() + " is immune to being vote kicked!");
							return true;
						}
						
						currentVote = true;
						
						Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] " + sender.getName() + " has started a vote kick against: " + targetPlayer.getName());
						Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] Please vote by using /votekick <yes/no>");
						
						voteKickTimer();
						
						return true;
					}
					
				}else{
					if(sender instanceof Player){
						sender.sendMessage(ChatColor.RED + "Please specify a player");
					}else{
						sender.sendMessage("Please specify a player");
					}
				}
			}else if(args[0].equalsIgnoreCase("end")){
				if(currentVote){
					currentVote = false;
					Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] The current vote kick against: " + playerName + ", has been cancelled");
				}else{
					if(sender instanceof Player){
						sender.sendMessage(ChatColor.GREEN + "There is no current vote kick to stop");
					}else{
						sender.sendMessage("There is no current vote kick to stop");
					}
				}
			}else if(args[0].equalsIgnoreCase("yes")){
				if(currentVote){
					if(yesVotes.size() >= 1){
						if(yesVotes.contains(sender)){
							sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "You have already voted yes!");
							return true;
						}
					}else{
						if(noVotes.size() >= 1){
							if(noVotes.contains(sender)){
								noVotes.remove(sender);
								Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] " + sender.getName() + " has changed their vote");
							}
						}else{
							Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] " + sender.getName() + " has voted");
						}
						yesVotes.add((Player) sender);
						return true;
					}
				}else{
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "There is no current vote kick to vote on!");
				}
				return true;
			}else if(args[0].equalsIgnoreCase("no")){
				if(currentVote){
					if(noVotes.size() >= 1){
						if(noVotes.contains(sender)){
							sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "You have already voted no!");
							return true;
						}
					}else{
						if(yesVotes.size() >= 1){
							if(yesVotes.contains(sender)){
								yesVotes.remove(sender);
								Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] " + sender.getName() + " has changed their vote");
							}
						}else{
							Bukkit.broadcastMessage(ChatColor.GREEN + "[Vote Kick] " + sender.getName() + " has voted");
						}
						noVotes.add((Player) sender);
						return true;
					}
				}else{
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "There is no current vote kick to vote on!");
				}
				return true;
			}else if(args[0].equalsIgnoreCase("list")){
				if(currentVote && targetPlayer != null){
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick]" + ChatColor.GREEN + "" + ChatColor.UNDERLINE + " Current votes against: " + targetPlayer.getDisplayName());
					for(Player p : yesVotes){
						sender.sendMessage(ChatColor.GREEN + p.getDisplayName() + " - Yes");
					}
					for(Player p : noVotes){
						sender.sendMessage(ChatColor.RED + p.getDisplayName() + " - No");
					}
					
					return true;
				}else{
					
					if(sender instanceof Player){
						sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "There is no current vote kick!");
					}else{
						sender.sendMessage("[Vote Kick] There is no current vote kick!");
					}
				}
				
				if(immunePlayers.size() != 0){
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.GREEN + ChatColor.UNDERLINE + "All immune players:");
					for(Player p : immunePlayers){
						sender.sendMessage(ChatColor.GOLD + p.getDisplayName());
					}
				}else{
					if(sender instanceof Player){
						sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "There are no immune players!");
					}else{
						sender.sendMessage("[Vote Kick] There are no immune players!");
					}
				}
				
				
				return true;
			}else if(args[0].equalsIgnoreCase("immune")){

				Player addedPlayer;
				
				try{
					addedPlayer = Bukkit.getPlayer(args[1]);
				}catch(Exception e){
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + "That player can not be found!");
					return true;
				}
				
				
				
				if(immunePlayers.contains(addedPlayer)){
					immunePlayers.remove(addedPlayer);
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + addedPlayer.getDisplayName() + " has been removed from the immune list!");
				}else{
					immunePlayers.add(addedPlayer);
					sender.sendMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.GREEN + addedPlayer.getDisplayName() + " has been added to the immune list!");
				}
				return true;
			}	
			
		}	
		
		return true;
	}
	
	int i = 60;
	
	public void voteKickTimer(){
		

		timerActive = true;
		
		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			public void run(){
				if(currentVote){
					if(i == 60 || i == 50 || i == 40 || i == 30 || i == 20 || i == 10){
						Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Vote Kick] Current vote kick against: " + targetPlayer.getDisplayName() + " will be over in " + i + " seconds");
					}else if(i == 0){
						timerActive = false;
						Bukkit.getServer().getScheduler().cancelAllTasks();
						
						
						//This will run the method synchronously!
						Bukkit.getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run() {
								voteKickOver();
							}
						});
					}
					i--;
				}else{
					Bukkit.getServer().getScheduler().cancelAllTasks();
				}
			}
		}, 0L, 20L);
		
		i = 60;
		
	}
	
	public synchronized void voteKickOver(){
		currentVote = false;
		
		recentKicks.add(targetPlayer);
		
		kickCooldown(targetPlayer);
		
		kickCooldown(targetPlayer);
		
		Bukkit.broadcastMessage(ChatColor.AQUA + "[Vote Kick]" + ChatColor.GREEN + " The current vote kick against: " + targetPlayer.getDisplayName() + " is over");
		Bukkit.broadcastMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.GREEN + yesVotes.size() + " - " + ChatColor.RED + noVotes.size());
		if(noVotes.size() >= yesVotes.size()){
			Bukkit.broadcastMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.GREEN + targetPlayer.getDisplayName() + " will not be kicked!");
		}else{
			Bukkit.broadcastMessage(ChatColor.AQUA + "[Vote Kick] " + ChatColor.RED + targetPlayer.getDisplayName() + " will be kicked!");
			targetPlayer.kickPlayer("You have been vote kicked");
		}
		
		yesVotes.clear();
		noVotes.clear();
		
		playerName = null;
		targetPlayer = null;
	}
	
	public synchronized void kickCooldown(Player p){
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				recentKicks.remove(p);
			}
		}, 1200L);
	}
	
}
