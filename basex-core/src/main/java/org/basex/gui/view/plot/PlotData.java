package org.basex.gui.view.plot;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.stats.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * An additional layer which prepares the data for the scatter plot.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
final class PlotData {
  /** Database context. */
  final Context context;
  /** The x axis of the plot. */
  final PlotAxis xAxis;
  /** The y axis of the plot. */
  final PlotAxis yAxis;
  /** Items pre values. */
  int[] pres;
  /** Item token selected by user. */
  byte[] item = EMPTY;

  /**
   * Default constructor.
   * @param ctx database context
   */
  PlotData(final Context ctx) {
    xAxis = new PlotAxis(this);
    yAxis = new PlotAxis(this);
    pres = new int[0];
    context = ctx;
  }

  /**
   * Returns a string array with all top items and the keys of the specified set.
   * @return items
   */
  String[] getItems() {
    final Data data = context.data();
    final StringList sl = new StringList();
    for(final byte[] name : data.paths.desc(EMPTY, true, true)) {
      if(getCategories(name).length > 1) sl.add(name);
    }
    return sl.finish();
  }

  /**
   * Returns a string array with suitable categories for the specified item.
   * @param name element/attribute name
   * @return categories
   */
  String[] getCategories(final byte[] name) {
    final Data data = context.data();
    final StringList sl = new StringList();
    for(final byte[] nm : data.paths.desc(name, true, false)) {
      final Names names = startsWith(nm, '@') ? data.attrNames : data.elemNames;
      if(names.stats(names.id(delete(nm, '@'))).type != StatsType.NONE) sl.add(nm);
    }
    return sl.finish();
  }

  /**
   * Called if the user changes the item level displayed in the plot.
   * If a new item was selected, the plot data is recalculated.
   * @param newItem item selected by the user
   * @return {@code true} if a new item was selected and the plot data has been recalculated
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
   * Refreshes item list and coordinates if the selection has changed.
   * So far, only numerical data is considered for plotting.
   * @param nodes nodes to be displayed
   * @param sub determine descendant nodes of given context nodes
   */
  void refreshItems(final DBNodes nodes, final boolean sub) {
    final Data data = context.data();
    final IntList il = new IntList();
    final int itmID = data.elemNames.id(item);

    if(!sub) {
      pres = nodes.pres();
      Arrays.sort(pres);
      return;
    }

    final int[] contextPres = nodes.pres();
    for(int p : contextPres) {
      if(p >= data.meta.size) break;
      final int nl = p + data.size(p, Data.ELEM);
      while(p < nl) {
        final int kind = data.kind(p);
        if(kind == Data.ELEM) {
          if(data.nameId(p) == itmID) il.add(p);
          p += data.attSize(p, Data.ELEM);
        } else {
          ++p;
        }
      }
    }
    pres = il.finish();
    Arrays.sort(pres);
  }

  /**
   * Returns the array position of a given pre value by performing a binary
   * search on the sorted pre values array currently displayed in the plot.
   * @param pre pre value to be looked up
   * @return array index if found, {@code -1} if not
   */
  int findPre(final int pre) {
    return Arrays.binarySearch(pres, pre);
  }
}