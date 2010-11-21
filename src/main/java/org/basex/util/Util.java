package org.basex.util;

import java.io.File;
import static org.basex.core.Text.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.server.LoginException;

/**
 * This class contains static methods, which are used throughout the project.
 * The methods are used for dumping error output, debugging information,
 * getting the application path, etc.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Util {
  /** Debug mode. */
  public static boolean debug;

  /** Hidden constructor. */
  private Util() { }

  /**
   * Returns an information string for an unexpected exception.
   * @param ext optional extension
   * @return dummy object
   */
  public static String bug(final Object... ext) {
    final TokenBuilder tb = new TokenBuilder(
        "Possible bug? Feedback is welcome: " + MAIL);
    if(ext.length != 0) {
      tb.add(NL + NAME + ' ' + VERSION + COLS + NL);
      for(final Object e : ext) tb.add(e + NL);
    }
    return tb.toString();
  }

  /**
   * Throws a runtime exception for an unexpected exception.
   * @param ext optional extension
   * @return dummy object
   */
  public static Object notexpected(final Object... ext) {
    throw new RuntimeException(bug(ext));
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return dummy object
   */
  public static Object notimplemented(final Object... ext) {
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
    error(obj + NL, ext);
  }

  /**
   * Prints a string to standard error.
   * @param string debug string
   * @param ext text optional extensions
   */
  public static void error(final String string, final Object... ext) {
    System.err.print(info(string, ext));
  }

  /**
   * Returns a server error message.
   * @param ex exception reference
   * @return error message
   */
  public static String server(final Exception ex) {
    debug(ex);
    if(ex instanceof BindException) return SERVERBIND;
    else if(ex instanceof LoginException) return SERVERLOGIN;
    else if(ex instanceof ConnectException) return SERVERERROR;
    else if(ex instanceof SocketTimeoutException) return SERVERTIMEOUT;
    else if(ex instanceof SocketException) return SERVERBIND;
    return ex.getMessage();
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
   */
  public static void stack() {
    errln("You're here:");
    stack(new Throwable());
  }

  /**
   * Prints the stack of the specified error to standard error.
   * @param th error/exception instance
   */
  public static void stack(final Throwable th) {
    final String u = Util.class.getName();
    for(final StackTraceElement s : th.getStackTrace()) {
      if(!s.getClassName().equals(u)) errln("  " + s);
    }
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
    String home = f.isFile() ? f.getPath() : f.getParent();
    if(new File(home, IO.BASEXSUFFIX).exists()) return home;

    // not found; choose user home directory
    return System.getProperty("user.home");
  }

  /**
   * Returns the absolute path to this application.
   * @return application path
   */
  public static String applicationPath() {
    // raw application path
    String path = Util.class.getProtectionDomain().
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
      return tb.toString();
    }
  }
}
