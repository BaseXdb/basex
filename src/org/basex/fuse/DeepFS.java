package org.basex.fuse;

import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.fs.FSParser;
import org.basex.build.fs.NewFSParser;
import org.basex.build.fs.FSText;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.SeqIter;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * DeepFS: The XQuery Filesystem. Database-side implementation of DeepFS.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, alex@holupirek.de
 * @author Christian Gruen, Hannes Schwarz
 */
public final class DeepFS extends DeepFuse implements DataText {
  /* Data reference - [AH] temporarily removed.. */
  // public GUI gui;
  /** Data reference. */
  public Data data;
  /** Index References. */
  public int deepfsID;
  /** Index References. */
  public int fileID;
  /** Index References. */
  public int dirID;
  /** Index References. */
  public int suffixID;
  /** Index References. */
  public int mtimeID;
  /** Index References. */
  public int modeID;
  /** Index References. */
  public int unknownID;
  /** Index mount point. */
  public int mountpointID;
  /** Index backing store. */
  public int backingstoreID;
  /** Index References. */
  public int sizeID;
  /** Index References. */
  public int nameID;
  /** Index References. */
  public int contentID;
  /** Context. */
  private Context context = new Context();

  /*
   * ------------------------------------------------------------------------
   * Native deepfs method declarations (org_basex_fuse_DeepFS.h)
   * ------------------------------------------------------------------------
   */

  /**
   * Mount database as FUSE.
   * @param mp path where to mount BaseX.
   * @param bs path to backing storage root of this instance.
   * @return 0 on success, errno in case of failure.
   */
  public native int nativeMount(final String mp, final String bs);

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
   * Initialize often used tags and attributes.
   */
  private void initNames() {
    // initialize tags and attribute names
    deepfsID       = data.tags.index(DEEPFS,       null, false);
    dirID          = data.tags.index(DIR,          null, false);
    fileID         = data.tags.index(FILE,         null, false);
    unknownID      = data.tags.index(UNKNOWN,      null, false);
    contentID      = data.tags.index(CONTENT,      null, false);
    backingstoreID = data.atts.index(BACKINGSTORE, null, false);
    mountpointID   = data.atts.index(MOUNTPOINT,   null, false);
    nameID         = data.atts.index(NAME,         null, false);
    sizeID         = data.atts.index(SIZE,         null, false);
    mtimeID        = data.atts.index(MTIME,        null, false);
    modeID         = data.atts.index(MODE,         null, false);
    suffixID       = data.atts.index(SUFFIX,       null, false);
  }

  /**
   * Constructor.
   * @param d data reference
   */
  public DeepFS(final Data d) {
    data = d;

    final String mountpoint = d.meta.mountpoint;
    final String backingpath = d.meta.backingpath;
    initNames();

    if(Prop.fuse) {
      final File mp = new File(mountpoint);
      final File bp = new File(backingpath);
      /* --- prepare, (ie., potentially make) mountpoint & backing store --- */
      // - mountpoint
      if(!mp.exists()) {
        if(!mp.mkdirs()) {
          BaseX.debug(FSText.NOMOUNTPOINT + mp.toString());
          return;
        }
      }
      // - backing store
      if(!bp.exists()) {
        if(!bp.mkdirs()) {
          BaseX.debug(FSText.NOBACKINGPATH + bp.toString());
          return;
        }
      }
      nativeMount(mountpoint, backingpath);
    }
  }

  /**
   * Constructor for {@link DeepShell} and java only test cases (no mount).
   * @param name name of initially empty database.
   * @param ctx database context
   */
  public DeepFS(final Context ctx, final String name) {
    data = createEmptyDB(ctx, name);
    initNames();
    final MemData m = new MemData(3, data.tags, data.atts, data.ns, data.path);
    final int tagID = data.tags.index(DEEPFS, null, false);
    // tag, namespace, dist, # atts (+ 1), node size (+ 1), has namespaces
    m.addElem(tagID, 0, 1, 3, 3, false);
    m.addAtt(mountpointID, 0, NOTMOUNTED, 2);
    m.addAtt(backingstoreID, 0, NOBACKING, 3);
    data.insert(1, 0, m);
    data.flush();
  }

  /**
   * Creates an empty database.
   * @param n name of database instance
   * @param ctx database context
   * @return data reference to empty database
   */
  private Data createEmptyDB(final Context ctx, final String n) {
    try {
      final Data d = CreateDB.xml(ctx, Parser.emptyParser(IO.get(n)), n);
      ctx.openDB(d);
      d.fs = this;
      return d;
    } catch(final IOException e) {
      e.printStackTrace();
    } catch(final Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Closes the fuse instance.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    if(Prop.fuse) {
      final String method = "[BaseX.close] ";
      BaseX.debug(method + "Initiating DeepFS shutdown sequence ");
      // -- unmount running fuse.
      for(int i = 3; i > 0; i--) {
        Performance.sleep(1000);
        BaseX.err(i + " .. ");
      }
      BaseX.debug("GO.");
      final String cmd = "umount -f " + data.meta.mountpoint;
      BaseX.errln(method + "Trying to unmount deepfs: " + cmd);
      final Runtime r = Runtime.getRuntime();
      final java.lang.Process p = r.exec(cmd);
      try {
        p.waitFor();
      } catch(final InterruptedException e) {
        e.printStackTrace();
      }
      final int rc = p.exitValue();
      String msg = method + "Unmount " + data.meta.mountpoint;
      if(rc == 0) msg = msg + " ... OK.";
      else msg = msg + " ... FAILED(" + rc + ") (Please unmount manually)";
      BaseX.debug(msg);
    }
  }

  /**
   * Checks if the specified node is a file.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isFile(final int pre) {
    return data.kind(pre) == Data.ELEM
        && data.tagID(pre) == data.tags.id(DataText.FILE);
  }

  /**
   * Checks if the specified node is a directory.
   * @param pre pre value
   * @return result of comparison
   */
  public boolean isDir(final int pre) {
    return data.kind(pre) == Data.ELEM
        && data.tagID(pre) == data.tags.id(DataText.DIR);
  }

  /**
   * Returns the absolute file path.
   * @param pre pre value
   * @param backing whether to return backing path or mountpath
   * @return file path.
   */
  public byte[] path(final int pre, final boolean backing) {
    int p = pre;
    int k = data.kind(p);
    final IntList il = new IntList();
    while(p >= 0 && k != Data.DOC) {
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    final int s = il.size();
    if(s != 0) {
      final byte[] b = Prop.fuse && !backing ? mountpoint(il.get(s - 1)) :
        backingstore(il.get(s - 1));
      if(b.length != 0) {
        tb.add(b);
        if(!endsWith(b, '/')) tb.add('/');
      }
    }
    for(int i = s - 2; i >= 0; i--) {
      final byte[] node = replace(name(il.get(i)), '\\', '/');
      tb.add(node);
      if(!endsWith(node, '/')) tb.add('/');
    }
    final byte[] node = tb.finish();
    return endsWith(node, '/') ? substring(node, 0, node.length - 1) : node;
  }

  /**
   * Returns mountpoint attribute value.
   * @param pre pre value
   * @return mountpoint value.
   */
  private byte[] mountpoint(final int pre) {
    return attr(pre, data.fs.mountpointID);
  }

  /**
   * Returns backing store attribute value.
   * @param pre pre value
   * @return path mountpoint.
   */
  private byte[] backingstore(final int pre) {
    return attr(pre, data.fs.backingstoreID);
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

    final String path = string(path(pre, false));
    try {
      final Runtime run = Runtime.getRuntime();
      if(Prop.MAC) {
        run.exec(new String[] { "open", path});
      } else if(Prop.UNIX) {
        run.exec(new String[] { "xdg-open", path});
      } else {
        run.exec("rundll32.exe url.dll,FileProtocolHandler " + path);
      }
    } catch(final IOException ex) {
      BaseX.debug(ex);
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a non-empty directory.
   * @param pre pre value
   */
  public void delete(final int pre) {
    if(Prop.fuse) {
      final String bpath = Token.string(path(pre, true));
      final File f = new File(bpath);
      if(f.isDirectory()) deleteDir(f);
      else if(f.isFile()) f.delete();
      nativeUnlink(Token.string(path(pre, false)));
    }
  }

  /*
   * ------------------------------------------------------------------------
   * FUSE utility methods.
   * ------------------------------------------------------------------------
   */

  /**
   * Processes the query string and print result.
   * @param query to process
   * @return result reference
   * @throws QueryException on failure
   */
  Nodes xquery(final String query) throws QueryException {
    return new QueryProcessor(query, context).queryNodes();
  }

  /**
   * Converts a pathname to a DeepFS XPath expression. FUSE always passes on
   * 'absolute, normalized' pathnames, i.e., starting with a slash, redundant
   * and trailing slashes removed.
   * @param path name
   * @param dir toggle flag
   * @return query
   */
  String pn2xp(final String path, final boolean dir) {
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
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    // [AH] temporarily removed..
    // if(gui != null) gui.notify.update();
    // data.flush();
  }

  /**
   * Deletes a file node.
   * @param path of file to delete
   * @param dir is directory
   * @param cont delete only content of file
   * @return zero on success, -1 on failure
   */
  int delete(final String path, final boolean dir, final boolean cont) {
    try {
      final StringBuilder qb = new StringBuilder();
      qb.append(pn2xp(path, dir));
      if(!dir && cont) qb.append("/content");
      final Nodes n = xquery(qb.toString());
      if(n.size() == 0) return -1;
      data.delete(n.nodes[0]);
      refresh();
    } catch(final QueryException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Deletes a non-empty directory.
   * @param dir to be deleted.
   * @return boolean true for success, false for failure.
   */
  private static boolean deleteDir(final File dir) {
    if(dir.isDirectory()) {
      for(final File ch : dir.listFiles()) if(!deleteDir(ch)) return false;
    }
    return dir.delete();
  }

  /**
   * Inserts extracted file content.
   * @param path to file at which to insert the extracted content
   * @return pre value of newly inserted content, -1 on failure
   */
  int insertContent(final String path) {
    final int fpre = pathPre(path);
    return fpre == -1 ? -1 : insert(fpre, buildContentData(path));
  }

  /**
   * Inserts MemData at given pre position and refresh GUI.
   * @param pre value at which to insert (content or file)
   * @param md memory data insert to insert
   * @return pre value of newly inserted node
   */
  private int insert(final int pre, final MemData md) {
    final int npre = pre + data.size(pre, data.kind(pre));
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
      final Nodes n = xquery(pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(final QueryException e) {
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
    final MemData md = new MemData(64, data.tags, data.atts, data.ns,
        data.path);

    try {
      final MemBuilder mb = new MemBuilder();
      mb.init(md);
      Prop.fscont = true;
      Prop.fsmeta = true;
      final String bpath = data.meta.backingpath + path;
      BaseX.debug("[DataFS_parse_file] path : " + path + " -> " + bpath);
      final Parser p = Prop.newfsparser ? new NewFSParser(bpath)
          : new FSParser(bpath);
      return (MemData) mb.build(p, "tmp_memdata4file");
    } catch(final IOException e) {
      e.printStackTrace();
    }
    return md;
  }

  /**
   * Constructs a MemData object containing <dir name="dirname"/> ... ready to
   * be inserted into main Data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildData(final String path, final int mode) {
    final String dname = basename(path);
    final int elemID = isDirFile(mode) ? dirID : unknownID;
    final MemData m = new MemData(4, data.tags, data.atts, data.ns, data.path);
    m.addElem(elemID, 0, 1, 4, 4, false);
    m.addAtt(nameID, 0, token(dname), 1);
    m.addAtt(sizeID, 0, ZERO, 2);
    m.addAtt(modeID, 0, token(Integer.toOctalString(mode)), 3);
    return m;
  }

  /**
   * Constructs a MemData object containing <file name="filename" .../> element
   * ready to be inserted into main data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildFileData(final String path, final int mode) {
    final String fname = basename(path);
    final MemData m = new MemData(6, data.tags, data.atts, data.ns, data.path);
    m.addElem(fileID, 0, 1, 6, 6, false);
    m.addAtt(nameID, 0, token(fname), 1);
    final int dot = fname.lastIndexOf('.');
    m.addAtt(suffixID, 0, lc(token(fname.substring(dot + 1))), 2);
    m.addAtt(sizeID, 0, ZERO, 3);
    m.addAtt(mtimeID, 0, ZERO, 4);
    m.addAtt(modeID, 0, token(Integer.toOctalString(mode)), 5);
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
      final Nodes n = xquery(pn2xp(dirname(path), true));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(final QueryException e) {
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
    final int ppre = parentPre(path);
    if(ppre == -1) return -1;
    return insert(ppre, isRegFile(mode) ? buildFileData(path, mode) :
      buildData(path, mode));
  }

  /**
   * Creates a new regular file or directory node.
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  int createNode(final String path, final int mode) {
    final int pre = insertFileNode(path, mode);
    return pre == -1 ? -1 : data.id(pre);
  }

  @Override
  public int destroy() {
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
    // if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR.
    final String method = "[-basex_mkdir] ";
    final int n = createNode(path, S_IFDIR | mode);
    BaseX.debug(method + "path: " + path + " mode: "
        + Integer.toOctalString(mode) + " id : (" + n + ")");
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
    // if(!isFile(mode)) return -1; // Linux does not submit S_IFREG.
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
      final Nodes n = xquery(pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(final QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int unlink(final String path) {
    final int n = delete(path, false, false);
    refresh();
    return n;
  }

  @Override
  public int opendir(final String path) {
    try {
      final String query = "count(" + pn2xp(path, true) + "/child::*)";
      final QueryProcessor xq = new QueryProcessor(query, context);
      final Result result = xq.query();
      final SeqIter s = (SeqIter) result;
      final Item i = s.next();
      return i != null ? (int) i.itr() : -1;
    } catch(final QueryException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int rmdir(final String path) {
    /* [AH] rmdir(2) deletes only empty dir. What happens with --ignore? */
    final String method = "[-basex_rmdir] ";
    BaseX.debug(method + "path: " + path);
    final int n = delete(path, true, false);
    refresh();
    return n;
  }

  @Override
  public String readdir(final String path, final int offset) {
    try {
      final String query = "string(" + pn2xp(path, true) + "/child::*[" + offset
          + "]/@name)";
      final QueryProcessor xq = new QueryProcessor(query, context);
      final SeqIter s = (SeqIter) xq.query();
      return s.size() != 1 ? null : string(s.next().str());
    } catch(final QueryException e) {
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
    final boolean dirty = true;

    final String method = "[-basex_release] ";
    BaseX.debug(method + "path: " + path);

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
