package org.deepfs.fs;

import static org.basex.util.Token.*;
import static org.deepfs.jfuse.JFUSEAdapter.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.CreateDB;
import org.basex.core.proc.Open;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.deepfs.DeepShell;
import org.deepfs.jfuse.DeepStat;

/**
 * DeepFS: The XQuery Filesystem. Database-side implementation of DeepFS.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Alexander Holupirek, Christian Gruen, Hannes Schwarz
 */
public final class DeepFS implements DataText {

  /** Context instance. */
  private Context ctx;
  /** Data instance. */
  private final Data data;

  /** Stat information for root node. */
  private DeepStat rootStat;

  /** Index References. */
  private int fileID;
  /** Index References. */
  private int dirID;
  /** Index References. */
  private int modeID;
  /** Index mount point. */
  private int mountpointID;
  /** Index backing store. */
  private int backingstoreID;
  /** Index References. */
  private int sizeID;
  /** Index References. */
  private int nameID;

  /** Index References. */
  public int suffixID;
  /** Index References. */
  public int mtimeID;
  /** Index References. */
  public int ctimeID;
  /** Index References. */
  public int atimeID;
  /** Index References. */
  public int nlinkID;
  /** Index References. */
  public int uidID;
  /** Index References. */
  public int gidID;
  /** Index References. */
  public int contentID;

  /** OS user id.*/
  private long sUID;
  /** OS group id.*/
  private long sGID;

  /**
   * Constructor for {@link DeepShell} and java only test cases (no mount).
   * @param dbname name of (initially empty) database.
   * @param mountpoint of DeepFS database
   */
  public DeepFS(final String dbname, final String mountpoint) {
    ctx = new Context();
    if(!new Open(dbname).execute(ctx))
      new CreateDB("<" + string(DEEPFS) + " " + "mountpoint=\""
          + mountpoint + "\"/>", dbname).execute(ctx);
    data = ctx.data();
    initNames();
    initRootStat();
  }

  /**
   * Constructor.
   * @param d data reference
   */
  public DeepFS(final Data d) {
    data = d;
    initNames();
    initRootStat();
  }

  /**
   * Constructor.
   * @param c existing context
   */
  public DeepFS(final Context c) {
    ctx = c;
    data = ctx.data();
    initNames();
    initRootStat();
  }

  /**
   * Initialize often used tags and attributes.
   */
  private void initNames() {
    // initialize tags and attribute names
    data.tags.index(DEEPFS,       null, false);
    dirID          = data.tags.index(DIR,          null, false);
    fileID         = data.tags.index(FILE,         null, false);
    contentID      = data.tags.index(CONTENT,      null, false);
    backingstoreID = data.atts.index(BACKINGSTORE, null, false);
    mountpointID   = data.atts.index(MOUNTPOINT,   null, false);
    nameID         = data.atts.index(NAME,         null, false);
    sizeID         = data.atts.index(SIZE,         null, false);
    mtimeID        = data.atts.index(MTIME,        null, false);
    ctimeID        = data.atts.index(CTIME,        null, false);
    atimeID        = data.atts.index(ATIME,        null, false);
    nlinkID        = data.atts.index(NLINK,        null, false);
    uidID          = data.atts.index(UID,          null, false);
    gidID          = data.atts.index(GID,          null, false);
    modeID         = data.atts.index(MODE,         null, false);
    suffixID       = data.atts.index(SUFFIX,       null, false);
  }

  /**
   * Initialize default file attributes for root ('/') access.
   */
  private void initRootStat() {
    rootStat = new DeepStat();
    rootStat.statimespec = System.currentTimeMillis();
    rootStat.stctimespec = System.currentTimeMillis();
    rootStat.stmtimespec = System.currentTimeMillis();
    rootStat.stmode = getSIFDIR() | 0755;
    rootStat.stsize = 0;
    rootStat.stuid =  sUID;
    rootStat.stgid =  sGID;
    rootStat.stnlink =  0;
    rootStat.stino = 1;
  }

  /**
   * Processes the query string and print result.
   * @param query to process
   * @return result reference
   * @throws QueryException on failure
   */
  private Nodes xquery(final String query) throws QueryException {
    return new QueryProcessor(query, ctx).queryNodes();
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
    //qb.append(S_DPFSNS);
    qb.append("/" + S_DEEPFS);
    if(path.equals("/")) return qb.toString();

    for(int i = 0; i < path.length(); i++) {
      final char c = path.charAt(i);
      if(c == '/') {
        if(eb.length() != 0) {
          qb.append(S_DIR + "[@" + S_NAME + " = \"" + eb + "\"]");
          eb.setLength(0);
        }
        qb.append(c);
      } else {
        eb.append(c);
      }
    }
    if(eb.length() != 0)
      if(dir) qb.append(S_DIR + "[@" + S_NAME + " = \"" + eb + "\"]");
      else qb.append("*[@" + S_NAME + " = \"" + eb + "\"]");

    String qu = qb.toString();
    qu = qu.endsWith("/") ? qu.substring(0, qu.length() - 1) : qu;

    Main.debug("[pn2xp] " + qu);

    return qu;
  }

  /**
   * Resolves path and returns pre.
   * @param path to be resolved
   * @return id of node or -1 if not found
   */
  private int path2pre(final String path) {
    try {
      final Nodes n = xquery(pn2xp(path, false));
      return n.size() == 0 ? -1 : n.nodes[0];
    } catch(final QueryException ex) {
      ex.printStackTrace();
      return -1;
    }
  }


  /**
   * Resolve child axis from path and return pre values of children.
   * @param path to be resolved
   * @return pre values of children found
   */
  private int[] path2preChildren(final String path) {
    try {
      final Nodes n = xquery(pn2xp(path, true) + "/child::*");
      return n.nodes;
    } catch(final QueryException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Deletes any prefix ending with the last slash `/' character present in
   * string. FUSE always passes 'absolute, normalized' pathnames, i.e., starting
   * with a slash, redundant and trailing slashes removed.
   * @param path to extract filename
   * @return filename of path
   */
  private String basename(final String path) {
    return path.equals("/") ? path : path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * Deletes the filename portion, beginning with the last slash `/' character
   * to the end of string. FUSE always passes 'absolute, normalized' pathnames,
   * i.e., starting with a slash, redundant and trailing slashes removed.
   * Example:
   * <ul>
   * <li>dirname("/usr/bin/trail") returns "/usr/bin"</li>
   * <li>dirname("/") returns "/"</li>
   * </ul>
   * @param path to extract dirname
   * @return dirname of path
   */
  private String dirname(final String path) {
    final int s = path.lastIndexOf('/');
    return s > 0 ? path.substring(0, s) : "/";
  }

  /**
   * Construct file node as MemData object, ready to be inserted into main
   * data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildFileNode(final String path, final int mode) {
    final String fname = basename(path);
    final int nodeSize = 10; // 1x elem, 9x attr
    final MemData m = new MemData(nodeSize, data.tags, data.atts, data.ns,
        data.path, data.meta.prop);
    final int tagID = isReg(mode) ? fileID : dirID;
    final byte [] time = token(System.currentTimeMillis());
    m.addElem(tagID , 0, 1, nodeSize, nodeSize, false);
    m.addAtt(nameID , 0, token(fname), 1);
    m.addAtt(sizeID , 0, ZERO, 2);
    m.addAtt(modeID , 0, token(mode), 3);
    m.addAtt(uidID  , 0, token(sUID), 4);
    m.addAtt(gidID  , 0, token(sGID), 5);
    m.addAtt(atimeID, 0, time, 6);
    m.addAtt(mtimeID, 0, time, 7);
    m.addAtt(ctimeID, 0, time, 8);
    m.addAtt(nlinkID, 0, token("1"), 9);
    return m;
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
   * Returns a file attribute.
   * @param pre pre value
   * @param at the attribute id of the attribute to be found
   * @return attribute or empty token.
   */
  private byte[] attr(final int pre, final int at) {
    final byte[] att = data.attValue(at, pre);
    return att != null ? att : EMPTY;
  }

  /**
   * Refreshes the data reference and GUI.
   */
  private void refresh() {
    // [AH] gui reference temporarily removed..
    // if(gui != null) gui.notify.update();
    // data.flush();
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

  //  /**
  //   * Evaluates given path and returns the pre value.
  //   * @param path to be traversed
  //   * @return pre value of file node or -1 if none is found
  //   */
  //  private int pathPre(final String path) {
  //    try {
  //      final Nodes n = xquery(pn2xp(path, false));
  //      return n.size() == 0 ? -1 : n.nodes[0];
  //    } catch(final QueryException ex) {
  //      ex.printStackTrace();
  //      return -1;
  //    }
  //  }

  //  /**
  //   * Extracts content of file and build a MemData object.
  //   * @param path from which to include content (it's in backing store).
  //   * @return MemData reference
  //   */
  //  private MemData buildContentData(final String path) {
  //    final Prop prop = data.meta.prop;
  //    final MemData md = new MemData(64, data.tags, data.atts, data.ns,
  //        data.path, prop);
  //
  //    try {
  //      prop.set(Prop.FSCONT, true);
  //      prop.set(Prop.FSMETA, true);
  //      final String bpath = data.meta.backing + path;
  //      final Parser p = prop.is(Prop.NEWFSPARSER) ? new NewFSParser(
  //          bpath, ctx.prop) : new FSParser(bpath, ctx.prop);
  //      final MemBuilder mb = new MemBuilder(p);
  //      mb.init(md);
  //      BaseX.debug("[DataFS_parse_file] path : " + path + " -> " + bpath);
  //      return (MemData) mb.build();
  //    } catch(final IOException ex) {
  //      ex.printStackTrace();
  //    }
  //    return md;
  //  }

  //  /**
  //   * Inserts extracted file content.
  //   * @param path to file at which to insert the extracted content
  //   * @return pre value of newly inserted content, -1 on failure
  //   */
  //  private int insertContent(final String path) {
  //    final int fpre = pathPre(path);
  //    return fpre == -1 ? -1 : insert(fpre, buildContentData(path));
  //  }

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
    } catch(final QueryException ex) {
      ex.printStackTrace();
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
    return insert(ppre, buildFileNode(path, mode));
  }

  //  /**
  //   * Deletes a non-empty directory.
  //   * @param dir to be deleted.
  //   * @return boolean true for success, false for failure.
  //   */
  //  private static boolean deleteDir(final File dir) {
  //    if(dir.isDirectory()) {
  //      for(final File ch : dir.listFiles()) if(!deleteDir(ch)) return false;
  //    }
  //    return dir.delete();
  //  }

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
      final Nodes n = xquery(qb.toString());
      if(n.size() == 0) return -1;
      data.delete(n.nodes[0]);
      refresh();
    } catch(final QueryException ex) {
      ex.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Creates a new regular file or directory node.
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits)
   * @return id of the newly created file or -1 on failure
   */
  private int createNode(final String path, final int mode) {
    final int pre = insertFileNode(path, mode);
    return pre == -1 ? -1 : data.id(pre);
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
    // [AH] this.fileID may be sufficient?
    return data.kind(pre) == Data.ELEM
    && data.tagID(pre) == data.tags.id(DataText.DIR);
  }

  /**
   * Resolve filesystem pathname and fill stat information.
   * For root access ('/') return default access.
   * Note we store the pre value as inode.
   * @param path to file
   * @return file attributes or null
   */
  public DeepStat stat(final String path) {
    final String method = "[stat] ";
    final DeepStat sbuf = new DeepStat();

    if(path.equals("/")) return rootStat;

    final int pre = path2pre(path);
    if(pre == -1) {
      Main.debug(method + path + " (-1)");
      return null;
    }
    final byte[] mtime = attr(pre, mtimeID);
    final byte[] ctime = attr(pre, ctimeID);
    final byte[] atime = attr(pre, atimeID);
    final byte[] mode  = attr(pre, modeID);
    final byte[] size  = attr(pre, sizeID);
    final byte[] uid   = attr(pre, uidID);
    final byte[] gid   = attr(pre, gidID);
    final byte[] nlink = attr(pre, nlinkID);
    Main.debug(
        "pre/inode: " + pre +
        "\natime: " + string(atime) +
        "\nmtime: " + string(mtime) +
        "\nctime: " + string(ctime) +
        "\nmode: " + string(mode) +
        "\nsize: " + string(size) +
        "\nuid: " + string(uid) +
        "\ngid: " + string(gid) +
        "\nnlink: " + string(nlink)
    );
    sbuf.stino = pre;
    sbuf.statimespec = Long.parseLong(string(atime));
    sbuf.stctimespec = Long.parseLong(string(ctime));
    sbuf.stmtimespec = Long.parseLong(string(mtime));
    sbuf.stmode = Long.parseLong(string(mode));
    sbuf.stsize = Long.parseLong(string(size));
    sbuf.stuid = Long.parseLong(string(uid));
    sbuf.stgid = Long.parseLong(string(gid));
    sbuf.stnlink = Long.parseLong(string(nlink));
    Main.debug(method + path + " ino: " + sbuf.stino);
    return sbuf;
  }

  /**
   * Read directory entries.
   *
   * @param path directory to be listed.
   * @return directory entries, null on failure
   */
  public byte[][] readdir(final String path) {
    final int[] cld = path2preChildren(path);
    if(cld == null) return null;
    final int len = cld.length;
    final byte[][] dents = new byte[len][];
    for(int i = 0; i < len; i++)
      dents[i] = attr(cld[i], nameID);
    return dents;
  }

  /** Called when filesystem is unmounted. */
  public void umount() {
    ctx.closeDB();
    ctx.close();
  }

  /** Getter for actual context.
   * @return context
   */
  public Context getContext() {
    return ctx;
  }

  /**
   * Remove directory.
   *
   * @param path to directory to be removed
   * @return zero on success, -1 on failure
   */
  public int rmdir(final String path) {
    Main.debug("[basex_rmdir] path: " + path);
    final int n = delete(path, true, false);
    refresh();
    return n;
  }

  /**
   * Closes the fuse instance.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    if(data.meta.prop.is(Prop.FUSE)) {
      final String method = "[BaseX.close] ";
      Main.debug(method + "Initiating DeepFS shutdown sequence ");
      // -- unmount running fuse.
      for(int i = 3; i > 0; i--) {
        Performance.sleep(1000);
        Main.err(i + " .. ");
      }
      Main.debug("GO.");
      final String cmd = "umount -f " + data.meta.mount;
      Main.errln(method + "Trying to unmount deepfs: " + cmd);
      final Runtime r = Runtime.getRuntime();
      final java.lang.Process p = r.exec(cmd);
      try {
        p.waitFor();
      } catch(final InterruptedException ex) {
        ex.printStackTrace();
      }
      final int rc = p.exitValue();
      Main.debug(method + "Unmount " + data.meta.mount + (rc == 0 ?
          " ... OK." : " ... FAILED(" + rc + ") (Please unmount manually)"));
    }
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
      final byte[] b = data.meta.prop.is(Prop.FUSE) && !backing ?
          mountpoint(il.get(s - 1)) : backingstore(il.get(s - 1));
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
      Main.debug(ex);
      ex.printStackTrace();
    }
  }

  /**
   * Deletes a non-empty directory.
   * @param pre pre value
   */
  public void delete(final int pre) {
    if (pre == 0) /* avoid checkstyle warnings. */;
    //    if(data.meta.prop.is(Prop.FUSE)) {
    //      final String bpath = Token.string(path(pre, true));
    //      final File f = new File(bpath);
    //      if(f.isDirectory()) deleteDir(f);
    //      else if(f.isFile()) f.delete();
    //      nativeUnlink(Token.string(path(pre, false)));
    //    }
  }

  /**
   * Creates a new directory.
   * @param path to directory to be created
   * @param mode of directory
   * @return id of the newly created directory or -1 on failure
   */
  public int mkdir(final String path, final int mode) {
    // if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR.
    final String method = "[mkdir] ";
    final int n = createNode(path, getSIFDIR() | mode);
    Main.debug(method + "path: " + path + " mode: "
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
  public int create(final String path, final int mode) {
    // if(!isFile(mode)) return -1; // Linux does not submit S_IFREG.
    final int n = createNode(path, getSIFREG() | mode);
    refresh();
    return n;
  }

  /** Unlink a file node.
   *
   * @param path to file to be deleted
   * @return success or failure
   */
  public int unlink(final String path) {
    final int n = delete(path, false, false);
    refresh();
    return n;
  }

  //  public int opendir(final String path) {
  //    try {
  //      final String query = "count(" + pn2xp(path, true) + "/child::*)";
  //      final QueryProcessor xq = new QueryProcessor(query, ctx);
  //      final Result result = xq.query();
  //      final SeqIter s = (SeqIter) result;
  //      final Item i = s.next();
  //      return i != null ? (int) i.itr() : -1;
  //    } catch(final QueryException ex) {
  //      ex.printStackTrace();
  //      return -1;
  //    }
  //  }
  //
  //  public int release(final String path) {
  //    final boolean dirty = true;
  //
  //    final String method = "[-basex_release] ";
  //    BaseX.debug(method + "path: " + path);
  //
  //    if(dirty) {
  //      delete(path, false, true);
  //      insertContent(path);
  //    }
  //    return 0;
  //  }

  //  /*
  //   * -----------------------------------------------------------------------
  //   * Native deepfs method declarations (org_basex_fuse_DeepFS.h)
  //   * -----------------------------------------------------------------------
  //   */
  //
  //  /**
  //   * Mount database as FUSE.
  //   * @param mp path where to mount BaseX.
  //   * @param bs path to backing storage root of this instance.
  //   * @return 0 on success, errno in case of failure.
  //   */
  //  public native int nativeMount(final String mp, final String bs);
  //
  //  /**
  //   * Unlink file in backing store.
  //   * @param pathname to file to delete
  //   * @return 0 on success, errno in case of failure.
  //   */
  //  public native int nativeUnlink(final String pathname);
  //
  //  /**
  //   * Tell DeepFS that the database will shutdown.
  //   */
  //  public native void nativeShutDown();

  /* ------------------------------------------------------------------------ */
}
