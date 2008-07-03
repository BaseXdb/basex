package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Performs a pwd command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class PWD {

  /** Data reference. */
  private final Context context;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** Shows if an error occurs. */
  private boolean fError;

  /** Shows if job is done. */
  private boolean fAccomplished;

  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public PWD(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs an pwd command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void pwdMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "h", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          fAccomplished = true;
          break;
        case ':':         
          fError = true;
          out.print("ls: missing argument");
          break;  
        case '?':         
          fError = true;
          out.print("ls: illegal option");
          break;
      }      
      if(fError || fAccomplished) {
        return;
      }
      ch = g.getopt();
    }

    // if there is path expression go to dir
    Data data = context.data();
    if(g.getPath() != null) {      
      curDirPre = FSUtils.goToDir(data, curDirPre, g.getPath());      
      if(curDirPre == -1) {
        out.print("pwd " + g.getPath() + "No such file or directory. ");
      }
    }
    out.print(FSUtils.getPath(data, curDirPre));
  }
  
  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSPWD);
  }
}
