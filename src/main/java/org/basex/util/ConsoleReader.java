package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Reflect.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.core.*;
import org.basex.core.parse.*;
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
  /** Password prompt. */
  static final String PASSWORD_PROMPT = PASSWORD + COLS;

  /** Password reader. */
  private final PasswordReader pwReader = new PasswordReader() {
    @Override
    public String password() {
      return md5(readPassword());
    }
  };

  /**
   * Reads next line. If no input, then the method blocks the thread.
   * @return next line or {@code null} if EOF is reached
   */
  public abstract String readLine();

  /**
   * Reads a password.
   * @return password as plain text
   */
  public abstract String readPassword();

  /**
   * Create a new password reader for this console.
   * @return a new instance of {@link PasswordReader}
   */
  public PasswordReader pwReader() {
    return pwReader;
  }

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

    @Override
    public String readPassword() {
      Util.out(PASSWORD_PROMPT);
      return Util.password();
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
    /** Password echo character. */
    private static final Character PASSWORD_ECHO = (char) 0;

    /** JLine console reader class. */
    private final Class<?> consoleReaderClass;
    /** Method to read the next line. */
    private final Method readLine;
    /** Method to read the next line with echoing a character. */
    private final Method readEcho;
    /** Method to set the default prompt. */
    private final Method setDefaultPrompt;
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
      consoleReaderClass = find(JLINE_CONSOLE_READER);

      readLine = method(consoleReaderClass, "readLine");
      readEcho = method(consoleReaderClass, "readLine", String.class, Character.class);
      setDefaultPrompt = method(consoleReaderClass, "setDefaultPrompt", String.class);

      // initialization
      reader = consoleReaderClass.newInstance();

      defaultConfiguration();
    }

    /** Apply default configuration. */
    private void defaultConfiguration() {
      final Class<?> historyClass = find(JLINE_HISTORY);
      final File hist = new File(Prop.HOME, HISTORY_FILE);
      final Object history = get(find(historyClass, File.class), hist);

      invoke(method(consoleReaderClass, "setUseHistory", boolean.class), reader, true);
      invoke(method(consoleReaderClass, "setBellEnabled", boolean.class), reader, false);
      invoke(method(consoleReaderClass, "setHistory", historyClass), reader, history);

      restoreDefaultPrompt();
    }

    /** Restore the default prompt. */
    private void restoreDefaultPrompt() {
      invoke(setDefaultPrompt, reader, DEFAULT_PROMPT);
    }

    @Override
    public String readLine() {
      return (String) invoke(readLine, reader);
    }

    @Override
    public String readPassword() {
      final String pw = (String) invoke(readEcho, reader, PASSWORD_PROMPT, PASSWORD_ECHO);
      restoreDefaultPrompt();
      return pw;
    }
  }
}
