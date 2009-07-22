package org.basex.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;
import org.basex.BaseX;
import org.basex.core.AbstractProcess;
import org.basex.core.CommandParser;
import org.basex.core.Context;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.core.proc.Exit;
import org.basex.core.proc.Prompt;
import org.basex.core.proc.Set;
import org.basex.core.proc.XQuery;
import org.basex.io.IO;
import org.basex.io.CachedOutput;
import org.basex.io.ConsoleOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.util.Token;
import static org.basex.Text.*;
import static org.basex.core.Commands.*;

/**
 * This is the starter class for the client console mode.
 * It sends all commands to the server instance.
 * Add the '-h' option to get a list on all available command-line arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public final class BaseXClientNew {
  /** Database Context. */
  final Context context = new Context();
  /** Stand-alone or Client/Server mode. */
  boolean standalone;
  /** Output file for queries. */
  String output;
  /** Console mode. */
  boolean console = true;

  /** Flag for showing info on command processing. */
  private boolean info;
  /** Flag for showing detailed info on command processing. */
  private boolean allInfo;
  /** XQuery file. */
  private String query;
  /** User query. */
  private String commands;
  /** Server name; default is 'localhost'. */
  private String host = "localhost";
  /** Server port. */
  private int port = Prop.port;
  /** Socket. */
  private Socket socket;

  /**
   * Main method of the <code>BaseXClient</code>, launching a local
   * client instance that sends all commands to a server instance.
   * Use <code>-h</code> to get a list of all available command-line
   * arguments.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new BaseXClientNew().init(args);
  }

  /**
   * Initializes the console mode and parses the command-line arguments.
   * interacting with the server instance.
   * @param args command line arguments
   */
  protected void init(final String[] args) {
    if(!parseArguments(args)) return;
    try {
      socket = new Socket(host, port);
    } catch(final IOException e) {
      BaseX.errln(SERVERERR);
    }
    run();
  }

  /**
   * Runs the application, dependent on the command-line arguments.
   */
  void run() {
    // guarantee correct shutdown...
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Prop.write();
        context.close();
      }
    });

    if(allInfo || info)
      process(new Set(CmdSet.INFO, allInfo ? ALL : ON), false);

    try {
      // replace input file with content
      if(query != null) {
        query = content();
        if(query == null) return;
      }

      if(query != null) {
        process(new XQuery(query), info || Prop.xmlplan);
      } else if(commands != null) {
        process(commands);
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
      process(new Prompt(), false);
      input = input("");
      if("xquery".equals(input)) {
        String in = "";
        while((in = input(INPUTCONT)).length() != 0) input += " " + in;
      }
    } while(input != null && process(input));

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
      BaseX.outln();
      return null;
    }
  }

  /**
   * Quits <code>BaseX</code>.
   * @param force tells if quit was forced
   * (e.g., by pressing CTRL-c).
   */
  protected void quit(final boolean force) {
    if(!force) BaseX.outln(CLIENTBYE[new Random().nextInt(4)]);
  }

  /**
   * Evaluates the input, which can be interactive input or the commands
   * specified after the <code>-q</code> command-line argument.
   * @param in input commands
   * @return false if exit command was sent
   */
  private boolean process(final String in) {
    try {
      for(final Process p : new CommandParser(in).parse()) {
        if(p instanceof Exit) {
          final AbstractProcess proc = getProcess(p);
          try {
            return proc.execute(context);
          } catch(final IOException e) {
            e.printStackTrace();
          }
        }
        final boolean qu = p instanceof XQuery;
        if(!process(p, info || qu && Prop.xmlplan)) break;
      }
    } catch(final QueryException ex) {
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
      return Token.string(IO.get(query).content()).trim();
    } catch(final IOException ex) {
      error(ex, ex.getMessage());
      BaseX.debug(ex);
    }
    return null;
  }

  /**
   * Processes the specified command, specifying verbose output.
   * @param p process
   * @param v verbose flag
   * @return true if operation was successful
   */
  protected boolean process(final Process p, final boolean v) {
    final AbstractProcess proc = getProcess(p);
    try {
      final boolean ok = proc.execute(context);
      if(ok && p.printing()) {
        final PrintOutput out = output != null ? new PrintOutput(output) :
            new ConsoleOutput(System.out);
        proc.output(out);
        out.close();
      }

      final boolean set = p instanceof Set;
      if(!ok || v || !p.printing() || set) {
        final CachedOutput out = new CachedOutput();
        proc.info(out);
        final String inf = out.toString();
        if(!ok || inf.length() != 0) {
          if(!ok) {
            error(null, inf);
          } else if(v || console) {
            BaseX.outln(inf);
            //if(console) BaseX.outln();
          }
        }

        if(set && p.args[0].equals(CmdSet.INFO.name())) {
          info = inf.contains(INFOON);
        }
      } else if(console && !(p instanceof Prompt)) {
        BaseX.outln();
      }
      return ok;
    } catch(final FileNotFoundException ex) {
      error(ex, ex.getMessage());
    } catch(final IOException ex) {
      error(ex, SERVERERR);
    }
    return false;
  }

  /**
   * Returns a process instance.
   * @param p process
   * @return process
   */
  protected AbstractProcess getProcess(final Process p) {
    return new ClientProcessNew(socket, p);
  }

  /**
   * Parses the command line arguments, specified by the user.
   * @param args command line arguments
   * @return true if arguments have been correctly parsed
   */
  protected boolean parseArguments(final String[] args) {
    boolean ok = true;

    // loop through all arguments
    for(int a = 0; a < args.length; a++) {
      ok = false;
      // interpret argument without dash as XQuery
      if(!args[a].startsWith("-")) {
        console = false;
        query = args[a];
        ok = true;
      } else {
        for(int i = 1; i < args[a].length(); i++) {
          ok = false;
          final char c = args[a].charAt(i);
          if(c == 'c' && standalone) {
            // chop XML whitespaces while creating new database instances
            process(new Set(CmdSet.CHOP, ON), false);
            ok = true;
          } else if(c == 'd') {
            // activate debug mode
            Prop.debug = true;
            ok = true;
          } else if(c == 'D' && standalone) {
            // hidden option: show dot query graph
            Prop.dotplan = true;
            ok = true;
          } else if(c == 'e' && standalone) {
            // skip parsing of XML entities
            process(new Set(CmdSet.ENTITY, OFF), false);
            ok = true;
          } else if(c == 'm' && standalone) {
            // hidden option: activate table main memory mode
            Prop.tablemem = true;
            ok = true;
          } else if(c == 'M' && standalone) {
            // hidden option: activate on-the-fly parsing
            Prop.mainmem = true;
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
            if(p <= 0) {
              error(null, SERVERPORT + args[a].substring(i));
              break;
            }
            port = p;
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
            if(++i == args[a].length()) {
              a++;
              i = 0;
            }
            if(a == args.length) break;
            // turn off result serialization
            final int runs = Math.max(1, Token.toInt(args[a].substring(i)));
            process(new Set("RUNS", Integer.toString(runs)), false);
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
            process(new Set(CmdSet.XMLOUTPUT, ON), false);
            ok = true;
          } else if(c == 'X') {
            // hidden option: show xml query plan
            Prop.xmlplan = true;
            ok = true;
          } else if(c == 'z') {
            // turn off result serialization
            process(new Set(CmdSet.SERIALIZE, OFF), false);
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
  protected void error(final Exception ex, final String msg) {
    BaseX.errln((console ? "" : INFOERROR) + msg);
    BaseX.debug(ex);
  }
}
