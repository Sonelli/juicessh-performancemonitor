package com.sonelli.juicessh.pluginlibrary.exceptions;

/**
 * Thrown when a plugin attempts to communicate with the plugin service
 * but the plugin service isn't bound
 */
public class ServiceNotConnectedException extends Exception {
    public ServiceNotConnectedException() {
        super("Not connected to the JuiceSSH Plugin Service.");
    }
}
