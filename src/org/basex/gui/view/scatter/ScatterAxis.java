package org.basex.gui.view.scatter;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.util.Performance;
import org.basex.util.TokenList;

/**
 * Axis component of the scatter plot visualization.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterAxis {
  /** Text length limit for text to number transformation. */
  private static final int TEXTLENGTH = 8;
  /** Scatter data reference. */
  private final ScatterData scatterData;
  /** Tag reference to selected attribute. */
  private int attrID;
  /** True if attribute is a tag, false if attribute. */
  private boolean isTag;
  /** True if attribute is numerical. */
  boolean numeric;
  /** Number of different categories for x attribute. */
  int nrCats;
  /** Data type. */
  Kind numType;

  /** Coordinates of items. */
  double[] co = {};
  /** Item values. */
  byte[][] vals = {};
  /** Number of captions to display. */
  int nrCaptions;
  /** Step for axis caption. */
  double captionStep;
  /** Calculated caption step, view size not considered for calculation. */
  private double calculatedCaptionStep;
  /** Minimum value in case selected attribute is numerical. */
  double min;
  /** Maximum  value in case selected attribute is numerical. */
  double max;
  /** The different categories for the x attribute. */
  byte[][] cats;
  
  /**
   * Constructor.
   * @param data scatte data reference
   */
  ScatterAxis(final ScatterData data) {
    scatterData = data;
  }
  
  /**
   * (Re)Initializes axis. 
   */
  private void initialize() {
    isTag = false;
    numeric = false;
    numType = Kind.INT;
    co = new double[0];
    vals = new byte[0][];
    nrCats = -1;
    cats = new byte[0][];
    nrCaptions = -1;
    captionStep = -1;
    calculatedCaptionStep = -1;
    min = Integer.MIN_VALUE;
    max = Integer.MAX_VALUE;
  }
  
  /**
   * Called if the user has changed the caption of the axis. If a new 
   * attribute was selected the positions of the plot items are recalculated.
   * @param attribute attribute selected by the user
   * @return true if new attribute was selected
   */
  boolean setAxis(final String attribute) {
    if(attribute == null) return false;
    final byte[] b = token(attribute);
    initialize();
    refreshAxis(b);
    return true;
  }
  
  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting. If the selected attribute
   * is of kind TEXT, it is treated as INT.
   * @param attr attribute
   */
  void refreshAxis(final byte[] attr) {
    if(attr.length == 0) return;
    isTag = !contains(attr, '@');
    final byte[] a = delete(attr, '@');

    final Data data = GUI.context.data();
    attrID = isTag ? data.tags.id(a) : data.atts.id(a);

    final StatsKey key = isTag ? data.tags.stat(attrID) :
      data.atts.stat(attrID);
    numeric = key.kind == Kind.INT || key.kind == Kind.DBL || 
      key.kind == Kind.TEXT;

    if(numeric) {
      numType = key.kind;
    } else {
      final TokenList tl = new TokenList();
      for(final byte[] k : key.cats.keys()) tl.add(k);
      tl.sort(true);
      cats = tl.finish();
      nrCats = cats.length;
    }

    final int[] items = scatterData.pres;
    co = new double[items.length];
    vals = new byte[items.length][];
    for(int i = 0; i < items.length; i++) {
      byte[] value = getValue(items[i]);
      if(numeric && numType == Kind.TEXT && value.length > TEXTLENGTH) {
        value = substring(value, 0, TEXTLENGTH);
      }
      vals[i] = value;
    }
    
    if(numeric) {
      if(numType == Kind.TEXT) textToNum();
      calcExtremeValues();
    }
    for(int i = 0; i < vals.length; i++) {
      co[i] = calcPosition(vals[i]);
    }
    vals = null;
  }
  
  /**
   * Transforms TEXT data to numerical data by summing up the unicode value
   * of the first four characters. If a node has less than four characters
   * the value of the empty ones is 0. 
   */
  private void textToNum() {
    Performance perf = new Performance();
    final double p = Math.pow(10, 5);
    for(int i = 0; i < vals.length; i++) {
      int v = 0;
      final int vl = vals[i].length;
      for(int j = 0; j < vl; j++) {
        v += ((vals[i][j] & 0xFF) << ((TEXTLENGTH - 1 - j) << 3)) % p;
      }
      vals[i] = token(v);
    }
    System.out.println(perf.getTimer());
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * @param value item value
   * @return relative x or y value of the item
   */
  double calcPosition(final byte[] value) {
    if(value.length == 0) {
      return -1;
    }
    
    double percentage = 0d;
    if(numeric) {
      final double d = toDouble(value);
      final double range = max - min;
      if(range == 0)
        
        percentage = 0.5d;
      else
        percentage = 1 / range * (d - min);
     
    } else {
      if(nrCats == 1)
        percentage = 0.5d;
      else
        for(int i = 0; i < nrCats; i++) {
          if(eq(value, cats[i])) {
            percentage = (1.0d / (nrCats - 1)) * i;
          }
        }
    }
    return percentage;
  }
  
  /**
   * Returns the value for the specified pre value.
   * @param pre pre value
   * @return item value
   */
  byte[] getValue(final int pre) {
    final Data data = GUI.context.data();
    final int limit = pre + data.size(pre, Data.ELEM);
    for(int p = pre; p < limit; p++) {
      final int kind = data.kind(p);
      if(kind == Data.ELEM && isTag && attrID == data.tagID(p)) {
        return data.atom(p);
      }
      if(kind == Data.ATTR && !isTag && attrID == data.attNameID(p)) {
        return data.atom(p);
      }
    }
    return EMPTY;
  }
  
  /**
   * Determines the smallest and greatest occurring values for a specific item.
   * Afterwards the extremes are slightly rounded to support a decent axis
   * caption.
   *
   * If range between min/max < 10 extremes are calculated by floor/ceil 
   * function.
   * If range >=10 the minimum is allowed to lie 25% of the range under the
   * actual minimum. After that the final extreme is calculated as the value
   * between the smallest allowed minimum and the actual minimum which is 
   * dividable by pow(10, log10(range)-1).
   * 
   * Maximum v.v. . 
   */
  private void calcExtremeValues() {
    min = Integer.MAX_VALUE;
    max = Integer.MIN_VALUE;
    for(int i = 0; i < vals.length; i++) {
      if(vals[i].length < 1)
        continue;
      double d = toDouble(vals[i]);
      if(d < min)
        min = d;
      if(d > max) 
        max = d;
    }
    
    final double range = max - min;
    final double lmin = min - range / 2;
    final double lmax = max + range / 2;
    final double rangePow = Math.floor(Math.log10(range) + .5d);
    final double lstep = (int) (Math.pow(10, rangePow));
    calculatedCaptionStep = (int) (Math.pow(10, rangePow - 1));
    
    // find minimum axis assignment
    double c = Math.floor(min);
    double m = c - c % calculatedCaptionStep;
    if(m > lmin) min = m;
    m = c - c % lstep;
    if(m > lmin) min = m;
    
    // find maximum axis assignment
    c = Math.ceil(max);
    boolean f = false;
    while(c < lmax) {
      if(c % lstep == 0) {
        max = c;
        break;
      }
      if(!f && c % calculatedCaptionStep == 0 && 
          ((c - min) / calculatedCaptionStep) % 2 == 0) {
        max = c;
        f = true;
      }
      c -= c % calculatedCaptionStep;
      c += calculatedCaptionStep;
    }
  }
  
  /**
   * Calculates axis caption depending on view width / height.
   * @param space space of view axis available for captions
   */
  void calcCaption(final int space) {
    if(numeric) {
      final double range = max - min;
      captionStep = calculatedCaptionStep;
      nrCaptions = (int) (range / captionStep) + 1;
      if(nrCaptions * ScatterView.itemSize(false) * 2 > space) {
        captionStep *= 2;
        nrCaptions = (int) (range / captionStep) + 1;
      }
    } else {
      nrCaptions = nrCats;
    }
  }
}