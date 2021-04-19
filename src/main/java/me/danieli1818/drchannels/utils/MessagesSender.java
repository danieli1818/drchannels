package me.danieli1818.drchannels.utils;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class MessagesSender {

	private String prefix;
	
	private static MessagesSender instance;
	
	private MessagesSender(String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
	}
	
	public static MessagesSender getInstance(String prefix) {
		if (instance == null) {
			instance = new MessagesSender(prefix);
		}
		return instance;
	}
	
	public static MessagesSender getInstance() {
		return getInstance(null);
	}
	
	public void sendMessage(String message, CommandSender sender) {
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.prefix + message));
	}
	
	public void sendMessage(String[] messages, CommandSender sender) {
		sender.sendMessage(this.prefix);
		for (int i = 0; i < messages.length; i++) {
			messages[i] = ChatColor.translateAlternateColorCodes('&', messages[i]);
		}
		sender.sendMessage(messages);
	}
	
	public void sendMessage(BaseComponent message, CommandSender sender) {
		TextComponent newMessage = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', this.prefix + message.toPlainText())));
		newMessage.setClickEvent(message.getClickEvent());
		newMessage.setHoverEvent(message.getHoverEvent());
		sender.spigot().sendMessage(newMessage);
	}
	
	public void sendDontHavePermissionErrorMessage(String permission, CommandSender sender) {
		sendMessage("Error! You don't have the permission to run this command! (" + permission + ")", sender);
	}
	
}
