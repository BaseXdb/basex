package org.basex.fuse;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;

/**
 * BaseX as filesystem in userspace implementation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Alexander Holupirek
 * @author Christian Gruen
 */
public class DeepBase extends DeepFuse {

  /** Debug flag. */
  private static boolean debug = true;

  /** Filesystem database name. */
  private static final String DBNAME = "deepbase";

  /** Filesystem data reference. */
  private Data data;

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
  private void debug(final String m, final String... s) {
    if(!debug) return;
    System.err.printf("%25s", m);
    for(final String x : s)
      System.err.printf(" %s", x);
    System.err.printf("\n");
  }

  /**
   * Initialization.
   */
  private void startup() {
    try {
      debug("[DeepBase.startup]");
      Prop.read();
      data = Open.open(DBNAME);
    } catch(FileNotFoundException e) {
      try {
        data = CreateDB.xml(IO.get("deepfs.xml"), DBNAME);
        startup();
      } catch(IOException ex) {
        ex.printStackTrace();
        System.exit(1);
      }
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
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
    qb.append("/deepfuse");
    if(path.equals("/")) return qb.toString();
    for(int i = 0; i < path.length(); i++) {
      final char c = path.charAt(i);
      if(c == '/') {
        if(eb.length() != 0) {
          qb.append("dir[@name = \"" + eb + "\"]");
          eb.setLength(0);
        }
        qb.append(c);

      } else {
        eb.append(c);
      }
    }
    if(eb.length() != 0) qb.append("*[@name = \"" + eb + "\"]");

    String qu = qb.toString();
    qu = qu.endsWith("/") ? qu.substring(0, qu.length() - 1) : qu;
    debug("[DeepBase.xpath]", "xpath: '" + qu + "'");
    return qu;
  }

  /**
   * Performs an XQuery query and returns the resulting node set.
   * 
   * @param xq query
   * @return result nodes
   * @throws QueryException on failure
   */
  private Nodes query(final String xq) throws QueryException {
    return new QueryProcessor(xq).queryNodes(new Nodes(0, data));
  }

  /**
   * Performs an XQuery query and returns the resulting node set.
   * 
   * @param xq query
   * @return result nodes
   * @throws QueryException on failure
   */
  private Nodes queryOne(final String xq) throws QueryException {
    Nodes n = new QueryProcessor(xq).queryNodes(new Nodes(0, data));
    if(n.size != 1) throw new QueryException("Expected exactly one match for "
        + xq + " but got " + n.size);
    return n;
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
  @Override
  public int getattr(final String path) {
    debug("[DeepBase.getattr]", path);
    try {
      final Nodes n = query(xpath(path));
      debug("[DeepBase.getattr]", "found " + n.size);
      if(n.size > 0) {
        int pre = n.nodes[0];
        debug("[DeepBase.getattr]", "pre " + pre + " id " + data.id(pre));
        return data.id(pre);
      }
    } catch(QueryException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Create a new regular file.
   * 
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  @Override
  public int create(final String path, final int mode) {
    try {
      debug("[DeepBase.create]", "path " + path, "mode "
          + Integer.toOctalString(mode));

      if(isDir(mode)) return -1;
      // construct regular file entry.
      MemData m = new MemData(2, data.tags, data.atts, data.ns, data.skel);
      int tagID = data.tags.index(FILE, null, false);
      int tagID2 = data.atts.index(NAME, null, false);
      m.addElem(tagID, 0, 1, 2, 2, false);
      m.addAtt(tagID2, 0, getName(path, mode).getBytes(), 1);
      // Get pre value of directory to insert.
      Nodes pnode = queryOne(xpath(chopFilename(path, mode)));
      int ppre = pnode.nodes[0];
      int pid = data.id(ppre);
      int ipre = ppre + 2;
      debug("[DeepBase.create]", "ppre " + ppre + " id " + pid);
      data.insert(ipre, 1, m);
      data.flush();
      data.meta.update();
      int iid = data.id(ipre);
      debug("[DeepBase.create]", "ipre " + ipre + " id " + iid);
      return iid;
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int init() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int destroy() {
    try {
      debug("[DeepBase.destroy]");
      data.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public int unlink(final String path) {
    try {
      debug("[DeepBase.unlink] " + path);
      Nodes n = queryOne(xpath(path));
      data.delete(n.nodes[0]);
      data.flush();
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  @Override
  public int access(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int bmap(final String path, final long blocksize, final long idx) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int chmod(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int chown(final String path, final int owner, final int group) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int fgetattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int flush(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int fsync(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int fsyncdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int ftruncate(final String path, final long off) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int link(final String name1, final String name2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int listxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int lock(final String path, final int cmd) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int mkdir(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int mknod(final String path, final int mode, final int dev) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int open(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int opendir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public byte[] read(final String path, final int length, final int offset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int readdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String readlink(final String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int release(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int releasedir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int removexattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int rename(final String from, final String to) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int rmdir(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int setxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int statfs(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int symlink(final String from, final String to) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int truncate(final String path, final long off) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int utimens(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int write(final String path, final int length, final int offset,
      final byte[] data1) {
    // TODO Auto-generated method stub
    return 0;
  }
}
