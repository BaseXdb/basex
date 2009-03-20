package org.basex.fuse;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.fs.FSParser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.gui.GUI;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;

/**
 * Stores a file hierarchy as XML.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 */
public final class DeepBase extends DeepFuse implements DataText {

  /** GUI reference. */
  protected GUI gui;

  /** Database reference. */
  public DataFS datafs;

  /** Database instance name. */
  String dbname;

  /** Path to backing store. */
  String backingstore;

  /** Path to mount point. */
  String mountpoint;

  /**
   * Constructor.
   * @param mountPoint mount point of DeepFUSE
   * @param backingStore backing storage root path
   * @param dbName of the database to open/create
   */
  public DeepBase(final String mountPoint, final String backingStore,
      final String dbName) {
    mountpoint = mountPoint;
    dbname = dbName;
    backingstore = backingStore;
  }

  /**
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    gui.notify.update();
  }

  /**
   * Inserts extracted file content.
   * @param path to file at which to insert the extracted content
   * @return pre value of newly inserted content, -1 on failure
   */
  int insertContent(final String path) {
    int fpre = pathPre(path);
    if(fpre == -1) return -1;
    return insert(fpre, buildContentData(path));
  }

  /**
   * Inserts MemData at given pre position and refresh GUI.
   * @param pre value at which to insert (content or file)
   * @param md memory data insert to insert
   * @return pre value of newly inserted node
   */
  private int insert(final int pre, final MemData md) {
    final Data data = datafs.data;
    int npre = pre + data.size(pre, data.kind(pre));
    data.insert(npre, pre, md);
    refresh();
    return npre;
  }

  /**
   * Evaluates given path and returns the pre value.
   * @param path to be traversed
   * @return pre value of file node or -1 if none is found
   */
  private int pathPre(final String path) {
    try {
      Nodes n = datafs.xquery(datafs.pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }
  
  /**
   * Extracts content of file and build a MemData object.
   * @param path from which to include content (it's in backing store).
   * @return MemData reference
   */
  private MemData buildContentData(final String path) {
    final Data data = datafs.data;
    MemData md = new MemData(64, data.tags, data.atts, data.ns, data.path);
    try {
      MemBuilder mb = new MemBuilder();
      mb.init(md);
      Prop.fscont = true;
      Prop.fsmeta = true;
      FSParser p = new FSParser(backingstore + path);
      mb.build(p, "tmp_memdata4file");
      return mb.finish();
    } catch(IOException e) {
      e.printStackTrace();
    }
    return md;
  }
  
  /**
   * Constructs a MemData object containing <dir name="dirname"/> ... 
   * ready to be inserted into main Data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildData(final String path, final int mode) {
    final Data data = datafs.data;
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
    final Data data = datafs.data;
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
      Nodes n = datafs.xquery(datafs.pn2xp(dirname(path), true));
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
   * Creates a new regular file or directory node.
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  int createNode(final String path, final int mode) {
    int pre = insertFileNode(path, mode);
    return (pre == -1) ? -1 : datafs.data.id(pre);
  }

  
  
  @Override
  public int destroy() {
    gui.context.close();
    gui.notify.init();
    return 0;
  }

  /**
   * Creates a new directory.
   * @param path to directory to be created
   * @param mode of directory
   * @return id of the newly created directory or -1 on failure
   */
  @Override
  public int mkdir(final String path, final int mode) {
    //if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR. 
    final int n = createNode(path, S_IFDIR | mode);
    refresh();
    return n;
  }

  /**
   * Creates a new regular file.
   * @param path to the file to be created
   * @param mode of regular file
   * @return id of the newly created file or -1 on failure
   */
  @Override
  public int create(final String path, final int mode) {
    //if(!isFile(mode)) return -1; // Linux does not submit S_IFREG.
    final int n = createNode(path, S_IFREG | mode);
    refresh();
    return n;
  }

  /**
   * Resolves path and returns id. id serves as index into stat array in native
   * fuse implementation.
   * @param path to be resolved
   * @return id of node or -1 if not found
   */
  @Override
  public int getattr(final String path) {
    try {
      Nodes n = datafs.xquery(datafs.pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int unlink(final String path) {
    final int n = datafs.delete(path, false, false);
    refresh();
    return n;
  }

  @Override
  public int opendir(final String path) {
    try {
      String query = "count(" + datafs.pn2xp(path, true) + "/child::*)";
      QueryProcessor xq = new QueryProcessor(query, new Nodes(0, datafs.data));
      Result result = xq.query();
      SeqIter s = (SeqIter) result;
      Item i = s.next();
      return (i != null) ? (int) i.itr() : -1;
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int rmdir(final String path) {
    /* [AH] rmdir(2) deletes only empty dir. What happens with --ignore? */
    final int n = datafs.delete(path, true, false);
    refresh();
    return n;
  }

  @Override
  public String readdir(final String path, final int offset) {
    try {
      String query = "string(" + datafs.pn2xp(path, true) +
        "/child::*[" + offset + "]/@name)";
      QueryProcessor xq = new QueryProcessor(query, new Nodes(0, datafs.data));
      SeqIter s = (SeqIter) xq.query();
      if(s.size() != 1) return null;
      return (s.size() != 1) ? null : new String(((Str) s.next()).str());
    } catch(QueryException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public int rename(final String from, final String to) {
    return 0;
  }

  @Override
  public int access(final String path, final int mode) {
    
    return 0;
  }

  @Override
  public int bmap(final String path, final long blocksize, final long idx) {
    
    return 0;
  }

  @Override
  public int chmod(final String path, final int mode) {
    
    return 0;
  }

  @Override
  public int chown(final String path, final int uid, final int gid) {
    
    return 0;
  }

  @Override
  public int fgetattr(final String path) {
    
    return 0;
  }

  @Override
  public int flush(final String path) {
    
    return 0;
  }

  @Override
  public int fsync(final String path) {
    
    return 0;
  }

  @Override
  public int fsyncdir(final String path) {
    
    return 0;
  }

  @Override
  public int ftruncate(final String path, final long off) {
    
    return 0;
  }

  @Override
  public int getxattr(final String path) {
    
    return 0;
  }

  @Override
  public int link(final String name1, final String name2) {
    
    return 0;
  }

  @Override
  public int listxattr(final String path) {
    
    return 0;
  }

  @Override
  public int lock(final String path, final int cmd) {
    
    return 0;
  }

  @Override
  public int mknod(final String path, final int mode, final int dev) {
    
    return 0;
  }

  @Override
  public int open(final String path) {
    
    return 0;
  }

  @Override
  public byte[] read(final String path, final int length, final int offset) {
    
    return null;
  }

  @Override
  public String readlink(final String path) {
    
    return null;
  }

  @Override
  public int release(final String path) {
    boolean dirty = true;

    if(dirty) {
      datafs.delete(path, false, true);
      insertContent(path);
    }

    return 0;
  }

  @Override
  public int releasedir(final String path) {
    
    return 0;
  }

  @Override
  public int removexattr(final String path) {
    
    return 0;
  }

  @Override
  public int setxattr(final String path) {
    
    return 0;
  }

  @Override
  public int statfs(final String path) {
    
    return 0;
  }

  @Override
  public int symlink(final String from, final String to) {
    
    return 0;
  }

  @Override
  public int truncate(final String path, final long off) {
    
    return 0;
  }

  @Override
  public int utimens(final String path) {
    
    return 0;
  }

  @Override
  public int write(final String path, final int length, final int offset,
      final byte[] mydata) {
    
    return 0;
  }
}
