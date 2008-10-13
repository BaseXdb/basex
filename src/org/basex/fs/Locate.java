package org.basex.fs;

import java.io.IOException;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.util.GetOpts;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Performs a locate command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Hannes Schwarz - Hannes.Schwarz@gmail.com
 */
public final class Locate extends FSCmd {
  /** Just print number of found files. */
  private boolean cFlag = false;
  /** filename to search for. */
  private String path;
  /** Counter of files found. */
  private int filesfound;
  /** Version. */
  char version = (char) -1;
  /** Limit output to number of file names and exit. */
  int limit = Integer.MAX_VALUE;

  @Override
  public void args(final String args) throws FSException {
    // get all Options
    final GetOpts g = new GetOpts(args, "cl:V:");
    while(g.more()) {
      final int ch = checkOpt(g);
      switch (ch) {
        case 'c':
          cFlag = true;
          break;
        case 'l':
          limit = Integer.parseInt(g.getOptarg());
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
    // Version: 1 = use table, 2 = use xquery
    switch (version) {
      case '1':
        path = fs.regex(path);
        locateTable(DataFS.ROOTDIR, out);
        break;
      case '2':
        locateQuery(out);
        break;
    }

    if(cFlag) {
      out.println(Integer.toString(filesfound));
    }
  }

  /**
   * Performs a locate command.
   * @param pre - the current dir.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void locateTable(final int pre, final PrintOutput out)
      throws IOException {

    final IntList dirs = new IntList();
    final byte[] pat = Token.token(path);
    
    for(final int c : fs.children(pre)) {
      if(filesfound >= limit) return;

      //if(Pattern.matches(path, Token.string(name))) {
      final boolean dir = fs.isDir(c);
      if(Token.contains(fs.name(c), pat)) {
        if(!cFlag) out.println(fs.path(c));
        if(dir) printDir(c, out);
        ++filesfound;
      } else {
        if(dir) dirs.add(c);
      }
    }
    // repeat for all dirs
    for(final int dir : dirs.finish()) {
      locateTable(dir, out);
    }
  }

  /**
   * Print recursive all content of the dir.
   * @param i pre value of the dir to print
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void printDir(final int i, final PrintOutput out) throws IOException {
    final IntList dirs = new IntList();

    for(final int c : fs.children(i)) {
      if(filesfound++ >= limit) return;
      if(!cFlag) out.println(fs.path(c));
      if(fs.isDir(c)) dirs.add(c);
    }
    
    for(final int dir : dirs.finish()) {
      printDir(dir, out);
    }
  }

  /**
   * Performs a locate command.
   * @param out output stream
   * @throws IOException in case of problems with the PrintOutput
   */
  private void locateQuery(final PrintOutput out) throws IOException {
    final TokenBuilder query = new TokenBuilder("/");
    final String[] names = path.split("/");
    
    // build query string
    if(names.length > 1) {
      add(query, "ends-with", names[0]);
      final int nl = names.length - 1;
      for(int n = 1; n < nl; n++) add(query, "contains", names[n]);
      add(query, "starts-with", names[nl]);
    } else {
      add(query, "contains", path);
    }

    // include limit in query
    final XPathProcessor qu = new XPathProcessor(
        "(/descendant-or-self::*" + query + ")[position() <= " + limit + "]");
    
    try {
      final Nodes result = qu.queryNodes(context.current());
      filesfound = result.size;
      if(!cFlag) {
        for(int i = 0; i < filesfound; i++) {
          out.println(fs.path(result.nodes[i]));
        }
      }
    } catch(final QueryException e) {
      e.printStackTrace();
    }
  }

  /**
   * Adds an XPath step with the specified function and file.
   * @param tb token builder
   * @param fun function
   * @param name file name
   */
  private void add(final TokenBuilder tb, final String fun, final String name) {
    tb.add("/*[" + fun + "(@name, \"" + name + "\")]");
  }
}
