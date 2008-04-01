package org.basex.query.fs;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.query.fs.Exception.PathNotFoundException;
import org.basex.util.Token;

/**
 * Performs a pwd command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public class PWD {

  /** Data reference. */
  private final Context context;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** Shows if an error occurs. */
  private boolean fError;


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

    GetOpts g = new GetOpts(cmd, "h");
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
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
      if(fError) {
        // more options ?
        return;
      }
      ch = g.getopt();
    }

    // if there is path expression go to dir
    if(g.getPath() != null) {
      curDirPre = FSUtils.goToDir(context.data(), curDirPre, g.getPath());
      if(curDirPre == -1) {
        throw new PathNotFoundException("cd", g.getPath());
      }
    }
    pwd(curDirPre);
  }
  
  /**
   * Construct name of current/working directory (cwd).
   * @param n pre value of cwd
   * @throws IOException in case of problems with the PrintOutput
   */
  private void pwd(final int n) throws IOException {
    final Data data = context.data();
    if(n > 3) {
      pwd(data.parent(n, data.kind(n)));
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    } else {
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    }    
  }
  
  /**
   * Print the help.
   * 
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printHelp() throws IOException {
    out.print("help");
   
  }

}

