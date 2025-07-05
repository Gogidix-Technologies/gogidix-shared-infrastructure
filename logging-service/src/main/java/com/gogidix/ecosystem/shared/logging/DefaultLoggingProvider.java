package com.gogidix.ecosystem.shared.logging;

import org.slf4j.LoggerFactory;

/**
 * Default implementation of LoggingProvider using SLF4J.
 */
public class DefaultLoggingProvider implements LoggingProvider {

    @Override
    public Logger getLogger(Class<?> clazz) {
        return new SLF4JLoggerAdapter(LoggerFactory.getLogger(clazz));
    }

    @Override
    public Logger getLogger(String name) {
        return new SLF4JLoggerAdapter(LoggerFactory.getLogger(name));
    }
}
