package org.basex;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This is the starter class for the stand-alone console mode.
 * It executes all commands locally.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseX extends Main {
  /** Commands to be executed. */
  private IntList ops;
  /** Command arguments. */
  private StringList vals;

  /** Flag for writing properties to disk. */
  private boolean writeProps;

  /**
   * Main method, launching the standalone mode.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseX(args);
    } catch(final IOException ex) {
      Util.debug(ex);
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

    final StringBuilder serial = new StringBuilder();
    final StringBuilder bind = new StringBuilder();
    boolean v = false, qi = false, qp = false;

    console = true;
    try {
      // loop through all commands
      for(int i = 0; i < ops.size(); i++) {
        final int c = ops.get(i);
        String val = vals.get(i);
        Object[] prop = null;

        if(c == 'b') {
          // set/add variable binding
          if(bind.length() != 0) bind.append(',');
          // commas are escaped by a second comma
          val = bind.append(val.replaceAll(",", ",,")).toString();
          prop = Prop.BINDINGS;
        } else if(c == 'c') {
          // evaluate commands
          final IO io = IO.get(val);
          if(io.exists() && !io.isDir()) {
            execute(io.string());
          } else {
            execute(val);
          }
          console = false;
        } else if(c == 'd') {
          // toggle debug mode
          Prop.debug ^= true;
        } else if(c == 'D') {
          // hidden option: show/hide dot query graph
          prop = Prop.DOTPLAN;
        } else if(c == 'i') {
          // open database or create main memory representation
          execute(new Set(Prop.MAINMEM, true), false);
          execute(new Check(val), verbose);
          execute(new Set(Prop.MAINMEM, false), false);
        } else if(c == 'L') {
          // toggle newline separators
          newline ^= true;
          execute(new Set(Prop.SERIALIZER, newline ?
            SerializerProp.S_ITEM_SEPARATOR[0] + "=\\n" : ""), false);
        } else if(c == 'o') {
          // change output stream
          if(out != System.out) out.close();
          out = new PrintOutput(val);
          session().setOutputStream(out);
        } else if(c == 'q') {
          // evaluate query
          execute(new XQuery(val), verbose);
          console = false;
        } else if(c == 'Q') {
          // evaluate file contents or string as query
          final IO io = IO.get(val);
          if(io.exists() && !io.isDir()) {
            query(io);
          } else {
            execute(new XQuery(val), verbose);
          }
          console = false;
        } else if(c == 'r') {
          // hidden option: parse number of runs
          prop = Prop.RUNS;
        } else if(c == 's') {
          // set/add serialization parameter
          if(serial.length() != 0) serial.append(',');
          val = serial.append(val).toString();
          prop = Prop.SERIALIZER;
        } else if(c == 'u') {
          // (de)activate write-back for updates
          prop = Prop.WRITEBACK;
        } else if(c == 'v') {
          // show/hide verbose mode
          v ^= true;
        } else if(c == 'V') {
          // show/hide query info
          qi ^= true;
          prop = Prop.QUERYINFO;
        } else if(c == 'w') {
          // toggle chopping of whitespaces
          prop = Prop.CHOP;
        } else if(c == 'W') {
          // hidden option: toggle writing of properties before exit
          writeProps ^= true;
        } else if(c == 'x') {
          // show/hide xml query plan
          prop = Prop.XMLPLAN;
          qp ^= true;
        } else if(c == 'X') {
          // hidden option: show query plan before/after query compilation
          prop = Prop.COMPPLAN;
        } else if(c == 'z') {
          // toggle result serialization
          prop = Prop.SERIALIZE;
        }
        if(prop != null) execute(new Set(prop, val), false);
        verbose = qi || qp || v;
      }

      if(console) {
        verbose = true;
        // enter interactive mode
        Util.outln(CONSOLE + TRY_MORE_X, sa() ? LOCALMODE : CLIENTMODE);
        console();
      }

      if(writeProps) context.mprop.write();
    } finally {
      quit();
    }
  }

  /**
   * Runs a query file.
   * @param io input file
   * @throws IOException I/O exception
   */
  private void query(final IO io) throws IOException {
    execute(new Set(Prop.QUERYPATH, io.path()), false);
    execute(new XQuery(io.string()), verbose);
  }

  /**
   * Tests if this client is stand-alone.
   * @return stand-alone flag
   */
  boolean sa() {
    return true;
  }

  @Override
  protected Session session() throws IOException {
    if(session == null) session = new LocalSession(context, out);
    session.setOutputStream(out);
    return session;
  }

  @Override
  protected final void parseArguments(final String... args) throws IOException {
    ops = new IntList();
    vals = new StringList();

    final Args arg = new Args(args, this, sa() ? LOCALINFO : CLIENTINFO,
        Util.info(CONSOLE, sa() ? LOCALMODE : CLIENTMODE));
    while(arg.more()) {
      final char c;
      String v = null;
      if(arg.dash()) {
        c = arg.next();
        if(c == 'b' || c == 'c' || c == 'C' || c == 'i' || c == 'o' || c == 'q' ||
           c == 'r' || c == 's') {
          // options followed by a string
          v = arg.string();
        } else if(c == 'd' || c == 'D' && sa() || c == 'L' || c == 'u' || c == 'v' ||
           c == 'V' || c == 'w' || c == 'W' || c == 'x' || c == 'X' || c == 'z') {
          // options to be toggled
          v = "";
        } else if(!sa()) {
          // client options: need to be set before other options
          if(c == 'n') {
            // set server name
            context.mprop.set(MainProp.HOST, arg.string());
          } else if(c == 'p') {
            // set server port
            context.mprop.set(MainProp.PORT, arg.number());
          } else if(c == 'P') {
            // specify password
            context.mprop.set(MainProp.PASSWORD, arg.string());
          } else if(c == 'U') {
            // specify user name
            context.mprop.set(MainProp.USER, arg.string());
          } else {
            arg.usage();
          }
        } else {
          arg.usage();
        }
      } else {
        v = arg.string().trim();
        // interpret at commands if input starts with < or ends with command script suffix
        c = v.startsWith("<") || v.endsWith(IO.BXSSUFFIX) ? 'c' : 'Q';
      }
      if(v != null) {
        ops.add(c);
        vals.add(v);
      }
    }
  }
}
