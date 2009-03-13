package org.basex.fuse;

import static org.basex.util.Token.*;

import java.io.IOException;

import org.basex.BaseXWin;
import org.basex.build.Builder;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.fs.FSParser;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.gui.GUI;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.util.Performance;

/**
 * Stores a file hierarchy as XML.
 * 
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 */
public final class DeepBase extends DeepFuse implements DataText {

  /** GUI reference. */
  protected GUI gui;

  /** GUI flag. */
  private boolean wgui;

  /** Database reference. */
  private Data data;

  /** Database name attribute. */
  private String name;

  /** Database instance name. */
  private String dbname;

  /** Path to backing store. */
  private String backingstore;

  /** Path to mount point. */
  private String mountpoint;

  /**
   * Constructor.
   * 
   * @param path absolute path
   * @param mountPoint mount point of DeepFUSE
   * @param backingStore backing storage root path
   * @param dbName of the database to open/create
   * @param guitoggle whether BaseXWin is wanted or not
   */
  public DeepBase(final String path, final String mountPoint,
      final String backingStore, final String dbName, final boolean guitoggle) {
    name = path;
    mountpoint = mountPoint;
    dbname = dbName;
    backingstore = backingStore;
    wgui = guitoggle;

    init();

    if(wgui) {
      final BaseXWin win = new BaseXWin(new String[] {});
      while(win.gui == null)
        Performance.sleep(100);
      gui = win.gui;
      gui.context.data(data);
      gui.notify.init();
    }
  }

  /**
   * Creates an empty database.
   * @param n name of database instance
   */
  private void createEmptyDB(final String n) {
    try {
      Prop.read();
      Context ctx = new Context();
      final Parser p = new Parser(IO.get(n)) {
        @Override
        public void parse(final Builder build) { /* empty */}
      };
      ctx.data(CreateDB.xml(p, n));
      data = ctx.data();

      // initialize tags and attribute names
      data.tags.index(DEEPFS, null, false);
      data.tags.index(DIR, null, false);
      data.tags.index(FILE, null, false);
      data.tags.index(UNKNOWN, null, false);

      data.atts.index(NAME, null, false);
      data.atts.index(SIZE, null, false);
      data.atts.index(MTIME, null, false);
      data.atts.index(MODE, null, false);
      data.atts.index(SUFFIX, null, false);
      data.initNames();
      
    } catch(IOException e) {
      e.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Converts a pathname to an DeepFS XPath expression. FUSE always passes
   * 'absolute, normalized' pathnames, i.e., starting with a slash, redundant
   * and trailing slashes removed.
   * @param path name
   * @param dir toogle flag
   * @return xpath query
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
   * Process the query string and print result.
   * @param query to process
   * @return result reference
   * @throws QueryException on failure
   */
  private Nodes xquery(final String query) throws QueryException {
    return new QueryProcessor(query).queryNodes(new Nodes(0, data));
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
   * Evaluates given path and returns the pre value.
   * 
   * @param path to be traversed
   * @return pre value of file node or -1 if none is found
   */
  private int pathPre(final String path) {
    try {
      Nodes n = xquery(pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Create a new regular file or directory node.
   * 
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  private int createNode(final String path, final int mode) {
    int pre = insertFileNode(path, mode);
    return (pre == -1) ? -1 : data.id(pre);
  }

  /**
   * Construct a MemData object containing <dir name="dirname"/> ... 
   * ready to be inserted into main Data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildData(final String path, final int mode) {
    final String dname = basename(path);
    int elemID = isDir(mode) ? data.fs.dirID : data.fs.unknownID;
    MemData m = new MemData(4, data.tags, data.atts, data.ns, data.path);
    m.addElem(elemID, 0, 1, 4, 4, false);
    m.addAtt(data.nameID, 0, token(dname), 1);
    m.addAtt(data.sizeID, 0, ZERO, 2);
    m.addAtt(data.fs.modeID, 0, token(Integer.toOctalString(mode)), 3);
    return m;
  }

  /**
   * Construct a MemData object containing <file name="filename" .../> 
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
   * Extract content of file and build a MemData object.
   * @param path from which to include content (it's in backing store).
   * @return MemData reference
   */
  private MemData buildContentData(final String path) {
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
   * Insert a file node (regular file, directory ...).
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
   * Insert extracted file content.
   * @param path to file at which to insert the extracted content
   * @return pre value of newly inserted content, -1 on failure
   */
  private int insertContent(final String path) {
    int fpre = pathPre(path);
    if(fpre == -1) return -1;
    return insert(fpre, buildContentData(path));
  }

  /**
   * Insert MemData at given pre position and refresh GUI.
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
   * Deletes a file node.
   * @param path of file to delete
   * @param dir is directory
   * @param cont delete only content of file
   * @return zero on success, -1 on failure
   */
  private int delete(final String path, final boolean dir, final boolean cont) {
    try {
      final StringBuilder qb = new StringBuilder();
      qb.append(pn2xp(path, dir));
      if(!dir && cont) qb.append("/content");
      Nodes n = xquery(qb.toString());
      if(n.size() == 0) return -1;
      else {
        data.delete(n.nodes[0]);
        refresh();
      }
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    data.meta.update();
    data.flush();
    if (wgui) gui.notify.update();
  }

  /**
   * Return data reference.
   * @return data
   */
  public Data getData() {
    return data;
  }

  /**
   * Set data reference.
   * @param d data reference
   */
  public void setData(final Data d) {
    data = d;
  }

  /**
   * Insert DeepFS root element.
   * @return zero on success
   */
  @Override
  public int init() {
    createEmptyDB(dbname);
    MemData m = new MemData(3, data.tags, data.atts, data.ns, data.path);
    int tagID = data.tags.index(DEEPFS, null, false);
    int attID2 = data.atts.index(MOUNTPOINT, null, false);
    int attID3 = data.atts.index(BACKINGSTORE, null, false);
    // tag, namespace, dist, # atts (+ 1), node size (+ 1), has namespaces
    m.addElem(tagID, 0, 1, 4, 4, false);
    m.addAtt(data.nameID, 0, token(name), 1);
    m.addAtt(attID2, 0, token(mountpoint), 2);
    m.addAtt(attID3, 0, token(backingstore), 3);
    data.insert(1, 0, m);
    data.flush();
    data.meta.update();

    return 0;
  }

  @Override
  public int destroy() {
    try {
      if (wgui) gui.dispose();
      data.close();
      return 0;
    } catch(IOException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Create a new directory.
   * 
   * @param path to directory to be created
   * @param mode of directory
   * @return id of the newly created directory or -1 on failure
   */
  @Override
  public int mkdir(final String path, final int mode) {
    //if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR. 
    return createNode(path, S_IFDIR | mode);
  }

  /**
   * Create a new regular file.
   * 
   * @param path to the file to be created
   * @param mode of regular file
   * @return id of the newly created file or -1 on failure
   */
  @Override
  public int create(final String path, final int mode) {
    //if(!isFile(mode)) return -1; // Linux does not submit S_IFREG.
    return createNode(path, S_IFREG | mode);
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
      Nodes n = xquery(pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int unlink(final String path) {

    return delete(path, false, false);
  }

  @Override
  public int opendir(final String path) {
    try {
      String query = "count(" + pn2xp(path, true) + "/child::*)";
      QueryProcessor xquery = new QueryProcessor(query);
      Result result = xquery.query(new Nodes(0, data));
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
    return delete(path, true, false);
  }

  @Override
  public String readdir(final String path, final int offset) {
    try {
      String query = "string(" + pn2xp(path, true) + "/child::*[" + offset
          + "]/@name)";
      QueryProcessor xquery = new QueryProcessor(query);
      SeqIter s = (SeqIter) xquery.query(new Nodes(0, data));
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
      delete(path, false, true);
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
