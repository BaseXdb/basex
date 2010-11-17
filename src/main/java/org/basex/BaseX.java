package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.XQuery;
import org.basex.io.IO;
import org.basex.io.PrintOutput;
import org.basex.io.TextInput;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Util;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    new BaseX(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  protected BaseX(final String... args) {
    super(args);
    if(success) run();
  }

  /**
   * Constructor.
   */
  private void run() {
    try {
      session();

      boolean u = false;
      if(input != null) execute(new Check(input), verbose);

      if(file != null) {
        // query file contents
        context.query = IO.get(file);
        final String qu = content();
        if(qu != null) execute(new XQuery(qu), verbose);
      } else if(query != null) {
        // query file contents
        execute(new XQuery(query), verbose);
      } else if(commands != null) {
        // execute command-line arguments
        execute(commands);
      } else {
        // enter interactive mode
        Util.outln(CONSOLE, sa() ? LOCALMODE : CLIENTMODE, CONSOLE2);
        u = console();
      }
      if(writeProps) context.prop.write();
      quit(u);
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
    }
  }

  /**
   * Reads in a query file and returns the content.
   * @return file content
   */
  private String content() {
    final IO io = IO.get(file);
    if(!io.exists()) {
      Util.errln(FILEWHICH, file);
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

  @SuppressWarnings("unused")
  @Override
  protected Session session() throws IOException {
    if(session == null) session = new LocalSession(context, out);
    return session;
  }

  @Override
  protected final boolean parseArguments(final String[] args) {
    String serial = "";
    try {
      final Args arg = new Args(args, this, sa() ? LOCALINFO : CLIENTINFO);
      while(arg.more()) {
        if(arg.dash()) {
          final char c = arg.next();
          if(c == 'c') {
            // specify command to be evaluated
            commands = arg.remaining();
          } else if(c == 'd') {
            // activate debug mode
            context.prop.set(Prop.DEBUG, true);
          } else if(c == 'D' && sa()) {
            // hidden option: show dot query graph
            arg.check(set(Prop.DOTPLAN, true));
          } else if(c == 'i' && sa()) {
            // open initial file or database
            input = arg.string();
          } else if(c == 'n' && !sa()) {
            // set server name
            context.prop.set(Prop.HOST, arg.string());
          } else if(c == 'o') {
            // specify file for result output
            out = new PrintOutput(arg.string());
          } else if(c == 'p' && !sa()) {
            // set server port
            context.prop.set(Prop.PORT, arg.num());
          } else if(c == 'P' && !sa()) {
            // specify password
            pass = arg.string();
          } else if(c == 'q') {
            // specify query to be evaluated
            query = arg.remaining();
          } else if(c == 'r') {
            // hidden option: parse number of runs
            arg.check(set(Prop.RUNS, arg.string()));
          } else if(c == 's') {
            // set/add serialization parameter
            serial += "," + arg.string();
            arg.check(set(Prop.SERIALIZER, serial));
          } else if(c == 'u') {
            // activate write-back for updates
            arg.check(set(Prop.WRITEBACK, true));
          } else if(c == 'U' && !sa()) {
            // specify user name
            user = arg.string();
          } else if(c == 'v') {
            // show command info
            verbose = true;
          } else if(c == 'V') {
            // show query info
            verbose = true;
            arg.check(set(Prop.QUERYINFO, true));
          } else if(c == 'w') {
            // activate write-back for updates
            arg.check(set(Prop.CHOP, false));
          } else if(c == 'W') {
            // hidden option: write properties before exit
            writeProps = true;
          } else if(c == 'x' && sa()) {
            // hidden option: show original query plan
            arg.check(set(Prop.COMPPLAN, false));
          } else if(c == 'X') {
            // hidden option: show xml query plan
            arg.check(set(Prop.XMLPLAN, true));
            verbose = true;
          } else if(c == 'z') {
            // turn off result serialization
            arg.check(set(Prop.SERIALIZE, false));
          } else {
            arg.check(false);
          }
        } else {
          file = file == null ? arg.string() : file + " " + arg.string();
        }
      }
      console = file == null && commands == null && query == null;
      return arg.finish();
    } catch(final IOException ex) {
      Util.errln(Util.server(ex));
      return false;
    }
  }
}
