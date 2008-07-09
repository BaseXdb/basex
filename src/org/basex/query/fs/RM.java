package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Performs a rm command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class RM {

  /** Data reference. */
  private final Context context;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** Remove the all. */
  private boolean fRecursive;

  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public RM(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs a rm command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void rmMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "Rh", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          return;
        case 'R':
          fRecursive = true;
          break;          
        case ':':         
          FSUtils.printError(out, "rm", g.getPath(), 99);                    
          return;  
        case '?':         
          FSUtils.printError(out, "rm", g.getPath(), 102);
          return;
      }      
      ch = g.getopt();
    }

    // if there is path expression remove it     
    if(g.getPath() != null) {      
      remove(g.getPath());
    } 
  }

  /**
   * Performs a rm command.
   *  
   *  @param path The name of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void remove(final String path) throws IOException {
    Data data = context.data();

    int[] del = FSUtils.getSpecificFilesOrDirs(data, curDirPre, 
        path);
    long sizeOfNode = 0;
    for(int toDel : del) {
      if(toDel == -1) {
        FSUtils.printError(out, "rm", path, 2);
        return;
      } else {
        /* 
         * Pre Value of all nodes changes if one node is deleted.
         * This is the adjustment of the former 
         */
        toDel -= sizeOfNode;
        if((FSUtils.isDir(data, toDel) && fRecursive) ||
            (FSUtils.isFile(data, toDel))) {

          sizeOfNode += data.size(toDel, Data.ELEM);
          FSUtils.delete(data, toDel);
        } else {
          FSUtils.printError(out, "rm", path, 21);
        }
      }
    }

  }


  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSRM);

  }

}

