package org.basex.query.fs;

import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.query.fs.Exception.PathNotFoundException;
import org.basex.util.GetOpts;
import org.basex.util.Token;

/**
 * Performs a du command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public class DU {

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
    du(".", curDirPre);
  }
  
  /**
   * The du utility displays the file system block usage for each file argu-
   * ment and for each directory in the file hierarchy rooted in each direc-
   * tory argument.  If no file is specified, the block usage of the hierarchy
   * rooted in the current directory is displayed.
   * 
   * @param path pfad
   * @param pre the pre value
   * @throws IOException in case of problems with the PrintOutput
   * @return die speicher
   */
  private long du(final String path, final int pre) throws IOException {    

    final Data data = context.data();
    int n = pre;    
    long diskusage = FSUtils.getSize(data, n);

    int size = data.size(n, data.kind(n)) + n;               
    n += data.attSize(n, data.kind(n));

    while(n < size) {      
      if(FSUtils.isDir(data, n)) {  
        diskusage += du(path + "/" + Token.string(FSUtils.getName(data, n)), n);
        n += data.size(n, data.kind(n));
      } else {
        diskusage += FSUtils.getSize(data, n);
        n += data.attSize(n, data.kind(n));
      }
    }
    out.println(diskusage + "\t" + path);
    return diskusage;
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

