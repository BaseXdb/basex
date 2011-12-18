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
import org.basex.io.IOContent;
import org.basex.io.in.NewlineInput;
import org.basex.io.out.PrintOutput;
import org.basex.server.LocalSession;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class BaseX extends Main {
  /** User name. */
  protected String user;
  /** Password. */
  protected String pass;

  /** Flag for writing properties to disk. */
  private boolean writeProps;
  /** Operations to be executed. */
  private StringList ops;

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
      for(int i = 0; i < ops.size(); i += 2) {
        final String key = ops.get(i);
        final String val = ops.get(i + 1);
        if(key.equals("c")) {
          // run database command
          execute(val);
        } else if(key.equals("f")) {
          // query file
          final IO io = IO.get(val);
          if(!io.exists()) throw new BaseXException(FILEWHICH, val);
          final String query = Token.string(
              new NewlineInput(io, null).content()).trim();
          execute(new Set(Prop.QUERYPATH, io.path()), false);
          execute(new XQuery(query), verbose);
        } else if(key.equals("i")) {
          // create main memory database if input is XML snippet
          final boolean mem = IO.get(val) instanceof IOContent;
          execute(new Set(Prop.MAINMEM, mem), false);
          execute(new Check(val), verbose);
          execute(new Set(Prop.MAINMEM, false), false);
        } else if(key.equals("q")) {
          // run query
          execute(new XQuery(val), verbose);
        }
      }

      if(console) {
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
    ops = new StringList();

    final HashMap<Object[], Object> options = new HashMap<Object[], Object>();
    final Args arg = new Args(args, this, sa() ? LOCALINFO : CLIENTINFO,
        Util.info(CONSOLE, sa() ? LOCALMODE : CLIENTMODE));
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'b') {
          // set/add variable binding
          if(bind.length() != 0) bind.append(',');
          // commas are escaped by a second comma
          bind.append(arg.string().replaceAll(",", ",,"));
        } else if(c == 'c') {
          // specify command to be evaluated
          ops.add("c").add(arg.string());
        } else if(c == 'd') {
          // activate debug mode
          context.mprop.set(MainProp.DEBUG, true);
        } else if(c == 'D' && sa()) {
          // hidden option: show dot query graph
          options.put(Prop.DOTPLAN, true);
        } else if(c == 'i') {
          // open initial file or database
          ops.add("i").add(arg.string());
        } else if(c == 'n' && !sa()) {
          // set server name
          context.mprop.set(MainProp.HOST, arg.string());
        } else if(c == 'o') {
          // specify file for result output
          out = new PrintOutput(arg.string());
          if(session != null) session.setOutputStream(out);
        } else if(c == 'p' && !sa()) {
          // set server port
          context.mprop.set(MainProp.PORT, arg.number());
        } else if(c == 'P' && !sa()) {
          // specify password
          pass = arg.string();
        } else if(c == 'q') {
          // specify query to be evaluated
          ops.add("q").add(arg.string());
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
        ops.add("f").add(arg.string());
      }
    }
    console = ops.size() == 0;

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
