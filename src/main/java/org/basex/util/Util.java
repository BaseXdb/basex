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
 * @author BaseX Team 2005-12, BSD License
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
  public static RuntimeException notexpected(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder();
    tb.addExt("%", ext.length == 0 ? "Not Expected." : ext[0]);
    throw new RuntimeException(tb.toString());
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return runtime exception (indicates that an error is raised)
   */
  public static RuntimeException notimplemented(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder("Not Implemented");
    if(ext.length != 0) tb.addExt(" (%)", ext);
    throw new UnsupportedOperationException(tb.add('.').toString());
  }

  /**
   * Returns the class name of the specified object.
   * @param o object
   * @return class name
   */
  public static String name(final Object o) {
    return name(o.getClass());
  }

  /**
   * Returns the name of the specified class.
   * @param o object
   * @return class name
   */
  public static String name(final Class<?> o) {
    return o.getSimpleName();
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
   * Returns a better understandable error message for the specified exception.
   * @param ex throwable reference
   * @return error message
   */
  public static String message(final Throwable ex) {
    final String msg = ex.getMessage();
    if(ex instanceof BindException) return SRV_RUNNING;
    else if(ex instanceof FileNotFoundException) return info(RES_NOT_FOUND_X, msg);
    else if(ex instanceof LoginException) return ACCESS_DENIED;
    else if(ex instanceof ConnectException) return CONNECTION_ERROR;
    else if(ex instanceof SocketTimeoutException) return TIMEOUT_EXCEEDED;
    else if(ex instanceof SocketException) return CONNECTION_ERROR;
    else if(ex instanceof UnknownHostException) return info(UNKNOWN_HOST_X, msg);
    else if(ex instanceof RuntimeException) {
      return ex.getStackTrace()[0].getClassName() + ": " + ex;
    }
    return msg != null && !msg.isEmpty() ? msg : ex.toString();
  }

  /**
   * Prints the exception stack trace if the {@link #debug} flag is set.
   * @param ex exception
   * @return always false
   */
  public static boolean debug(final Throwable ex) {
    if(Prop.debug && ex != null) stack(ex);
    return false;
  }

  /**
   * Prints a string to standard error if the {@link #debug} flag is set.
   * @param str debug string
   * @param ext text optional extensions
   */
  public static void debug(final Object str, final Object... ext) {
    if(Prop.debug) errln(str, ext);
  }

  /**
   * Prints performance information if the {@link #debug} flag is set.
   * @param perf performance reference
   */
  public static void memory(final Performance perf) {
    if(!Prop.debug) return;
    errln(" " + perf + " (" + Performance.getMemory() + ')');
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
    stack(new Throwable(), i);
  }

  /**
   * Prints the stack of the specified error to standard error.
   * @param th error/exception instance
   */
  public static void stack(final Throwable th) {
    stack(th, 0);
  }

  /**
   * Prints the stack of the specified error to standard error.
   * @param th error/exception instance
   * @param i number of steps to print
   */
  private static void stack(final Throwable th, final int i) {
    final String[] stack = toArray(th);
    int l = stack.length;
    if(i > 0 && i < l) l = i;
    for(int s = 0; s < l; ++s) errln(stack[s]);
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
    for(int i = 0; i < st.length; i++) obj[i + 1] = "  " + st[i];
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
        "-cp", System.getProperty("java.class.path"), clz.getName(), "-D", };
    final StringList sl = new StringList().add(largs).add(args);

    try {
      return new ProcessBuilder(sl.toArray()).start();
    } catch(final IOException ex) {
      notexpected(ex);
      return null;
    }
  }

  /**
   * Checks if the specified string is "yes", "true" or "on".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean yes(final String string) {
    return Token.eqic(string, YES, TRUE, ON, INFOON);
  }

  /**
   * Checks if the specified string is "no", "false" or "off".
   * @param string string to be checked
   * @return result of check
   */
  public static boolean no(final String string) {
    return Token.eqic(string, NO, FALSE, OFF, INFOOFF);
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
