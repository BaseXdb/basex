package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.Token;

/**
 * Performs a du command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class DU {

  /** Data reference. */
  private final Context context;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** Display an entry for each file in the file hierarchy. */
  private boolean fPrintAll;


  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public DU(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs an du command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void duMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "ah", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'a':         
          fPrintAll = true;
          break;
        case 'h':
          printHelp();
          return;
        case ':':         
          FSUtils.printError(out, "du", g.getPath(), 99);
          return;
        case '?':         
          FSUtils.printError(out, "du", g.getPath(), 102);
          return;
      }      
      ch = g.getopt();
    }
    // if there is path expression go to dir
    if(g.getPath() != null) {
      int[] sources = FSUtils.getSpecificFilesOrDirs(context.data(),
          curDirPre, g.getPath());
      // curDirPre = FSUtils.goToDir(context.data(), curDirPre, g.getPath());
      if(sources.length == 1 && sources[0] == -1) {
        FSUtils.printError(out, "du", g.getPath(), 2);
      }
      du(sources);
    } else {
      du(new int[] {curDirPre}); 
    }
  }


  /**
   * The du utility displays the file system block usage for each file argu-
   * ment and for each directory in the file hierarchy rooted in each direc-
   * tory argument.  If no file is specified, the block usage of the hierarchy
   * rooted in the current directory is displayed.
   * 
   * @param sources pre values of the nodes to print
   * @throws IOException in case of problems with the PrintOutput
   * @return die speicher
   */
  private long du(final int[] sources) 
  throws IOException {    
    for(int pre : sources) {
      final Data data = context.data();
      if(FSUtils.isFile(data, pre)) {
        out.print(FSUtils.getSize(data, pre) + "\t" + 
            Token.string(FSUtils.getRelativePath(data, pre, curDirPre)) + 
            Token.string(FSUtils.getName(data, pre)) + "\r");
      } else {
        final DirIterator it = new DirIterator(data, pre);                
        long diskusage = FSUtils.getSize(data, pre);

        while(it.more()) {      
          int n = it.next();                
          if(FSUtils.isDir(data, n)) {  
            diskusage += du(new int[]{n});      
          } else {      
            long diskuse = FSUtils.getSize(data, n);
            if(fPrintAll) {
              out.print(diskuse + "\t" + 
                  Token.string(FSUtils.getRelativePath(data, pre, curDirPre)) 
                  + "/" + Token.string(FSUtils.getName(data, n)) + "\r");
            }
            diskusage += diskuse;

          }
        }
        out.println(diskusage + "\t" + 
            Token.string(FSUtils.getRelativePath(data, pre, curDirPre)));
        return diskusage;
      }  
    }
    return -1;
  }

  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSDU);

  }

}

