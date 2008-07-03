package org.basex.query.fs;

import static org.basex.query.fs.FSText.*;
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
        FSUtils.printError(out, "cat", sourcefile, 2);
        break;
      case 1:        

        byte[] size = ("" + FSUtils.getSize(data, sources[0])).getBytes();
        byte[] mtime = ("" + System.currentTimeMillis()).getBytes();

        if(target.length == 1) {
          if(FSUtils.isDir(data, target[0])) {
            // copy file to dir
            byte[] name = FSUtils.getName(data, sources[0]);
            byte[] suffix = FSUtils.getSuffix(data, sources[0]);

            if(!(target[0] == FSUtils.getROOTDIR())) {
              preOfNewFile = target[0] + 5;
            }                    
            insert(data, name, suffix, size, mtime, target[0], preOfNewFile);
          } else {
            // file exists - override
            data.update(target[0] + 3, "size".getBytes(), 
                ("" + size).getBytes());
            data.update(target[0] + 4, "mtime".getBytes(), 
                ("" + System.currentTimeMillis()).getBytes());
            data.flush();
          }
        } else {
          // create new file and insert into current dir
          byte[] name = targetfile.getBytes();
          byte[] suffix = getSuffix(targetfile);

          if(!(curDirPre == FSUtils.getROOTDIR())) {
            preOfNewFile = curDirPre + 5;
          }        
          insert(data, name, suffix, size, mtime, curDirPre, preOfNewFile);
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
        toInsert.add(FSUtils.getName(data, i));
        toInsert.add(FSUtils.getSuffix(data, i));
        toInsert.add(("" + FSUtils.getSize(data, i)).getBytes());
      }
      for(int i = 0; i < sources.length; ++i) {
        mtime = ("" + System.currentTimeMillis()).getBytes();
        insert(data, toInsert.remove(0), toInsert.remove(0), toInsert.remove(0),
            mtime, target[0], preOfNewFile);
      }       
      break;
    }      
  }


  /**
   * Inserts a new entry into the table.
   * 
   * @param data - the data table
   * @param name - filename
   * @param suffix - suffix of the file
   * @param size - size of the file
   * @param mtime - make time
   * @param parrent - pre value of the parrent
   * @param pre - position to insert
   * 
   */
  private void insert(final Data data, final byte[] name, final byte[] suffix, 
      final byte[] size, final byte[] mtime, final int parrent, final int pre) {

    data.insert(pre, parrent, "file".getBytes(), Data.ELEM);
    data.insert(pre + 1, pre, "name".getBytes(), name);
    data.insert(pre + 2, pre, "suffix".getBytes(), suffix);
    data.insert(pre + 3, pre, "size".getBytes(), size);
    data.insert(pre + 4, pre, "mtime".getBytes(), mtime);  
    data.flush();
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
    out.print(FSCP);

  }

}

