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
  /** Flag for writing options to disk. */
  private boolean writeOptions;

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

    final StringBuilder serial = new StringBuilder();
    final StringBuilder bind = new StringBuilder();
    boolean v = false, qi = false, qp = false;

    console = true;
    try {
      // loop through all commands
      for(int i = 0; i < ops.size(); i++) {
        final int c = ops.get(i);
        String val = vals.get(i);
        Option opt = null;

        if(c == 'b') {
          // set/add variable binding
          if(bind.length() != 0) bind.append(',');
          // commas are escaped by a second comma
          val = bind.append(val.replaceAll(",", ",,")).toString();
          opt = MainOptions.BINDINGS;
        } else if(c == 'c') {
          // evaluate commands
          final IO io = IO.get(val);
          String base = ".";
          if(io.exists() && !io.isDir()) {
            val = io.string();
            base = io.path();
          }
          execute(new Set(MainOptions.QUERYPATH, base), false);
          execute(val);
          execute(new Set(MainOptions.QUERYPATH, ""), false);
          console = false;
        } else if(c == 'd') {
          // toggle debug mode
          Prop.debug ^= true;
        } else if(c == 'D') {
          // hidden option: show/hide dot query graph
          opt = MainOptions.DOTPLAN;
        } else if(c == 'i') {
          // open database or create main memory representation
          execute(new Set(MainOptions.MAINMEM, true), false);
          execute(new Check(val), verbose);
          execute(new Set(MainOptions.MAINMEM, false), false);
        } else if(c == 'L') {
          // toggle newline separators
          newline ^= true;
          if(serial.length() != 0) serial.append(',');
          val = serial.append(SerializerOptions.S_ITEM_SEPARATOR.name).
              append("=\\n").toString();
          opt = MainOptions.SERIALIZER;
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
          String base = ".";
          if(io.exists() && !io.isDir()) {
            val = io.string();
            base = io.path();
          }
          execute(new Set(MainOptions.QUERYPATH, base), false);
          execute(new XQuery(val), verbose);
          execute(new Set(MainOptions.QUERYPATH, ""), false);
          console = false;
        } else if(c == 'r') {
          // hidden option: parse number of runs
          opt = MainOptions.RUNS;
        } else if(c == 's') {
          // set/add serialization parameter
          if(serial.length() != 0) serial.append(',');
          val = serial.append(val.replaceAll(",", ",,")).toString();
          opt = MainOptions.SERIALIZER;
        } else if(c == 'u') {
          // (de)activate write-back for updates
          opt = MainOptions.WRITEBACK;
        } else if(c == 'v') {
          // show/hide verbose mode
          v ^= true;
        } else if(c == 'V') {
          // show/hide query info
          qi ^= true;
          opt = MainOptions.QUERYINFO;
        } else if(c == 'w') {
          // toggle chopping of whitespaces
          opt = MainOptions.CHOP;
        } else if(c == 'W') {
          // hidden option: toggle writing of options before exit
          writeOptions ^= true;
        } else if(c == 'x') {
          // show/hide xml query plan
          opt = MainOptions.XMLPLAN;
          qp ^= true;
        } else if(c == 'X') {
          // hidden option: show query plan before/after query compilation
          opt = MainOptions.COMPPLAN;
        } else if(c == 'z') {
          // toggle result serialization
          opt = MainOptions.SERIALIZE;
        }
        if(opt != null) execute(new Set(opt, val), false);
        verbose = qi || qp || v;
      }

      if(console) {
        verbose = true;
        // enter interactive mode
        Util.outln(CONSOLE + TRY_MORE_X, sa() ? LOCALMODE : CLIENTMODE);
        console();
      }

      if(writeOptions) context.globalopts.write();
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
            context.globalopts.string(GlobalOptions.HOST, arg.string());
          } else if(c == 'p') {
            // set server port
            context.globalopts.number(GlobalOptions.PORT, arg.number());
          } else if(c == 'P') {
            // specify password
            context.globalopts.string(GlobalOptions.PASSWORD, arg.string());
          } else if(c == 'U') {
            // specify user name
            context.globalopts.string(GlobalOptions.USER, arg.string());
          } else {
            arg.usage();
          }
        } else {
          arg.usage();
        }
      } else {
        v = arg.string().trim();
        // interpret as command file if input string ends with command script suffix
        c = v.endsWith(IO.BXSSUFFIX) ? 'c' : 'Q';
      }
      if(v != null) {
        ops.add(c);
        vals.add(v);
      }
    }
  }
}
