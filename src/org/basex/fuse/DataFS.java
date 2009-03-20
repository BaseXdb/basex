package org.basex.fuse;

import static org.basex.build.fs.FSText.*;
import static org.basex.util.Token.*;

import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.fs.FSUtils;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.IntList;
import org.basex.util.TokenBuilder;

/**
 * Preliminary collection of file system methods.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen, Alexander Holupirek, Hannes Schwarz
 */
public final class DataFS extends DeepFuse {
  /** Data reference. */
  final Data data;
  /** GUI reference. */
  public GUI gui;
  /** Index References. */
  public int fileID;
  /** Index References. */
  public int dirID;
  /** Index References. */
  public int suffID;
  /** Index References. */
  public int timeID;
  /** Index References. */
  public int modeID;
  /** Index References. */
  public int unknownID;
  /** Index mount point. */
  public int mountpointID;
  /** Index backing store. */
  public int backingstoreID;
  
  /* ------------------------------------------------------------------------ 
   *   Native deepfs method declarations (org_basex_fuse_DataFS.h)
   * ------------------------------------------------------------------------ */
  /**
   * Mount database as FUSE.
   * @param mountpoint path where to mount BaseX.
   * @param backing path to backing storage root.
   * @param dbname name of the BaseX database.
   * @return 0 on success, errno in case of failure.
   */
  public native int nativeMount(final String mountpoint,
      final String backing, final String dbname);
  
  /**
   * Unlink file in backing store.
   * @param pathname to file to delete
   * @return 0 on success, errno in case of failure.
   */
  public native int nativeUnlink(final String pathname);
  
  /** 
   * Tell DeepFS that BaseX will shutdown.
   */
  public native void nativeShutDown();
  /* ------------------------------------------------------------------------ */
  
  /**
   * Constructor.
   * @param d data reference
   */
  public DataFS(final Data d) {
    data = d;
    dirID  = d.tags.id(DataText.DIR);
    fileID = d.tags.id(DataText.FILE);
    unknownID  = d.tags.id(DataText.UNKNOWN);

    suffID = d.atts.id(DataText.SUFFIX);
    timeID = d.atts.id(DataText.MTIME);
    modeID = d.atts.id(DataText.MODE);
    backingstoreID = d.atts.id(DataText.BACKINGSTORE);
    mountpointID = d.atts.id(DataText.MOUNTPOINT);

    if(Prop.fuse) {
      final File mp = new File(Prop.mountpoint);
      if (!mp.mkdirs()) {
        if (mp.exists())
          if (!FSUtils.deleteDir(mp) || !mp.mkdirs()) {
            System.err.println(MOUNTPOINTEXISTS + Prop.mountpoint);
            return;
          }
      }
      nativeMount("/mnt/deepfs", "/var/tmp/deepfs", "demo");
    }
    
    if(Prop.gui)
      BaseX.err("GUI MODE\n");
    else
      BaseX.err("CONSOLE MODE\n");
  }
  
  /**
   * Checks if the specified node is a file.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isFile(final int pre) {
    return data.kind(pre) == Data.ELEM &&
      data.tagID(pre) == data.tags.id(DataText.FILE);
  }

  /**
   * Checks if the specified node is a directory.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isDir(final int pre) {
    return data.kind(pre) == Data.ELEM &&
      data.tagID(pre) == data.tags.id(DataText.DIR);
  }

  /**
   * Returns the absolute file path.
   * @param pre pre value
   * @return file path.
   */
  public byte[] path(final int pre) {
    int p = pre;
    int k = data.kind(p);
    final IntList il = new IntList();
    while(p >= 0 && k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }
    
    final TokenBuilder tb = new TokenBuilder();
    final int s = il.size;
    if (s != 0) {
      final byte[] b = mountpoint(il.list[s - 1]);
      if (b.length != 0) {
        tb.add(b);
        if(!endsWith(b, '/')) tb.add('/');
      }
    }
    for(int i = s - 2; i >= 0; i--) {
      final byte[] node = replace(name(il.list[i]), '\\', '/');
      tb.add(node);
      if(!endsWith(node, '/')) tb.add('/');
    }
    final byte[] node = tb.finish();
    return endsWith(node, '/') ? substring(node, 0, node.length - 1) : node;
  }

  /**
   * Returns the mountpoint of a file hierarchy.
   * @param pre pre value
   * @return path mountpoint.
   */
  public byte[] mountpoint(final int pre) {
    return attr(pre, data.fs.mountpointID);
  }
  
  /**
   * Returns the name of a file.
   * @param pre pre value
   * @return file name.
   */
  public byte[] name(final int pre) {
    return attr(pre, data.nameID);
  }
  
  /**
   * Returns the size of a file.
   * @param pre pre value
   * @return file size
   */
  public byte[] size(final int pre) {
    return attr(pre, data.sizeID);
  }

  /**
   * Returns a file attribute.
   * @param pre pre value
   * @param at the attribute to be found
   * @return attribute or empty token.
   */
  private byte[] attr(final int pre, final int at) {
    final byte[] att = data.attValue(at, pre);
    return att != null ? att : EMPTY;
  }

  /**
   * Opens the file which is defined by the specified pre value.
   * @param pre pre value
   */
  public void launch(final int pre) {
    if(pre == -1 || !isFile(pre)) return;

    final String path = string(path(pre));
    try {
      final Runtime run = Runtime.getRuntime();
      if(Prop.MAC) {
        run.exec(new String[] { "open", path });
        System.err.println("open " + path);
      } else if(Prop.UNIX) {
        run.exec(new String[] { "xdg-open", path });
      } else {
        run.exec("rundll32.exe url.dll,FileProtocolHandler " + path);
      }
    } catch(final IOException ex) {
      BaseX.debug(ex);
      ex.printStackTrace();
    }
  }

  /* ------------------------------------------------------------------------ 
   *  FUSE utility methods.
   * ------------------------------------------------------------------------ */
  /**
   * Processes the query string and print result.
   * @param query to process
   * @return result reference
   * @throws QueryException on failure
   */
  private Nodes xquery(final String query) throws QueryException {
    BaseX.err("[basex_xquery] execute: " + query + "\n");
    return new QueryProcessor(query, new Nodes(0, data)).queryNodes();
  }
  
  /**
   * Converts a pathname to a DeepFS XPath expression. FUSE always passes on
   * 'absolute, normalized' pathnames, i.e., starting with a slash, redundant
   * and trailing slashes removed.
   * @param path name
   * @param dir toggle flag
   * @return query
   */
  private String pn2xp(final String path, final boolean dir) {
    final StringBuilder qb = new StringBuilder();
    final StringBuilder eb = new StringBuilder();
    qb.append("/deepfs");
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
    if(eb.length() != 0) if(dir) qb.append("dir[@name = \"" + eb + "\"]");
    else qb.append("*[@name = \"" + eb + "\"]");

    String qu = qb.toString();
    qu = qu.endsWith("/") ? qu.substring(0, qu.length() - 1) : qu;

    return qu;
  }
  
  /**
   * Constructs a MemData object containing <dir name="dirname"/> ... 
   * ready to be inserted into main Data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildData(final String path, final int mode) {
    final String dname = basename(path);
    int elemID = isDirFile(mode) ? data.fs.dirID : data.fs.unknownID;
    MemData m = new MemData(4, data.tags, data.atts, data.ns, data.path);
    m.addElem(elemID, 0, 1, 4, 4, false);
    m.addAtt(data.nameID, 0, token(dname), 1);
    m.addAtt(data.sizeID, 0, ZERO, 2);
    m.addAtt(data.fs.modeID, 0, token(Integer.toOctalString(mode)), 3);
    return m;
  }

  /**
   * Constructs a MemData object containing <file name="filename" .../> 
   * element ready to be inserted into main data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildFileData(final String path, final int mode) {
    final String fname = basename(path);
    MemData m = new MemData(6, data.tags, data.atts, data.ns, data.path);
    m.addElem(data.fs.fileID, 0, 1, 6, 6, false);
    m.addAtt(data.nameID,   0, token(fname), 1);
    final int dot = fname.lastIndexOf('.');
    m.addAtt(data.fs.suffID, 0, lc(token(fname.substring(dot + 1))), 2);
    m.addAtt(data.sizeID,   0, ZERO, 3);
    m.addAtt(data.fs.timeID,  0, ZERO, 4);
    m.addAtt(data.fs.modeID,  0, token(Integer.toOctalString(mode)), 5);
    return m;
  }
  
  /**
   * Evaluates given path and returns the pre value of the parent directory (if
   * any).
   * @param path to be analyzed
   * @return pre value of parent directory or -1 if none is found
   */
  private int parentPre(final String path) {
    try {
      Nodes n = xquery(pn2xp(dirname(path), true));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }
  
  /**
   * Inserts a file node (regular file, directory ...).
   * @param path of file to insert
   * @param mode of file
   * @return pre value of newly inserted node
   */
  private int insertFileNode(final String path, final int mode) {
    int ppre = parentPre(path);
    if(ppre == -1) return -1;
    if (isRegFile(mode))
      return insert(ppre, buildFileData(path, mode));
    else
      return insert(ppre, buildData(path, mode));
  }
  
  /**
   * Inserts MemData at given pre position and refresh GUI.
   * @param pre value at which to insert (content or file)
   * @param md memory data insert to insert
   * @return pre value of newly inserted node
   */
  private int insert(final int pre, final MemData md) {
    int npre = pre + data.size(pre, data.kind(pre));
    data.insert(npre, pre, md);
    refresh();
    return npre;
  }
  
  /**
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    data.meta.update();
    data.flush();
//    if (Prop.gui) gui.notify.update();
  }
  
  /**
   * Creates a new regular file or directory node.
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  private int createNode(final String path, final int mode) {
    int pre = insertFileNode(path, mode);
    return (pre == -1) ? -1 : data.id(pre);
  }
  /* ------------------------------------------------------------------------ */

  /* ------------------------------------------------------------------------ 
   *  FUSE callbacks.
   * ------------------------------------------------------------------------ */
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
  public int chown(final String path, final int uid, final int gid) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int create(final String path, final int mode) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int destroy() {
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
  public int fsgui() {
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
  public int getattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getxattr(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int init() {
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
    BaseX.err("[basex_mkdir] path: " + path + " mode: " 
        + Integer.toOctalString(mode) + "\n");
    //if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR. 
    return createNode(path, S_IFDIR | mode);
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
  public String readdir(final String path, final int offset) {
    // TODO Auto-generated method stub
    return null;
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
  public int unlink(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int utimens(final String path) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int write(final String path, final int length
      , final int offset, final byte[] databuf) {
    // TODO Auto-generated method stub
    return 0;
  }
}
