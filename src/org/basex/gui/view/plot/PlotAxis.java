package org.basex.gui.view.plot;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.util.IntList;
import org.basex.util.Set;

/**
 * Axis component of the scatter plot visualization.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class PlotAxis {
  /** Text length limit for text to number transformation. */
  private static final int TEXTLENGTH = 11;
  /** Plot data reference. */
  private final PlotData plotData;
  /** Tag reference to selected attribute. */
  int attrID;
  /** True if attribute is a tag, false if attribute. */
  boolean isTag;
  /** Number of different categories for x attribute. */
  int nrCats;
  /** Data type. */
  Kind type;

  /** Coordinates of items. */
  double[] co = {};
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
  /** Significant value for axis caption. */
  double sigVal;
  /** The first category for text data. */
  byte[] firstCat;
  /** The last category for text data. */
  byte[] lastCat;
  
  /**
   * Constructor.
   * @param data scatte data reference
   */
  PlotAxis(final PlotData data) {
    plotData = data;
  }
  
  /**
   * (Re)Initializes axis. 
   */
  private void initialize() {
    isTag = false;
    type = Kind.INT;
    co = new double[0];
    nrCats = -1;
    firstCat = EMPTY;
    lastCat = EMPTY;
    nrCaptions = -1;
    captionStep = -1;
    calculatedCaptionStep = -1;
    min = Integer.MIN_VALUE;
    max = Integer.MAX_VALUE;
    sigVal = 0;
  }
  
  /**
   * Called if the user has changed the caption of the axis. If a new 
   * attribute was selected the positions of the plot items are recalculated.
   * @param attribute attribute selected by the user
   * @return true if new attribute was selected
   */
  boolean setAxis(final String attribute) {
    if(attribute == null) return false;
    initialize();
    byte[] b = token(attribute);
    isTag = !contains(b, '@');
    b = delete(b, '@');
    final Data data = GUI.context.data();
    attrID = isTag ? data.tags.id(b) : data.atts.id(b);
    refreshAxis();
    return true;
  }

  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting. If the selected attribute
   * is of kind TEXT, it is treated as INT.
   */
  void refreshAxis() {
    if(plotData.size == 0) return;
    final Data data = GUI.context.data();
    final StatsKey key = isTag ? data.tags.stat(attrID) :
      data.atts.stat(attrID);
    type = key.kind;
    if(type == Kind.CAT)
      type = Kind.TEXT;

    final int[] items = plotData.pres;
    co = new double[items.length];
    byte[][] vals = new byte[items.length][];
    for(int i = 0; i < items.length; i++) {
      byte[] value = getValue(items[i]);
      if(type == Kind.TEXT && value.length > TEXTLENGTH) {
        value = substring(value, 0, TEXTLENGTH);
      }
      vals[i] = value;
    }
    
    if(type == Kind.TEXT)
      textToNum(vals);
    else {
      calcExtremeValues(vals);
      // coordinates for TEXT already calculated in textToNum()
      for(int i = 0; i < vals.length; i++)
        co[i] = calcPosition(vals[i]);
    }
    vals = null;
  }
  
  /**
   * TEXT data is transformed to categories, meaning each unique string forms
   * a category. The text values of the items are first sorted. The coordinate
   * for an item is then calculated as the position of the text value of this
   * item in the sorted category set.   
   * @param vals item values
   */
  private void textToNum(final byte[][] vals) {
    // sort text values alphabetical (asc).
    Set set = new Set();
    for(int i = 0; i < vals.length; i++) {
      set.add(vals[i]);
    }
    
    // get sorted indexes for values
    final int[] tmpI = IntList.createOrder(vals, false, true).finish();
    final int vl = vals.length;
    int i = 0;
    
    // find first non empty value
    while(i < vl && vals[i].length == 0) {
      co[tmpI[i]] = -1;
      i++;
    }

    // 2 additional empty categories are added for layout reasons -> +2
    nrCats = set.size();
    // empty string is treated as non existing value -> coordinate = -1
    if(i > 0) nrCats--;
    set = null;
    
    // get first/last category for axis caption
    firstCat = i < vl ? vals[i] : EMPTY;
    lastCat = i < vl ? vals[vl - 1] : EMPTY;
    
    // number of current category / position of item value in ordered 
    // text set.
    int p = 0;
    while(i < vl) {
      // next string category to be tested
      final byte[] b = vals[i];
      // l: highest index in sorted array for value b
      int l = i;
      // determining highest index of value/category b
      while(l < vl && eq(vals[l], b)) {
        l++;
      }
      // calculating positions for all items with value b in current category.
      while(i < l) {
        // centering items if only a single category exists (.5d)
        final double d = nrCats != 1 ? (1.0d / (nrCats - 1)) * p : .5d;
        co[tmpI[i]] = d;
        i++;
      }
      p++;
    }
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * The position for a TEXT value of an item is calculated in textToNum().
   * @param value item value
   * @return relative x or y value of the item
   */
  double calcPosition(final byte[] value) {
    if(value.length == 0) {
      return -1;
    }
    
    final double d = toDouble(value);
    final double range = max - min;
    if(range == 0)
      return 0.5d;
    else
      return 1 / range * (d - min);
  }
  
  /**
   * Calculates relative coordinate for a given value.
   * @param value given value
   * @return relative coordinate
   */
  double calcPosition(final double value) {
    return calcPosition(token(value));
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
   * Afterwards the extremes are rounded to support a decent axis
   * caption.
   * @param vals item values
   */
  private void calcExtremeValues(final byte[][] vals) {
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
    if(max - min == 0) return;
//    // range as driving force for following calculations, no matter if INT
//    // or DBL ... whatsoever
//    double range = Math.abs(max - min);
//    
//    if(range < 1) {
//      double dec = 1.0d / range;
//      double pow = (int) (Math.floor(Math.log10(dec) + .5d) + 2) * 2;
//      double fac = (int) (Math.pow(10, pow));
//      double tmin = min * fac;
//      double tmax = max * fac;
//      range = Math.abs(tmax - tmin);
//      final double d = tmin + range * .6d;
//      
//      pow = range < 10 ? 0 : (int) (Math.floor(Math.log10(range) + .5d)) - 1;
//      double lstep = (int) (Math.pow(10, pow));
//      sigVal = d - d % lstep;
//      sigVal /= fac;
//      lstep /= fac;
////      calculatedCaptionStep = lstep;
//      
//      return;
//    }
//    
//    final int pow = range < 10 ? 0 : 
//      (int) (Math.floor(Math.log10(range) + .5d)) - 1;
//    double lstep = (int) (Math.pow(10, pow));
//    final double d = min + range * .6d;
//    sigVal = d - d % lstep;
//    calculatedCaptionStep = lstep;
    
    
    if(min == Integer.MAX_VALUE) min = 0;
    if(max == Integer.MIN_VALUE) max = 0;
    // flag for deepFS @size attribute
    //final Data data = GUI.context.data();
    int fsplus = 6;
    //final boolean fss = data.fs != null && !isTag && 
    //  eq(data.atts.key(attrID), DataText.SIZE);
    final boolean fss = false;

    final double range = max - min;
    final double lmin = min - range / 2;
    final double lmax = max + range / 2;
    final double rangePow = Math.floor(fss ? 10 : Math.log10(range) + .5d);
    final double lstep = (int) (Math.pow(fss ? 2 : 10, 
        fss ? rangePow + fsplus : rangePow));
    calculatedCaptionStep = (int) (Math.pow(fss ? 2 : 10, rangePow - 
        (fss ? -fsplus : 1)));
    
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
    if(type == Kind.DBL || type == Kind.INT) {
      final double range = max - min;
      if(range == 0) {
        nrCaptions = 3;
        captionStep = 0;
        return;
      }
      captionStep = calculatedCaptionStep;
      nrCaptions = (int) (range / captionStep) + 1;
      if(nrCaptions * PlotView.itemSize(false) * 2 > space) {
        captionStep *= 2;
        nrCaptions = (int) (range / captionStep) + 1;
      }
    
    } else {
      nrCaptions = space / (PlotView.CAPTIONWHITESPACE * 3);
      if(nrCaptions > nrCats)
        nrCaptions = nrCats;
      captionStep = 1.0d / (nrCaptions - 1);
    }
  }
}