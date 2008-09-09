package org.basex.gui.view.scatter;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.index.Names;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * An additional abstraction layer which prepares the data for the scatter plot.
 * It consists of a simple array for fast plotting and two hash maps which
 * support a fast item search after interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public class ScatterData {
  /** Items pre values. */
  int[] pres;
  /** Number of items displayed in plot. */ 
  int size;
  /** The x axis of the plot. */
  ScatterAxis xAxis;
  /** The y axis of the plot. */
  ScatterAxis yAxis;
  
  /** A temporary list for pre values which match the given item. */
  private IntList tmpPres;
  /** Item token selected by user. */
  private byte[] item;
  
  /**
   * Default Constructor.
   */
  public ScatterData() {
    xAxis = new ScatterAxis(this);
    yAxis = new ScatterAxis(this);
    size = 0;
    item = Token.token("");
  }
  
  /**
   * Returns a string array with all distinct keys
   * and the keys of the specified set.
   * @return key array
   */
  String[] getStatKeys() {
    Data data = GUI.context.data();
    
    final Names tags = data.tags;
    final StringList sl = new StringList();
    for(int i = 1; i <= tags.size(); i++) {
      if(tags.counter(i) == 0) continue;
      final StatsKey key = tags.stat(tags.id(tags.key(i)));
      final Kind kind = key.kind;
      if(kind == Kind.DBL || kind == Kind.INT || kind == Kind.CAT && 
          key.cats.size() <= 20) {
        sl.add(Token.string(tags.key(i)));
      }
    }
    
    final Names atts = data.atts;
    for(int i = 0; i <= data.atts.size(); i++) {
      if(data.atts.counter(i) == 0) continue;
      final StatsKey key = atts.stat(atts.id(atts.key(i)));
      final Kind kind = key.kind;
      if(kind == Kind.DBL || kind == Kind.INT || kind == Kind.CAT && 
          key.cats.size() <= 20) {
        sl.add("@" + Token.string(atts.key(i)));
      }
    }
    final String[] vals = sl.finish();
    Arrays.sort(vals);
    
    return vals;
  }
  
  /**
   * Called if the user changes the item level displayed in the plot. If a new
   * item was selected the plot data is recalculated.
   * @param newItem item selected by the user
   * @return true if a new item was selected and the plot data has been 
   * recalculated
   */
  boolean setItem(final String newItem) {
    if(newItem == null) return false;
    final byte[] b = Token.token(newItem);
    if(Token.eq(b, item)) return false;
    item = b;
    refreshItems();
    xAxis.refreshAxis();
    yAxis.refreshAxis();
    return true;
  }
  
  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting.
   */
  void refreshItems() {
    final Data data = GUI.context.data();
    tmpPres = new IntList();
    final int s = data.size;
    final int itmID = data.tagID(item);
    int p = 1;
    while(p < s) {
      final int kind = data.kind(p);
      if(kind == Data.ELEM && data.tagID(p) == itmID) {
        if(data.tagID(p) == itmID) {
          tmpPres.add(p);
          p += data.size(p, kind);
        } else {
          p += data.attSize(p, kind);
        }
      } else {
        p++;
      }
    }
    pres = tmpPres.finish();
    size = pres.length;
  }
}