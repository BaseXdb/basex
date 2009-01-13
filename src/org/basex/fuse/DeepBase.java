package org.basex.fuse;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.deepfs.IDeepFuse;

/**
 * BaseX as filesystem in userspace implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Alexander Holupirek
 * @author Christian Gruen
 */
public class DeepBase implements IDeepFuse {

  /** Filesystem database name. */
  private static final String DBNAME = "deepbase";

  /** Filesystem data reference. */
  private Data data;

  /** Reference to root node. */
  private Nodes root;

  /** Default constructor. */
  public DeepBase() {
    startup();
  }

  /**
   * Debug message.
   * 
   * @param m method name
   * @param s debug msg
   */
  private void debug(final String m, final String s) {
    System.err.printf("%25s %s\n", m, s);
  }

  /**
   * Initialization.
   */
  private void startup() {
    debug("[DeepBase.startup]", "");
    Prop.read();
    try {
      data = Open.open(DBNAME);
      root = new Nodes(0, data);
    } catch(FileNotFoundException e) {
      try {
        Context ctx = new Context();
        final Parser p = new Parser(IO.get(DBNAME)) {
          @Override
          public void parse(final Builder build) { /* empty */}
        };
        ctx.data(CreateDB.xml(p, DBNAME));
        data = Open.open(DBNAME);
        root = new Nodes(0, data);
      } catch(final IOException ex) {
        e.printStackTrace();
      }
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Converts the file path into an XPath query.
   * 
   * @param path file path
   * @return xpath query
   */
  private String xpath(final String path) {
    final StringBuilder qb = new StringBuilder();
    final StringBuilder eb = new StringBuilder();
    qb.append("/deepfs");
    for(int i = 0; i < path.length(); i++) {
      final char c = path.charAt(i);
      if(c == '/') {
        if(i == 0) {
          qb.append("/dir/");
        } else {
          if(eb.length() != 0) {
            qb.append("dir[@name = \"" + eb + "\"]");
            eb.setLength(0);
          }
          qb.append(c);
        }
      } else {
        eb.append(c);
      }
    }
    if(eb.length() != 0) qb.append("*[@name = \"" + eb + "\"]");

    final String qu = qb.toString();
    return qu.endsWith("/") ? qu.substring(0, qu.length() - 1) : qu;
  }

  /**
   * Performs an XPath query and returns the resulting node set.
   * 
   * @param xpath query
   * @return result nodes
   * @throws QueryException on failure
   */
  private Nodes query(final String xpath) throws QueryException {
    return new XPathProcessor(xpath).queryNodes(root);
  }

  /**
   * Get attributes for file.
   * 
   * Instead of returning the stat information for path, the id of the node is
   * returned. The DeepBase counterpart C implementation holds the native stat
   * information for the returned id.
   * 
   * @param path filesystem pathname the id is requested for
   * @return int node id or -1 on failure
   */
  public int getattr(final String path) {
    debug("[DeepBase.getattr]", path);
    try {
      String xp = xpath(path);
      debug("[DeepBase.getattr]", "xpath " + xp);
      final Nodes nodes = query(xp);
      debug("[DeepBase.getattr]", "found " + nodes.size);
      if(nodes.size > 0) {
        int pre = nodes.nodes[0];
        debug("[DeepBase.getattr]", "pre " + pre);
        debug("[DeepBase.getattr]", "id " + data.id(pre));
      }

    } catch(QueryException e) {
      e.printStackTrace();
    }
    return 13;
  }

  /**
   * Create a new regular file.
   * 
   * @param path to the file to be created
   * @return id of the newly created file or -1 on failure
   */
  public int create(final String path) {
    System.err.printf("%25s path '%s'\n", "[DeepBase.create]", path);
    return -1;
  }

  public int access(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int bmap(final String path, final long blocksize, final long idx) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int chmod(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int chown(final String path, final int owner, final int group) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int destroy() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int fgetattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int flush(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int fsync(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int fsyncdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int ftruncate(final String path, final long off) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int init() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int link(final String name1, final String name2) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int listxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int lock(final String path, final int cmd) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int mkdir(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int mknod(final String path, final int mode, final int dev) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int open(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int opendir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public byte[] read(final String path, final int length, final int offset) {
    // TODO Auto-generated method stub
    return null;
  }

  public int readdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public String readlink(final String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public int release(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int releasedir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int removexattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int rename(final String from, final String to) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int rmdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int setxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int statfs(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int symlink(final String from, final String to) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int truncate(final String path, final long off) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int unlink(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int utimens(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int write(final String path, final int length, final int offset,
      final byte[] databuf) {
    // TODO Auto-generated method stub
    return 0;
  }
}
