package org.basex.util;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;

import jline.console.completer.*;
import jline.console.history.*;

/**
 * Console reader.
 *
 * @author BaseX Team 2005-23, BSD License
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
    if(Reflect.available("jline.console.ConsoleReader")) {
      try {
        return new JLineConsoleReader();
      } catch(final IOException ex) {
        throw Util.notExpected(ex);
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
        throw Util.notExpected(ex);
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
    /** Console reader. */
    private final jline.console.ConsoleReader reader;
    /** File history. */
    private final FileHistory history;

    /**
     * Constructor.
     * @throws IOException I/O error
     */
    JLineConsoleReader() throws IOException {
      history = new FileHistory(new File(Prop.HOMEDIR, IO.BASEXSUFFIX + "history"));
      reader = new jline.console.ConsoleReader();
      reader.setHistory(history);
      reader.setHistoryEnabled(true);
      reader.setBellEnabled(false);
      reader.setExpandEvents(false);
      reader.addCompleter(new EnumCompleter(Cmd.class));
      reader.addCompleter(new FileNameCompleter());
    }

    @Override
    public String readLine(final String prompt) {
      try {
        return reader.readLine(prompt);
      } catch(final IOException ex) {
        throw Util.notExpected(ex);
      }
    }

    @Override
    public String password() {
      try {
        return reader.readLine(PW_PROMPT, (char) 0);
      } catch(final IOException ex) {
        throw Util.notExpected(ex);
      }
    }

    @Override
    public void close() {
      try {
        history.flush();
      } catch(final IOException ex) {
        throw Util.notExpected(ex);
      }
    }
  }
}
