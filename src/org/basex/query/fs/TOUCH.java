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

    GetOpts g = new GetOpts(cmd, "h", 1);
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
   * Performs a touch command.
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
        out.print("touch: " + path + " No such file or directory");
      } else {
        file = path.substring(beginIndex + 1);
      }
    }

//  int[] preFound =  FSUtils.getSpecificFilesOrDirs(context.data(), 
//  curDirPre, path);

    int filePre = FSUtils.getSpecificFile(context.data(), 
        curDirPre, file.getBytes());
    // if directory - go to next pre value
    //   if(!FSUtils.isFile(context.data(), filePre)) continue;       

    if(filePre > 0) {
      // file found - update timestamp  
      context.data().update(filePre + 4, "mtime".getBytes(), 
          ("" + System.currentTimeMillis()).getBytes());      
    } else {   
      // add new file  
      try {
        int preNewFile = 4;
        if(!(curDirPre == FSUtils.getROOTDIR())) {
          preNewFile = curDirPre + 5;
        }
        context.data().insert(preNewFile, 
            curDirPre, "file".getBytes(), Data.ELEM);
        context.data().insert(preNewFile + 1, preNewFile, 
            "name".getBytes(), file.getBytes());
        context.data().insert(preNewFile + 2, 
            preNewFile, "suffix".getBytes(), 
            getSuffix(file));
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
   * Extracts the suffix of a file.
   * 
   * @param file the filename
   * @return the suffix of the file
   */
  private byte[] getSuffix(final String file) {
    int point = file.lastIndexOf('.');
    if(point > 0)
      return file.substring(point + 1).getBytes();
    return "".getBytes();
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

