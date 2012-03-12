package org.basex.core;

import java.io.*;
import java.lang.reflect.*;
import org.basex.util.*;

/**
 * Console reader.
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public abstract class ConsoleReader {
  /** Default prompt. */
  public static final String DEFAULT_PROMPT = "> ";

  /**
   * Change the prompt of the console.
   * @param prompt new prompt
   * @return current instance
   */
  public abstract ConsoleReader setPrompt(String prompt);

  /**
   * Read next line. If no input, then the method blocks the thread.
   * @return next line or {@code null} if EOF is reached
   */
  public abstract String readLine();

  /**
   * Create a new instance.
   * @return instance of console reader
   */
  public static final ConsoleReader newInstance() {

    if(JLineConsoleReader.isAvailable()) {
      try {
        return new JLineConsoleReader();
      } catch (Exception e) {
        Util.debug(e);
      }
    }
    return new SimpleConsoleReader();
  }

  /** Simple console reader implementation. */
  private static class SimpleConsoleReader extends ConsoleReader {
    /** Current prompt. */
    private String prompt = DEFAULT_PROMPT;
    /** Input reader. */
    private final BufferedReader in;

    /** Constructor. */
    public SimpleConsoleReader() {
      in = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public ConsoleReader setPrompt(final String p) {
      prompt = p;
      return this;
    }

    @Override
    public String readLine() {
      try {
        Util.out(prompt);
        return in.readLine();
      } catch(IOException e) {
        // should not happen
        throw new RuntimeException(e);
      }
    }
  }


  /** Implementation which provides advanced features, such as history. */
  private static class JLineConsoleReader extends ConsoleReader {
    /** JLine console reader class. */
    private static final String JLINE_CONSOLE_READER = "jline.ConsoleReader";
    /** Implementation. */
    private final Object reader;

    /** JLine console reader class. */
    private final Class<?> clz;
    /** Method to read the next line. */
    private final Method readLine;
    /** Method to set the prompt. */
    private final Method setDefaultPrompt;
    /** Method to set the history flag. */
    private final Method setUseHistory;

    /**
     * Is JLine implementation available?
     * @return {@code true} if JLine is in the classpath
     */
    public static boolean isAvailable() {
      return Reflect.available(JLINE_CONSOLE_READER);
    }

    /**
     * Constructor.
     * @throws Exception error
     */
    public JLineConsoleReader() throws Exception {
      clz = Reflect.find(JLINE_CONSOLE_READER);
      reader = clz.newInstance();

      readLine = Reflect.method(clz, "readLine");
      setDefaultPrompt = Reflect.method(clz, "setDefaultPrompt", String.class);
      setUseHistory = Reflect.method(clz, "setUseHistory", Boolean.class);

      setUseHistory(true);
      setPrompt(DEFAULT_PROMPT);
    }

    /**
     * Set a flag, if history should be kept.
     * @param h history flag
     * @return current instance
     */
    public JLineConsoleReader setUseHistory(final boolean h) {
      Reflect.invoke(setUseHistory, reader, h);
      return this;
    }

    @Override
    public JLineConsoleReader setPrompt(final String prompt) {
      Reflect.invoke(setDefaultPrompt, reader, prompt);
      return this;
    }

    @Override
    public String readLine() {
      return (String) Reflect.invoke(readLine, reader);
    }
  }
}

