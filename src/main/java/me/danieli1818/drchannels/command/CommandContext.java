package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.ChannelRegistry;
import me.danieli1818.drchannels.chat.ChannelChatService;
import me.danieli1818.drchannels.chatter.ChatterManager;
import me.danieli1818.drchannels.util.Messages;

/**
 * Services shared by all subcommands.
 */
public record CommandContext(ChannelRegistry channels, ChatterManager chatters, ChannelChatService chat, Messages messages) {
}
