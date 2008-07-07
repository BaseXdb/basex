package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.Token;

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
          FSUtils.printError(out, "touch", g.getPath(), 99);      
          return;  
        case '?':         
          FSUtils.printError(out, "touch", g.getPath(), 22);      
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
   * @throws IOException - in case of problems with the PrintOutput 
   */
  private void touch(final String path) throws IOException {

    String file = path.substring(path.lastIndexOf('/') + 1);

    int[] preFound =  FSUtils.getSpecificFilesOrDirs(context.data(), 
        curDirPre, path);
    if(preFound.length  > 0) {
      for(int i : preFound) {
        if(FSUtils.isFile(context.data(), i)) {
          context.data().update(i + 4, "mtime".getBytes(), 
              Token.token(System.currentTimeMillis()));
        }
      }
    } else {   
      // add new file 
      if(file.indexOf('?') > 0 || file.indexOf('*') > 0 
          || file.indexOf('[') > 0) {
        FSUtils.printError(out, "touch", file, 101);              
        return;
      } 
      try {
        int preNewFile = 4;
        if(!(curDirPre == FSUtils.getROOTDIR())) {
          preNewFile = curDirPre + 5;
        }
        FSUtils.insert(context.data(), false, Token.token(file), 
            getSuffix(file), Token.token(0), 
            Token.token(System.currentTimeMillis()),
            curDirPre, preNewFile);        
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
      return Token.token(file.substring(point + 1));
    return Token.token("");
  }
  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print(FSTOUCH);

  }

}

