package me.danieli1818.drchannels.api;

/**
 * Thrown when a channel definition cannot be turned into a channel. The message is shown to admins.
 */
public class InvalidChannelException extends Exception {

    public InvalidChannelException(String message) {
        super(message);
    }
}
