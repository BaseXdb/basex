package org.basex.core;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.util.Performance;

/**
 * This class stores the reference to the currently opened database.
 * Moreover, it provides references to the currently used, marked and
 * copied node sets.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Central data reference. */
  private Data data;
  /** Current context. */
  private Nodes current;
  /** Currently marked nodes. */
  private Nodes marked;
  /** Currently copied nodes. */
  private Nodes copied;
  
  /**
   * Constructor.
  */
  public Context() {
    Prop.read();
  }
  
  /**
   * Returns true if a data reference has been set.
   * @return result of check
  */
  public boolean db() {
    return data != null;
  }
  
  /**
   * Returns true if all current nodes refer to document nodes.
   * @return result of check
   */
  public boolean root() {
    if(current == null) return true;
    for(final int n : current.nodes) if(data.kind(n) != Data.DOC) return false;
    return true;
  }
  
  /**
   * Returns data reference.
   * @return data reference
   */
  public Data data() {
    return data;
  }
  
  /**
   * Sets a new data instance.
   * @param d data reference
   */
  public void data(final Data d) {
    if(data != null) {
      BaseX.debug("Warning: database still open.");
      close();
    }
    data = d;
    copied = null;
    marked = new Nodes(d);
    update();
  }
  
  /**
   * Updates references to the document nodes.
   */
  public void update() {
    current = new Nodes(data.doc(), data);
  }
    
  /**
   * Closes the database instance.
   * @return true if operation was successful
   */
  public synchronized boolean close() {
    try {
      if(data != null) {
        final Data d = data;
        final String mp = data.meta.mountpoint; // !! data = null
        data = null;
        current = null;
        marked = null;
        copied = null;
        d.close();
        if(Prop.mainmem || Prop.onthefly) Performance.gc(1);
        // -- close fuse instance
        if(Prop.fuse) { 
          final String method = "[BaseX.close] ";
          BaseX.debug(method + "Initiating DeepFS shutdown sequence ");
          // -- unmount running fuse.
          for (int i = 3; i > 0; i--) {
            Performance.sleep(1000);
            BaseX.err(i + " .. ");
          }
          BaseX.debug("GO.");
          final String cmd = "umount -f " + mp;
          BaseX.errln(method + "Trying to unmount deepfs: " + cmd);
          Runtime r = Runtime.getRuntime();
          java.lang.Process p = r.exec(cmd);
          try {
            p.waitFor();
          } catch(InterruptedException e) {
            e.printStackTrace();
          }
          int rc = p.exitValue();
          String msg = method + "Unmount "  + mp;
          if (rc == 0) msg = msg + " ... OK."; 
          else msg = msg + " ... FAILED(" + rc + ") (Please unmount manually)";
          BaseX.debug(msg);
        }
      }
      return true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return false;
    }
  }
  
  /**
   * Returns the current context set.
   * @return current context set
   */
  public Nodes current() {
    return current;
  }
  
  /**
   * Sets the current context set.
   * @param curr current context set
   */
  public void current(final Nodes curr) {
    current = curr;
  }
  
  /**
   * Returns the copied context set.
   * @return copied context set
   */
  public Nodes copied() {
    return copied;
  }
  
  /**
   * Sets the current node set as copy.
   * @param copy current node set as copy.
   */
  public void copy(final Nodes copy) {
    copied = copy;
  }
  
  /**
   * Returns the marked context set.
   * @return marked context set
   */
  public Nodes marked() {
    return marked;
  }
  
  /**
   * Sets the marked context set.
   * @param mark marked context set
   */
  public void marked(final Nodes mark) {
    marked = mark;
  }
}
