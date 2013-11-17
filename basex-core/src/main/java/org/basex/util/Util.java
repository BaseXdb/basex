package org.basex.util;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.basex.core.*;
import org.basex.server.*;
import org.basex.util.list.*;

/**
 * This class contains static methods, which are used throughout the project.
 * The methods are used for dumping error output, debugging information,
 * getting the application path, etc.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class Util {
  /** Flag for using default standard input. */
  private static final boolean NOCONSOLE = System.console() == null;

  /** Hidden constructor. */
  private Util() { }

  /**
   * Returns an information string for an unexpected exception.
   * @param ex exception
   * @return dummy object
   */
  public static String bug(final Throwable ex) {
    final TokenBuilder tb = new TokenBuilder(BUGINFO);
    tb.add(NL).add("Contact: ").add(MAIL);
    tb.add(NL).add("Version: ").add(TITLE);
    tb.add(NL).add("Java: ").add(System.getProperty("java.vendor"));
    tb.add(", ").add(System.getProperty("java.version"));
    tb.add(NL).add("OS: ").add(System.getProperty("os.name"));
    tb.add(", ").add(System.getProperty("os.arch"));
    tb.add(NL).add("Stack Trace: ");
    for(final String e : toArray(ex)) tb.add(NL).add(e);
    return tb.toString();
  }

  /**
   * Throws a runtime exception for an unexpected exception.
   * @param ext optional extension
   * @return runtime exception (indicates that an error is raised)
   */
  public static RuntimeException notExpected(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt("%", ext.length == 0 ? "Not Expected." : ext[0]);
    return new RuntimeException(tb.toString());
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return runtime exception (indicates that an error is raised)
   */
  public static UnsupportedOperationException notImplemented(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder("Not Implemented");
    if(ext.length != 0) tb.addExt(" (%)", ext);
    return new UnsupportedOperationException(tb.add('.').toString());
  }

  /**
   * Returns the class name of the specified object, excluding its path.
   * @param obj object
   * @return class name
   */
  public static String className(final Object obj) {
    return className(obj.getClass());
  }

  /**
   * Returns the name of the specified class, excluding its path.
   * @param clz class
   * @return class name
   */
  public static String className(final Class<?> clz) {
    return clz.getSimpleName();
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
   * @return password
   */
  public static String password() {
    // use standard input if no console if defined (such as in Eclipse)
    if(NOCONSOLE) return input();
    // hide password
    final char[] pw = System.console().readPassword();
    return pw != null ? new String(pw) : "";
  }

  /**
   * Prints a newline to standard output.
   */
  public static void outln() {
    out(NL);
  }

  /**
   * Prints a string to standard output, followed by a newline.
   * @param str output string
   * @param ext text optional extensions
   */
  public static void outln(final Object str, final Object... ext) {
    out((str instanceof byte[] ? Token.string((byte[]) str) : str) + NL, ext);
  }

  /**
   * Prints a string to standard output.
   * @param str output string
   * @param ext text optional extensions
   */
  public static void out(final Object str, final Object... ext) {
    System.out.print(info(str, ext));
  }

  /**
   * Prints a string to standard error, followed by a newline.
   * @param obj error string
   * @param ext text optional extensions
   */
  public static void errln(final Object obj, final Object... ext) {
    err((obj instanceof Throwable ? message((Throwable) obj) : obj) + NL, ext);
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
   * @param ex throwable reference
   * @return error message
   */
  public static String message(final Throwable ex) {
    debug(ex);
    if(ex instanceof BindException) return SRV_RUNNING;
    if(ex instanceof LoginException) return ACCESS_DENIED;
    if(ex instanceof ConnectException) return CONNECTION_ERROR;
    if(ex instanceof SocketTimeoutException) return TIMEOUT_EXCEEDED;
    if(ex instanceof SocketException) return CONNECTION_ERROR;
    String msg = ex.getMessage();
    if(msg == null || msg.isEmpty()) msg = ex.toString();
    if(ex instanceof FileNotFoundException) return info(RES_NOT_FOUND_X, msg);
    if(ex instanceof UnknownHostException) return info(UNKNOWN_HOST_X, msg);
    return msg;
  }

  /**
   * Prints the exception stack trace if the {@link Prop#debug} flag is set.
   * @param ex exception
   */
  public static void debug(final Throwable ex) {
    if(Prop.debug && ex != null) stack(ex);
  }

  /**
   * Prints a string to standard error if the {@link Prop#debug} flag is set.
   * @param str debug string
   * @param ext text optional extensions
   */
  public static void debug(final Object str, final Object... ext) {
    if(Prop.debug) errln(str, ext);
  }

  /**
   * Returns a string and replaces all % characters by the specified extensions
   * (see {@link TokenBuilder#addExt} for details).
   * @param str string to be extended
   * @param ext text text extensions
   * @return extended string
   */
  public static String info(final Object str, final Object... ext) {
    return Token.string(inf(str, ext));
  }

  /**
   * Returns a token and replaces all % characters by the specified extensions
   * (see {@link TokenBuilder#addExt} for details).
   * @param str string to be extended
   * @param ext text text extensions
   * @return token
   */
  public static byte[] inf(final Object str, final Object... ext) {
    return new TokenBuilder().addExt(str, ext).finish();
  }

  /**
   * Prints the current stack trace to System.err.
   * @param i number of steps to print
   */
  public static void stack(final int i) {
    errln("You're here:");
    final String[] stack = toArray(new Throwable());
    final int l = Math.min(Math.max(2, i + 2), stack.length);
    for(int s = 2; s < l; ++s) errln(stack[s]);
  }

  /**
   * Prints the stack of the specified error to standard error.
   * @param th error/exception instance
   */
  public static void stack(final Throwable th) {
    //for(final String s : toArray(th)) errln(s);
    th.printStackTrace();
  }

  /**
   * Returns an string array representation of the specified throwable.
   * @param th throwable
   * @return string array
   */
  private static String[] toArray(final Throwable th) {
    final StackTraceElement[] st = th.getStackTrace();
    final String[] obj = new String[st.length + 1];
    obj[0] = th.toString();
    for(int i = 0; i < st.length; i++) obj[i + 1] = "\tat " + st[i];
    return obj;
  }

  /**
   * Starts the specified class in a separate process.
   * @param clz class to start
   * @param args command-line arguments
   * @return reference to a {@link Process} instance representing the started process
   */
  public static Process start(final Class<?> clz, final String... args) {
    final String[] largs = { "java", "-Xmx" + Runtime.getRuntime().maxMemory(),
        "-cp", System.getProperty("java.class.path") };
    final StringList sl = new StringList().add(largs);

    for(final Map.Entry<Object, Object> o : System.getProperties().entrySet()) {
      final String k = o.getKey().toString();
      if(k.startsWith(Prop.DBPREFIX)) sl.add("-D" + o.getValue());
    }
    sl.add(clz.getName()).add("-D").add(args);

    try {
      return new ProcessBuilder(sl.toArray()).start();
    } catch(final IOException ex) {
      throw notExpected(ex);
    }
  }

  /**
   * Checks if the specified string is "yes", "true" or "on".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean yes(final String string) {
    return Token.eqic(string, YES, TRUE, ON);
  }

  /**
   * Checks if the specified string is "no", "false" or "off".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean no(final String string) {
    return Token.eqic(string, NO, FALSE, OFF);
  }

  /**
   * Returns an info message for the specified flag.
   * @param flag current flag status
   * @return ON/OFF message
   */
  public static String flag(final boolean flag) {
    return flag ? INFOON : INFOOFF;
  }
}
