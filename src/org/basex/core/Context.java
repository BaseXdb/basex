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
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Context {
  /** Central data reference. */
  private Data data;
  /** Current context set. */
  private Nodes current;
  /** Currently marked context nodes. */
  private Nodes marked;
  /** Currently copied data set. */
  private Nodes copied;
  /** Current FTPrePos values. **/
  private int[][] ftData;
  /** Current FTPointer on FTPrePos values. **/
  private int[] ftPoi; 
  /** FTSearchStrings out of query. **/
  private byte[] ftss; 
  
  /**
   * Returns true if a data reference has been set.
   * @return result of check
  */
  public boolean db() {
    return data != null;
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
      BaseX.debug("Warning: Database still open.");
      close();
    }
    data = d;
    current = new Nodes(0, d);
    marked = new Nodes(d);
  }
  
  /**
   * Closes the database instance.
   * @return true if operation was successful
   */
  public synchronized boolean close() {
    try {
      if(data != null) {
        final Data d = data;
        data = null;
        current = null;
        marked = null;
        d.close();
        if(Prop.mainmem) Performance.gc(1);
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
   * Sets the current node set copy.
   * @param copy current node set copy.
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
  
  /**
   * Sets the ftdata.
   * @param prepos int[][] prepos values
   * @param poi int|[] pointer values
   */
  public void ftData(final int[][] prepos, final int[] poi) {
    ftData = prepos;
    ftPoi = poi;
  }
  
  /**
   * Getter for FTData.
   * @return int[][] ftData.
   */
  public int[][] getFTData() {
    return ftData;
  }
  
  /**
   * Getter for FTPointer.
   * @return int[] ftPointer
   */
  public int[] getFTPointer() {
    return ftPoi;
  }
  
  /**
   * Setter for FTSearchString, extracted out of query.
   * @param ftSearchString fulltext search string
   */
  public void setFTSearchString(final byte[] ftSearchString) {
    ftss = ftSearchString;
  }
 
  /**
   * Setter for FTSearchString, extracted out of query.
   * @return ftSearchString
   */
   public byte[] getFTSearchString() {
     return ftss;
   }
  
}
