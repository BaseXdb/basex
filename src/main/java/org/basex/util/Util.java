package org.basex.util;

import java.io.File;
import static org.basex.core.Text.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.server.LoginException;

/**
 * This class contains static methods, which are used throughout the project.
 * The methods are used for dumping error output, debugging information,
 * getting the application path, etc.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Util {
  /** Language (applied after restart). */
  public static String language = LANGUAGE;
  /** Flag for showing language keys. */
  public static boolean langkeys;
  /** Debug mode. */
  public static boolean debug;

  /** Hidden constructor. */
  private Util() { }

  /**
   * Returns an information string for an unexpected exception.
   * @param ex exception
   * @return dummy object
   */
  public static String bug(final Throwable ex) {
    final TokenBuilder tb = new TokenBuilder(
      "Potential bug? Improper use? Your feedback is welcome:");
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
    final TokenBuilder tb = new TokenBuilder("Not expected");
    if(ext.length != 0) tb.addExt(": %", ext);
    throw new RuntimeException(tb.add('.').toString());
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return runtime exception (indicates that an error is raised)
   */
  public static RuntimeException notimplemented(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder("Not Implemented");
    if(ext.length != 0) tb.addExt(" (%)", ext);
    throw new RuntimeException(tb.add('.').toString());
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
    out(str + NL, ext);
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
    if(obj instanceof Exception) {
      final Exception ex = (Exception) obj;
      err(message(ex) + NL, ext);
    } else {
      err(obj + NL, ext);
    }
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
  public static String message(final Exception ex) {
    debug(ex);
    final String msg = ex.getMessage();
    if(ex instanceof BindException) return SERVERBIND;
    else if(ex instanceof LoginException) return SERVERDENIED;
    else if(ex instanceof ConnectException) return SERVERERROR;
    else if(ex instanceof SocketTimeoutException) return SERVERTIMEOUT;
    else if(ex instanceof SocketException) return SERVERBIND;
    else if(ex instanceof UnknownHostException) return info(SERVERUNKNOWN, msg);
    return msg != null ? msg : ex.toString();
  }

  /**
   * Prints the exception stack trace if the {@link #debug} flag is set.
   * @param ex exception
   * @return always false
   */
  public static boolean debug(final Throwable ex) {
    if(debug && ex != null) stack(ex);
    return false;
  }

  /**
   * Prints a string to standard error if the {@link #debug} flag is set.
   * @param str debug string
   * @param ext text optional extensions
   */
  public static void debug(final Object str, final Object... ext) {
    if(debug) errln(str, ext);
  }

  /**
   * Performs garbage collection and prints performance information.
   * if the debug flag is set.
   * @param perf performance reference
   */
  public static void gc(final Performance perf) {
    if(!debug) return;
    Performance.gc(4);
    errln(" " + perf + " (" + Performance.getMem() + ")");
  }

  /**
   * Returns a string and replaces all % characters by the specified extensions.
   * See {@link TokenBuilder#addExt} for details.
   * @param str string to be extended
   * @param ext text text extensions
   * @return extended string
   */
  public static String info(final Object str, final Object... ext) {
    return Token.string(inf(str, ext));
  }

  /**
   * Returns a token and replaces all % characters by the specified extensions.
   * (see {@link TokenBuilder#addExt} for details.
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
  public static String[] toArray(final Throwable th) {
    final StackTraceElement[] st = th.getStackTrace();
    final String[] obj = new String[st.length + 1];
    obj[0] = th.toString();
    for(int i = 0; i < st.length; i++) obj[i + 1] = "  " + st[i];
    return obj;
  }

  /**
   * Returns the the preferred directory for storing the property file,
   * databases directory, etc.
   * @return application path
   */
  public static String homeDir() {
    // check working directory for property file
    final String work = System.getProperty("user.dir");
    if(new File(work, IO.BASEXSUFFIX).exists()) return work;

    // not found; check application directory
    final File f = new File(applicationPath());
    final String home = f.isFile() ? f.getParent() : f.getPath();
    if(new File(home, IO.BASEXSUFFIX).exists()) return home;

    // not found; choose user home directory
    return System.getProperty("user.home");
  }

  /**
   * Returns the absolute path to this application.
   * @return application path
   */
  private static String applicationPath() {
    // raw application path
    final String path = Util.class.getProtectionDomain().
      getCodeSource().getLocation().getPath();

    // decode path; URLDecode returns wrong results
    final TokenBuilder tb = new TokenBuilder();
    final int pl = path.length();
    for(int p = 0; p < pl; ++p) {
      final char ch = path.charAt(p);
      if(ch == '%' && p + 2 < pl) {
        tb.addByte((byte) Integer.parseInt(path.substring(p + 1, p + 3), 16));
        p += 2;
      } else {
        tb.add(ch);
      }
    }
    try {
      // return path, using the correct encoding
      return new String(tb.finish(), Prop.ENCODING);
    } catch(final Exception ex) {
      // use default path; not expected to occur
      stack(ex);
      return tb.toString();
    }
  }
}
