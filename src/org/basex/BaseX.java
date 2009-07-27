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
import org.basex.core.proc.Prompt;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
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
  /** Standalone or Client/Server mode. */
  private final boolean standalone;
  /** XQuery file. */
  private String file;
  /** Output file for queries. */
  private String output;
  /** User query. */
  private String commands;
  /** Console mode. */
  private boolean console = true;

  /**
   * Main method, launching the standalone console mode.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
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
    boolean user = false;

    // arguments were successfully parsed...
    if(parseArguments(args)) {
      if(console) set(Prop.INFO, ON);
  
      if(file != null) {
        // query file contents
        final String query = content();
        if(query != null) process(new XQuery(query), true);
      } else if(commands != null) {
        // process command line arguments
        process(commands);
      } else {
        // enter interactive mode
        user = console();
      }
    }
    quit(user);
  }

  /**
   * Executes the specified process. This method is overwritten by the
   * implementing classes to enable client/server communication.
   * @param pr process
   * @return success flag
   */
  protected ALauncher launcher(final Process pr) {
    return new Launcher(pr, context);
  }

  /**
   * Reads in a query file and returns the content.
   * @return file content
   */
  private String content() {
    try {
      return Token.string(IO.get(file).content()).trim();
    } catch(final IOException ex) {
      error(ex, ex.getMessage() + NL);
      debug(ex);
    }
    return null;
  }

  /**
   * Processes the specified command, specifying verbose output.
   * @param pr process
   * @param v verbose flag
   * @return true if operation was successful
   */
  private boolean process(final Process pr, final boolean v) {
    try {
      final ALauncher launcher = launcher(pr);
      final boolean ok = launcher.execute();
      if(ok && pr.printing()) {
        final PrintOutput out = output != null ? new PrintOutput(output) :
            new PrintOutput(System.out);
        launcher.out(out);
        out.close();
      }

      if(v) {
        final CachedOutput out = new CachedOutput();
        launcher.info(out);
        final String inf = out.toString();
        if(inf.length() != 0) {
          if(!ok) {
            error(null, inf);
          } else {
            BaseX.out(inf);
          }
        }
      }
      if(v && console && !(pr instanceof Prompt)) BaseX.outln();
      return ok;
    } catch(final IOException ex) {
      error(ex, SERVERERR + NL);
    }
    return false;
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
      error(ex, ex.getMessage() + NL);
    }
    return true;
  }

  /**
   * Quits the code.
   * @param user states if application was actively quit by the user
   */
  protected void quit(final boolean user) {
    if(user) BaseX.outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Launches the console mode, waiting and processing user input.
   * @return forced interrupt
   */
  private boolean console() {
    BaseX.outln(CONSOLE, standalone ? LOCALMODE : CLIENTMODE);

    while(true) {
      process(new Prompt(), true);
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
      // also catches interruptions such as ctrl+c, etc.
      return null;
    }
  }

  /**
   * Sets the specified option.
   * @param opt option to be set
   * @param arg argument
   */
  private void set(final Object[] opt, final Object arg) {
    process(new Set(opt, arg.toString()), false);
  }

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  private void error(final Exception ex, final String msg) {
    BaseX.err((console ? "" : INFOERROR) + msg);
    BaseX.debug(ex);
  }

  /**
   * Parses the command line arguments, specified by the user.
   * @param args command line arguments
   * @return true if arguments have been correctly parsed
   */
  private boolean parseArguments(final String[] args) {
    boolean ok = true;

    // loop through all arguments
    for(int a = 0; a < args.length; a++) {
      ok = false;
      // interpret argument without dash as XQuery
      if(!args[a].startsWith("-")) {
        console = false;
        file = args[a];
        ok = true;
      } else {
        final Prop prop = context.prop;
        for(int i = 1; i < args[a].length(); i++) {
          ok = false;
          final char c = args[a].charAt(i);
          if(c == 'c') {
            // hidden option: chop XML whitespaces
            set(Prop.CHOP, true);
            ok = true;
          } else if(c == 'd') {
            // activate debug mode
            Prop.debug = true;
            ok = true;
          } else if(c == 'D' && standalone) {
            // hidden option: show dot query graph
            set(Prop.DOTPLAN, true);
            ok = true;
          } else if(c == 'e') {
            // hidden option: skip parsing of XML entities
            set(Prop.ENTITY, false);
            ok = true;
          } else if(c == 'm') {
            // hidden option: activate table main memory mode
            set(Prop.TABLEMEM, true);
            ok = true;
          } else if(c == 'M') {
            // hidden option: activate main memory mode
            set(Prop.MAINMEM, true);
            ok = true;
          } else if(c == 'o') {
            // specify file for result output
            if(++i == args[a].length()) { a++; i = 0; }
            if(a == args.length) break;
            output = args[a].substring(i);
            i = args[a].length();
            ok = true;
          } else if(c == 'p' && !standalone) {
            // specify server port
            if(++i == args[a].length()) { a++; i = 0; }
            if(a == args.length) break;
            final int p = Token.toInt(args[a].substring(i));
            if(p <= 0) {
              error(null, SERVERPORT + args[a].substring(i));
              break;
            }
            prop.set(Prop.PORT, p);
            i = args[a].length();
            ok = true;
          } else if(c == 'q') {
            // send BaseX commands
            console = false;
            String input = "";
            if(i + 1 < args[a].length()) input = args[a].substring(i + 1);
            for(++a; a < args.length; a++) input += ' ' + args[a];
            commands = input.trim();
            return true;
          } else if(c == 'r') {
            // hidden option: parse number of runs
            if(++i == args[a].length()) { a++; i = 0; }
            if(a == args.length) break;
            // turn off result serialization
            final int runs = Math.max(1, Token.toInt(args[a].substring(i)));
            set(Prop.RUNS, runs);
            i = args[a].length();
            ok = true;
          } else if(c == 's' && !standalone) {
            // parse server name
            if(++i == args[a].length()) { a++; i = 0; }
            if(a == args.length) break;
            prop.set(Prop.HOST, args[a].substring(i));
            i = args[a].length();
            ok = true;
          } else if(c == 'v') {
            // show process info
            set(Prop.INFO, true);
            ok = true;
          } else if(c == 'V') {
            // show all process info
            set(Prop.INFO, ALL);
            ok = true;
          } else if(c == 'x') {
            // activate well-formed XML output
            set(Prop.XMLOUTPUT, true);
            ok = true;
          } else if(c == 'X') {
            // hidden option: show xml query plan
            set(Prop.XMLPLAN, true);
            ok = true;
          } else if(c == 'z') {
            // turn off result serialization
            set(Prop.SERIALIZE, false);
            ok = true;
          } else {
            break;
          }
        }
      }
      if(!ok) break;
    }
    if(!ok) BaseX.outln(standalone ? LOCALINFO : CLIENTINFO);
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
