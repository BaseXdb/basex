package org.basex.core;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.net.BindException;
import java.util.Random;
import org.basex.core.proc.Exit;
import org.basex.core.proc.Password;
import org.basex.core.proc.Set;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.server.LoginException;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This is the abstract main class for all starter classes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Database Context. */
  public final Context context = new Context();
  /** Successful command line parsing. */
  protected boolean success;
  /** Output file for queries. */
  protected String output;

  /** Session. */
  protected Session session;
  /** Console mode. */
  protected boolean console;

  /**
   * Constructor.
   * @param args command line arguments
   */
  protected Main(final String... args) {
    parseArguments(args);
    if(!success) return;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        context.close();
      }
    });
  }

  /**
   * Launches the console mode, waiting for and processing user input.
   * @return true if exit command was sent
   * @throws IOException I/O exception
   */
  protected final boolean console() throws IOException {
    set(Prop.INFO, ON);

    while(console) {
      Main.out("> ");
      final String in = System.console().readLine();
      if(in == null) break;
      if(!process(in)) return true;
    }
    return false;
  }

  /**
   * Quits the console mode.
   * @param user quit by user
   */
  protected synchronized void quit(final boolean user) {
    try {
      process(new Exit(), true);
    } catch(final IOException ex) {
      error(ex, true);
    }
    if(user) outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Parses and evaluates the input string.
   * @param in input commands
   * @return false if exit command was sent
   * @throws IOException I/O exception
   */
  protected final boolean process(final String in) throws IOException {
    try {
      for(final Process p : new CommandParser(in, context).parse()) {
        if(p instanceof Exit) return false;
        if(p instanceof Password && p.args[0] == null) {
          Main.out(SERVERPW + COLS);
          p.args[0] = new String(System.console().readPassword());
        }
        if(!process(p, true)) break;
      }
    } catch(final QueryException ex) {
      error(ex, ex.getMessage());
    }
    return true;
  }

  /**
   * Processes the specified command, specifying verbose output.
   * @param pr process
   * @param v verbose flag
   * @return true if operation was successful
   * @throws IOException I/O exception
   */
  protected final boolean process(final Process pr, final boolean v)
      throws IOException {

    final Session ss = session();
    if(ss == null) return false;
    final PrintOutput out = output != null ? new PrintOutput(output) :
      new PrintOutput(System.out);
    final boolean ok = ss.execute(pr, out);
    out.close();
    if(pr instanceof Exit) return true;

    if(v || !ok) {
      final String inf = ss.info();
      if(inf.length() != 0) {
        if(!ok) {
          error(null, inf);
        } else {
          out(inf);
        }
      }
    }
    if(v && console) outln();
    return ok;
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   * @return success flag
   * @throws IOException I/O exception
   */
  protected final boolean set(final Object[] opt, final Object arg)
      throws IOException {
    return process(new Set(opt, arg), false);
  }

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  protected final void error(final Exception ex, final String msg) {
    errln((console ? "" : INFOERROR) + msg.trim());
    debug(ex);
  }

  /**
   * Returns the session.
   * @return session
   * @throws IOException I/O exception
   */
  protected abstract Session session() throws IOException;

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   */
  protected abstract void parseArguments(final String[] args);

  // GLOBAL STATIC METHODS ====================================================

  /**
   * Prints some information for an unexpected exception.
   * @param ext optional extension
   * @return dummy object
   */
  public static String bug(final Object... ext) {
    final TokenBuilder sb = new TokenBuilder(
        "Bug? Please send to " + MAIL);
    if(ext.length != 0) sb.add(" (%)", ext);
    return sb.toString();
  }

  /**
   * Throws a runtime exception for an unexpected exception.
   * @param ext optional extension
   * @return dummy object
   */
  public static Object notexpected(final Object... ext) {
    throw new BaseXException(bug(ext));
  }

  /**
   * Throws a runtime exception for an unimplemented method.
   * @param ext optional extension
   * @return dummy object
   */
  public static Object notimplemented(final Object... ext) {
    final TokenBuilder sb = new TokenBuilder("Not Implemented.");
    if(ext.length != 0) sb.add(" (%)", ext);
    throw new BaseXException(sb.add('.').toString());
  }

  /**
   * Returns the class name of the specified object.
   * @param o object
   * @return class name
   */
  public static String name(final Class<?> o) {
    return o.getSimpleName();
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
   * Returns an info message for the specified flag.
   * @param flag current flag status
   * @return ON/OFF message
   */
  public static String flag(final boolean flag) {
    return flag ? INFOON : INFOOFF;
  }

  /**
   * Global method for printing a newline.
   */
  public static void outln() {
    out(NL);
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
   * Global method for printing information to the standard output.
   * @param str output string
   * @param ext text optional extensions
   */
  public static void out(final Object str, final Object... ext) {
    System.out.print(info(str, ext));
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
   * @param string debug string
   * @param ext text optional extensions
   */
  public static void err(final String string, final Object... ext) {
    System.err.print(info(string, ext));
  }

  /**
   * Prints a server error message.
   * @param ex exception reference
   * @param msg message flag
   */
  public static void error(final Exception ex, final boolean msg) {
    if(msg) {
      debug(ex);
      if(ex instanceof BindException) {
        errln(SERVERBIND);
      } else if(ex instanceof LoginException) {
        errln(SERVERLOGIN);
      } else if(ex instanceof IOException) {
        errln(SERVERERR);
      } else {
        errln(ex.getMessage());
      }
    } else {
      ex.printStackTrace();
    }
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
   * Global method for printing debug information if the
   * {@link Prop#debug} flag is set.
   * @param str debug string
   * @param ext text optional extensions
   */
  public static void debug(final Object str, final Object... ext) {
    if(Prop.debug) errln(str, ext);
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
}
