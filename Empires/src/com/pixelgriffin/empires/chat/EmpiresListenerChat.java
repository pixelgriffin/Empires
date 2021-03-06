package com.pixelgriffin.empires.chat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.dthielke.Herochat;
import com.dthielke.api.ChatResult;
import com.dthielke.api.Chatter;
import com.dthielke.api.event.ChannelChatEvent;
import com.pixelgriffin.empires.Empires;
import com.pixelgriffin.empires.EmpiresConfig;
import com.pixelgriffin.empires.enums.Relation;
import com.pixelgriffin.empires.handler.EmpiresPlayer;
import com.pixelgriffin.empires.handler.Joinable;
import com.pixelgriffin.empires.handler.PlayerHandler;

public class EmpiresListenerChat implements Listener {
	
	private Set<Relation> allyRelations = new HashSet<Relation>();
	
	public EmpiresListenerChat() {
		Herochat.getChannelManager().addChannel(new AllyChat());
		Herochat.getChannelManager().addChannel(new KingdomChat());
		
		allyRelations.add(Relation.US);
		allyRelations.add(Relation.E_K);
		allyRelations.add(Relation.ALLY);
	}
	
	@EventHandler
	public void onChannelChat(ChannelChatEvent evt) {
		if(evt.getChannel().getName().equalsIgnoreCase("ally")) {
			onAllyChat(evt);
		} else if(evt.getChannel().getName().equalsIgnoreCase("kingdom")) {
			onCityChat(evt);
		} else {
			onGenericChat(evt);
		}
	}
	
	private void onGenericChat(ChannelChatEvent evt) {
		String msg = evt.getFormat();
		System.out.println("format: ");
		
		if(msg.contains("{default}")) {
			msg = msg.replace("{default}", EmpiresConfig.m_defaultChatFormat);
		}
		
		//msg = msg.replace("{msg}", evt.getMessage());
		//msg = msg.replace("{color}", evt.getChannel().getColor().toString());
		//msg = msg.replace("{nick}", evt.getChannel().getNick());
		//msg = msg.replace("{sender}", sender.getPlayer().getDisplayName());
		EmpiresPlayer ep = Empires.m_playerHandler.getPlayer(evt.getChatter().getPlayer().getUniqueId());
		
		//String ourTitle = Empires.m_playerHandler.getPlayerTitle(evt.getChatter().getPlayer().getUniqueId());
		String ourTitle = ep.getTitle();
		if(!ourTitle.isEmpty()) {
			ourTitle = ourTitle + " ";
		}
		msg = msg.replace("{title}", ourTitle);
		msg = msg.replace("{role}", ep.getRole().getPrefix());
		
		//Joinable j = Empires.m_joinableHandler.getJoinable(Empires.m_playerHandler.getPlayerJoinedCivilization(evt.getChatter().getPlayer().getUniqueId()));
		Joinable j = ep.getJoined();
		String ourJoined = PlayerHandler.m_defaultCiv;
		if(j != null) {
			ourJoined = j.getDisplayName();
		}
		if(ourJoined.equals(PlayerHandler.m_defaultCiv)) {
			ourJoined = "";
		} else {
			ourJoined = ourJoined + " ";
		}
		msg = msg.replace("{joined}", ourJoined);
		
		evt.setFormat(msg);
	}
	
	private void onAllyChat(ChannelChatEvent evt) {
		Chatter sender = evt.getChatter();
		HashSet<Player> recv = getAllyRecipients(sender.getPlayer());
		
		String msg = evt.getFormat();
		msg = msg.replace("{msg}", evt.getMessage());
		msg = msg.replace("{color}", evt.getChannel().getColor().toString());
		msg = msg.replace("{nick}", evt.getChannel().getNick());
		msg = msg.replace("{sender}", sender.getPlayer().getDisplayName());
		
		EmpiresPlayer ep = Empires.m_playerHandler.getPlayer(evt.getChatter().getPlayer().getUniqueId());
		
		//String ourTitle = Empires.m_playerHandler.getPlayerTitle(evt.getChatter().getPlayer().getUniqueId());
		String ourTitle = ep.getTitle();
		if(!ourTitle.isEmpty()) {
			ourTitle = ourTitle + " ";
		}
		msg = msg.replace("{title}", ourTitle);
		msg = msg.replace("{role}", ep.getRole().getPrefix());
		//Joinable j = Empires.m_joinableHandler.getJoinable(Empires.m_playerHandler.getPlayerJoinedCivilization(evt.getChatter().getPlayer().getUniqueId()));
		Joinable j = ep.getJoined();
		String ourJoined = PlayerHandler.m_defaultCiv;
		if(j != null) {
			ourJoined = j.getDisplayName();
		}
		if(ourJoined.equals(PlayerHandler.m_defaultCiv)) {
			ourJoined = "";
		} else {
			ourJoined = ourJoined + " ";
		}
		msg = msg.replace("{joined}", ourJoined);
		
		for(Player listen : recv) {
			listen.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
		
		evt.setResult(ChatResult.FAIL);
		recv.clear();
		//evt.setChannel(Herochat.getChannelManager().getChannel("void"));
	}
	
	private HashSet<Player> getAllyRecipients(Player p) {
		HashSet<Player> allies = new HashSet<Player>();
		UUID pid = p.getUniqueId();
		
		///String joinedName = Empires.m_playerHandler.getPlayerJoinedCivilization(pid);
		Joinable joined = Empires.m_playerHandler.getPlayer(pid).getJoined();
		//if(!joinedName.equalsIgnoreCase(PlayerHandler.m_defaultCiv)) {
		if(joined != null) {
			//Joinable joined = Empires.m_joinableHandler.getJoinable(joinedName);
			
			//Set<String> relations = Empires.m_joinableHandler.getJoinableRelationNameSet(joinedName);
			Set<String> relations = joined.getRelationWishSet();
			ArrayList<UUID> players = new ArrayList<UUID>();
			
			for(String relation : relations) {
				Joinable other = Empires.m_joinableHandler.getJoinable(relation);
				if(allyRelations.contains(joined.getRelation(other))) {
					players.addAll(other.getJoined());
					
					Player temp;
					for(UUID player : players) {
						temp = Bukkit.getPlayer(player);
						if(temp != null) {
							allies.add(temp);
						}
					}
				}
			}
			
			players.clear();
			players.addAll(joined.getJoined());
			
			Player temp;
			for(UUID player : players) {
				temp = Bukkit.getPlayer(player);
				if(temp != null) {
					allies.add(temp);
				}
			}
		}
		
		return allies;
	}
	
	private void onCityChat(ChannelChatEvent evt) {
		Chatter sender = evt.getChatter();
		HashSet<Player> recv = (HashSet<Player>) getCityRecipients(sender.getPlayer());
		
		String msg = evt.getFormat();
		msg = msg.replace("{msg}", evt.getMessage());
		msg = msg.replace("{color}", evt.getChannel().getColor().toString());
		msg = msg.replace("{nick}", evt.getChannel().getNick());
		msg = msg.replace("{sender}", sender.getPlayer().getDisplayName());
		
		EmpiresPlayer ep = Empires.m_playerHandler.getPlayer(evt.getChatter().getPlayer().getUniqueId());
		
		//String ourTitle = Empires.m_playerHandler.getPlayerTitle(evt.getChatter().getPlayer().getUniqueId());
		String ourTitle = ep.getTitle();
		if(!ourTitle.isEmpty()) {
			ourTitle = ourTitle + " ";
		}
		msg = msg.replace("{title}", ourTitle);
		msg = msg.replace("{role}", ep.getRole().getPrefix());
		//Joinable j = Empires.m_joinableHandler.getJoinable(Empires.m_playerHandler.getPlayerJoinedCivilization(evt.getChatter().getPlayer().getUniqueId()));
		Joinable j = ep.getJoined();
		String ourJoined = PlayerHandler.m_defaultCiv;
		if(j != null) {
			ourJoined = j.getDisplayName();
		}
		if(ourJoined.equals(PlayerHandler.m_defaultCiv)) {
			ourJoined = "";
		} else {
			ourJoined = ourJoined + " ";
		}
		msg = msg.replace("{joined}", ourJoined);
		
		for(Player listen : recv) {
			listen.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}
		
		evt.setResult(ChatResult.FAIL);
		recv.clear();
		//evt.setChannel(Herochat.getChannelManager().getChannel("void"));
	}
	
	private Set<Player> getCityRecipients(Player sen) {
		Set<Player> ret = new HashSet<Player>();
		
		//gather joined name
		//String joinedName = Empires.m_playerHandler.getPlayerJoinedCivilization(sen.getUniqueId());
		Joinable joined = Empires.m_playerHandler.getPlayer(sen.getUniqueId()).getJoined();
		
		//is it wilderness?
		//if(!joinedName.equalsIgnoreCase(PlayerHandler.m_defaultCiv)) {
		if(joined != null) {
			//Joinable joined = Empires.m_joinableHandler.getJoinable(joinedName);
			
			//gather our players
			//ArrayList<UUID> players = Empires.m_joinableHandler.getJoinableJoinedPlayers(joinedName);
			ArrayList<UUID> players = joined.getJoined();
			
			Player p;
			for(UUID player : players) {
				//if they are online
				p = Bukkit.getPlayer(player);
				
				if(p != null) {
					ret.add(p);//send them the message
				}
			}
		}
		
		//return a set of recipients
		return ret;
	}
}