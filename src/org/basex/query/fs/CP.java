package org.basex.query.fs;

import java.io.IOException;
import java.util.ArrayList;

import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;

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

    GetOpts g = new GetOpts(cmd, "h", 1);
    // get all Options
    int ch = g.getopt();
    while (ch != -1) {
      switch (ch) {
        case 'h':
          printHelp();
          return;
        case ':':         
          out.print("cp: missing argument");
          return;  
        case '?':         
          out.print("cp: illegal option");
          return;
      }      
      ch = g.getopt();
    }
    // if there is path expression remove it     
    if(g.getPath() != null) {      
      cp(g.getFoundArgs());
    } 
  }

  /**
   * Performs a cp command.
   *  
   *  @param args The name of the file
   *  @throws IOException in case of problems with the PrintOutput 
   */
  private void cp(final ArrayList<String> args) throws IOException {

    if(args.size() != 2) {
      printHelp();
      return;
    }
    String sourcefile = args.remove(0);
    String targetfile = args.remove(0);

    String file = "";   
    int beginIndex = sourcefile.lastIndexOf('/');
    if(beginIndex == -1) {
      file = sourcefile;
    } else {
      curDirPre = FSUtils.goToDir(context.data(), curDirPre, 
          sourcefile.substring(0, beginIndex));   
      if(curDirPre == -1) {
        out.print("cp: " + sourcefile + " No such file or directory");
      } else {
        file = sourcefile.substring(beginIndex + 1);
      }
    }
    int sourceFilePre =  FSUtils.getSpecificFile(context.data(), 
        curDirPre, file.getBytes());
    
    if(sourceFilePre > 0) {
      // source_file found       
      
      Data data = context.data();
      
      beginIndex = targetfile.lastIndexOf('/');
      if(beginIndex == -1) {
        file = targetfile;
      } else {
        curDirPre = FSUtils.goToDir(context.data(), curDirPre, 
            targetfile.substring(0, beginIndex));   
        if(curDirPre == -1) {
          out.print("cp: " + targetfile + " No such file or directory");
        } else {
          file = targetfile.substring(beginIndex + 1);
        }
      }
      int targetfilePre =  FSUtils.getSpecificFile(context.data(), 
          curDirPre, file.getBytes());
      
      long size = FSUtils.getSize(data, sourceFilePre);      
      
      if(targetfilePre > 0) {
        // update
        data.update(targetfilePre + 3, "size".getBytes(), 
            ("" + size).getBytes());
        context.data().update(targetfilePre + 4, "mtime".getBytes(), 
            ("" + System.currentTimeMillis()).getBytes()); 
      } else {
        // insert
        int preNewFile = 4;
        if(!(curDirPre == FSUtils.getROOTDIR())) {
          preNewFile = curDirPre + 5;
        }        
        data.insert(preNewFile, 
            curDirPre, "file".getBytes(), Data.ELEM);
        data.insert(preNewFile + 1, preNewFile, 
            "name".getBytes(), file.getBytes());
        data.insert(preNewFile + 2, 
            preNewFile, "suffix".getBytes(), 
            getSuffix(file));
        data.insert(preNewFile + 3, 
            preNewFile, "size".getBytes(), 
            ("" + size).getBytes());
        data.insert(preNewFile + 4, 
            preNewFile, "mtime".getBytes(), 
            ("" + System.currentTimeMillis()).getBytes());   
      }      
    } else {   
      //cp: XXX: No such file or directory
      out.print("cp: " + targetfile + " No such file or directory");
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
    out.print("cp  source_file target_file");

  }

}

