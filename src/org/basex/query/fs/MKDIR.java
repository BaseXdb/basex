package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

/**
 * Performs a touch command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public class MKDIR {

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
  public MKDIR(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs a touch command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void mkdirMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "h");
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          return;
        case ':':         
          out.print("mkdir: missing argument");
          return;  
        case '?':         
          out.print("mkdir: illegal option");
          return;
      }      
      ch = g.getopt();
    }
    // if there is path expression remove it     
    if(g.getPath() != null) {      
      mkdir(g.getPath());
    } 
  }

  /**
   * Performs an mkdir command.
   *  
   *  @param path The name of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void mkdir(final String path) throws IOException {

    String dir = "";
    int beginIndex = path.lastIndexOf('/');
    if(beginIndex == -1) {
      dir = path;
    } else {
      curDirPre = FSUtils.goToDir(context.data(), curDirPre, 
          path.substring(0, beginIndex));   
      if(curDirPre == -1) {
        out.print("mkdir: " + path + " No such file or directory");
      } else {
        dir = path.substring(beginIndex + 1);
      }
    }
    int dirPre =  FSUtils.getSpecificDir(context.data(), 
        curDirPre, dir.getBytes());
    if(dirPre > 0) {
      out.print("mkdir: '" + dir + "': Directory exists");
    } else {   
      // add new dir  
      try {
        int preNewFile = 4;
        if(!(curDirPre == FSUtils.getROOTDIR())) {
          preNewFile = curDirPre + 5;
        }
        context.data().insert(preNewFile, 
            curDirPre, "dir".getBytes(), Data.ELEM);
        context.data().insert(preNewFile + 1, preNewFile, 
            "name".getBytes(), dir.getBytes());
        context.data().insert(preNewFile + 2, 
            preNewFile, "suffix".getBytes(), 
            "".getBytes());
        context.data().insert(preNewFile + 3, 
            preNewFile, "size".getBytes(), 
            "0".getBytes());
        context.data().insert(preNewFile + 4, 
            preNewFile, "mtime".getBytes(), 
            ("" + System.currentTimeMillis()).getBytes());   
        context.data().flush();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }


  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSMKDIR);

  }

}

