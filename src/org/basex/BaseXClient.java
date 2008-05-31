package org.basex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import org.basex.core.AbstractProcess;
import org.basex.core.ClientProcess;
import org.basex.core.Command;
import org.basex.core.CommandParser;
import org.basex.core.Commands;
import org.basex.core.Prop;
import org.basex.core.proc.Set;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.io.ConsoleOutput;
import org.basex.io.PrintOutput;
import org.basex.util.Token;
import static org.basex.Text.*;

/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class BaseXClient {
  /** Stand-alone or Client/Server mode. */
  protected boolean standalone;

  /** Flag for showing info on command processing. */
  private boolean info;
  /** Flag for showing detailed info on command processing. */
  private boolean allInfo;
  /** XPath file. */
  private String query;
  /** XQuery file. */
  private boolean xpath;
  /** Output file for XPath queries. */
  protected String output;
  /** User query. */
  private String commands;
  /** Server name; default is 'localhost'. */
  private String host = "localhost";
  /** Server port. */
  private int port = Prop.port;
  /** Console mode. */
  boolean console = true;

  /**
   * Main method of the <code>BaseXClient</code>, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXClient().init(args);
  }

  /**
   * Initializes the console mode and parses the command-line arguments.
   * interacting with the server instance.
   * @param args command line arguments
   */
  void init(final String[] args) {
    Prop.read();
    if(!parseArguments(args)) return;
    run();
  }

  /**
   * Runs the application, dependent on the command-line arguments.
   */
  final void run() {
    if(allInfo || info) {
      execute(Commands.SET, Set.INFO + " " + (allInfo ? Set.ALL : ON), false);
    }

    try {
      // replace input file with content
      if(query != null) {
        query = content();
        if(query == null) return;
      }

      if(query != null) {
        execute(xpath ? Commands.XPATH : Commands.XQUERY, query, info);
      } else if(commands != null) {
        parse(commands);
        quit(true);
      } else {
        console();
      }
    } catch(final Exception ex) {
      error(ex, ex.getMessage());
    }
  }

  /**
   * Starts the console mode.
   */
  private void console() {
    BaseX.outln(CONSOLE, standalone ? LOCALMODE : CLIENTMODE);

    // wait for user input
    String input = null;
    do {
      execute(Commands.PROMPT, null, false);
      input = input(INPUT);
      if("xquery".equals(input)) {
        String in = "";
        while((in = input(INPUTCONT)).length() != 0) input += " " + in;
      }
    } while(input != null && parse(input));
    
    quit(input == null);
  }

  /**
   * Returns the next user input.
   * @param prompt prompt string
   * @return user input
   */
  private String input(final String prompt) {
    BaseX.out(prompt);
    // get user input
    try {
      final InputStreamReader isr = new InputStreamReader(System.in);
      return new BufferedReader(isr).readLine().trim();
    } catch(final Exception ex) {
      // also catches interruptions such as ctrl+c, etc.
      BaseX.outln("");
      return null;
    }
  }

  /**
   * Quits <code>BaseX</code>.
   * @param force tells if quit was forced
   * (e.g., by pressing CTRL-c).
   */
  synchronized void quit(final boolean force) {
    if(!force) BaseX.outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Evaluates the user input, which can be any user input or the commands
   * specified after the <code>-q</code> command-line argument.
   * @param input input commands
   * @return false if exit command was sent
   */
  private boolean parse(final String input) {
    try {
      final CommandParser cp = new CommandParser(input);
      while(cp.more()) {
        final Command cmd = cp.next();
        if(cmd.name.local()) {
          if(cmd.name == Commands.EXIT || cmd.name == Commands.QUIT)
            return false;
          throw new IllegalArgumentException(
              BaseX.info("Unknown local command '%'.", input));
        }
        if(!process(cmd, info)) break;
      }
    } catch(final IllegalArgumentException ex) {
      error(ex, ex.getMessage());
    }
    return true;
  }

  /**
   * Reads in a query file and returns the content.
   * @return file content
   */
  private String content() {
    // open file
    try {
      return Token.string(new IO(query).content()).trim();
    } catch(final IOException ex) {
      error(ex, ex.getMessage());
      BaseX.debug(ex);
    }
    return null;
  }

  /**
   * Processes the specified command, specifying verbose output.
   * @param comm command
   * @param arg argument
   * @param v verbose flag
   * @return true if operation was successful
   */
  protected final boolean execute(final Commands comm, final String arg,
      final boolean v) {
    return process(new Command(comm, arg), v);
  }

  /**
   * Processes the specified command, specifying verbose output.
   * @param cmd command
   * @param v verbose flag
   * @return true if operation was successful
   */
  protected final boolean process(final Command cmd, final boolean v) {
    final AbstractProcess proc = getProcess(cmd);
    try {
      final boolean ok = proc.execute();
      final Commands name = cmd.name;
      if(ok && name.printing()) {
        final PrintOutput out = output != null ? new PrintOutput(output) :
            new ConsoleOutput(System.out);
        proc.output(out);
        out.close();
      }

      if(!ok || v || !name.printing() || name == Commands.SET) {
        final CachedOutput out = new CachedOutput();
        proc.info(out);
        final String inf = out.toString();
        if(inf.length() != 0) {
          if(!ok) {
            error(null, inf);
          } else {
            if(info || console) {
              BaseX.outln(inf);
              if(console) BaseX.outln("");
            }
          }
        }

        if(name == Commands.SET && cmd.args().startsWith(Set.INFO)) {
          info = inf.contains(INFOON);
        }
      }
      return ok;
    } catch(final IOException ex) {
      error(ex, SERVERERR);
      return false;
    }
  }

  /**
   * Return command process.
   * @param cmd command
   * @return process
   */
  protected AbstractProcess getProcess(final Command cmd) {
    return new ClientProcess(host, port, cmd);
  }

  /**
   * Parses the command line arguments, specified by the user.
   * @param args command line arguments
   * @return true if arguments have been correctly parsed
   */
  protected final boolean parseArguments(final String[] args) {
    boolean ok = true;

    // loop through all arguments
    for(int a = 0; a < args.length; a++) {
      ok = false;
      // interpret argument without dash as XQuery
      if(!args[a].startsWith("-")) {
        console = false;
        query = args[a];
        Prop.onthefly = true;
        Prop.mainmem = true;
        ok = true;
      } else {
        for(int i = 1; i < args[a].length(); i++) {
          ok = false;
          final char c = args[a].charAt(i);
          if(c == 'c' && standalone) {
            // chop XML whitespaces while creating new database instances
            execute(Commands.SET, Set.CHOP + " on", false);
            ok = true;
          } else if(c == 'd') {
            // activate debug mode
            Prop.debug = true;
            ok = true;
          } else if(c == 'e' && standalone) {
            // skip parsing of XML entities
            execute(Commands.SET, Set.ENTITY  + OFF, false);
            ok = true;
          } else if(c == 'o') {
            // specify file for result output
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            output = args[a].substring(i);
            i = args[a].length();
            ok = true;
          } else if(c == 'p' && !standalone) {
            // specify server port
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            final int p = Token.toInt(args[a].substring(i));
            if(p < 0) {
              error(null, BaseX.info(SERVERPORT, args[a].substring(i)));
              break;
            }
            port = p;
            i = args[a].length();
            ok = true;
          /*} else if(c == 'p') {
            // switch to XPath parser
            xpath = true;
            ok = true;
          */
          } else if(c == 'q') {
            // send BaseX commands
            console = false;
            String input = "";
            if(i + 1 < args[a].length()) input = args[a].substring(i + 1);
            for(++a; a < args.length; a++) input += ' ' + args[a];
            commands = input.trim();
            return true;
          } else if(c == 'r' && standalone) {
            // hidden option: parse number of runs
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            Prop.runs = Math.max(1, Token.toInt(args[a].substring(i)));
            i = args[a].length();
            ok = true;
          } else if(c == 's' && !standalone) {
            // parse server name
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            host = args[a].substring(i);
            i = args[a].length();
            ok = true;
          } else if(c == 'v') {
            // show process info
            info = true;
            ok = true;
          } else if(c == 'V') {
            // show all process info
            allInfo = true;
            info = true;
            ok = true;
          } else if(c == 'x') {
            // activate well-formed XML output
            execute(Commands.SET, Set.XMLOUTPUT + " " + ON, false);
            ok = true;
          } else if(c == 'y' && standalone) {
            // hidden option: activate main memory mode
            Prop.mainmem = true;
            ok = true;
          } else if(c == 'Y' && standalone) {
            // hidden option: activate on-the-fly parsing
            Prop.onthefly = true;
            ok = true;
          } else if(c == 'z') {
            // turn off result serialization
            execute(Commands.SET, Set.SERIALIZE + " " + OFF, false);
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

  /**
   * Prints an error message.
   * @param ex exception reference
   * @param msg message
   */
  protected final void error(final Exception ex, final String msg) {
    BaseX.errln((console ? "" : INFOERROR + ": ") + msg);
    BaseX.debug(ex);
  }
}
