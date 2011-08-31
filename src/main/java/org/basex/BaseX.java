package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.basex.core.MainProp;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.XQuery;
import org.basex.io.IO;
import org.basex.io.in.TextInput;
import org.basex.io.out.PrintOutput;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Util;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class BaseX extends Main {
  /** Flag for writing properties to disk. */
  private boolean writeProps;
  /** User query. */
  private String commands;
  /** Query file. */
  private String file;
  /** Input file for queries. */
  protected String input;
  /** Query. */
  private String query;
  /** User name. */
  protected String user;
  /** Password. */
  protected String pass;

  /**
   * Main method, launching the standalone console mode.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    if(new BaseX(args).failed()) System.exit(1);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseX(final String... args) {
    super(args);
    if(failed) return;
    run();
  }

  @Override
  public void run() {
    try {
      session();

      boolean u = false;
      if(input != null) {
        if(failed(execute(new Check(input), verbose))) return;
      }

      if(file != null) {
        // query file contents
        context.query = IO.get(file);
        final String qu = content();
        failed(qu != null && execute(new XQuery(qu), verbose));
      } else if(query != null) {
        // query file contents
        failed(execute(new XQuery(query), verbose));
      } else if(commands != null) {
        // execute command-line arguments
        final Boolean b = execute(commands);
        failed(b == null || b);
      } else {
        // enter interactive mode
        Util.outln(CONSOLE + CONSOLE2, sa() ? LOCALMODE : CLIENTMODE);
        u = console();
      }
      if(failed) return;
      if(writeProps) context.mprop.write();
      quit(u);
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
      failed = true;
    }
  }

  /**
   * Reads in a query file and returns the content.
   * @return file content
   */
  private String content() {
    final IO io = IO.get(file);
    if(!io.exists()) {
      Util.errln(INFOERROR + FILEWHICH, file);
    } else {
      try {
        return TextInput.content(io).toString().trim();
      } catch(final IOException ex) {
        error(ex, ex.getMessage());
      }
    }
    return null;
  }

  /**
   * Tests if this client is stand-alone.
   * @return stand-alone flag
   */
  protected boolean sa() {
    return true;
  }

  @Override
  protected Session session() throws IOException {
    if(session == null) session = new LocalSession(context, out);
    session.setOutputStream(out);
    return session;
  }

  @Override
  protected final boolean parseArguments(final String[] args) {
    final StringBuilder serial = new StringBuilder();
    final StringBuilder bind = new StringBuilder();
    try {
      final HashMap<Object[], Object> options = new HashMap<Object[], Object>();
      final Args arg = new Args(args, this, sa() ? LOCALINFO : CLIENTINFO,
          Util.info(CONSOLE, sa() ? LOCALMODE : CLIENTMODE));
      while(arg.more()) {
        if(arg.dash()) {
          final char c = arg.next();
          if(c == 'b') {
            // set/add variable binding
            if(bind.length() != 0) bind.append(',');
            bind.append(arg.string());
            options.put(Prop.BINDINGS, bind);
          } else if(c == 'c') {
            // specify command to be evaluated
            commands = arg.remaining();
          } else if(c == 'd') {
            // activate debug mode
            context.mprop.set(MainProp.DEBUG, true);
          } else if(c == 'D' && sa()) {
            // hidden option: show dot query graph
            options.put(Prop.DOTPLAN, true);
          } else if(c == 'i' && sa()) {
            // open initial file or database
            input = arg.string();
          } else if(c == 'n' && !sa()) {
            // set server name
            context.mprop.set(MainProp.HOST, arg.string());
          } else if(c == 'o') {
            // specify file for result output
            out = new PrintOutput(arg.string());
            if(session != null) session.setOutputStream(out);
          } else if(c == 'p' && !sa()) {
            // set server port
            context.mprop.set(MainProp.PORT, arg.num());
          } else if(c == 'P' && !sa()) {
            // specify password
            pass = arg.string();
          } else if(c == 'q') {
            // specify query to be evaluated
            query = arg.remaining();
          } else if(c == 'r') {
            // hidden option: parse number of runs
            options.put(Prop.RUNS, arg.string());
          } else if(c == 's') {
            // set/add serialization parameter
            if(serial.length() != 0) serial.append(',');
            serial.append(arg.string());
            options.put(Prop.SERIALIZER, serial);
          } else if(c == 'u') {
            // activate write-back for updates
            options.put(Prop.WRITEBACK, true);
          } else if(c == 'U' && !sa()) {
            // specify user name
            user = arg.string();
          } else if(c == 'v') {
            // show command info
            verbose = true;
          } else if(c == 'V') {
            // show query info
            verbose = true;
            options.put(Prop.QUERYINFO, true);
          } else if(c == 'w') {
            // activate write-back for updates
            options.put(Prop.CHOP, false);
          } else if(c == 'W') {
            // hidden option: write properties before exit
            writeProps = true;
          } else if(c == 'x' && sa()) {
            // hidden option: show original query plan
            options.put(Prop.COMPPLAN, false);
          } else if(c == 'X') {
            // hidden option: show xml query plan
            options.put(Prop.XMLPLAN, true);
            verbose = true;
          } else if(c == 'z') {
            // turn off result serialization
            options.put(Prop.SERIALIZE, false);
          } else {
            arg.ok(false);
          }
        } else {
          file = file == null ? arg.string() : file + " " + arg.string();
        }
      }
      console = file == null && commands == null && query == null;

      // set cached options
      for(final Map.Entry<Object[], Object> entry : options.entrySet()) {
        if(!arg.ok(set(entry.getKey(), entry.getValue()))) break;
      }
      return arg.finish();
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
      return false;
    }
  }
}
