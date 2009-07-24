package org.basex;

import static org.basex.Text.*;

import java.io.IOException;

import org.basex.core.AbstractProcess;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This is the starter class for the stand-alone console mode.
 * It overwrites the {@link BaseXClient} to allow local database
 * operations. Next, it offers some utility methods which are used
 * throughout the project.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BaseX extends BaseXClient {
  /** Database Context. */
  final Context context = new Context();

  /**
   * Main method, launching the stand-alone console mode.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseX().init(args);
  }

  @Override
  public void init(final String[] args) {
    standalone = true;
    super.init(args);

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Prop.write();
        context.close();
      }
    });
  }

  @Override
  protected boolean execute(final AbstractProcess proc) throws IOException {
    return proc.execute(context);
  }

  @Override
  protected AbstractProcess getProcess(final Process p) {
    return p;
  }

  /**
   * Global method, replacing all % characters
   * (see {@link TokenBuilder#add(Object, Object...)} for details.
   * @param str string to be extended
   * @param ext text text extensions
   * @return token
   */
  public static byte[] inf(final Object str, final Object... ext) {
    final TokenBuilder info = new TokenBuilder();
    info.add(str, ext);
    return info.finish();
  }

  /**
   * Global method, replacing all % characters
   * (see {@link TokenBuilder#add(Object, Object...)} for details.
   * @param str string to be extended
   * @param ext text text extensions
   * @return extended string
   */
  public static String info(final Object str, final Object... ext) {
    return Token.string(inf(str, ext));
  }

  /**
   * Global method for printing debug information if the
   * {@link Prop#debug} flag is set.
   * @param str debug string
   * @param ext text optional extensions
   */
  public static void debug(final Object str, final Object... ext) {
    if(Prop.debug) errln(str, ext);
  }

  /**
   * Global method for printing the exception stack trace if the
   * {@link Prop#debug} flag is set.
   * @param ex exception
   * @return always false
   */
  public static boolean debug(final Exception ex) {
    if(Prop.debug && ex != null) ex.printStackTrace();
    return false;
  }

  /**
   * Global method for printing information to the standard output.
   * @param string debug string
   * @param ext text optional extensions
   */
  public static void err(final String string, final Object... ext) {
    System.err.print(info(string, ext));
  }

  /**
   * Global method for printing information to the standard output.
   * @param obj error string
   * @param ext text optional extensions
   */
  public static void errln(final Object obj, final Object... ext) {
    err(obj + NL, ext);
  }

  /**
   * Global method for printing information to the standard output.
   * @param str output string
   * @param ext text optional extensions
   */
  public static void out(final Object str, final Object... ext) {
    System.out.print(info(str, ext));
  }

  /**
   * Global method for printing information to the standard output.
   * @param str output string
   * @param ext text optional extensions
   */
  public static void outln(final Object str, final Object... ext) {
    out(str + NL, ext);
  }

  /**
   * Global method for printing a newline.
   */
  public static void outln() {
    out(NL);
  }

  /**
   * Returns an info message for the specified flag.
   * @param flag current flag status
   * @return ON/OFF message
   */
  public static String flag(final boolean flag) {
    return flag ? INFOON : INFOOFF;
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return dummy object
   */
  public static Object notimplemented(final Object... ext) {
    final TokenBuilder sb = new TokenBuilder("Not Implemented.");
    if(ext.length != 0) sb.add(" (%)", ext);
    throw new UnsupportedOperationException(sb.add('.').toString());
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
   * Prints some information for an unexpected exception.
   * @param ext optional extension
   * @return dummy object
   */
  public static String bug(final Object... ext) {
    final TokenBuilder sb = new TokenBuilder(
        "Potential Bug? Please send to " + MAIL);
    if(ext.length != 0) sb.add(" (%)", ext);
    return sb.toString();
  }
}
