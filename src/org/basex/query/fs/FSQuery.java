package org.basex.query.fs;

import static org.basex.Text.NL;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.Token;


/**
 * This class simulates some file system operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen & Hannes Schwarz
 */
public final class FSQuery {
  /** Data reference. */
  private final Context context;

  /**
   * Simplified Constructor.
   * @param ctx data context
   */
  public FSQuery(final Context ctx) {
    context = ctx;
  }

  /**
   * The current working directory as set by the cd command.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void pwd(final PrintOutput out) throws IOException {
    pwd(out, context.current().pre[0]);
    out.print(NL);
  }

  /**
   * Construct name of current/working directory (cwd).
   * @param out stream receiving cwd.
   * @param n pre value of cwd
   * @throws IOException in case of problems with the PrintOutput
   */
  private void pwd(final PrintOutput out, final int n) throws IOException {
    final Data data = context.data();
    if(n > 3) {
      pwd(out, data.parent(n, data.kind(n)));
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    } else {
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    }    
  }

  /**
   * Performs an ls command.
   * @param cmd directory path
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void ls(final String cmd, final PrintOutput out) 
  throws IOException {        
    LS ls = new LS(context, out);
    ls.lsMain(cmd);
    out.print(NL);
  }



  /**
   * Performs a du command.
   * @param cmd options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void du(final String cmd, final PrintOutput out)
  throws IOException {
    DU du = new DU(context, out);
    du.duMain(cmd);    
    out.print(NL);
  }

 

  /**
   * Perform cd.
   * 
   * @param cmd options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void cd(final String cmd, final PrintOutput out) 
  throws IOException {
    CD cd = new CD(context, out);
    cd.cdMain(cmd);    
    out.print(NL);
  }

}
