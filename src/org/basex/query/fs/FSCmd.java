package org.basex.query.fs;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Defines common methods for file system commands.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public abstract class FSCmd {
  /** Data reference. */
  protected Context context;
  /** Data reference. */
  protected Data data;
  /** Current dir. */
  protected int curPre;

  /**
   * Sets the query context.
   * @param ctx data context
   */
  public final void context(final Context ctx) {
    context = ctx;
    data = context.data();
    curPre = ctx.current().pre[0];
  }
  
  /**
   * Checks the command line arguments.
   * @param args command line arguments
   * @throws FSException file system exception
   */
  public abstract void args(final String args) throws FSException;
  
  /**
   * Executes the command..
   * @param out output stream
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public abstract void exec(final PrintOutput out) throws IOException;

  /**
   * Checks the default options and returns the option parser.
   * @param args command arguments
   * @return option parser
   * @throws FSException file system exception
   */
  final GetOpts defaultOpts(final String args) throws FSException {
    final GetOpts g = new GetOpts(args, "h");
    while(g.more()) checkOpt(g);
    return g;
  }

  /**
   * Checks the next argument for the default options and returns it.
   * Returns an error if command help is shown or input is invalid.
   * @param g option parser
   * @return specified argument
   * @throws FSException file system exception
   */
  final int checkOpt(final GetOpts g) throws FSException {
    final int c = g.next();
    if(c == 'h') throw new FSException(help());
    if(c == ':') error(g.getPath(), 99);
    if(c == 0) error("", 102);
    return c;
  }

  /**
   * Checks the specified pre value for validity.
   * @param path path for optional error message
   * @param pre pre value
   * @return pre value
   * @throws FSException file system exception
   */
  final int checkPre(final String path, final int pre) throws FSException {
    if(pre == -1) error(path, 2);
    return pre;
  }

  /**
   * Checks the specified pre array for entries.
   * @param path path for optional error message
   * @param pre pre array
   * @return pre value
   * @throws FSException file system exception
   */
  final int[] checkPre(final String path, final int[] pre) throws FSException {
    if(pre.length == 0) error(path, 2);
    return pre;
  }
  
  /**
   * Creates a filesystem exception.
   * @param arg - exception message
   * @param error - error code
   * @throws FSException - in case of problems with the PrintOutput
   */
  final void error(final Object arg, final int error) throws FSException {
    throw new FSException(name(), arg, error);
  }
  
  /**
   * Returns the command help.
   * @return help string
   */
  private String help() {
    try {
      // Get FS Help Text via Reflection (FSText.FS...)
      final String cmd = getClass().getSimpleName();
      return FSText.class.getField("FS" + cmd).get(null).toString();
    } catch(final Exception ex) {
      BaseX.debug(ex);
      return "";
    }
  }

  /**
   * Returns the command name.
   * @return command name
   */
  private String name() {
    return getClass().getSimpleName().toLowerCase();
  }

  /**
   * Formats a file size according to the binary size orders (KB, MB, ...).
   * @param size file size
   * @return formatted size value
   */
  final String format(final long size) {
    if(size > (1 << 30)) return ((size + (1 << 29)) >> 30) + "G";
    if(size > (1 << 20)) return ((size + (1 << 19)) >> 20) + "M";
    if(size > (1 << 10)) return ((size + (1 <<  9)) >> 10) + "K";
    return Long.toString(size);
  }
}
