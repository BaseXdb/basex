package org.basex.query.fs;

import java.io.IOException;

import org.basex.data.Data;
import org.basex.io.PrintOutput;
import org.basex.util.GetOpts;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * Performs a cp command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class CP extends FSCmd {
  /** Remove the all. */
  private boolean fRecursive;
  /** Specified paths. */
  private StringList paths;

  @Override
  public void args(final String args, final int pos) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "hR", pos);
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'R':
          fRecursive = true;
          break;
      }
    }
    paths = g.getFoundArgs();

    // less than two files/paths were specified...
    if(paths.size < 2) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    // Last element of arguments is the target
    String targetfile = paths.remove(paths.size - 1);

    // All other argument should be copied
    while(paths.size > 0) {
      String sourcefile = paths.remove(0);
      // Get all pre values of the source files
      final int[] sources = FSUtils.getChildren(data,
          curPre, sourcefile);
      sourcefile = sourcefile.substring(sourcefile.lastIndexOf('/') + 1);
      // check if there is an existing target dir or file
      final int[] target = FSUtils.getChildren(data,
          curPre, targetfile);
      targetfile = targetfile.substring(targetfile.lastIndexOf('/') + 1);
      // The pre value of the new file
      int preOfNewFile = 4;

      switch(sources.length) {
        case 0:
          // There is no source file
          error(sourcefile, 2);
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
              error(FSUtils.getName(data, sources[0]), 100);
              break;
            }
          }

          final byte[] size = FSUtils.getSize(data, sources[0]);
          final byte[] mtime = FSUtils.currTime();

          if(target.length == 1) {
            if(FSUtils.isDir(data, target[0])) {
              // copy file to dir
              final byte[] name = FSUtils.getName(data, sources[0]);
              final byte[] suffix = FSUtils.getSuffix(data, sources[0]);

              if(!(target[0] == FSUtils.getROOTDIR())) {
                preOfNewFile = target[0] + FSUtils.NUMATT;
              }
              FSUtils.insert(data, false, name, suffix, size, mtime,
                  target[0], preOfNewFile);
            } else {
              // file exists - override
              //FSUtils.setName(data, target[0], 
              //  FSUtils.getName(data, target[0]));
              //FSUtils.setSuffix(data, target[0],  
              //  FSUtils.getSuffix(data, target[0]));
              FSUtils.setSize(data, target[0], size);
              FSUtils.setMtime(data, target[0], mtime);              
            }
          } else {
            // create new file and insert into current dir
            final byte[] name = Token.token(targetfile);
            final byte[] suffix = getSuffix(targetfile);

            if(!(curPre == FSUtils.getROOTDIR())) {
              preOfNewFile = curPre + FSUtils.NUMATT;
            }
            FSUtils.insert(data, false, name, suffix, size, mtime,
                curPre, preOfNewFile);
          }
          break;


        default:
          if(target.length != 1) {
            error("", 99);
            break;
          }
        if(FSUtils.isFile(data, target[0])) {
          error(FSUtils.getName(data, target[0]), 20);
          break;
        }
        if(!(target[0] == FSUtils.getROOTDIR())) {
          preOfNewFile = target[0] + FSUtils.NUMATT;
        }
        final TokenList toInsert = new TokenList();
        int sizeToAdd = 0;
        for(int i : sources) {
          i += sizeToAdd;
          // if i is dir and frecursive is false go to next value
          if(FSUtils.isDir(data, i)) {
            if(fRecursive) {
              sizeToAdd += data.size(i, Data.ELEM);
              cpRecursive(i, target, "");
              continue;
            } else {
              error(FSUtils.getName(data, i), 100);
              continue;
            }
          }
          toInsert.add(FSUtils.getName(data, i));
          toInsert.add(FSUtils.getSuffix(data, i));
          toInsert.add(FSUtils.getSize(data, i));
        }
        for(int j = 0; j < toInsert.size; ++j) {
          FSUtils.insert(data, false, toInsert.delete(0), toInsert.delete(0),
              toInsert.delete(0), FSUtils.currTime(), target[0], preOfNewFile);
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
    final TokenList toInsert = new TokenList();

    if(target.length == 1) {
      if(FSUtils.isFile(data, target[0])) {
        error(FSUtils.getName(data, target[0]), 20);
        return;
      }
    }
    int d = 0;
    // remember fs hierarchy
    while(0 < dirs.length) {
      final IntList allDir = new IntList();
      while(d < dirs.length) {
        final int[] toCopy = FSUtils.getChildren(data, dirs[d]);
        toInsert.add(null);
        // remember relative path for insertion
        toInsert.add(FSUtils.getRelativePath(data, dirs[d], pre));
        for(final int p : toCopy) {
          if(FSUtils.isDir(data, p)) {
            allDir.add(p);
            toInsert.add(Token.token("d"));
          } else {
            toInsert.add(Token.token("f"));
          }
          toInsert.add(FSUtils.getName(data, p));
          toInsert.add(FSUtils.getSuffix(data, p));
          toInsert.add(FSUtils.getSize(data, p));
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
        preOfNewFile = target[0] + FSUtils.NUMATT;
      }
      parPre = target[0];
      // insert a copy of the source dir into the target dir
      FSUtils.insert(data, true, FSUtils.getName(data, pre), Token.EMPTY,
          Token.ZERO, FSUtils.currTime(), parPre, preOfNewFile);
      parPre = preOfNewFile;
    } else {
      // target does not exist - create a new directory and copy all contents
      // of the source directory to it
      if(!(curPre == FSUtils.getROOTDIR())) {
        preOfNewFile = curPre + FSUtils.NUMATT;
      }
      parPre = preOfNewFile;
      FSUtils.insert(data, true, Token.token(targetFile), Token.EMPTY,
          Token.ZERO, FSUtils.currTime(), curPre, preOfNewFile);
    }
    // insert fs hierarchy
    copyRoot = parPre;
    final byte[] f = Token.token("f");
    while(toInsert.size != 0) {
      if(toInsert.list[0] == null) {
        toInsert.delete(0);
        parPre = FSUtils.goToDir(data, copyRoot,
            Token.string(toInsert.delete(0)));
        continue;
      }
      final boolean isDir = !Token.eq(toInsert.delete(0), f);
      FSUtils.insert(data, isDir, toInsert.delete(0), toInsert.delete(0),
          toInsert.delete(0), FSUtils.currTime(), parPre,
          parPre + FSUtils.NUMATT);
    }
  }

  /**
   * Extracts the suffix of a file.
   * @param file the filename
   * @return the suffix of the file
   */
  private byte[] getSuffix(final String file) {
    final int i = file.lastIndexOf('.');
    return i > 0 ? Token.token(file.substring(i + 1)) : Token.EMPTY;
  }
}

