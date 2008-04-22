package org.basex.query.fs;

import static org.basex.Text.NL;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.io.PrintOutput;

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
   * @param cmd options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void pwd(final String cmd, final PrintOutput out) throws IOException {
    PWD pwd = new PWD(context, out);
    pwd.pwdMain(cmd);
    out.print(NL);
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
  
  /**
   * Perform locate.
   * 
   * @param cmd options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void locate(final String cmd, final PrintOutput out) 
  throws IOException {
    LOCATE locate = new LOCATE(context, out);
    locate.locateMain(cmd);    
    out.print(NL);
  }


}
