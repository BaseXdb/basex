package org.basex.gui.view.plot;

import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.index.stats.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Axis component of the scatter plot visualization.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
final class PlotAxis {
  /** Text length limit for text to number transformation. */
  private static final int TEXTLENGTH = 11;
  /** Plot data reference. */
  private final PlotData plotData;
  /** Reference to selected attribute. */
  int attrID;
  /** Number of different categories for x attribute. */
  int nrCats;
  /** Data type. */
  StatsType type;
  /** Coordinates of items. */
  double[] co = {};
  /** First label to be drawn after minimum label. */
  double startvalue;
  /** The first category for text data. */
  byte[] firstCat;
  /** The last category for text data. */
  byte[] lastCat;
  /** Number of captions to display. */
  int nrCaptions;
  /** Step for axis caption. */
  double actlCaptionStep;
  /** Calculated caption step, view size not considered for calculation. */
  private double calculatedCaptionStep;
  /** Minimum value in case selected attribute is numerical. */
  double min;
  /** Maximum value in case selected attribute is numerical. */
  double max;
  /** Axis uses logarithmic scale. */
  boolean log;

  /** True if attribute is an element, false if attribute. */
  private boolean elem;
  /** Ln of min. */
  private double logMin;
  /** Ln of max. */
  private double logMax;

  /**
   * Constructor.
   * @param data plot data reference
   */
  PlotAxis(final PlotData data) {
    plotData = data;
  }

  /**
   * (Re)Initializes axis.
   */
  private void initialize() {
    elem = false;
    type = StatsType.INTEGER;
    co = new double[0];
    nrCats = -1;
    firstCat = EMPTY;
    lastCat = EMPTY;
    nrCaptions = 0;
    actlCaptionStep = -1;
    calculatedCaptionStep = -1;
    min = Integer.MIN_VALUE;
    max = Integer.MAX_VALUE;
    startvalue = 0;
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
    elem = !contains(b, '@');
    b = delete(b, '@');
    final Data data = plotData.context.data();
    attrID = (elem ? data.elemNames : data.attrNames).id(b);
    refreshAxis();
    return true;
  }

  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting. If the selected attribute
   * is of kind TEXT, it is treated as INT.
   */
  void refreshAxis() {
    final Data data = plotData.context.data();
    final Stats key = elem ? data.elemNames.stat(attrID) :
      data.attrNames.stat(attrID);
    if(key == null) return;
    type = key.type;
    if(type == null) return;
    if(type == StatsType.CATEGORY)
      type = StatsType.TEXT;

    final int[] items = plotData.pres;
    final int il = items.length;
    if(il < 1) return;
    co = new double[il];
    final byte[][] vals = new byte[il][];
    for(int i = 0; i < il; ++i) {
      byte[] value = getValue(items[i]);
      if(type == StatsType.TEXT && value.length > TEXTLENGTH) {
        value = substring(value, 0, TEXTLENGTH);
      }
      vals[i] = lc(value);
    }

    if(type == StatsType.TEXT) {
      textToNum(vals);
    } else {
      minMax(vals);
      // calculations for axis labeling
      if(!log) prepareLinAxis();

      // coordinates for TEXT already calculated in textToNum()
      final int vl = vals.length;
      for(int v = 0; v < vl; ++v) co[v] = calcPosition(vals[v]);
    }
  }

  /**
   * TEXT data is transformed to categories, meaning each unique string forms
   * a category. The text values of the items are first sorted. The coordinate
   * for an item is then calculated as the position of the text value of this
   * item in the sorted category set.
   * @param vals item values
   */
  private void textToNum(final byte[][] vals) {
    // get sorted indexes for values
    final int[] tmpI = Array.createOrder(vals, false, true);
    final int vl = vals.length;
    int i = 0;

    // find first non-empty value
    // empty string is treated as non existing value -> coordinate = -1
    while(i < vl && vals[i].length == 0) co[tmpI[i++]] = -1;

    // count number of unique values
    nrCats = new TokenSet(vals).size();
    if(i > 0) --nrCats;

    // get first/last category for axis caption
    firstCat = i < vl ? vals[i] : EMPTY;
    lastCat = i < vl ? vals[vl - 1] : EMPTY;

    // number of current category/position of item value in ordered text set
    int p = 0;
    while(i < vl) {
      // next string category to be tested
      final byte[] b = vals[i];
      // l: highest index in sorted array for value b
      int l = i;
      // determining highest index of value/category b
      while(l < vl && eq(vals[l], b)) ++l;

      // calculating positions for all items with value b in current category
      while(i < l) {
        // centering items if only a single category exists (.5d)
        final double d = nrCats == 1 ? .5d : 1.0d / (nrCats - 1) * p;
        co[tmpI[i++]] = d;
      }
      ++p;
    }
  }

  /**
   * Calculates the relative position of an item in the plot for a given value.
   * The position for a TEXT value of an item is calculated in
   * {@link #textToNum}.
   * @param value item value
   * @return relative x or y value of the item
   */
  private double calcPosition(final byte[] value) {
    if(value.length == 0) {
      return -1;
    }

    double range = max - min;
    if(range == 0) return 0.5d;
    final double d = toDouble(value);
    if(!log) return 1 / range * (d - min);

    // calculate position on a logarithmic scale. to display negative
    // values on a logarithmic scale, three cases are to be distinguished:
    // 0. both extreme values are greater or equal 0.
    // 1. the minimum value is smaller 0, hence the axis is 'mirrored' at 0.
    // 2. both extreme values are smaller 0; axis is also 'mirrored' and
    //    values above the max value are not displayed.
    range = logMax - logMin;
    // case 1
    if(min < 0 && max >= 0) {
      // p is the portion of the range between minimum value and zero compared
      // to the range between zero and the maximum value.
      // (needed for mirroring, s.a.)
      final double p = 1 / (logMin + logMax) * logMin;
      if(d == 0) return p;
      if(d < 0) return 1.0d - (1 - p) - 1.0d / logMin * ln(d) * p;

      return p + 1.0d / logMax * ln(d) * (1 - p);
    }

    // case 2 and 0
    return 1 / range * (ln(d) - logMin);
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
   * Calculates base e logarithm for the given value.
   * @param d value
   * @return base e logarithm for d
   */
  private static double ln(final double d) {
    return d == 0 ? 0 : StrictMath.log1p(Math.abs(d));
  }

  /**
   * Returns the value for the specified pre value.
   * @param pre pre value
   * @return item value
   */
  byte[] getValue(final int pre) {
    final Data data = plotData.context.data();
    final int limit = pre + data.size(pre, Data.ELEM);
    for(int p = pre; p < limit; ++p) {
      final int kind = data.kind(p);
      if((kind == Data.ELEM && elem || kind == Data.ATTR && !elem) &&
          attrID == data.name(p)) return data.atom(p);
    }
    return EMPTY;
  }

  /**
   * Determines the extreme values of the current data set.
   * @param vals values of plotted nodes
   */
  private void minMax(final byte[][] vals) {
    min = Integer.MAX_VALUE;
    max = Integer.MIN_VALUE;
    int v = -1;
    boolean b = false;
    final int vl = vals.length;
    while(++v < vl) {
      if(vals[v].length > 0) {
        b = true;
        final double d = toDouble(vals[v]);
        if(d < min) min = d;
        if(d > max) max = d;
      }
    }
    if(!b) {
      min = 0;
      max = 0;
    }

    if(log) {
      logMin = ln(min);
      logMax = ln(max);
    }
  }

  /**
   * Executes some calculations to support a dynamic axis labeling for a
   * linear scale.
   */
  private void prepareLinAxis() {
    // range as driving force for following calculations, no matter if INT
    // or DBL ... whatsoever
    double range = Math.abs(max - min);
    if(range == 0) return;

    // small ranges between min and max value
    if(range < 1) {
      final double dec = 1.0d / range;
      double pow = (int) (Math.floor(StrictMath.log10(dec) + .5d) + 1) * 2;
      final double fac = (int) StrictMath.pow(10, pow);
      final double tmin = min * fac;
      final double tmax = max * fac;
      range = Math.abs(tmax - tmin);

      pow = range < 10 ? 0 : (int) Math.floor(StrictMath.log10(range) + .5d) - 1;
      calculatedCaptionStep = (int) StrictMath.pow(10, pow);
      calculatedCaptionStep /= fac;
      return;
    }

    final int pow = range < 10 ? 0 :
      (int) Math.floor(StrictMath.log10(range) + .5d) - 1;
    calculatedCaptionStep = (int) StrictMath.pow(10, pow);
  }

  /**
   * Calculates axis caption depending on view width / height.
   * @param space space of view axis available for captions
   */
  void calcCaption(final int space) {
    if(type == StatsType.DOUBLE || type == StatsType.INTEGER) {
      final double range = Math.abs(max - min);
      if(range == 0) {
        nrCaptions = 1;
        return;
      }

      // labeling for logarithmic scale
      if(log) {
        startvalue = min;
        nrCaptions = 3;
        return;
      }

      // labeling for linear scale
      final boolean dbl = type == StatsType.DOUBLE;
      actlCaptionStep = calculatedCaptionStep;
      nrCaptions = (int) (range / actlCaptionStep) + 1;
      while(2 * nrCaptions * PlotView.CAPTIONWHITESPACE * 3 < space &&
          (dbl || actlCaptionStep % 2 == 0)) {
        actlCaptionStep /= 2;
        nrCaptions = (int) (range / actlCaptionStep);
      }
      while(nrCaptions * PlotView.CAPTIONWHITESPACE * 3 > space) {
        actlCaptionStep *= 2;
        nrCaptions = (int) (range / actlCaptionStep);
      }
      // calculate first value to be drawn
      startvalue = min + actlCaptionStep - min % actlCaptionStep;
      if(startvalue - min < actlCaptionStep / 4) startvalue += actlCaptionStep;

      // type == TEXT / CAT
    } else {
      nrCaptions = space / (PlotView.CAPTIONWHITESPACE * 3);
      if(nrCaptions > nrCats)
        nrCaptions = nrCats;
      actlCaptionStep = 1.0d / (nrCaptions - 1);
    }
  }
}