package org.basex.fs;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Defines common methods for file system commands.
 * The implementing class names are supposed to equal the command names
 * (case sensitivity doesn't matter)
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public abstract class FSCmd {
  /** Data reference. */
  protected Context context;
  /** Data FS reference. */
  protected DataFS fs;
  /** Current dir. */
  protected int curPre;

  /**
   * Sets the query context.
   * @param ctx data context
   */
  public final void context(final Context ctx) {
    context = ctx;
    fs = context.data().fs;
    curPre = ctx.current().nodes[0];
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
   * @throws IOException in case of problems with the PrintOutput 
   */
  public abstract void exec(final PrintOutput out) throws IOException;

  /**
   * Checks the default options and returns the option parser.
   * @param args command arguments
   * @return option parser
   * @throws FSException file system exception
   */
  final GetOpts defaultOpts(final String args) throws FSException {
    final GetOpts g = new GetOpts(args, "");
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
    if(c == ':') error(g.getPath(), 99);
    if(c == 0) help();
    return c;
  }

  /**
   * Throws an error, containing help on the current command.
   * @throws FSException help exception
   */
  final void help() throws FSException {
    throw new FSException(Help.help(getClass().getSimpleName()));
  }

  /**
   * Evaluates the specified path and sets the current pre value.
   * @param path path for optional error message
   * @throws FSException file system exception
   */
  final void curPre(final String path) throws FSException {
    curPre = fs.goTo(curPre, path);
    if(curPre == -1) error(path, 2);
  }

  /**
   * Evaluates the specified path and checks the resulting array for entries.
   * @param path path
   * @return resulting array
   * @throws FSException file system exception
   */
  final int[] children(final String path) throws FSException {
    final int[] nodes = fs.children(curPre, path);
    if(nodes.length == 0) error(path, 2);
    return nodes;
  }
  
  /**
   * Creates a filesystem exception.
   * @param arg exception message
   * @param error error code
   * @throws FSException in case of problems with the PrintOutput
   */
  final void error(final Object arg, final int error) throws FSException {
    throw new FSException(name(), arg, error);
  }
  
  /**
   * Creates a warning.
   * @param out output stream
   * @param arg exception message
   * @param error error code
   * @throws IOException exception
   */
  final void warning(final PrintOutput out, final Object arg, final int error)
      throws IOException {
    out.println(FSException.error(name(), arg, error));
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
