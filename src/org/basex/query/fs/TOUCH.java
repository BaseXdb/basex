package org.basex.query.fs;

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
public final class TOUCH {

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
  public TOUCH(final Context ctx, final PrintOutput output) {
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
  public void touchMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "Rh");
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          return;
        case ':':         
          out.print("touch: missing argument");
          return;  
        case '?':         
          out.print("touch: illegal option");
          return;
      }      
      ch = g.getopt();
    }
    // if there is path expression remove it     
    if(g.getPath() != null) {      
      touch(g.getPath());
    } 
  }

  /**
   * Performs an rm command.
   *  
   *  @param path The name of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void touch(final String path) throws IOException {

    String file = "";
    int beginIndex = path.lastIndexOf('/');
    if(beginIndex == -1) {
      file = path;
    } else {
      curDirPre = FSUtils.goToDir(context.data(), curDirPre, 
          path.substring(0, beginIndex));   
      if(curDirPre == -1) {
        out.print("rm: " + path + " No such file or directory");
      } else {
        file = path.substring(beginIndex + 1);
      }
    }
    //TODO<HS>: Debug !!!!  
    try {
      int preNewFile = 4;
      if(!(curDirPre == FSUtils.getROOTDIR())) {
        preNewFile = curDirPre + 5;
      }
      int time = 20178009;
      context.data().insert(preNewFile, 
          curDirPre, "file".getBytes(), Data.ELEM);
      context.data().insert(preNewFile + 1, preNewFile, 
          "name".getBytes(), file.getBytes());
      context.data().insert(preNewFile + 2, 
          preNewFile, "suffix".getBytes(), 
"text".getBytes());
      context.data().insert(preNewFile + 3, 
          preNewFile, "size".getBytes(), (byte) 0);
      context.data().insert(preNewFile + 4, 
          preNewFile, "mtime".getBytes(), (byte) time);
      context.data().flush();
      } catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print("touch  ...");

  }

}

