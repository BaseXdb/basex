package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
/**
 * Performs a cd command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class CD {

  /** Data reference. */
  private final Context context;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public CD(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs an cd command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void cdMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "h", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          return;
        case ':':         
          FSUtils.printError(out, "cd", g.getPath(), 99);
          return;
        case '?':         
          FSUtils.printError(out, "cat", g.getPath(), 22);
          return;
      }     
      ch = g.getopt();
    }

    // if there is path expression go to work
    if(g.getPath() != null) {    
      curDirPre = FSUtils.goToDir(context.data(), curDirPre, g.getPath());
      if(curDirPre == -1) {
        FSUtils.printError(out, "cd", g.getPath(), 2);
      } else {
        context.current().pre[0] = curDirPre;
      }
    } else {
      context.current().pre[0] = FSUtils.getROOTDIR();
    } 
  }


  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSCD);

  }

}

