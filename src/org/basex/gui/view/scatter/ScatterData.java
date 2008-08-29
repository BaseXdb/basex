package org.basex.gui.view.scatter;

import java.util.Arrays;
import java.util.LinkedList;

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
  /** Items x coordinates relative. */
  Double[] x;
  /** Items y coordinates relative. */
  Double[] y;
  /** Number of items displayed in plot. */ 
  int size;
  
  /** A temporary list for pre values which match the given item. */
  private IntList tmpPres;
  /** A temporary list for x coordinates. */
  private LinkedList<Double> tmpX;
  /** A temporary list for y coordinates. */
  private LinkedList<Double> tmpY;
  /** Item token selected by user. */
  private byte[] item;
  /** X attribute selected by user. */
  private byte[] attrX;
  /** Y attribute selected by user. */
  private byte[] attrY;
  /** Holds minimum x value in case x attribute is numerical. */
  private double xMin;
  /** Holds maximum x value in case x attribute is numerical. */
  private double xMax;
  /** Holds maximum y value in case y attribute is numerical. */
  private double yMax;
  /** Holds maximum y value in case y attribute is numerical. */
  private double yMin;
  /** True if x attribute is a tag, false if attribute. */
  private boolean xIsTag;
  /** True if y attribute is a tag, false if attribute. */
  private boolean yIsTag;
  /** True if x attribute is numerical. */
  private boolean xNumeric;
  /** True if y attribute is numerical. */
  private boolean yNumeric;
  /** Number of different categories for x attribute. */
  private int xNrCats;
  /** Number of different categories for y attribute. */
  private int yNrCats;
  /** The different categories for the x attribute. */
  private byte[][] xCats;
  /** The different categories for the y attribute. */
  private byte[][] yCats;
  /** Token for String operations. */
  private static final byte[] AT = Token.token("@");
  
  /**
   * Default Constructor.
   */
  public ScatterData() {
    size = 0;
    attrX = Token.token("");
    attrY = Token.token("");
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
   * Called if the user has changed the caption of the x axis. If a new 
   * attribute was selected the positions of the plot items are recalculated.
   * @param xAttribute x attribute selected by the user
   * @return true if a new attribute was selected and the plot data has been 
   * recalculated
   */
  boolean setXaxis(final String xAttribute) {
    if(xAttribute == null) return false;
    byte[] b = Token.token(xAttribute);
    final boolean tmp = !Token.contains(b, AT);
    b = Token.delete(b, AT);
//    if(Token.eq(b, attrX)) return false;
    xIsTag = tmp;
    attrX = b;
    refreshCoordinates();
    return true;
  }
  
  /**
   * Called if the user has changed the caption of the y axis. If a new 
   * attribute was selected the positions of the plot items are recalculated.
   * @param yAttribute y attribute selected by the user
   * @return true if a new attribute was selected and the plot data has been 
   * recalculated
   */
  boolean setYaxis(final String yAttribute) {
    if(yAttribute == null) return false;
    byte[] b = Token.token(yAttribute);
    final boolean tmp = !Token.contains(b, AT);
    b = Token.delete(b, AT);
//    if(Token.eq(b, attrY)) return false;
    yIsTag = tmp;
    attrY = b;
    refreshCoordinates();
    return true;
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
    refreshCoordinates();
    return true;
  }
  
  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting.
   */
  void refreshCoordinates() {
    if(Token.eq(attrX, Token.token("")) || Token.eq(attrY, Token.token("")))
      return;
    final Data data = GUI.context.data();
    final StatsKey xKey = xIsTag ? data.tags.stat(data.tags.id(attrX)) :
      data.atts.stat(data.atts.id(attrX));
    xNumeric = xKey.kind == Kind.INT || xKey.kind == Kind.DBL;
    if(xNumeric) {
      xMin = xKey.min;
      xMax = xKey.max;
    } else {
      xCats = xKey.cats.keys();
      xNrCats = xCats.length;
      System.out.println(xNrCats);
    }
    final StatsKey yKey = yIsTag ? data.tags.stat(data.tags.id(attrY)) :
      data.atts.stat(data.atts.id(attrY));
    yNumeric = yKey.kind == Kind.INT || yKey.kind == Kind.DBL;
    if(yNumeric) {
      yMin = yKey.min;
      yMax = yKey.max;
    } else {
      yCats = yKey.cats.keys();
      yNrCats = yCats.length;
      System.out.println(yNrCats);
    }
    
    tmpPres = new IntList();
    tmpX = new LinkedList<Double>();
    tmpY = new LinkedList<Double>();
    final int s = data.size;
    final int itmID = data.tagID(item);
    int p = 1;
    while(p < s) {
      final int kind = data.kind(p);
      if(kind == Data.ELEM) {
        if(data.tagID(p) == itmID) {
          findItemValue(p, data);
        }
        p += data.attSize(p, kind);
      } else {
        p++;
      }
    }
    pres = tmpPres.finish();
    size = pres.length;
    x = new Double[size];
    y = new Double[size];
    tmpX.toArray(x);
    tmpY.toArray(y);
  }
  
  /**
   * Searches the database for the x and y values of a given item. Starts at 
   * the given pre value of the item. 
   * @param pre pre value of the given item
   * @param data data reference
   */
  private void findItemValue(final int pre, final Data data) {
    final int limit = pre + data.size(pre, Data.ELEM);
    double currX = -1;
    double currY = -1;
    int p = pre;
    p++;
    while(p < limit) {
      final int kind = data.kind(p);
      if(kind == Data.ELEM) {
        final byte[] currName = data.tag(p);
        boolean isXvalue = false;
        if((isXvalue = Token.eq(attrX, currName)) && xIsTag || 
            Token.eq(attrY, currName) && yIsTag) {
          final int attSize = data.attSize(p, kind);
          final byte[] value = data.text(p + attSize);
          if(isXvalue) { 
            currX = calcPosition(true, value);
          } else {
            currY = calcPosition(false, value);
          }
        }
      } else if(kind == Data.ATTR) {
        boolean isXvalue = false;
        final byte[] currName = data.attName(p);
        if((isXvalue = Token.eq(attrX, currName)) && !xIsTag || 
            Token.eq(attrY, currName) && !yIsTag) {
          final byte[] value = data.attValue(p);
          if(isXvalue) {
            currX = calcPosition(true, value);
          } else {
            currY = calcPosition(false, value);
          }
        }
      }
      p++;
    }
    tmpPres.add(pre);
    tmpX.add(currX);
    tmpY.add(currY);
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * @param calcX true if x value is calculated
   * @param value item value
   * @return relative x or y value of the item
   */
  private double calcPosition(final boolean calcX, final byte[] value) {
    final double d = Token.toDouble(value);
    double percentage = 0d;
    if(calcX) {
      final double range = xMax - xMin;
      percentage = 1 / range * (d - xMin);
    } else {
      final double range = yMax - yMin;
      percentage = 1 / range * (d - yMin);
    }
    return percentage;
  }
}
