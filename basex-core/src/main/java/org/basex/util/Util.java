package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import javax.net.ssl.*;

import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains static methods, which are used throughout the project.
 * The methods are used for dumping error output, debugging information,
 * getting the application path, etc.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Util {
  /** Flag for using default standard input. */
  private static final boolean NOCONSOLE = System.console() == null;

  /** Hidden constructor. */
  private Util() { }

  /**
   * Returns an information string for an unexpected exception.
   * @param throwable exception
   * @return dummy object
   */
  public static String bug(final Throwable throwable) {
    final TokenBuilder tb = new TokenBuilder().add(S_BUGINFO);
    tb.add(NL).add("Contact: ").add(MAILING_LIST);
    tb.add(NL).add("Version: ").add(TITLE);
    tb.add(NL).add("Java: ").add(System.getProperty("java.vendor"));
    tb.add(", ").add(System.getProperty("java.version"));
    tb.add(NL).add("OS: ").add(System.getProperty("os.name"));
    tb.add(", ").add(System.getProperty("os.arch"));
    tb.add(NL).add("Stack Trace: ");
    for(final String string : toArray(throwable)) tb.add(NL).add(string);
    return tb.toString();
  }

  /**
   * Throws a runtime exception for an unexpected exception.
   * @return runtime exception (indicates that an error is raised)
   */
  public static RuntimeException notExpected() {
    return new RuntimeException("Not Expected.");
  }

  /**
   * Throws a runtime exception for an unexpected exception.
   * @param message message
   * @param ext optional extension
   * @return runtime exception (indicates that an error is raised)
   */
  public static RuntimeException notExpected(final Object message, final Object... ext) {
    return new RuntimeException(info(message, ext));
  }

  /**
   * Returns the class name of the specified object, excluding its path.
   * @param object object
   * @return class name
   */
  public static String className(final Object object) {
    return className(object.getClass());
  }

  /**
   * Returns the name of the specified class, excluding its path.
   * @param clazz class
   * @return class name
   */
  public static String className(final Class<?> clazz) {
    return clazz.getSimpleName();
  }

  /**
   * Returns a single line from standard input.
   * @return string
   */
  public static String input() {
    final Scanner sc = new Scanner(System.in);
    return sc.hasNextLine() ? sc.nextLine().trim() : "";
  }

  /**
   * Returns a password from standard input.
   * @return password or empty string
   */
  public static String password() {
    // use standard input if no console is defined (such as in Eclipse)
    if(NOCONSOLE) return input();
    // hide password
    final char[] pw = System.console().readPassword();
    return pw != null ? new String(pw) : "";
  }

  /**
   * Prints a string to standard output, followed by a newline.
   * @param string output string
   * @param ext text optional extensions
   */
  public static void println(final Object string, final Object... ext) {
    print((string instanceof byte[] ? string((byte[]) string) : string) + NL, ext);
  }

  /**
   * Prints a string to standard output.
   * @param string output string
   * @param ext text optional extensions
   */
  public static void print(final Object string, final Object... ext) {
    System.out.print(info(string, ext));
  }

  /**
   * Returns the root query exception.
   * @param throwable throwable
   * @return root exception
   */
  public static Throwable rootException(final Throwable throwable) {
    Throwable th = throwable;
    while(true) {
      debug(th);
      final Throwable ca = th.getCause();
      if(ca == null || th instanceof QueryException && !(ca instanceof QueryException)) return th;
      th = ca;
    }
  }

  /**
   * Prints a string to standard error, followed by a newline.
   * @param object error object
   * @param ext text optional extensions
   */
  public static void errln(final Object object, final Object... ext) {
    err((object instanceof Throwable ? message((Throwable) object) : object) + NL, ext);
  }

  /**
   * Prints a string to standard error.
   * @param string debug string
   * @param ext text optional extensions
   */
  public static void err(final String string, final Object... ext) {
    System.err.print(info(string, ext));
  }

  /**
   * Returns a more user-friendly error message for the specified exception.
   * @param th throwable reference
   * @return error message
   */
  public static String message(final Throwable th) {
    debug(th);
    if(th instanceof BindException) return SRV_RUNNING;
    if(th instanceof ConnectException) return CONNECTION_ERROR;
    if(th instanceof SocketTimeoutException) return TIMEOUT_EXCEEDED;
    if(th instanceof SocketException) return CONNECTION_ERROR;

    String msg = th.getMessage();
    if(th instanceof JobException) return msg;
    if(msg == null || msg.isEmpty() || th instanceof RuntimeException || th instanceof Error) {
      msg = th.toString();
    }
    if(th instanceof ReflectiveOperationException) {
      if(th.getCause() != null) msg += "; " + th.getCause();
    }
    if(th instanceof FileNotFoundException || th instanceof NoSuchFileException)
      return info(RES_NOT_FOUND_X, msg);
    if(th instanceof UnknownHostException) return info(UNKNOWN_HOST_X, msg);
    if(th instanceof SSLException) return "SSL: " + msg;

    // chop long error messages. // example: doc("http://google.com/sdffds")
    if(th.getClass() == IOException.class && msg.length() > 200) {
      msg = msg.substring(0, 200) + DOTS;
    }
    return msg;
  }

  /**
   * Prints the exception stack trace if the {@link Prop#debug} flag is set.
   * @param throwable exception
   */
  public static void debug(final Throwable throwable) {
    if(Prop.debug && throwable != null) stack(throwable);
  }

  /**
   * Prints a string to standard error if the {@link Prop#debug} flag is set.
   * @param string debug string
   * @param ext text optional extensions
   */
  public static void debugln(final Object string, final Object... ext) {
    if(Prop.debug) errln(string, ext);
  }

  /**
   * Returns a string and replaces all % characters by the specified extensions
   * (see {@link TokenBuilder#addExt} for details).
   * @param string string to be extended
   * @param ext text text extensions
   * @return extended string
   */
  public static String info(final Object string, final Object... ext) {
    return string(inf(string, ext));
  }

  /**
   * Returns a token and replaces all % characters by the specified extensions
   * (see {@link TokenBuilder#addExt} for details).
   * @param string string to be extended
   * @param ext text text extensions
   * @return token
   */
  public static byte[] inf(final Object string, final Object... ext) {
    return new TokenBuilder().addExt(string, ext).finish();
  }

  /**
   * Prints the current stack trace to System.err.
   * @param message error message
   */
  public static void stack(final String message) {
    stack(message, Short.MAX_VALUE);
  }

  /**
   * Prints the current stack trace to System.err. Used for testing.
   * @param depth number of steps to print
   */
  public static void stack(final int depth) {
    stack("You're here:", depth);
  }

  /**
   * Prints the current stack trace to System.err.
   * @param message message
   * @param depth number of steps to print
   */
  private static void stack(final String message, final int depth) {
    errln(message);
    final String[] stack = toArray(new Throwable());
    final int l = Math.min(Math.max(2, depth + 2), stack.length);
    for(int s = 3; s < l; s++) errln(stack[s]);
  }

  /**
   * Prints the stack of the specified error to standard error.
   * @param throwable error/exception instance
   */
  public static void stack(final Throwable throwable) {
    throwable.printStackTrace();
  }

  /**
   * Returns a string array representation of the specified throwable.
   * @param throwable throwable
   * @return string array
   */
  private static String[] toArray(final Throwable throwable) {
    final StringList sl = new StringList().add(throwable.toString());
    for(final StackTraceElement st : throwable.getStackTrace()) sl.add("\tat " + st);
    return sl.finish();
  }

  /**
   * Starts the specified class in a separate process. A -D (daemon) flag will be added to the
   * command-line options.
   * @param clazz class to start
   * @param args command-line arguments
   * @return reference to a {@link Process} instance representing the started process
   */
  public static Process start(final Class<?> clazz, final String... args) {
    final String[] largs = { "java", "-Xmx" + Runtime.getRuntime().maxMemory(),
        "-cp", System.getProperty("java.class.path") };
    final StringList sl = new StringList().add(largs);

    System.getProperties().forEach((key, value) -> {
      if(key.toString().startsWith(Prop.DBPREFIX)) sl.add("-D" + key + '=' + value);
    });
    sl.add(clazz.getName()).add("-D").add(args);

    try {
      return new ProcessBuilder(sl.finish()).start();
    } catch(final IOException ex) {
      throw notExpected(ex);
    }
  }

  /**
   * Returns the error message of a process.
   * @param process process
   * @param timeout time out in milliseconds
   * @return error message or {@code null}
   */
  public static String error(final Process process, final int timeout) {
    final int wait = 200, to = Math.max(timeout / wait, 1);
    for(int c = 0; c < to; c++) {
      try {
        final int exit = process.exitValue();
        if(exit == 1) return new IOStream(process.getErrorStream()).string();
        break;
      } catch(final IllegalThreadStateException ex) {
        debug(ex);
        // process is still running
        Performance.sleep(wait);
      } catch(final IOException ex) {
        // return error message of exception
        return ex.getLocalizedMessage();
      }
    }
    return null;
  }

  /**
   * Returns the contents of a property resource.
   * @param name name of the property resource
   * @return property map
   */
  public static TokenMap properties(final String name) {
    final InputStream is = Util.class.getResourceAsStream('/' + name);
    if(is == null) throw notExpected("%: Property resource missing.", name);

    final TokenMap map = new TokenMap();
    try(NewlineInput nli = new NewlineInput(is)) {
      for(String line; (line = nli.readLine()) != null;) {
        final int s = line.indexOf('=');
        if(s == -1 || Strings.startsWith(line, '#')) continue;
        final byte[] key = token(line.substring(0, s).trim());
        if(map.contains(key)) throw notExpected("%: '%' is declared twice.", name, key);
        map.put(key, token(line.substring(s + 1).trim().replace("\\t", "\t").replace("\\n", "\n")));
      }
    } catch(final IOException ex) {
      throw notExpected("%: %", name, ex);
    }
    return map;
  }

  /**
   * Checks if long values are small enough to be multiplied without overflow.
   * @param value1 first value
   * @param value2 second value
   * @return result of check
   */
  public static boolean inBounds(final long value1, final long value2) {
    return value2 == 0 || value1 <= Long.MAX_VALUE / value2;
  }
}
