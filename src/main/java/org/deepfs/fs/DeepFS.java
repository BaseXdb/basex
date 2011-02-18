package org.deepfs.fs;

import static org.basex.util.Token.*;
import static org.deepfs.fs.DeepStat.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.data.Data;
import org.basex.data.DataText;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Atts;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.deepfs.DeepShell;
import org.deepfs.fsml.DeepNS;

/**
 * DeepFS: The XQuery Filesystem. Database-side implementation of DeepFS.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Alexander Holupirek, Christian Gruen, Hannes Schwarz, Bastian Lemke
 */
public final class DeepFS implements DataText {
  // DEEPFS STRINGS ===========================================================

  /** Name of the root node for a fsml document. */
  public static final String S_FSML = DeepNS.DEEPURL.tag("fsml");
  /** Name of the root node for a file system. */
  public static final String S_DEEPFS = DeepNS.DEEPURL.tag("deepfs");
  /** File tag in fs namespace. */
  public static final String S_FILE = DeepNS.DEEPURL.tag("file");
  /** Directory tag in fs namespace. */
  public static final String S_DIR = DeepNS.DEEPURL.tag("dir");
  /** XML content tag in fs namespace. */
  public static final String S_XML_CONTENT = DeepNS.DEEPURL.tag("xml");
  /** Text content tag in fs namespace. */
  public static final String S_TEXT_CONTENT = DeepNS.DEEPURL.tag("text");
  /** Content tag in fs namespace. */
  public static final String S_CONTENT = DeepNS.DEEPURL.tag("content");

  /** file/dir name. */
  public static final String S_NAME = "name";
  /** backingstore path. */
  public static final String S_BACKINGSTORE = "backingstore";
  /** File suffix. */
  public static final String S_SUFFIX = "suffix";
  /** file/dir size. */
  public static final String S_SIZE = "size";
  /** Time of last access. */
  public static final String S_ATIME = "atime";
  /** Time of creation. */
  public static final String S_CTIME = "ctime";
  /** Time of last modification. */
  public static final String S_MTIME = "mtime";

  /** FSML token. */
  public static final byte[] FSML = token(S_FSML);
  /** DeepFS token. */
  public static final byte[] DEEPFS = token(S_DEEPFS);
  /** Directory token. */
  public static final byte[] DIR = token(S_DIR);
  /** File token. */
  public static final byte[] FILE = token(S_FILE);
  /** Text content token. */
  public static final byte[] TEXT_CONTENT = token(S_TEXT_CONTENT);

  /** Name attribute token. */
  public static final byte[] NAME = token(S_NAME);
  /** Size attribute token. */
  public static final byte[] SIZE = token(S_SIZE);
  /** Time of last modification token. */
  public static final byte[] MTIME = token(S_MTIME);
  /** Suffix attribute. */
  public static final byte[] SUFFIX = token(S_SUFFIX);
  /** Time of last access token. */
  public static final byte[] ATIME = token(S_ATIME);
  /** Time of creation token. */
  public static final byte[] CTIME = token(S_CTIME);
  /** Number of links token. */
  public static final byte[] NLINK = token("nlink");
  /** User id token. */
  public static final byte[] UID = token("uid");
  /** Group id token. */
  public static final byte[] GID = token("gid");
  /** Offset attribute. */
  public static final byte[] OFFSET = token("offset");
  /** File mode attribute. */
  public static final byte[] MODE = token("mode");
  /** Mount point attribute. */
  public static final byte[] MOUNTPOINT = token("mountpoint");
  /** Backing store attribute. */
  public static final byte[] BACKINGSTORE = token(S_BACKINGSTORE);
  /** Negative mount point attribute. */
  public static final byte[] NOTMOUNTED = token("(not mounted)");

  /** Context instance. */
  private Context ctx;
  /** Data instance. */
  private final Data data;

  /** Stat information for root node. */
  private DeepStat rootStat;

  /** Index references. */
  public int fsmlID;
  /** Index references. */
  public int deepfsID;
  /** Index references. */
  public int fileID;
  /** Index references. */
  public int dirID;
  /** Index references. */
  public int modeID;
  /** Index mount point. */
  public int mountpointID;
  /** Index backing store. */
  public int backingstoreID;
  /** Index references. */
  public int sizeID;
  /** Index references. */
  public int nameID;

  /** Index references. */
  public int suffixID;
  /** Index references. */
  public int mtimeID;
  /** Index references. */
  public int ctimeID;
  /** Index references. */
  public int atimeID;
  /** Index references. */
  public int nlinkID;
  /** Index references. */
  public int uidID;
  /** Index references. */
  public int gidID;

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
    this(c.data);
    ctx = c;
  }

  /**
   * Constructor for {@link DeepShell} and java only test cases (no mount).
   * @param name name of (initially empty) database
   * @param mp of DeepFS database
   */
  public DeepFS(final String name, final String mp) {
    this(initData(name, mp));
  }

  /**
   * Initializes the data.
   * @param name name of (initially empty) database
   * @param mp of DeepFS database
   * @return context
   */
  private static Context initData(final String name, final String mp) {
    final Context ctx = new Context();
    try {
      new Open(name).execute(ctx);
    } catch(final BaseXException ex) {
      try {
        new CreateDB(name, "<" + S_DEEPFS + " " + "mountpoint=\""
            + mp + "\"/>").execute(ctx);
      } catch(final BaseXException exx) {
        Util.notexpected(exx);
      }
    }
    return ctx;
  }

  /**
   * Initializes often used tags and attributes.
   */
  private void initNames() {
    // initialize tags and attribute names
    fsmlID         = data.tags.index(FSML,         null, false);
    deepfsID       = data.tags.index(DEEPFS,       null, false);
    dirID          = data.tags.index(DIR,          null, false);
    fileID         = data.tags.index(FILE,         null, false);
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
   * Initializes default file attributes for root ('/') access.
   */
  private void initRootStat() {
    rootStat = new DeepStat();
    rootStat.statimespec = System.currentTimeMillis();
    //rootStat.stctimespec = rootStat.statimespec;
    //rootStat.stmtimespec = rootStat.statimespec;
    rootStat.stmode = getSIFDIR() | 0755;
    rootStat.stsize = 0;
    rootStat.stuid =  0;
    rootStat.stgid =  0;
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
    // qb.append(S_DPFSNS);
    qb.append("/" + S_DEEPFS);
    if(path.equals("/")) return qb.toString();

    for(int i = 0; i < path.length(); ++i) {
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

    Util.debug("[pn2xp] " + qu);

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
      return n.size() == 0 ? -1 : n.list[0];
    } catch(final QueryException ex) {
      Util.stack(ex);
      return -1;
    }
  }

  /**
   * Resolves child axis from path and return pre values of children.
   * @param path to be resolved
   * @return pre values of children found
   */
  private int[] path2preChildren(final String path) {
    try {
      final Nodes n = xquery(pn2xp(path, true) + "/child::*");
      return n.list;
    } catch(final QueryException ex) {
      Util.stack(ex);
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
   * Extracts file name suffix.
   * @param name of the file
   * @return suffix or EMPTY token
   */
  public static byte[] getSuffix(final String name) {
    final int s = name.lastIndexOf('.');
    final byte[] suf = s != -1 ? lc(token(name.substring(s + 1))) : EMPTY;
    return suf;
  }

  /**
   * Constructs file node as MemData object, ready to be inserted into main
   * data instance.
   * @param path to file to build MemData for
   * @param mode to determine file type
   * @return MemData reference
   */
  private MemData buildFileNode(final String path, final int mode) {
    final String fn = basename(path);
    final MemData m = new MemData(data);
    final int tagID = isReg(mode) ? fileID : dirID;
    final byte [] time = token(System.currentTimeMillis());
    final int nodeSize = 11; // 1x elem, 10x attr
    m.buffer(nodeSize);
    m.elem(1, tagID, nodeSize, nodeSize, 0, false);
    m.attr(1, 1, nameID, token(fn), 0, false);
    m.attr(2, 2, sizeID, ZERO, 0, false);
    m.attr(3, 3, modeID, token(mode), 0, false);
    m.attr(4, 4, uidID, token(getUID()), 0, false);
    m.attr(5, 5, gidID, token(getGID()), 0, false);
    m.attr(6, 6, atimeID, time, 0, false);
    m.attr(7, 7, ctimeID, time, 0, false);
    m.attr(8, 8, mtimeID, time, 0, false);
    m.attr(9, 9, nlinkID, ONE, 0, false);
    m.attr(10, 10, suffixID, getSuffix(fn), 0, false);
    m.insert(0);
    return m;
  }

  /**
   * Constructs attributes for file and directory tags.
   * @param f file name
   * @param root if true, attributes for a filesystem root node are created
   *          instead of attributes for a simple directory
   * @return attributes as byte[][]
   */
  public static Atts atts(final File f, final boolean root) {
    final String name = f.getName();
    final byte[] time = token(System.currentTimeMillis());

    /** Temporary attribute array. */
    final Atts atts = new Atts();
    if(root)
      atts.add(BACKINGSTORE, token(f.getAbsolutePath().replace("\\", "/")));
    else atts.add(NAME, token(name));
    atts.add(SIZE, token(f.length()));
    if(f.isDirectory()) atts.add(MODE, token(getSIFDIR() | 0755));
    else atts.add(MODE, token(getSIFREG() | 0644));
    atts.add(UID, token(getUID()));
    atts.add(GID, token(getGID()));
    atts.add(ATIME, time);
    atts.add(CTIME, time);
    atts.add(MTIME, token(f.lastModified()));
    atts.add(NLINK, ONE);
    atts.add(SUFFIX, getSuffix(name));
    return atts;
  }

  /**
   * Returns mountpoint attribute value.
   * @param pre pre value
   * @return mountpoint value
   */
  private byte[] mountpoint(final int pre) {
    return attr(pre, data.fs.mountpointID);
  }

  /**
   * Returns backing store attribute value.
   * @param pre pre value
   * @return path mountpoint
   */
  private byte[] backingstore(final int pre) {
    return attr(pre, data.fs.backingstoreID);
  }

  /**
   * Returns a file attribute.
   * @param pre pre value
   * @param at the attribute id of the attribute to be found
   * @return attribute or empty token
   */
  private byte[] attr(final int pre, final int at) {
    final int a = pre + data.attSize(pre, data.kind(pre));
    int p = pre;
    while(++p != a) if(data.name(p) == at) return data.text(p, false);
    return EMPTY;
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
    return npre;
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
      return n.size() == 0 ? -1 : n.list[0];
    } catch(final QueryException ex) {
      Util.stack(ex);
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
      data.delete(n.list[0]);
    } catch(final QueryException ex) {
      Util.stack(ex);
      return -1;
    }
    return 0;
  }

  /**
   * Creates a new regular file or directory node.
   * @param path to the file to be created
   * @param mode of file (directory, regular file ..., permission bits
   * @return id of the newly created file or -1 on failure
   */
  private int createNode(final String path, final int mode) {
    final int pre = insertFileNode(path, mode);
    return pre == -1 ? -1 : data.id(pre);
  }

  /**
   * Checks if this node is a file.
   * @param pre pre value
   * @return true if this node is a file, false otherwise
   */
  public boolean isFile(final int pre) {
    return isValidNode(pre, fileID) && hasValidParents(pre);
  }

  /**
   * Checks if this node is a directory.
   * @param pre pre value
   * @return true if this node is a directory, false otherwise
   */
  public boolean isDir(final int pre) {
    return isValidNode(pre, dirID) && hasValidParents(pre);
  }

  /**
   * Checks if this node is a file, directory, fsml or deepfs node.
   * @param pre pre value
   * @return true if this node is a deepfs node, false otherwise
   */
  public boolean isFSnode(final int pre) {
    return isValidNode(pre, fileID, dirID, fsmlID, deepfsID) &&
        hasValidParents(pre);
  }

  /**
   * Checks if this node is a valid filesystem node with one of the given type
   * (file/dir/deepfs/fsml).
   * @param pre pre value
   * @param acceptedNames type of the filesystem node
   * @return true if this node is valid
   */
  private boolean isValidNode(final int pre, final int... acceptedNames) {
    final int k = data.kind(pre);
    if(k != Data.ELEM) return false;
    final int n = data.name(pre);
    for(final int i : acceptedNames) if(n == i) return true;
    return false;
  }

  /**
   * Checks if all parent nodes of this node are valid.
   * @param pre pre value
   * @return true if all parent nodes are valid fsml nodes
   */
  private boolean hasValidParents(final int pre) {
    int par = data.parent(pre, data.kind(pre));
    int k = data.kind(par);
    // a regular xml file may contain a <file> node...
    while(par >= 0 && k != Data.DOC) {
      if(!isValidNode(par, dirID, fsmlID, deepfsID)) return false;
      par = data.parent(par, data.kind(par));
      k = data.kind(par);
    }
    return true;
  }

  /**
   * Resolves filesystem pathname and fill stat information.
   * For root access ('/') return default access.
   * Note we store the pre value as inode.
   * @param path to file
   * @return file attributes or {@code null}
   */
  public DeepStat stat(final String path) {
    final String method = "[stat] ";
    final DeepStat sbuf = new DeepStat();

    if(path.equals("/")) return rootStat;

    final int pre = path2pre(path);
    if(pre == -1) {
      Util.debug(method + path + " (-1)");
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
    Util.debug(
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
    //sbuf.stctimespec = Long.parseLong(string(ctime));
    //sbuf.stmtimespec = Long.parseLong(string(mtime));
    sbuf.stmode = Long.parseLong(string(mode));
    sbuf.stsize = Long.parseLong(string(size));
    sbuf.stuid = Long.parseLong(string(uid));
    sbuf.stgid = Long.parseLong(string(gid));
    sbuf.stnlink = Long.parseLong(string(nlink));
    Util.debug(method + path + " ino: " + sbuf.stino);
    return sbuf;
  }

  /**
   * Reads directory entries.
   * @param path directory to be listed
   * @return directory entries, {@code null} on failure
   */
  public byte[][] readdir(final String path) {
    final int[] cld = path2preChildren(path);
    if(cld == null) return null;
    final int len = cld.length;
    final byte[][] dents = new byte[len][];
    for(int i = 0; i < len; ++i)
      dents[i] = attr(cld[i], nameID);
    return dents;
  }

  /** Called when filesystem is unmounted. */
  public void umount() {
    ctx.closeDB();
    ctx.close();
  }

  /**
   * Getter for actual context.
   * @return context
   */
  public Context getContext() {
    return ctx;
  }

  /**
   * Removes directory.
   * @param path to directory to be removed
   * @return zero on success, -1 on failure
   */
  public int rmdir(final String path) {
    Util.debug("[basex_rmdir] path: " + path);
    final int n = delete(path, true, false);
    return n;
  }

  /**
   * Closes the fuse instance.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    if(data.meta.prop.is(Prop.FUSE)) {
      final String method = "[" + Text.NAME + ".close] ";
      Util.debug(method + "Initiating DeepFS shutdown sequence ");
      // -- unmount running fuse
      for(int i = 3; i > 0; i--) {
        Performance.sleep(1000);
        Util.error(i + " .. ");
      }
      Util.debug("GO.");
      final String cmd = "umount -f " + data.meta.mount;
      Util.errln(method + "Trying to unmount deepfs: " + cmd);
      final Runtime r = Runtime.getRuntime();
      final java.lang.Process p = r.exec(cmd);
      try {
        p.waitFor();
      } catch(final InterruptedException ex) {
        Util.stack(ex);
      }
      final int rc = p.exitValue();
      Util.debug(method + "Unmount " + data.meta.mount + (rc == 0 ?
          " ... OK." : " ... FAILED(" + rc + ") (Please unmount manually)"));
    }
  }

  /**
   * Returns the absolute file path.
   * @param pre pre value
   * @param backing whether to return backing path or mountpath
   * @return file path
   */
  public byte[] path(final int pre, final boolean backing) {
    int p = pre;
    int k = data.kind(p);

    // select parent file/dir/fsml/deepfs node
    while(p > 0 && !isValidNode(p, fileID, dirID, deepfsID, fsmlID)) {
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final IntList il = new IntList();
    while(p >= 0 && k != Data.DOC) {
      if(!isFSnode(p)) return EMPTY;
      il.add(p);
      p = data.parent(p, k);
      k = data.kind(p);
    }

    final TokenBuilder tb = new TokenBuilder();
    final int s = il.size();
    if(s > 1) {
      final byte[] b = data.meta.prop.is(Prop.FUSE) && !backing ?
          mountpoint(il.get(s - 2)) : backingstore(il.get(s - 2));
      if(b.length != 0) {
        tb.add(b);
        if(!endsWith(b, '/')) tb.add('/');
      }
    }
    for(int i = s - 3; i >= 0; i--) {
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
   * @return file name
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
   * @throws IOException thrown if no default application to launch exists
   */
  public void launch(final int pre) throws IOException {
    if(pre == -1 || !isFile(pre)) return;
    Desktop.getDesktop().open(new File(string(path(pre, false))));
  }

  /**
   * Creates a new directory.
   * @param path to directory to be created
   * @param mode of directory
   * @return id of the newly created directory or -1 on failure
   */
  public int mkdir(final String path, final int mode) {
    // if(!isDir(mode)) return -1; // Linux does not submit S_IFDIR
    final String method = "[mkdir] ";
    final int n = createNode(path, getSIFDIR() | mode);
    Util.debug(method + "path: " + path + " mode: "
        + Integer.toOctalString(mode) + " id : (" + n + ")");
    return n;
  }

  /**
   * Creates a new regular file.
   * @param path to the file to be created
   * @param mode of regular file
   * @return id of the newly created file or -1 on failure
   */
  public int create(final String path, final int mode) {
    // if(!isFile(mode)) return -1; // Linux does not submit S_IFREG
    final int n = createNode(path, getSIFREG() | mode);
    return n;
  }
}
