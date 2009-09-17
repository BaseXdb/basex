package org.basex;

import static org.basex.Text.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import org.basex.core.ALauncher;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Launcher;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.core.proc.IntPrompt;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Args;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 * Next, the class offers some utility methods which are used
 * throughout the project.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public class BaseX {
  /** Database Context. */
  protected final Context context = new Context();
  /** Launcher. */
  protected ALauncher launcher;
  /** Standalone or Client/Server mode. */
  protected boolean standalone;

  /** XQuery file. */
  private String file;
  /** Output file for queries. */
  private String output;
  /** User query. */
  private String commands;
  /** Console mode. */
  private boolean console;

  /**
   * Main method, launching the standalone console mode.
   * Use <code>-h</code> to get a list of optional command-line arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseX(true).run(args);
  }

  /**
   * Constructor.
   * @param sa standalone flag
   */
  public BaseX(final boolean sa) {
    standalone = sa;

    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        context.close();
      }
    });
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public void run(final String[] args) {
    // parse arguments
    if(!parseArguments(args)) return;

    boolean user = false;
    if(file != null) {
      // query file contents
      final String query = content();
      if(query != null) process(new XQuery(query), true);
    } else if(commands != null) {
      // process command-line arguments
      process(commands);
    } else {
      // enter interactive mode
      set(Prop.INFO, ON);
      user = console();
    }
    quit(user);
  }

  /**
   * Launches the console mode, waiting for and processing user input.
   * @return forced interrupt
   */
  private boolean console() {
    outln(CONSOLE, standalone ? LOCALMODE : CLIENTMODE);

    while(true) {
      if(!process(new IntPrompt(), true)) return false;
      final String in = input();
      if(in == null) return false;
      if(!process(in)) return true;
    }
  }

  /**
   * Returns the next user input.
   * @return user input
   */
  private String input() {
    // get user input
    try {
      final InputStreamReader isr = new InputStreamReader(System.in);
      return new BufferedReader(isr).readLine().trim();
    } catch(final Exception ex) {
      // also catches forced interruptions such as ctrl+c
      return null;
    }
  }

  /**
   * Executes the specified process. This method is overwritten by the
   * implementing classes to enable client/server communication.
   * @return success flag
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected ALauncher launcher() throws IOException {
    if(launcher == null) launcher = new Launcher(context);
    return launcher;
  }

  /**
   * Reads in a query file and returns the content.
   * @return file content
   */
  private String content() {
    final IO io = IO.get(file);
    if(!io.exists()) {
      errln(FILEWHICH, file);
    } else {
      try {
        return Token.string(io.content()).trim();
      } catch(final IOException ex) {
        error(ex, ex.getMessage());
      }
    }
    return null;
  }

  /**
   * Evaluates the input, which can be interactive input or the commands
   * specified after the <code>-q</code> command-line argument.
   * @param in input commands
   * @return false if exit command was sent
   */
  private boolean process(final String in) {
    try {
      for(final Process p : new CommandParser(in, context).parse()) {
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
      final ALauncher la = launcher();
      final boolean ok = la.execute(pr);

      if(ok && pr.printing()) {
        final PrintOutput out = output != null ? new PrintOutput(output) :
            new PrintOutput(System.out);
        la.output(out);
        out.close();
      }

      if(v || !ok) {
        final CachedOutput out = new CachedOutput();
        la.info(out);
        final String inf = out.toString();
        if(inf.length() != 0) {
          if(!ok) {
            error(null, inf);
          } else {
            out(inf);
          }
        }
      }
      if(v && console && !(pr instanceof IntPrompt)) outln();
      return ok;
    } catch(final IOException ex) {
      error(ex, SERVERERR);
      return false;
    }
  }

  /**
   * Quits the code.
   * @param user states if application was actively quit by the user
   */
  private void quit(final boolean user) {
    try {
      launcher().execute(new Exit());
    } catch(final Exception ex) {
      BaseXServer.error(ex, true);
    }
    if(user) outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   * @return success flag
   */
  private boolean set(final Object[] opt, final Object arg) {
    return process(new Set(opt, arg), false);
  }

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  private void error(final Exception ex, final String msg) {
    errln((console ? "" : INFOERROR) + msg.trim());
    debug(ex);
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @return true if arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    final Args arg = new Args(args);
    boolean ok = true;
    while(arg.more() && ok) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'd') {
          // activate debug mode
          context.prop.set(Prop.DEBUG, true);
        } else if(c == 'D' && standalone) {
          // hidden option: show dot query graph
          ok = set(Prop.DOTPLAN, true);
        } else if(c == 'm') {
          // hidden option: activate table main memory mode
          ok = set(Prop.TABLEMEM, true);
        } else if(c == 'M') {
          // hidden option: activate main memory mode
          ok = set(Prop.MAINMEM, true);
        } else if(c == 'o') {
          // specify file for result output
          output = arg.string();
        } else if(c == 'p' && !standalone) {
          // parse server port
          context.prop.set(Prop.PORT, Integer.parseInt(arg.string()));
        } else if(c == 'q') {
          // send database commands
          commands = arg.rest();
        } else if(c == 'r') {
          // hidden option: parse number of runs
          ok = set(Prop.RUNS, arg.string());
        } else if(c == 's' && !standalone) {
          // parse server name
          context.prop.set(Prop.HOST, arg.string());
        } else if(c == 'v') {
          // show process info
          ok = set(Prop.INFO, true);
        } else if(c == 'V') {
          // show all process info
          ok = set(Prop.INFO, ALL);
        } else if(c == 'x') {
          // activate well-formed XML output
          ok = set(Prop.XMLOUTPUT, true);
        } else if(c == 'X') {
          // hidden option: show xml query plan
          ok = set(Prop.XMLPLAN, true);
        } else if(c == 'z') {
          // turn off result serialization
          ok = set(Prop.SERIALIZE, false);
        } else {
          ok = false;
        }
      } else {
        file = file == null ? arg.string() : file + " " + arg.string();
      }
    }
    console = file == null && commands == null;
    if(!ok) outln(standalone ? LOCALINFO : CLIENTINFO);

    // returns the success flag and initializes the execution
    return ok;
  }

  // GLOBAL STATIC METHODS ====================================================

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
   * Returns the class name of the specified object.
   * @param o object
   * @return class name
   */
  public static String name(final Object o) {
    return name(o.getClass());
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
        "Bug? Please send to " + MAIL);
    if(ext.length != 0) sb.add(" (%)", ext);
    return sb.toString();
  }
}
