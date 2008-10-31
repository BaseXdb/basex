package org.basex.gui.view.scatter;

import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.util.IntList;
import org.basex.util.Set;
import org.basex.util.TokenList;

/**
 * Axis component of the scatter plot visualization.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterAxis {
  /** Text length limit for text to number transformation. */
  private static final int TEXTLENGTH = 11;
  /** Scatter data reference. */
  private final ScatterData scatterData;
  /** Tag reference to selected attribute. */
  int attrID;
  /** Constant to determine whether axis attribute equals deepFS "@size". */
  private static final byte[] ATSIZE = token("size");
  /** True if attribute is a tag, false if attribute. */
  boolean isTag;
  /** Number of different categories for x attribute. */
  int nrCats;
  /** Data type. */
  Kind type;

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
    type = Kind.INT;
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
    final Data data = GUI.context.data();
    final StatsKey key = isTag ? data.tags.stat(attrID) :
      data.atts.stat(attrID);
    type = key.kind;

    if(type == Kind.CAT) {
      final TokenList tl = new TokenList();
      tl.add(EMPTY);
      for(final byte[] k : key.cats.keys()) tl.add(k);
      tl.sort(true);
      tl.add(EMPTY);
      cats = tl.finish();
      nrCats = cats.length;
    }

    final int[] items = scatterData.pres;
    co = new double[items.length];
    vals = new byte[items.length][];
    for(int i = 0; i < items.length; i++) {
      byte[] value = getValue(items[i]);
      if(type == Kind.TEXT && value.length > TEXTLENGTH) {
        value = substring(value, 0, TEXTLENGTH);
      }
      vals[i] = value;
    }
    
    if(type != Kind.CAT) {
      if(type == Kind.TEXT) {
        textToNum();
      } else {
        calcExtremeValues();
      }
    }
    // coordinates for TEXT already calculated in textToNum()
    if(type != Kind.TEXT) {
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
   */
  private void textToNum() {
    // sort text values alphabetical (asc).
    Set set = new Set();
    for(int i = 0; i < vals.length; i++) {
      set.add(vals[i]);
    }
    // items lacking values are painted outside of plot and 2 additional empty
    // categories are added for layout reasons -> +2
    nrCats = set.size() + 2;
    set = null;
    
    // get sorted indexes for values
    final int[] tmpI = IntList.createOrder(vals, false, true).finish();
    final int vl = vals.length;
    int i = 0;
    
    // find first non empty value
    while(i < vl && vals[i].length == 0) {
      co[tmpI[i]] = -1;
      i++;
    }
    
    // number of current category / position of item value in ordered 
    // text set. first category remains empty for layout reasons
    int p = 1;
    while(i < vl) {
      // next string category to be tested
      final byte[] b = vals[i];
      // l: highest index for value b
      int l = i;
      // determing highest index of value/category b
      while(l < vl && eq(vals[l], b)) {
        l++;
      }
      // calculating positions for all items with value b in current category
      while(i < l) {
        final double d = (1.0d / (nrCats - 1)) * p;
        co[tmpI[i]] = d;
        i++;
      }
      p++;
    }
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
    if(type != Kind.CAT) {
      final double d = toDouble(value);
      final double range = max - min;
      if(range == 0)
        
        percentage = 0.5d;
      else
        percentage = 1 / range * (d - min);
     
    } else {
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
   * Afterwards the extremes are rounded to support a decent axis
   * caption.
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
    if(max - min == 0) return;
    
    // flag for deepFS @size attribute
    final Data data = GUI.context.data();
    int fsplus = 6;
    final boolean fss = data.fs != null && eq(data.atts.key(attrID), ATSIZE);
//    final boolean fss = false;
    final double range = max - min;
    final double lmin = min - range / 2;
    final double lmax = max + range / 2;
    final double rangePow = Math.floor(fss ? 
        Math.log(Math.pow(Math.E, Math.exp(2))) : Math.log10(range) + .5d);
    final double lstep = (int) (Math.pow(fss ? 2 : 10, 
        fss ? rangePow + fsplus : rangePow));
    calculatedCaptionStep = (int) (Math.pow(fss ? 2 : 10, rangePow - 
        (fss ? -fsplus : 1)));
//    calculatedCaptionStep = (int) (Math.pow(10, rangePow - 1));
    
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
      if(nrCaptions * ScatterView.itemSize(false) * 2 > space) {
        captionStep *= 2;
        nrCaptions = (int) (range / captionStep) + 1;
      }
    
    } else if(type == Kind.CAT) {
      nrCaptions = nrCats;
    
    } else {
//      if(nrCats > 20) {
//        final int c = space / (3 * ScatterView.CAPTIONWHITESPACE);
//        nrCaptions = c >= 2 ? c : 2;
//      } else {
//        nrCaptions = nrCats;
//      }
      nrCaptions = 2;
    }
  }
}