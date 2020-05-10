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
 * @author BaseX Team 2005-20, BSD License
 * @author Dimitar Popov
 */
public abstract class ConsoleReader implements AutoCloseable, PasswordReader {
  /** Password prompt. */
  private static final String PW_PROMPT = PASSWORD + COLS;

  /**
   * Reads next line. If no input, then the method blocks the thread.
   * @param prompt prompt
   * @return next line or {@code null} if EOF is reached
   */
  public abstract String readLine(String prompt);

  @Override
  public abstract void close();

  /**
   * Creates a new instance.
   * @return instance of console reader
   */
  public static ConsoleReader get() {
    if(JLineConsoleReader.isAvailable()) {
      try {
        return new JLineConsoleReader();
      } catch(final Throwable th) {
        Util.debug(th);
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
    public String password() {
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
    private static final String JLINE_CONSOLE_READER = "org.jline.reader.LineReader";
    /** Command history file. */
    private static final File HISTORY_FILE = new File(Prop.HOMEDIR, IO.BASEXSUFFIX + "history");
    /** Password echo character. */
    private static final Character PASSWORD_ECHO = (char) 0;

    /** Method to read the next line. */
    private final Method readLine;
    /** Method to read the next line with echoing a character. */
    private final Method readEcho;
    /** Implementation. */
    private final Object reader;

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
      // not usig org.basex.util.Reflect, since there is no need to cache the Class objects
      // normally, there is only one instance of JLineConsoleReader.
      final Class<?> readerC = Class.forName(JLINE_CONSOLE_READER);
      readLine = readerC.getMethod("readLine", String.class);
      readEcho = readerC.getMethod("readLine", String.class, Character.class);

      final Class<?> completerC = Class.forName("org.jline.reader.Completer");
      final Object enumCompleter = Class.forName("org.jline.reader.impl.completer.EnumCompleter")
        .getDeclaredConstructor(Class.class)
        .newInstance(Cmd.class);
      final Object fileCompleter = Class.forName("org.jline.reader.impl.completer.FileNameCompleter")
        .getDeclaredConstructor()
        .newInstance();
      final Object completers = java.lang.reflect.Array.newInstance(completerC, 2);
      java.lang.reflect.Array.set(completers, 0, enumCompleter);
      java.lang.reflect.Array.set(completers, 1, fileCompleter);
      final Object completer = Class.forName("org.jline.reader.impl.completer.AggregateCompleter")
        .getDeclaredConstructor(Class.forName("[Lorg.jline.reader.Completer;"))
        .newInstance(completers);

      final Class<?> builderC = Class.forName("org.jline.reader.LineReaderBuilder");
      final Method builderVariable = builderC.getMethod("variable", String.class, Object.class);
      final Method builderCompleter = builderC.getMethod("completer", completerC);

      final Object readerBuilder = builderC.getMethod("builder").invoke(null);
      builderCompleter.invoke(readerBuilder, completer);
      builderVariable.invoke(readerBuilder, "bell-style", "off");
      builderVariable.invoke(readerBuilder, "history-file", HISTORY_FILE);

      reader = builderC.getMethod("build").invoke(readerBuilder);
    }

    @Override
    public String readLine(String prompt) {
      return (String) Reflect.invoke(readLine, reader, prompt);
    }

    @Override
    public String password() {
      return (String) Reflect.invoke(readEcho, reader, PW_PROMPT, PASSWORD_ECHO);
    }

    @Override
    public void close() {
      try {
        final Object history = reader.getClass().getMethod("getHistory").invoke(reader);
        history.getClass().getMethod("save").invoke(history);
      } catch (final Exception ex) {
        Util.debug(ex);
      }
    }
  }
}
