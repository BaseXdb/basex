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
import org.basex.data.Nodes;
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
public final class DataFS {
  /** Data reference. */
  public Data data;
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
       
      //nativeMount("/mnt/deepfs", "/var/tmp/deepfs", "demo");
      nativeMount(d.meta.mount, d.meta.backing, d.meta.dbname);
      
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
  Nodes xquery(final String query) throws QueryException {
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
    data.meta.update();
    //data.flush();
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
}
