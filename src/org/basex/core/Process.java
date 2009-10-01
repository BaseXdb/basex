package org.basex.core;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;

/**
 * This class provides the architecture for all internal command
 * implementations. It evaluates queries that are sent by the GUI, the client or
 * the standalone version.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Process extends Progress {
  /** Commands flag: standard. */
  protected static final int STANDARD = 0;
  /** Commands flag: printing command. */
  protected static final int PRINTING = 1;
  /** Commands flag: updating command. */
  protected static final int UPDATING = 2;
  /** Commands flag: data reference needed. */
  protected static final int DATAREF = 4;
  /** Flags for controlling process evaluation. */
  private final int flags;

  /** Command arguments. */
  public String[] args;
  /** Database context. */
  protected Context context;
  /** Database properties. */
  protected Prop prop;

  /** Container for query information. */
  protected TokenBuilder info = new TokenBuilder();
  /** Performance measurements. */
  protected Performance perf;
  /** Temporary query result. */
  protected Result result;

  /**
   * Constructor.
   * @param f command flags
   * @param a arguments
   */
  public Process(final int f, final String... a) {
    flags = f;
    args = a;
  }

  /**
   * Executes the process and serializes the results. If an error happens, an
   * exception is thrown.
   * @param ctx database context
   * @param out output stream reference
   * @throws Exception execution exception
   */
  public void execute(final Context ctx, final PrintOutput out)
      throws Exception {

    if(!execute(ctx)) throw new RuntimeException(info());
    output(out);
    out.print(info());
  }

  /**
   * Executes the process and returns a success flag.
   * @param ctx database context
   * @return success flag
   */
  public final boolean execute(final Context ctx) {
    perf = new Performance();
    context = ctx;
    prop = ctx.prop;

    final Data data = context.data();
    // data reference needed?
    if(data()) {
      if(data == null) return error(PROCNODB);
      // check update commands..
      if(updating() && (prop.is(Prop.TABLEMEM) || prop.is(Prop.MAINMEM)))
        return error(PROCMM);
    }

    // [AW] comment this out to disable database locking
    if(data != null) {
      // wait until update and read operations have been completed..
      //while(data.getLock() == 2 || (updating() && data.getLock() != 0))
      //  Performance.sleep(50);
    }

    boolean ok = false;
    if(data != null) data.setLock(updating() ? 2 : 1);
    try {
      ok = exec();
    } catch(final Throwable ex) {
      // catch unexpected errors...
      ex.printStackTrace();
      if(ex instanceof OutOfMemoryError) {
        Performance.gc(2);
        error(PROCOUTMEM);
      } else {
        error(PROCERR, this, ex.toString());
      }
    }
    if(data != null) data.setLock(0);
    return ok;
  }

  /**
   * Executes a process. This method is overwritten by many processes.
   * @return success of operation
   */
  protected boolean exec() {
    return true;
  }

  /**
   * Serializes the textual results.
   * @param out output stream
   * @throws IOException I/O exception
   */
  public final void output(final PrintOutput out) throws IOException {
    final Data data = context.data();
    try {
      out(out);
    } catch(final IOException ex) {
      if(data != null) data.setLock(0);
      throw ex;
    /*} catch(final Exception ex) {
      out.print(ex.toString());
      Main.debug(ex);*/
    }
    if(data != null) data.setLock(0);
  }

  /**
   * Returns a query result. This method is overwritten by many processes.
   * @param out output stream
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  protected void out(final PrintOutput out) throws IOException {
  }

  /**
   * Adds the error message to the message buffer {@link #info}.
   * @param msg error message
   * @param ext error extension
   * @return false
   */
  public final boolean error(final String msg, final Object... ext) {
    info.reset();
    info.add(msg == null ? "" : msg, ext);
    return false;
  }

  /**
   * Adds information on the process execution.
   * @param str information to be added
   * @param ext extended info
   * @return true
   */
  protected final boolean info(final String str, final Object... ext) {
    if(prop.is(Prop.INFO)) {
      info.add(str, ext);
      info.add(Prop.NL);
    }
    return true;
  }

  /**
   * Returns the query information as a string.
   * @return info string
   */
  public final String info() {
    return info.toString();
  }

  /**
   * Returns the result set, generated by the last query.
   * @return result set
   */
  public final Result result() {
    return result;
  }

  /**
   * Performs the specified XQuery.
   * @param q query to be performed
   * @param err this string is thrown as exception if the results are no
   *    element nodes
   * @return result set
   */
  protected final Nodes query(final String q, final String err) {
    try {
      final String query = q == null ? "" : q;
      final QueryProcessor qu = new QueryProcessor(query, context);
      progress(qu);
      final Nodes nodes = qu.queryNodes();
      // check if all result nodes are tags
      if(err != null) {
        final Data data = context.data();
        for(int i = nodes.size() - 1; i >= 0; i--) {
          if(data.kind(nodes.nodes[i]) != Data.ELEM) {
            error(err);
            return null;
          }
        }
      }
      return nodes;
    } catch(final QueryException ex) {
      Main.debug(ex);
      error(ex.getMessage());
      return null;
    }
  }

  /**
   * Returns if the current command yields some output.
   * @return result of check
   */
  public boolean printing() {
    return (flags & PRINTING) != 0;
  }

  /**
   * Returns if the current command needs a data reference for processing.
   * @return result of check
   */
  public boolean data() {
    return (flags & DATAREF) != 0;
  }

  /**
   * Returns if the current command generates updates in the data structure.
   * @return result of check
   */
  public boolean updating() {
    return (flags & UPDATING) != 0;
  }

  /**
   * Returns the length of the longest string.
   * @param str strings
   * @return maximum length
   */
  protected static int maxLength(final String[] str) {
    int max = 0;
    for(final String s : str)
      if(max < s.length()) max = s.length();
    return max;
  }

  /**
   * Returns the list of arguments.
   * @return arguments
   */
  public final String args() {
    final StringBuilder sb = new StringBuilder();
    for(final String a : args) if(a != null) sb.append(quote(a));
    return sb.toString();
  }

  /**
   * Returns the specified string in quotes, if spaces are found.
   * @param s string to be quoted
   * @return quoted string
   */
  protected String quote(final String s) {
    final StringBuilder sb = new StringBuilder();
    if(s.length() != 0) {
      sb.append(' ');
      final boolean spc = s.indexOf(' ') != -1;
      if(spc) sb.append('"');
      sb.append(s);
      if(spc) sb.append('"');
    }
    return sb.toString();
  }

  /**
   * Returns a string representation of the object. In the client/server
   * architecture, the command string is sent to and reparsed by the server.
   * @return string representation
   */
  @Override
  public String toString() {
    return Main.name(this).toUpperCase() + args();
  }
}
