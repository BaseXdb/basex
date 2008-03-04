package org.basex.query.fs;

import static org.basex.Text.NL;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.Token;
import org.basex.query.fs.Exception.PathNotFoundException;
/**
 * Performs a ls command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public class LS {

  /** BaseX table. */
  private final Data data;

  /** current dir. */
  private int curDirPre;

  /** PrintOutPutStream. */
  private PrintOutput out;

  /** list subdirectories also. */
  private boolean fRecursive;

  /** list files beginning with . */
  private boolean fListDot;

  /** Shows if an error occurs. */
  private boolean fError;


  /**
   * Simplified Constructor.
   * @param ctx data context
   * @param output output stream
   */
  public LS(final Context ctx, final PrintOutput output) {
    data = ctx.data();
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs an ls command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void lsMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "ahR", out);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'R':
          fRecursive = true;
          break;
        case 'a':
          fListDot = true;
          break;
        case 'h':
          printHelp();
          return;
        case '?':         
          // ERROR handling -> GetOpts
          fError = true;
          return;
      }
      if(!fError) {
        // more options ?
        ch = g.getopt();
      }
    }
    // if there is path expression set new pre value
    if(g.getPath() != null) {
      curDirPre = FSUtils.goToDir(data, curDirPre, g.getPath());
      if(curDirPre == -1)
        throw new PathNotFoundException("ls", g.getPath());
    }

    // go to work
    if(fRecursive) {
      lsRecursive(curDirPre);
    } else {
      print(FSUtils.getAllOfDir(data, curDirPre));
    }
  }

  /**
   * Recursively list subdirectories encountered.
   *  
   * @param pre Value of dir 
   * @throws IOException in case of problems with the PrintOutput 
   */
  private void lsRecursive(final int pre) throws IOException {         

    int[] dir = FSUtils.getAllOfDir(data, pre);        
    int[] allDir = new int[dir.length];
    int j = 0;
    for(int i : dir) {
      if(FSUtils.isDir(data, i))
        allDir[j++] = i; 
    }
    // print dir
    print(dir);   
    for(int i = 0; allDir[i] != 0 && i < allDir.length; i++) {
      out.print(NL);
      pwd(allDir[i]);
      out.print(NL);
      lsRecursive(allDir[i]);
    }
  }

  /**
   * Print the result.
   * @param result - array to print
   * @throws IOException in case of problems with the PrintOutput
   */
  private void print(final int[] result) throws IOException {
    for(int j : result) {
      byte[] name = FSUtils.getName(data, j);
      if(!fListDot) {
        // do not print files starting with .
        if(Token.startsWith(name, '.'))
          continue;
      }
      out.print(name);
      out.print("\t");        
    }
    out.print(NL);
  }

  /**
   * Construct name of current/working directory (cwd).
   * @param n pre value of cwd
   * @throws IOException in case of problems with the PrintOutput
   */
  private void pwd(final int n) throws IOException {    
    if(n > 3) {
      pwd(data.parent(n, data.kind(n)));
      out.print(FSUtils.getName(data, n));
      out.print("/");
    } else {      
      out.print("./");
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
