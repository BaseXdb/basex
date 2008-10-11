package org.basex.fs;

import static org.basex.Text.*;
import java.io.IOException;
import java.util.regex.Pattern;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.util.GetOpts;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Performs a locate command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Locate extends FSCmd {
  /** limit output. */
  private boolean lFlag = false;
  /** Just print number of found files. */
  private boolean cFlag = false;
  /** filename to search for. */
  private String path;
  /** Counter of files found. */
  private int filesfound;
  /** Version. */
  char version = (char) -1;
  /** Limit output to number of file names and exit. */
  int limit = -1;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "cl:V:");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'c':
          //Suppress normal output; instead print a
          //count of matching file names.
          cFlag = true;
          break;
        case 'l':
          // Limit output to number of file names and exit.
          limit = Integer.parseInt(g.getOptarg());
          lFlag = true;
          break;
        case 'V':
          version = g.getOptarg().charAt(0);
          break;
      }
    }
    path = g.getPath();
    if(path == null) error("", 100);
  }

  @Override
  public void exec(final PrintOutput out) throws IOException {
    //fileToFindByte = Token.token(fileToFind);
    // Version -  1 = use table
    //            2 = use xquery
    //            3 = use xquery + index
    switch (version) {
      case '1':
        path = fs.regex(path);
        locateTable(DataFS.ROOTDIR, out);
        break;
      case '2':
        locateXQuery(out);
        break;
      default:
        path = fs.regex(path);
        locateTable(DataFS.ROOTDIR, out);
      break;
    }

    if(cFlag) printCount(out);
  }

  /**
   * Performs a locate command.
   * @param pre - the current dir.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void locateTable(final int pre, final PrintOutput out)
      throws IOException {

    if(!lFlag || filesfound < limit) {
      final int[] contentDir = fs.children(pre);
      final IntList res = new IntList();

      for(final int i : contentDir) {
        if(fs.isDir(i)) {
          final byte[] name = fs.name(i);
          path = fs.regex(path);
          if(Pattern.matches(path, Token.string(name))) {
            if(!cFlag) {
              out.print(fs.path(i));
              out.print(NL);
            }
            ++filesfound;
            printDir(i, out);
          } else {
            res.add(i);
          }
        } else if(fs.isFile(i)) {
          // if found print with path
          final byte[] name = fs.name(i);
          //if(Token.eq(name, fileToFindByte)) {
          if(Pattern.matches(path, Token.string(name))) {
            ++filesfound;
            if(!cFlag) {
              out.print(fs.path(i));
              out.print(NL);
            }
          }
        }
      }
      // repeat for all dirs
      for(final int dir : res.finish()) {
        locateTable(dir, out);
      }
    }
  }

  /**
   * Performs a locate command.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void locateXQuery(final PrintOutput out) throws IOException {
    String query = "";
    int slash = path.indexOf('/');
    int lastSlash = 0;
    final int lastrIndexOfSlash = path.lastIndexOf('/');

    if(slash > 0) {
      query = "//*" + filter(path.substring(lastSlash, slash));
      while(slash < lastrIndexOfSlash) {
        query += "/*" + filter(path.substring(lastSlash, slash));
        lastSlash = slash;
        slash = path.indexOf('/', lastSlash);
      }
      query += "/*" + filter(path.substring(lastrIndexOfSlash + 1,
          path.length())) + "/descendant-or-self::*";
    } else {
      query = "//*" + filter(path) + "/descendant-or-self::*";
    }
    final XPathProcessor qu = new XPathProcessor(query);
    try {
      final Nodes result = qu.queryNodes(context.current());
      filesfound = result.size;
      if(!cFlag) {
        for(int i = 0; i < filesfound && (!lFlag || i < limit); i++) {
          out.println(fs.path(result.nodes[i]));
        }
      }
    } catch(final QueryException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns an XPath filter expression on the file name.
   * @param name file name
   * @return filter expression
   */
  private String filter(final String name) {
    return "[contains(@name, \"" + name + "\")]";
  }


  /**
   * Print recursive all content of the dir.
   * @param i pre value of the dir to print
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printDir(final int i, final PrintOutput out) throws IOException {
    final int toScan = i;
    final int[] subContentDir = fs.children(toScan);
    final IntList allDir = new IntList();

    for(final int j : subContentDir) {
      if(!cFlag) {
        out.print(fs.path(j));
        out.print(NL);
      }
      if(fs.isDir(j)) {
        allDir.add(j);
      }
      ++filesfound;
    }
    while(allDir.size > 0) {
      printDir(allDir.remove(0), out);
    }
  }

  /**
   * Print the number of files found.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printCount(final PrintOutput out) throws IOException {
    out.print("" + filesfound);
  }
}
