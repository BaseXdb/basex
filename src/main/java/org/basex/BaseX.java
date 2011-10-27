package org.basex;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.basex.core.BaseXException;
import org.basex.core.Main;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.cmd.Check;
import org.basex.core.cmd.Set;
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
   * Main method, launching the standalone mode.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseX(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public BaseX(final String... args) throws IOException {
    super(args);

    // create session to show optional login request
    session();

    try {
      // open initial document or database
      if(input != null) {
        execute(new Check(input), verbose);
      }

      // open specified file
      if(file != null) {
        // query file contents
        final IO io = IO.get(file);
        if(!io.exists()) throw new BaseXException(INFOERROR + FILEWHICH, file);
        query = TextInput.content(io).toString().trim();
        execute(new Set(Prop.QUERYPATH, io.path()), false);
      }

      if(query != null) {
        // run query
        execute(new XQuery(query), verbose);
      } else if(commands != null) {
        // run command-line arguments
        execute(commands);
      } else {
        // enter interactive mode
        Util.outln(CONSOLE + CONSOLE2, sa() ? LOCALMODE : CLIENTMODE);
        console();
      }

      if(writeProps) context.mprop.write();
    } finally {
      quit();
    }
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
  protected final void parseArguments(final String[] args) throws IOException {
    final StringBuilder serial = new StringBuilder();
    final StringBuilder bind = new StringBuilder();

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
        } else if(c == 'c') {
          // specify command to be evaluated
          commands = arg.remaining();
        } else if(c == 'd') {
          // activate debug mode
          context.mprop.set(MainProp.DEBUG, true);
        } else if(c == 'D' && sa()) {
          // hidden option: show dot query graph
          options.put(Prop.DOTPLAN, true);
        } else if(c == 'i') {
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
          // do not chop text nodes
          options.put(Prop.CHOP, false);
        } else if(c == 'W') {
          // hidden option: write properties before exit
          writeProps = true;
        } else if(c == 'x') {
          // hidden option: show xml query plan
          options.put(Prop.XMLPLAN, true);
          verbose = true;
        } else if(c == 'X') {
          // hidden option: show query plan before compiling the query
          options.put(Prop.COMPPLAN, false);
        } else if(c == 'z') {
          // turn off result serialization
          options.put(Prop.SERIALIZE, false);
        } else {
          arg.usage();
        }
      } else {
        file = file == null ? arg.string() : file + " " + arg.string();
      }
    }
    console = file == null && commands == null && query == null;

    // set cached options
    if(serial.length() != 0) options.put(Prop.SERIALIZER, serial);
    if(bind.length() != 0) options.put(Prop.BINDINGS, bind);
    for(final Map.Entry<Object[], Object> entry : options.entrySet()) {
      try {
        execute(new Set(entry.getKey(), entry.getValue()), false);
      } catch(final IOException ex) {
        Util.errln(ex);
        out.close();
        arg.usage();
      }
    }
  }
}
