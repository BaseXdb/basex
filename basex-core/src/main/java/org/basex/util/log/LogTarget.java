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
      if(SLF4J_LOGGER != null) {
        SLF4J_LOGGER.accept(entry.type, entry.toString());
      } else if(!warned) {
        warned = true;
        Util.errln("SLF4J logger not available.");
      }
    }
  };

  /** SLF4J logger ({@code null} if not available). */
  private static final BiConsumer<String, String> SLF4J_LOGGER = slf4j();
  /** Indicates if the user has been informed about the missing SLF4J logger. */
  private static boolean warned;

  /**
   * Initializes the SLF4J logger.
   * @return logger, or {@code null} if SLF4J is not available
   */
  private static BiConsumer<String, String> slf4j() {
    try {
      final Class<?> factory = Class.forName("org.slf4j.LoggerFactory");
      final Class<?> clazz = Class.forName("org.slf4j.Logger");
      final Object logger = factory.getMethod("getLogger", String.class).invoke(null, Prop.NAME);

      final Map<String, Method> methods = new HashMap<>();
      for(final String level : new String[] { "trace", "debug", "info", "warn", "error" }) {
        methods.put(level, clazz.getMethod(level, String.class));
      }
      return (type, text) -> {
        final String level = Strings.eqic(type, "trace", "debug", "warn", "error") ?
          type.toLowerCase(Locale.ENGLISH) : "info";
        // logging must never interfere with the caller
        try {
          methods.get(level).invoke(logger, text);
        } catch(final ReflectiveOperationException ex) {
          Util.debug(ex);
        }
      };
    } catch(final ReflectiveOperationException ex) {
      Util.debug(ex);
      return null;
    }
  }

  /**
   * Writes a log entry.
   * @param log logger
   * @param entry log entry
   * @throws IOException I/O exception
   */
  abstract void write(Log log, LogEntry entry) throws IOException;
}
