package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
import java.io.IOException;
import java.util.ArrayList;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
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
          FSUtils.printError(out, "cp", g.getPath(), 99);
          return;  
        case '?':         
          FSUtils.printError(out, "cp", g.getPath(), 22);
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

    Data data = context.data();

    if(args.size() != 2) {
      printHelp();
      return;
    }

    String sourcefile = args.remove(0);
    String targetfile = args.remove(0);
    int preOfNewFile = 4;

    int[] sources = FSUtils.getSpecificFilesOrDirs(data, 
        curDirPre, sourcefile);
    sourcefile = sourcefile.substring(sourcefile.lastIndexOf('/') + 1);

    int[] target = FSUtils.getSpecificFilesOrDirs(data, 
        curDirPre, targetfile);
    targetfile = targetfile.substring(targetfile.lastIndexOf('/') + 1);

    switch(sources.length) {
      case 0:
        FSUtils.printError(out, "cp", sourcefile, 2);
        break;
      case 1:        

        if(FSUtils.isDir(data, sources[0])) {
          FSUtils.printError(out, "cp", 
              Token.string(FSUtils.getName(data, sources[0])), 100);
          break;
        }
        //test ob source ein verzeichnis ist und dann nicht kopieren ? 
        
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
          out.print("cp: missing args");
          break;
        }
      if(FSUtils.isFile(data, target[0])) {
        out.print("cp: is not dir");
        break;
      }
      if(!(target[0] == FSUtils.getROOTDIR())) {
        preOfNewFile = target[0] + 5;
      }  
      ArrayList<byte[]>  toInsert = new ArrayList<byte[]>();

      for(int i : sources) {  
        // prüfen ob dir und wenn ja überspringen... ?
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
        mtime = ("" + System.currentTimeMillis()).getBytes();
        FSUtils.insert(data, false, toInsert.remove(0), toInsert.remove(0),
            toInsert.remove(0), mtime, target[0], preOfNewFile);
      }       
      break;
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

