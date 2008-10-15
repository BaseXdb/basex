package org.basex.gui.view.scatter;

import java.math.BigDecimal;
import java.util.Arrays;

import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.util.Token;

/**
 * Axis component of the scatter plot visualization.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterAxis {
  /** Token for String operations. */
  private static final byte[] AT = Token.token("@");
  /** Scatter data reference. */
  private final ScatterData scatterData;
  /** Attribute selected by user. */
  private byte[] attr;
  /** True if attribute is a tag, false if attribute. */
  private boolean isTag;
  /** True if attribute is numerical. */
  boolean numeric;
  /** Number of different categories for x attribute. */
  int nrCats;
  /** Type of data. */
  int numType;
  /** Data type integer. */
  static final int TYPEINT = 0;
  /** Data type double. */
  static final int TYPEDBL = 1;

  /** Coordinates of items. */
  double[] co = {};
  /** Item values. */
  byte[][] vals = {};
  /** Number of captions to display. */
  int nrCaptions;
  /** Step for axis caption. */
  double captionStep;
  /** Minimum value in case selected attribute is numerical. */
  double min;
  /** Maximum  value in case selected attribute is numerical. */
  double max;
  /** First caption value after minimum. */
  double firstCap;
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
   * Called if the user has changed the caption of the axis. If a new 
   * attribute was selected the positions of the plot items are recalculated.
   * @param attribute attribute selected by the user
   * @return true if new attribute was selected
   */
  boolean setAxis(final String attribute) {
    if(attribute == null) return false;
    byte[] b = Token.token(attribute);
    final boolean tmp = !Token.contains(b, AT);
    b = Token.delete(b, AT);
    isTag = tmp;
    attr = b;
    refreshAxis();
    return true;
  }
  
  /**
   * Refreshes item list and coordinates if the selection has changed. So far
   * only numerical data is considered for plotting.
   */
  void refreshAxis() {
    if(attr == null || attr.length == 0) return;

    final Data data = GUI.context.data();
    final StatsKey key = isTag ? data.tags.stat(data.tags.id(attr)) :
      data.atts.stat(data.atts.id(attr));
    numeric = key.kind == Kind.INT || key.kind == Kind.DBL;
    if(numeric) {
      numType = key.kind == Kind.INT ? TYPEINT : TYPEDBL;
      min = key.min;
      max = key.max;
    } else {
      cats = key.cats.keys();
      final String[] tmpCats = new String[cats.length];
      for(int i = 0; i < tmpCats.length; i++) {
        tmpCats[i] = Token.string(cats[i]);
      }
      Arrays.sort(tmpCats);
      for(int i = 0; i < tmpCats.length; i++) {
        cats[i] = Token.token(tmpCats[i]);
      }
      nrCats = cats.length;
//      nrCaptions = nrCats;
    }

    final int[] items = scatterData.pres;
    co = new double[items.length];
    vals = new byte[items.length][];
    for(int i = 0; i < items.length; i++) {
      int p = items[i];
      final int limit = p + data.size(p, Data.ELEM);
//      double currentValue = -1;
      byte[] value = {};
      p++;
      while(p < limit) {
        final int kind = data.kind(p);
        if(kind == Data.ELEM) {
          final byte[] currName = data.tag(p);
          if((Token.eq(attr, currName)) && isTag) {
            final int attSize = data.attSize(p, kind);
            value = data.text(p + attSize);
//            currentValue = calcPosition(value);
            break;
          }
        } else if(kind == Data.ATTR) {
          final byte[] currName = data.attName(p);
          if((Token.eq(attr, currName)) && !isTag) {
            value = data.attValue(p);
//            currentValue = calcPosition(value);
            break;
          }
        }
        p++;
      }
//      co[i] = currentValue;
      vals[i] = value;
    }
    
    calcExtremeValues();
    for(int i = 0; i < vals.length; i++) {
      final byte[] val = vals[i];
      if(val.length > 0) {
        co[i] = calcPosition(val);
      }
    }
  }
  
  /**
   * Determines the smallest and greatest occurring values for a specific item.
   * Afterwards the extremes are slightly rounded to support a decent axis
   * caption.
   *
   * If range between min/max < 10 extremes are calculated by floor/ceil 
   * function.
   * If range >=10 the minimum is allowed to lie 15% of the range under the
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
      double d = Token.toDouble(vals[i]);
      if(d < min)
        min = d;
      if(d > max) 
        max = d;
    }
    
    final double range = max - min;
    if(range < 10) {
      min = Math.floor(min);
      max = Math.ceil(max);
      return;
    }
    final int lmin = (int) (min - (range / (15 / 100)));
    final int lmax = (int) (max + (range / (15 / 100)));
    int tmin = (int) Math.floor(min);
    int tmax = (int) Math.ceil(max);
    final double rangePow = Math.floor(Math.log10(range) + .5d);
    final int step = (int) (Math.pow(10, rangePow - 1));
    min = tmin;
    while(tmin > lmin) {
      if(tmin % step == 0) {
        min = tmin;
        break;
      } else {
        tmin--;
      }
    }
    max = tmax;
    while(tmax < lmax) {
      if(tmax % step == 0) {
        max = tmax;
        break;
      } else {
        tmax++;
      }
    }
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * @param value item value
   * @return relative x or y value of the item
   */
  double calcPosition(final byte[] value) {
    final double d = Token.toDouble(value);
    double percentage = 0d;
    if(numeric) {
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
          if(Token.eq(value, cats[i])) {
            percentage = (1.0d / (nrCats - 1)) * i;
          }
        }
    }
    return percentage;
  }
  
  /**
   * Returns the value of an attribute for a given relative coordinate.
   * @param relative relative coordinate
   * @return item value
   */
  String getValue(final double relative) {
    if(numeric) {
      if(numType == TYPEDBL) {
        double d = min + (max - min) * relative;
        return Double.toString(roundDouble(d, 2));
      } else {
        int i = (int) (min + (max - min) * relative);
        return Integer.toString(i);
      }
    } else {
      final double pos = relative / (1.0d / (nrCats - 1));
      final int posI = (int) Math.floor(pos + 0.5d);
      if(Math.abs(pos - posI) <= 0.3d)
        return Token.string(cats[posI]);
      return "";
    }
  }
  
  /**
   * Rounds a double value.
   * @param d double value to be rounded
   * @param decs number of decimals
   * @return rounded double value
   */
  public static double roundDouble(final double d, final int decs) {
    BigDecimal bd = new BigDecimal(d);
    BigDecimal rounded = bd.setScale(decs, BigDecimal.ROUND_HALF_UP);
    return rounded.doubleValue();
  }
  
  /**
   * Calculates axis caption depending on view width / height.
   * @param space space of view axis available for captions
   */
  void calcCaption(final int space) {
    if(numeric) {
      if(numType == TYPEINT) {
        final int minI = (int) min;
        final int maxI = (int) max;
        int tmpStep = 1;
        do {
          captionStep = tmpStep;
          int tmpMin = minI - minI % tmpStep;
          int tmpMax = maxI + maxI % tmpStep;
          nrCaptions = (tmpMax - tmpMin) / tmpStep + 1;
          if(String.valueOf(tmpStep).startsWith("1"))
            tmpStep *= tmpStep == 1 ? 5 : 2.5;
          else
            tmpStep *= 2;
        } while(nrCaptions * ScatterView.CAPTIONWHITESPACE > space);
        // calculate first caption value after minimum
        final int l = (int) (min + captionStep);
        firstCap = minI + 1;
        while(firstCap <= l) {
          if(firstCap % captionStep == 0) {
            return;
          }
          firstCap++;
        }
        
      } else if(numType == TYPEDBL) {
        final double minD = min;
        final double maxD = max;
        double tmpStep = .01d;
        if(maxD - minD > 500)
          tmpStep = 1;
        do {
          captionStep = tmpStep;
//          double tmpMin = (int) (minD / tmpStep) * tmpStep;
//          double tmpMax = (int) ((maxD + tmpStep - .01d) / tmpStep) * tmpStep;
          nrCaptions = (int) ((maxD - minD) / tmpStep) + 1;
          if(String.valueOf(tmpStep).indexOf("1") > -1) {
            tmpStep *= 2.5;
          } else {
            tmpStep *= 2;
          }
        } while(nrCaptions * ScatterView.CAPTIONWHITESPACE > space &&
            maxD - minD > tmpStep);
     // calculate first caption value after minimum
        final double l = min + captionStep;
        final double c = Math.ceil(captionStep);
        firstCap = minD + .01d;
        while(firstCap <= l) {
          if(firstCap % c == 0) {
            return;
          }
          firstCap++;
        }
      }
      
    } else {
      nrCaptions = nrCats;
    }
  }
}