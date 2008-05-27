package org.basex.core.proc;

import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.fs.FSUtils;
import org.basex.util.Token;

/**
 * Link command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Link extends XPath {
  /** Maximum number of links. */
  public static final int MAXLINKS = Prop.UNIX ? 500 : 50;
  /** Link to symbolic search results. */
  public static final String LINKDIR = Prop.HOME + "/basex/";
  /** Temporary for Unix: Link to symbolic search results. */
  static final String ROOTPATH = "/Library/Application Support/Apple";
  /** Counter. */
  static int counter;

  @Override
  protected boolean exec() {
    final int count = ++counter;
    delete();

    final boolean ok = super.exec();
    if(!ok || !(result instanceof Nodes)) return false;

    final Data data = context.data();
    final Nodes nodes = (Nodes) result;
    if(count != counter) return false;

    final int len = Integer.toString(MAXLINKS).length();
    int c = 0;

    for(int n = 0; n < nodes.size && n < MAXLINKS; n++) {
      if(count != counter) return false;
      final int pre = nodes.pre[n];
      if(!FSUtils.isFile(data, pre) && !FSUtils.isDir(data, pre)) continue;

      // create file counter and symbolic link
      String nr = Integer.toString(n + 1);
      while(nr.length() < len) nr = "0" + nr;
      final byte[] name = FSUtils.getName(data, pre);

      final String src = "[" + nr + "] " + Token.string(name);
      final String tar = Token.string(FSUtils.getPath(data, pre));
      link(src, tar);
      c++;
    }

    if(c == MAXLINKS) {
      link(" " + c + " of " + nodes.size + " Results shown", "dummy");
    } else if(c == 0) {
      link("(0 Results)", "dummy");
    }

    // remove query info
    if(!Prop.allInfo) error("");
    return ok;
  }

  /**
   * Deletes the last search.
   */
  public static void delete() {
    final File path = new File(LINKDIR);
    if(!path.exists()) path.mkdirs();
    for(final File sub : path.listFiles()) sub.delete();
  }

  /**
   * Deletes the last search.
   * @param src source
   * @param tar target
   */
  public static void link(final String src, final String tar) {
    try {
      final Runtime run = Runtime.getRuntime();
      run.exec("ln -s \"" + tar + "\" \"" + LINKDIR + src + "\"");
    } catch(final IOException ex) {
      ex.printStackTrace();
    }
  }
}
