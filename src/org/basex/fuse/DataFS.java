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
  
  /**
   * Mount database as FUSE.
   * @param mountpoint path where to mount BaseX.
   * @param backing path to backing storage root.
   * @return 0 on success, errno in case of failure.
   */
  public native int nativeMount(final String mountpoint, final String backing);
  
  /**
   * Unlink file in backing store.
   * @param pathname to file to delete
   * @return 0 on success, errno in case of failure.
   */
  public native int nativeUnlink(final String pathname);
  
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
      nativeMount("a", "b");
    }
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
    System.err.println("Can't believe it.  Called back from DeepFS.");
    System.err.printf("- %s %o\n", path, mode);
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
