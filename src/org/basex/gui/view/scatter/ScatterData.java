package org.basex.gui.view.scatter;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.index.Names;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * An additional abstraction layer which prepares the data for the scatter plot.
 * It consists of a simple array for fast plotting and two hash maps which
 * support a fast item search after interactions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterData {
  /** The x axis of the plot. */
  final ScatterAxis xAxis;
  /** The y axis of the plot. */
  final ScatterAxis yAxis;
  /** Items pre values. */
  int[] pres;
  /** Number of items displayed in plot. */
  int size;

  /** A temporary list for pre values which match the given item. */
  private IntList tmpPres;
  /** Item token selected by user. */
  private byte[] item = EMPTY;

  /**
   * Default Constructor.
   */
  public ScatterData() {
    xAxis = new ScatterAxis(this);
    yAxis = new ScatterAxis(this);
  }

  /**
   * Returns a string array with all top items
   * and the keys of the specified set.
   * @return key array
   */
  TokenList getItems() {
    final Data data = GUI.context.data();
    final TokenList tl = new TokenList();
    for(final byte[] k : data.skel.desc(EMPTY, true, false)) {
      if(getCategories(k).size > 1) tl.add(k);
    }
    return tl;
  }

  /**
   * Returns a string array with suitable categories for the specified item.
   * @param it top item
   * @return key array
   */
  TokenList getCategories(final byte[] it) {
    final Data data = GUI.context.data();
    final TokenList tl = new TokenList();
    for(final byte[] k : data.skel.desc(it, true, false)) {
      final Names index = startsWith(k, '@') ? data.atts : data.tags;
      final Kind kind = index.stat(index.id(delete(k, '@'))).kind;
      if(kind == Kind.DBL || kind == Kind.INT || kind == Kind.CAT) tl.add(k);
    }
    return tl;
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
    final byte[] b = token(newItem);
    if(eq(b, item)) return false;
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

  /**
   * Returns the array position of a given pre value.
   * @param pre pre value to be looked up
   * @return array index if found, -1 if not
   */
  int getPrePos(final int pre) {
    for(int i = 0; i < size; i++) {
      if(pres[i] == pre)
        return i;
    }
    return -1;
  }
}