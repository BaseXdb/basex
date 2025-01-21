package org.basex.util.log;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

import org.basex.util.*;

/**
 * Log targets.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
enum LogTarget {
  /** Standard output. */
  STDOUT {
    @Override
    void write(final Log log, final LogEntry entry) {
      Util.println(entry);
    }
  },
  /** Standard error.*/
  STDERR {
    @Override
    void write(final Log log, final LogEntry entry) {
      Util.errln(entry);
    }
  },
  /** Database directory. */
  DATA {
    @Override
    void write(final Log log, final LogEntry entry) throws IOException {
      log.write(entry);
    }
  },
  /** SLF4J logging.*/
  SLF4J {
    @Override
    void write(final Log log, final LogEntry entry) {
      if(slf4j != null) slf4j.accept(entry.type, entry.toString());
    }
  };

  /** SLF4J logger ({@code null} if not available). */
  private static BiConsumer<String, String> slf4j;

  static {
    final Class<?> cLoggerFactory = Reflect.find("org.slf4j.LoggerFactory");
    final Class<?> cLogger = Reflect.find("org.slf4j.Logger");
    final Method mLogger = Reflect.method(cLoggerFactory, "getLogger", Class.class);
    final Object logger = Reflect.invoke(mLogger, null, Prop.NAME);
    slf4j = (type, text) -> {
      final String level = Strings.eqic(type, "trace", "debug", "warn", "error") ?
        type.toLowerCase(Locale.ENGLISH) : "info";
      final Method method = Reflect.method(cLogger, level, String.class);
      if(method != null) {
        Reflect.invoke(method, logger, text);
      } else {
        Util.errln("SLF4J logger not available.");
        slf4j = null;
      }
    };
  }

  /**
   * Writes a log entry.
   * @param log logger
   * @param entry log entry
   * @throws IOException I/O exception
   */
  abstract void write(Log log, LogEntry entry) throws IOException;
}
