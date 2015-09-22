package org.basex.util;

import static org.basex.core.Text.*;

import java.io.*;
import java.lang.reflect.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.Cmd;
import org.basex.io.*;

/**
 * Console reader.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public abstract class ConsoleReader implements AutoCloseable {
  /** Password prompt. */
  private static final String PW_PROMPT = PASSWORD + COLS;

  /** Password reader. */
  private final PasswordReader pwReader = new PasswordReader() {
    @Override
    public String password() {
      return readPassword();
    }
  };

  /**
   * Reads next line. If no input, then the method blocks the thread.
   * @param prompt prompt
   * @return next line or {@code null} if EOF is reached
   */
  public abstract String readLine(final String prompt);

  /**
   * Reads a password.
   * @return password as plain text
   */
  protected abstract String readPassword();

  @Override
  public abstract void close();

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
  public static ConsoleReader get() {
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
    public String readLine(final String prompt) {
      try {
        Util.out(prompt);
        return in.readLine();
      } catch(final IOException ex) {
        // should not happen
        throw new RuntimeException(ex);
      }
    }

    @Override
    public String readPassword() {
      Util.out(PW_PROMPT);
      return Util.password();
    }

    @Override
    public void close() {
    }
  }

  /** Implementation which provides advanced features, such as history. */
  private static class JLineConsoleReader extends ConsoleReader {
    /** JLine console reader class name. */
    private static final String JLINE_CONSOLE_READER = "jline.console.ConsoleReader";
    /** JLine file history class name. */
    private static final String JLINE_FILE_HISTORY = "jline.console.history.FileHistory";
    /** JLine history class name. */
    private static final String JLINE_HISTORY = "jline.console.history.History";
    /** JLine history class name. */
    private static final String JLINE_COMPLETER = "jline.console.completer.Completer";
    /** JLine history class name. */
    private static final String JLINE_ENUM_COMPLETER = "jline.console.completer.EnumCompleter";
    /** JLine history class name. */
    private static final String JLINE_FILE_NAME_COMPLETER =
        "jline.console.completer.FileNameCompleter";
    /** Command history file. */
    private static final String HISTORY_FILE = IO.BASEXSUFFIX + "history";
    /** Password echo character. */
    private static final Character PASSWORD_ECHO = (char) 0;

    /** Method to read the next line. */
    private final Method readLine;
    /** Method to read the next line with echoing a character. */
    private final Method readEcho;
    /** Implementation. */
    private final Object reader;

    /** File history class. */
    private final Class<?> fileHistoryC;
    /** File history. */
    private final Object fileHistory;

    /**
     * Checks if JLine implementation is available?
     * @return {@code true} if JLine is in the classpath
     */
    static boolean isAvailable() {
      return Reflect.available(JLINE_CONSOLE_READER);
    }

    /**
     * Constructor.
     * @throws Exception error
     */
    JLineConsoleReader() throws Exception {
      // reflection
      final Class<?> readerC = Reflect.find(JLINE_CONSOLE_READER);
      readLine = Reflect.method(readerC, "readLine", String.class);
      readEcho = Reflect.method(readerC, "readLine", String.class, Character.class);

      // initialization
      reader = readerC.newInstance();

      final Class<?> history = Reflect.find(JLINE_HISTORY);
      fileHistoryC = Reflect.find(JLINE_FILE_HISTORY);
      fileHistory = Reflect.get(Reflect.find(fileHistoryC, File.class),
          new File(Prop.HOME, HISTORY_FILE));

      final Class<?> completer = Reflect.find(JLINE_COMPLETER);
      final Class<?> enumCompleter = Reflect.find(JLINE_ENUM_COMPLETER);
      final Class<?> fileNameCompleter = Reflect.find(JLINE_FILE_NAME_COMPLETER);

      Reflect.invoke(Reflect.method(readerC, "setBellEnabled", boolean.class), reader, false);
      Reflect.invoke(Reflect.method(readerC, "setHistory", history), reader, fileHistory);
      Reflect.invoke(Reflect.method(readerC, "setHistoryEnabled", boolean.class), reader, true);

      // command completions
      Reflect.invoke(Reflect.method(readerC, "addCompleter", completer), reader,
          Reflect.get(Reflect.find(enumCompleter, Class.class), Cmd.class));
      Reflect.invoke(Reflect.method(readerC, "addCompleter", completer), reader,
          Reflect.get(Reflect.find(fileNameCompleter)));
    }

    @Override
    public String readLine(final String prompt) {
      return (String) Reflect.invoke(readLine, reader, prompt);
    }

    @Override
    public String readPassword() {
      return (String) Reflect.invoke(readEcho, reader, PW_PROMPT, PASSWORD_ECHO);
    }

    @Override
    public void close() {
      Reflect.invoke(Reflect.method(fileHistoryC, "flush"), fileHistory);
    }
  }
}
