package org.basex.util;

import static org.basex.util.Reflect.*;
import java.io.*;
import java.lang.reflect.*;

import org.basex.core.*;
import org.basex.io.*;

/**
 * Console reader.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public abstract class ConsoleReader {
  /** Default prompt. */
  private static final String DEFAULT_PROMPT = "> ";

  /**
   * Reads next line. If no input, then the method blocks the thread.
   * @return next line or {@code null} if EOF is reached
   */
  public abstract String readLine();

  /**
   * Creates a new instance.
   * @return instance of console reader
   */
  public static final ConsoleReader newInstance() {
    if(JLineConsoleReader.isAvailable()) {
      try {
        return new JLineConsoleReader();
      } catch(final Exception ex) {
        Util.errln(ex);
      }
    }
    return new SimpleConsoleReader();
  }

  /** Simple console reader implementation. */
  private static class SimpleConsoleReader extends ConsoleReader {
    /** Input reader. */
    private final BufferedReader in;

    /** Constructor. */
    SimpleConsoleReader() {
      in = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public String readLine() {
      try {
        Util.out(DEFAULT_PROMPT);
        return in.readLine();
      } catch(final IOException e) {
        // should not happen
        throw new RuntimeException(e);
      }
    }
  }

  /** Implementation which provides advanced features, such as history. */
  private static class JLineConsoleReader extends ConsoleReader {
    /** JLine console reader class name. */
    private static final String JLINE_CONSOLE_READER = "jline.ConsoleReader";
    /** JLine history class name. */
    private static final String JLINE_HISTORY = "jline.History";
    /** Command history file. */
    private static final String HISTORY_FILE = IO.BASEXSUFFIX + "history";

    /** Method to read the next line. */
    private final Method readLine;
    /** Implementation. */
    private final Object reader;

    /**
     * Checks if JLine implementation is available?
     * @return {@code true} if JLine is in the classpath
     */
    static boolean isAvailable() {
      return available(JLINE_CONSOLE_READER);
    }

    /**
     * Constructor.
     * @throws Exception error
     */
    JLineConsoleReader() throws Exception {
      // reflection
      final Class<?> clz = find(JLINE_CONSOLE_READER);
      final Class<?> historyClz = find(JLINE_HISTORY);
      readLine = method(clz, "readLine");

      // initialization
      reader = clz.newInstance();
      final File hist = new File(Prop.HOME, HISTORY_FILE);
      final Object history = get(find(historyClz, File.class), hist);

      // adjust default settings
      invoke(method(clz, "setDefaultPrompt", String.class), reader, DEFAULT_PROMPT);
      invoke(method(clz, "setUseHistory", boolean.class), reader, true);
      invoke(method(clz, "setBellEnabled", boolean.class), reader, false);
      invoke(method(clz, "setHistory", historyClz), reader, history);
    }

    @Override
    public String readLine() {
      return (String) invoke(readLine, reader);
    }
  }
}
