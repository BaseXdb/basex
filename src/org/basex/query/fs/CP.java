package org.basex.query.fs;

import static org.basex.Text.NL;
import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Performs a cp command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 *
 */
public final class CP {

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
  public CP(final Context ctx, final PrintOutput output) {
    this.context = ctx;
    curDirPre = ctx.current().pre[0];
    this.out = output;
  }

  /**
   * Performs a cp command.
   * 
   * @param cmd - command line
   * @throws IOException - in case of problems with the PrintOutput 
   */
  public void cpMain(final String cmd) 
  throws IOException {

    GetOpts g = new GetOpts(cmd, "hR", 1);
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
          FSUtils.printError(out, "cp", g.getPath(), 99);
          return;  
        case '?':         
          FSUtils.printError(out, "cp", g.getPath(), 102);
          return;
      }      
      ch = g.getopt();
    }
    // if there is path expression remove it     
    if(g.getPath() != null) {      
      cp(g.getFoundArgs());
    } 
    out.print(NL);
  }

  /**
   * Performs a cp command.
   *  
   *  @param args The name of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void cp(final StringList args) throws IOException {

    if(args.size < 2) {
      printHelp();
      return;
    }    
    Data data = context.data();
    // Last element of arguments is the target
    String targetfile = args.remove(args.size - 1);

    // All other argument should be copied
    while(args.size > 0) {
      String sourcefile = args.remove(0);
      // Get all pre values of the source files
      int[] sources = FSUtils.getSpecificFilesOrDirs(data, 
          curDirPre, sourcefile);
      sourcefile = sourcefile.substring(sourcefile.lastIndexOf('/') + 1);
      // check if there is an existing target dir or file 
      int[] target = FSUtils.getSpecificFilesOrDirs(data, 
          curDirPre, targetfile);
      targetfile = targetfile.substring(targetfile.lastIndexOf('/') + 1);
      // The pre value of the new file
      int preOfNewFile = 4;

      switch(sources.length) {
        case 0:
          // There is no source file
          FSUtils.printError(out, "cp", sourcefile, 2);
          break;
        case 1: 
          /* Just one file or dir to copy
           * Possibilities:
           *  1. Source = dir -> copy recursive if option -R is set
           *  2. Source = file / Target = dir -> copy to dir
           *  3. Source = file / Target = file -> override target
           *  4. Source = file / Target is not existing  -> create a new file
           */
          if(FSUtils.isDir(data, sources[0])) {
            //Test if it is a dir. Just copy if frecursive is set.
            if(fRecursive) {
              cpRecursive(sources[0], target, targetfile); 
              break;
            } else {
              FSUtils.printError(out, "cp", 
                  Token.string(FSUtils.getName(data, sources[0])), 100);
              break;
            }
          }

          byte[] size = Token.token(FSUtils.getSize(data, sources[0]));
          byte[] mtime = Token.token(System.currentTimeMillis());

          if(target.length == 1) {
            if(FSUtils.isDir(data, target[0])) {
              // copy file to dir
              byte[] name = FSUtils.getName(data, sources[0]);
              byte[] suffix = FSUtils.getSuffix(data, sources[0]);

              if(!(target[0] == FSUtils.getROOTDIR())) {
                preOfNewFile = target[0] + 5;
              }                             
              FSUtils.insert(data, false, name, suffix, size, mtime,
                  target[0], preOfNewFile);
            } else {
              // file exists - override
              FSUtils.update(data, FSUtils.getName(data, target[0]),
                  FSUtils.getSuffix(data, target[0]),
                  size, mtime, target[0]);
            }
          } else {
            // create new file and insert into current dir
            byte[] name = Token.token(targetfile);
            byte[] suffix = getSuffix(targetfile);

            if(!(curDirPre == FSUtils.getROOTDIR())) {
              preOfNewFile = curDirPre + 5;
            }        
            FSUtils.insert(data, false, name, suffix, size, mtime,
                curDirPre, preOfNewFile);
          }      
          break;
        default:
          if(target.length != 1) { 
            FSUtils.printError(out, "cp", "", 99);
            break;
          }
        if(FSUtils.isFile(data, target[0])) {
          FSUtils.printError(out, "cp", 
              Token.string(FSUtils.getName(data, target[0])), 20);
          break;
        }
        if(!(target[0] == FSUtils.getROOTDIR())) {
          preOfNewFile = target[0] + 5;
        }  
        ArrayList<byte[]>  toInsert = new ArrayList<byte[]>();

        for(int i : sources) {  
          // if i is dir go to next value
          if(FSUtils.isDir(data, i)) {
            FSUtils.printError(out, "cp", 
                Token.string(FSUtils.getName(data, i)), 100);
            continue;
          }      
          toInsert.add(FSUtils.getName(data, i));
          toInsert.add(FSUtils.getSuffix(data, i));
          toInsert.add(Token.token(FSUtils.getSize(data, i)));
        }
        for(int j = 0; j < toInsert.size(); ++j) {

          FSUtils.insert(data, false, toInsert.remove(0), toInsert.remove(0),
              toInsert.remove(0), Token.token(System.currentTimeMillis()),
              target[0], preOfNewFile);
        }       
        break;
      }      
    }
  }

  /**
   * Copies all contents of a dir.
   * 
   * @param pre value of the source
   * @param target the list if there is a target
   * @param targetFile the name of the target
   * @throws IOException in case of problems with the PrintOutput 
   */
  private void cpRecursive(final int pre,
      final int[] target, final String targetFile) throws IOException {

    int[] dirs = new int[]{pre};
    Data data = context.data();
    ArrayList<byte[]>  toInsert = new ArrayList<byte[]>();

    if(target.length == 1) {
      if(FSUtils.isFile(data, target[0])) {
        FSUtils.printError(out, "cp", 
            Token.string(FSUtils.getName(data, target[0])), 20);
        return;
      }
    }
    int d = 0;  
    // remember fs hierarchy
    while(0 < dirs.length) {
      IntList allDir = new IntList();
      while(d < dirs.length) {
        int[] toCopy = FSUtils.getAllOfDir(data, dirs[d]);
        toInsert.add(null);
        // remember relative path for insertion
        toInsert.add(FSUtils.getRelativePath(data, dirs[d], pre));
        for(int p : toCopy) {
          if(FSUtils.isDir(data, p)) {          
            allDir.add(p);
            toInsert.add(Token.token("d"));
          } else {
            toInsert.add(Token.token("f"));
          }          
          toInsert.add(FSUtils.getName(data, p));
          toInsert.add(FSUtils.getSuffix(data, p));
          toInsert.add(Token.token(FSUtils.getSize(data, p)));
        }
        ++d;
      }
      dirs = allDir.finish();
      d = 0;
    }
    // pre value of the dir of insertion
    int copyRoot;
    // pre value of the parrent node
    int parPre;
    // need to calculate the pre value of the new directory
    int preOfNewFile = 4;
    // if target exists - insert into the target the directory to copy
    if(target.length == 1) {
      if(!(target[0] == FSUtils.getROOTDIR())) {
        preOfNewFile = target[0] + 5;
      }  
      parPre = target[0];
      // insert a copy of the source dir into the target dir
      FSUtils.insert(data, true, FSUtils.getName(data, pre), Token.token(""),
          Token.token(0), Token.token(System.currentTimeMillis()),
          parPre, preOfNewFile);
      parPre = preOfNewFile;
    } else {     
      // target does not extist - create a new directory and copy all contents
      // of the source directory to it
      if(!(curDirPre == FSUtils.getROOTDIR())) {
        preOfNewFile = curDirPre + 5;
      }      
      parPre = preOfNewFile; 
      FSUtils.insert(data, true, Token.token(targetFile), Token.token(""),
          Token.token(0), Token.token(System.currentTimeMillis()),
          curDirPre, preOfNewFile);
    }
    out.print("Par: " + parPre + "\n");
    // insert fs hierarchy
    copyRoot = parPre;
    while(!toInsert.isEmpty()) {      
      if(toInsert.get(0) == null) {
        toInsert.remove(0);
        parPre = FSUtils.goToDir(data, copyRoot, 
            Token.string(toInsert.remove(0)));
        continue;
      }
      boolean isDir = true;
      if(Token.string(toInsert.remove(0)).equals("f"))
        isDir = false;      
      FSUtils.insert(data, isDir, toInsert.remove(0), toInsert.remove(0),
          toInsert.remove(0), Token.token(System.currentTimeMillis()),
          parPre, parPre + 5);
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
    out.print(FSCP);
  }
}

