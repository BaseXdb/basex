package org.basex.query.xpath.item;

import java.util.Arrays;
import org.basex.util.Array;

/**
 * NodeSet Constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class NodeBuilder {
  /** Node array. */
  public int[] nodes;
  /** Size of node array. */
  public int size;
  /** Sorting flag. */
  private boolean sort;
  /** FTPos values. */
  public int[][] pos;
  /** FTPointer values. */
  public int[][] poi;
  
  
  /**
   * Constructor, creating an empty node set.
   */
  public NodeBuilder() {
    nodes = new int[1];
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   */
  public NodeBuilder(final int[] ids) {
    if(ids.length == 0) {
      size = 0;
      nodes = new int[1];
    } else {
      nodes = ids;
      size = ids.length;
    }
    pos = null;
  }

  /**
   * Constructor, creating a new node set from the specified node ids.
   * @param ids node ids
   * @param p ftpos values
   */
 /* public NodeBuilder(final int[] ids, final int[][] p) {
    if(ids.length == 0) {
      size = 0;
      nodes = new int[1];
    } else {
      nodes = ids;
      size = ids.length;
    }
    pos = p;
  }

  */
  /**
   * Adds a pre value to the node set.
   * @param pre value to be added.
   */
  public void add(final int pre) {
    if(size == nodes.length) nodes = Array.extend(nodes);
    if(!sort && size != 0) {
      final int d = pre - nodes[size - 1];
      if(d == 0) return;
      sort = d <= 0;
    }
    nodes[size++] = pre;
  }
  
  /**
   * Adds position and pointer values.
   * 
   * @param ps ftposition values
   * @param pi ftpointer values
   */
  public void addPosPoiToLastPre(final int[] ps, final int[] pi) {
    if (pos == null) {
      pos = new int[1][];
      pos[0] = ps;
      poi = new int[1][];
      poi[0] = pi;
    } else {
      if (size - 1 == pos.length) pos = Array.extend(pos);
      if (size - 1 == poi.length) poi = Array.extend(poi);
      poi[size - 1] = pi;
      pos[size - 1] = ps;
    }
  }

  /**
   * Adds a pre value to the node set.
   * @param pre value to be added.
   * @param po posvalues for pre values
   * @param pi pointer for pre values
   * 
   */
  public void add(final int pre, final int[] po, final int[] pi) {
    if(size == nodes.length) {
      nodes = Array.extend(nodes);
      pos = Array.extend(pos);
      poi = Array.extend(poi);
    }
    if(!sort && size != 0) {
      final int d = pre - nodes[size - 1];
      if(d == 0) {
        pos[size - 1] = Array.merge(pos[size - 1], po);
        poi[size - 1] = Array.merge(poi[size - 1], pi);
        return;
      }
      sort = d <= 0;
    }
    nodes[size++] = pre;
    pos[size - 1] = po;
    poi[size - 1] = pi;
  }

  
  /**
   * Adds a node set.
   * @param build node set to be added.
   */
  public void add(final NodeBuilder build) {
    final int[] set = build.nodes;
    final int sl = build.size;
    if(sl == 0) return;

    if(sl == 1) {
      if(build.pos != null) add(set[0], build.pos[0], build.poi[0]); 
      else add(set[0]);
    } else {
      final int s = size + sl;
      int t = nodes.length;
      while(t <= s) t <<= 1;
      if(t != nodes.length) {
        nodes = Array.resize(nodes, size, t);
        if (pos != null && build.pos != null) pos = Array.resize(pos, size, t);
        if (poi != null && build.poi != null) poi = Array.resize(poi, size, t);
      }
      System.arraycopy(set, 0, nodes, size, sl);
      if (pos != null) {
        if (build.pos != null)
          System.arraycopy(build.pos, 0, pos, size, sl);
      } else pos = build.pos;
      if (poi != null) {
        if (build.poi != null)
          System.arraycopy(build.poi, 0, poi, size, sl);
      } else poi = build.poi;

      size = s;
      sort = true;
    }
  }

  /**
   * Reset the NodeSet so it can be reused.
   */
  public void reset() {
    size = 0;
    sort = false;
  }

  /**
   * Returns the node array after sorting and duplicate elimination.
   * @return node array.
   */
  public int[] finish() {
    if(sort && size >= 2) {
      Arrays.sort(nodes, 0, size);
      int j = 0;
      for(int i = 1; i != size; i++) {
        if(nodes[j] != nodes[i]) nodes[++j] = nodes[i];
      }
      size = ++j;
      sort = false;
    }
    return Array.finish(nodes, size);
  }
  
  /**
   * Finish ftposition values.
   * @return ftposition array
   */
  public int[][] finishPos() {
    if (sort) return null;
    return Array.finish(pos, size);
  }
  
  /**
   * Finish ftposition pointer values.
   * @return ftposition pointer array
   */
  public int[][] finishPoi() {
    if (sort) return null;
    return Array.finish(poi, size);
  }
}
