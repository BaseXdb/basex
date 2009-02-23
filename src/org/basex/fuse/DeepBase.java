package org.basex.fuse;

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
import org.basex.util.Token;

/**
 * Stores a file hierarchy as XML.
 * 
 * @author Workgroup DBIS, University of Konstanz 2008, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 */
public final class DeepBase extends DeepFuse {

  /** Default database name, if none is provided. */
  private static final String DEFAULT_DBNAME = "deepfuse";
  
  /** GUI reference. */
  protected GUI gui;

  /** Database reference. */
  private Data data;

  /** Database instance name. */
  private String dbname;

  /** Path to backing store. */
  private String backingstore;

  /** Path to mount point. */
  private String mountpoint;

  /** Constructor. */
  public DeepBase() {
    this("unknown", "unknown", DEFAULT_DBNAME);
  }

  /**
   * Constructor.
   * 
   * @param mountPoint mount point of DeepFUSE
   * @param backingStore backing storage root path
   * @param dbName of the database to open/create
   */
  public DeepBase(final String mountPoint, final String backingStore,
      final String dbName) {
    mountpoint = mountPoint;
    dbname = dbName;
    backingstore = backingStore;
  
    final BaseXWin win = new BaseXWin(new String[] {});
    init();
  
    while(win.gui == null)
      Performance.sleep(100);
    gui = win.gui;
    gui.context.data(data);
    gui.notify.init();
  }

  /**
   * Creates an empty database.
   * @param name of database instance
   */
  private void createEmptyDB(final String name) {
    try {
      Prop.read();
      Context ctx = new Context();
      final Parser p = new Parser(IO.get(name)) {
        @Override
        public void parse(final Builder build) { /* empty */}
      };
      ctx.data(CreateDB.xml(p, name));
      data = ctx.data();
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
   * @throws QueryException on failure
   */
  private int parentPre(final String path) throws QueryException {
    Nodes n = xquery(pn2xp(dirname(path), true));
    return n.size() == 0 ? -1 : n.nodes[0];
  }

  /**
   * Create a new regular file or directory node.
   * 
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  private int createNode(final String path, final int mode) {
    try {
      int pre = insertFileNode(path, mode);
      return (pre == -1) ? -1 : data.id(pre);
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  /**
   * Construct a MemData object containing <file name="filename"/> <dir
   * name="dirname"/> ready to be inserted into main Data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildData(final String path, final int mode) {
    final String dname = basename(path);
    byte[] elem;
    if(isDir(mode)) elem = DIR;
    else if(isFile(mode)) elem = FILE;
    else elem = Token.token("unknown");
    MemData m = new MemData(2, data.tags, data.atts, data.ns, data.skel);
    int tagID = data.tags.index(elem, null, false);
    int attID = data.atts.index(NAME, null, false);
    m.addElem(tagID, 0, 1, 2, 2, false);
    m.addAtt(attID, 0, Token.token(dname), 1);
    return m;
  }

  /**
   * Extract content of file and build a MemData object.
   * @param path from which to include content (it's in backing store).
   * @return MemData reference
   */
  private MemData buildContentData(final String path) {
    MemData md = new MemData(64, data.tags, data.atts, data.ns, data.skel);
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
   * @throws QueryException on failure
   * @return pre value of newly inserted node
   */
  private int insertFileNode(final String path, final int mode)
      throws QueryException {
    return insert(path, buildData(path, mode));
  }

  /**
   * Insert extracted file content.
   * @param path to file at which to insert the extracted content
   * @throws QueryException on failure
   * @return pre value of newly inserted content, -1 on failure
   */
  private int insertContent(final String path) throws QueryException {
    return insert(path, buildContentData(path));
  }

  /**
   * Insert MemData at given path position.
   * @param path at which to insert (content or file)
   * @param md memory data insert to insert
   * @return pre value of newly inserted node
   * @throws QueryException in case of failure
   */
  private int insert(final String path, final MemData md) 
    throws QueryException {
    int ppre = parentPre(path);
    if(ppre == -1) return -1;
    int npre = ppre + data.size(ppre, data.kind(ppre));
    data.insert(npre, ppre, md);
    refresh();
    return npre;
  }

  /**
   * Deletes a file node.
   * @param path of file to delete
   * @param dir is directory
   * @throws QueryException on failure
   * @return zero on success, -1 on failure
   */
  private int delete(final String path, final boolean dir)
      throws QueryException {
    Nodes n = xquery(pn2xp(path, dir));
    if(n.size() == 0) return -1;
    else {
      data.delete(n.nodes[0]);
      refresh();
    }
    return 0;
  }

  /**
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    data.meta.update();
    data.flush();
    gui.notify.update();
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
    MemData m = new MemData(3, data.tags, data.atts, data.ns, data.skel);
    int tagID = data.tags.index(Token.token("deepfuse"), null, false);
    int attID1 = data.atts.index(Token.token("mountpoint"), null, false);
    int attID2 = data.atts.index(Token.token("backingstore"), null, false);
    // tag, namespace, dist, # atts (+ 1), node size (+ 1), has namespaces
    m.addElem(tagID, 0, 1, 3, 3, false);
    m.addAtt(attID1, 0, Token.token(mountpoint), 1);
    m.addAtt(attID2, 0, Token.token(backingstore), 2);
    data.insert(1, 0, m);
    data.flush();
    data.meta.update();
  
    return 0;
  }

  @Override
  public int destroy() {
    try {
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
    if(!isDir(mode)) return -1;
    return createNode(path, mode);
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
    if(!isFile(mode)) return -1;
    return createNode(path, mode);
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
    try {
      return delete(path, false);
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
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
    /* TODO: rmdir deletes only empty dir. What happens with --ignore? */
    try {
      return delete(path, true);
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
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
    // TODO Auto-generated method stub
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
  public int chown(final String path, final int uid, final int gid) {
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
  public byte[] read(final String path, final int length, final int offset) {
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
    try {
      return insertContent(path);
    } catch(QueryException e) {
      e.printStackTrace();
      return -1;
    }
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
      final byte[] mydata) {
    // TODO Auto-generated method stub
    return 0;
  }
}
