package org.basex.core;

import static org.basex.core.Text.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.util.Random;
import org.basex.core.proc.Exit;
import org.basex.core.proc.IntPrompt;
import org.basex.core.proc.Set;
import org.basex.io.CachedOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.server.ClientLauncher;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This is the abstract class for all starter classes.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Main {
  /** Database Context. */
  public final Context context = new Context();
  /** Successful command line parsing. */
  protected boolean ok;
  /** Output file for queries. */
  protected String output;

  /** Launcher. */
  protected ALauncher launcher;
  /** Console mode. */
  protected boolean console;

  /**
   * Constructor.
   * @param sa standalone flag
   * @param sv server flag
   * @param args command line arguments
   */
  public Main(final boolean sa, final boolean sv, final String... args) {
    context.server = sv;

    if(sa) {
      launcher = new Launcher(context);
    } else {
      try {
        launcher = new ClientLauncher(context);
      } catch(final Exception ex) {
        // no server available; switches to standalone mode
        Main.error(ex, true);
        ok = false;
        return;
      }
    }
    parseArguments(args);
  }

  /**
   * Launches the console mode, waiting for and processing user input.
   * @return user interruption
   */
  protected boolean console() {
    if(console) set(Prop.INFO, ON);
    while(console) {
      if(!process(new IntPrompt(), true)) break;
      final String in = input();
      if(in == null) break;
      if(!process(in)) return true;
    }
    return false;
  }

  /**
   * Returns the next user input.
   * @return user input
   */
  private String input() {
    try {
      final InputStreamReader isr = new InputStreamReader(System.in);
      return new BufferedReader(isr).readLine().trim();
    } catch(final Exception ex) {
      // also catches forced interruptions such as ctrl+c
      return null;
    }
  }

  /**
   * Evaluates the input, which can be interactive input or the commands
   * specified after the <code>-q</code> command-line argument.
   * @param in input commands
   * @return false if exit command was sent
   */
  protected boolean process(final String in) {
    try {
      for(final Process p :
        new CommandParser(in, context, false, context.server).parse()) {
        if(p instanceof Exit) return false;
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
   */
  protected boolean process(final Process pr, final boolean v) {
    try {
      final boolean success = launcher.execute(pr);

      if(success && pr.printing()) {
        final PrintOutput out = output != null ? new PrintOutput(output) :
            new PrintOutput(System.out);
        launcher.output(out);
        out.close();
      }

      if(v || !success) {
        final CachedOutput out = new CachedOutput();
        launcher.info(out);
        final String inf = out.toString();
        if(inf.length() != 0) {
          if(!success) {
            error(null, inf);
          } else {
            out(inf);
          }
        }
      }
      if(v && console && !(pr instanceof IntPrompt)) outln();
      return success;
    } catch(final IOException ex) {
      error(ex, SERVERERR);
      return false;
    }
  }

  /**
   * Quits the code.
   * @param user states if application was actively quit by the user
   */
  protected void quit(final boolean user) {
    try {
      launcher.execute(new Exit());
    } catch(final Exception ex) {
      error(ex, true);
    }
    if(user) outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   * @return success flag
   */
  protected boolean set(final Object[] opt, final Object arg) {
    return process(new Set(opt, arg), false);
  }

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  protected void error(final Exception ex, final String msg) {
    errln((console ? "" : INFOERROR) + msg.trim());
    debug(ex);
  }

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
    throw new RuntimeException(bug(ext));
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
   * @param quiet quiet flag
   */
  public static void error(final Exception ex, final boolean quiet) {
    if(quiet) {
      debug(ex);
      if(ex instanceof BindException) {
        errln(SERVERBIND);
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
