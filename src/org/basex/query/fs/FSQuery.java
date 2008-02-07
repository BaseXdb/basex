package org.basex.query.fs;

import static org.basex.Text.NL;
import java.io.IOException;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.Token;


/**
 * This class simulates some file system operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen & Hannes Schwarz
 */
public final class FSQuery {
  /** Data reference. */
  private final Context context;

  /**
   * Simplified Constructor.
   * @param ctx data context
   */
  public FSQuery(final Context ctx) {
    context = ctx;
  }

  /**
   * The current working directory as set by the cd command.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void pwd(final PrintOutput out) throws IOException {
    pwd(out, context.current().pre[0]);
    out.print(NL);
  }

  /**
   * Construct name of current/working directory (cwd).
   * @param out stream receiving cwd.
   * @param n pre value of cwd
   * @throws IOException in case of problems with the PrintOutput
   */
  private void pwd(final PrintOutput out, final int n) throws IOException {
    final Data data = context.data();
    if(n > 3) {
      pwd(out, data.parent(n, data.kind(n)));
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    } else {
      out.print(Token.string(FSUtils.getName(data, n)) + "/");
    }    
  }

  /**
   * Performs an ls command.
   * @param options directory path
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void ls(final String options, final PrintOutput out) 
      throws IOException {        
    ls(out, context.current().pre[0], options);
    out.print(NL);
  }
  
  /**
   * Performs an ls command.
   * @param out - output stream
   * @param pre - value of the directory
   * @param path - path expression
   * @throws IOException in case of problems with the PrintOutput
   */
  private void ls(final PrintOutput out, final int pre,
      final String path) throws IOException {
    final Data data = context.data();
    int n = FSUtils.goToDir(data, pre, path);    
    if(n == -1) {
      out.println("ls: " + path + " No such file or directory");
      return;
    }
    int[] dir = FSUtils.getAllOfDir(data, n);
    for(int i : dir) {
      out.print(FSUtils.getName(data, i));
      out.print("\t");
    }
  }
  
  
  /**
   * Performs a du command.
   * @param options options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void du(final String options, final PrintOutput out)
      throws IOException {
    if(options.length() > 1) {
      out.println("Options not yet implemented");
    }
    du(".", out, context.current().pre[0]);
    out.print(NL);
  }
  
  /**
   * The du utility displays the file system block usage for each file argu-
   * ment and for each directory in the file hierarchy rooted in each direc-
   * tory argument.  If no file is specified, the block usage of the hierarchy
   * rooted in the current directory is displayed.
   * 
   * @param path pfad
   * @param out Output Stream
   * @param pre the pre value
   * @throws IOException in case of problems with the PrintOutput
   * @return die speicher
   */
  private long du(final String path, 
      final PrintOutput out, final int pre) throws IOException {    

    final Data data = context.data();
    int n = pre;    
    long diskusage = FSUtils.getSize(data, n);
    
    int size = data.size(n, data.kind(n)) + n;               
    n += data.attSize(n, data.kind(n));
    
    while(n < size) {      
      if(FSUtils.isDir(data, n)) {  
        diskusage += du(path + "/" + Token.string(FSUtils.getName(data, n)),
            out, n);
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
   * Perform cd.
   * 
   * @param arg options
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void cd(final String arg, final PrintOutput out) 
    throws IOException {
    cd(arg, out, context.current().pre[0]);
    out.print(NL);
  }
  
  /**
   * Perform cd.
   * 
   * @param path Pfad 
   * @param out output stream 
   * @param pre current directory
   * @throws IOException in case of problems with the PrintOutput
   */
  private void cd(final String path, final PrintOutput out, final int pre)
  throws IOException {

    int newDir = FSUtils.goToDir(context.data(), pre, path);
    if(newDir != -1) {
      context.current().pre[0] = newDir;
    } else {
      out.println(path + ": No such file or directory");
    }    
  }

  /**
   * Performs an ls command.
   * @param options directory path
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  public void lsr(final String options, final PrintOutput out) 
      throws IOException {        
    lsr(options, out, context.current().pre[0]);
    out.print(NL);
  }
  /**
   * Perform LS  with options.
   * 
   * @param options Pfad 
   * @param out output stream 
   * @param pre current directory
   * @throws IOException in case of problems with the PrintOutput
   */
  private void lsr(final String options, final PrintOutput out, final int pre)
  throws IOException {
    final Data data = context.data();
    String[] opt = FSUtils.readOptions(options);
    // Keine Pfadangabe -> nur aktuelles Verzeichnis ausgeben
    if(opt == null) {
      int[] dir = FSUtils.getAllOfDir(data, pre);
      for(int i : dir) {
        out.print(FSUtils.getName(data, i));
        out.print("\t");
      }
    } else {
      int newDir = pre;
      if(!(opt[opt.length - 1].charAt(0) == '-')) {
      // ins angegebene Verzeichnis springen
        newDir = FSUtils.goToDir(context.data(), pre,
                 opt[opt.length - 1]);    
      }
      // Optionen testen und entsprechen ausführen
      for(String x : opt) {        
        // rekursiv alle Verzeichnisse ausgeben        
        if(x.equals("-R")) {
          lsrecursive(newDir, out, data);
        }
      }
    }    
  }
  
  /**
   * Macht was.
   * @param pre -
   * @param out - 
   * @param data -
   * @throws IOException in case of problems with the PrintOutput 
   */
  private void lsrecursive(final int pre, final PrintOutput out,
      final Data data) throws IOException {     
    
    int[] allDir = FSUtils.getAllDir(data, pre);        
    // print dir
    int[] dir = FSUtils.getAllOfDir(data, pre);
    for(int j : dir) {
      out.print(FSUtils.getName(data, j));
      out.print("\t");        
    }
    out.print(NL);
    for(int i = 0; allDir[i] != 0 && i < allDir.length; i++) {
      // print path
      pwd(out, allDir[i]);
      out.print(NL);
      lsrecursive(allDir[i], out, data);
    }
  }
}
