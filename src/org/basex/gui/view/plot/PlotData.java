package org.basex.gui.view.plot;

import static org.basex.util.Token.*;
import java.util.Arrays;
import org.basex.core.Context;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.StatsKey.Kind;
import org.basex.index.Names;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * An additional layer which prepares the data for the scatter plot.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class PlotData {
  /** Context reference. */
  final Context context;
  /** The x axis of the plot. */
  final PlotAxis xAxis;
  /** The y axis of the plot. */
  final PlotAxis yAxis;
  /** Items pre values. */
  int[] pres;
  /** Number of items displayed in plot. */
  int size;
  /** Item token selected by user. */
  byte[] item = EMPTY;

  /**
   * Default constructor.
   * @param ctx context reference
   */
  public PlotData(final Context ctx) {
    xAxis = new PlotAxis(this);
    yAxis = new PlotAxis(this);
    pres = new int[0];
    context = ctx;
  }

  /**
   * Returns a string array with all top items
   * and the keys of the specified set.
   * @return key array
   */
  TokenList getItems() {
    final Data data = context.data();
    final TokenList tl = new TokenList();
    for(final byte[] k : data.path.desc(EMPTY, data, true, true)) {
      if(getCategories(k).size() > 1) tl.add(k);
    }
    return tl;
  }

  /**
   * Returns a string array with suitable categories for the specified item.
   * @param it top item
   * @return key array
   */
  TokenList getCategories(final byte[] it) {
    final Data data = context.data();
    final TokenList tl = new TokenList();
    for(final byte[] k : data.path.desc(it, data, true, false)) {
      final Names index = startsWith(k, '@') ? data.atts : data.tags;
      if(index.stat(index.id(delete(k, '@'))).kind != Kind.NONE) tl.add(k);
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
    refreshItems(context.current(), true);
    return true;
  }

  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting.
   * @param ctx context to be displayed
   * @param sub determine descendant nodes of given context nodes
   */
  void refreshItems(final Nodes ctx, final boolean sub) {
    final Data data = context.data();
    final IntList tmpPres = new IntList();
    final int itmID = data.tagID(item);

    if(!sub) {
      pres = ctx.nodes;
      Arrays.sort(pres);
      size = pres.length;
      return;
    }

    final int[] contextPres = ctx.nodes;
    for(int p : contextPres) {
      final int nl = p + data.size(p, Data.ELEM);
      while(p < nl) {
        final int kind = data.kind(p);
        if(kind == Data.ELEM) {
          if(data.tagID(p) == itmID) tmpPres.add(p);
          p += data.attSize(p, kind);
        } else {
          p++;
        }
      }
    }
    pres = tmpPres.finish();
    Arrays.sort(pres);
    size = pres.length;
  }

  /**
   * Returns the array position of a given pre value by performing a binary
   * search on the sorted pre values array currently displayed in the plot.
   * @param pre pre value to be looked up
   * @return array index if found, -1 if not
   */
  int findPre(final int pre) {
    return Arrays.binarySearch(pres, pre);
  }
}