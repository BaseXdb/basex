package org.basex.gui.view.scatter;

import java.util.Arrays;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.data.StatsKey;
import org.basex.data.StatsKey.Kind;
import org.basex.gui.GUI;
import org.basex.util.Performance;

/**
 * Axis component of the scatter plot visualization.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Lukas Kircher
 */
public final class ScatterAxis {
  /** Token for String operations. */
  private static final byte[] AT = token("@");
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
  /** Calculated caption step, view size not considered for calculation. */
  private double calculatedCaptionStep;
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
    final byte[] b = token(attribute);
    isTag = !contains(b, AT);
    attr = delete(b, '@');
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
    } else {
      cats = key.cats.keys();
      final String[] tmpCats = new String[cats.length];
      for(int i = 0; i < tmpCats.length; i++) {
        tmpCats[i] = string(cats[i]);
      }
      Arrays.sort(tmpCats);
      for(int i = 0; i < tmpCats.length; i++) {
        cats[i] = token(tmpCats[i]);
      }
      nrCats = cats.length;
    }

    final int[] items = scatterData.pres;
    co = new double[items.length];
    vals = new byte[items.length][];
    for(int i = 0; i < items.length; i++) {
      int p = items[i];
      final int limit = p + data.size(p, Data.ELEM);
      byte[] value = {};
      p++;
      while(p < limit) {
        final int kind = data.kind(p);
        if(kind == Data.ELEM) {
          final byte[] currName = data.tag(p);
          if((eq(attr, currName)) && isTag) {
            final int attSize = data.attSize(p, kind);
            value = data.text(p + attSize);
            break;
          }
        } else if(kind == Data.ATTR) {
          final byte[] currName = data.attName(p);
          if((eq(attr, currName)) && !isTag) {
            value = data.attValue(p);
            break;
          }
        }
        p++;
      }
      vals[i] = value;
    }
    
    if(numeric)
      calcExtremeValues();
    for(int i = 0; i < vals.length; i++) {
      final byte[] val = vals[i];
      if(val.length > 0) {
        co[i] = calcPosition(val);
      }
    }
    vals = null;
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * @param value item value
   * @return relative x or y value of the item
   */
  double calcPosition(final byte[] value) {
    final double d = toDouble(value);
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
          if(eq(value, cats[i])) {
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
        return Double.toString(d);
      } else {
        int i = (int) (min + (max - min) * relative);
        return Integer.toString(i);
      }
    } else {
      final double pos = relative / (1.0d / (nrCats - 1));
      final int posI = (int) Math.floor(pos + 0.5d);
      if(Math.abs(pos - posI) <= 0.3d)
        return string(cats[posI]);
      return "";
    }
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
    final Performance p = new Performance();
    
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
    final int lmin = (int) (min - (range / 2));
    final int lmax = (int) (max + (range / 2));
    int tmin = (int) Math.floor(min);
    int tmax = (int) Math.ceil(max);
    final double rangePow = Math.floor(Math.log10(range) + .5d);
    calculatedCaptionStep = (int) (Math.pow(10, rangePow - 1));
    final double tmpStepL = (int) (Math.pow(10, rangePow));
    
    boolean sFound = false;
    min = tmin;
    while(tmin > lmin) {
      if(tmin % tmpStepL == 0) {
        min = tmin;
        break;
      }
      if(!sFound && tmin % calculatedCaptionStep == 0) {
        min = tmin;
        sFound = true;
      }
      tmin--;
    }
    
    max = tmax;
    while(tmax < lmax) {
      final int tmpRange = (int) (tmax - min);
      if(tmax % tmpStepL == 0) {
        max = tmax;
        break;
      }
      if(!sFound && tmax % calculatedCaptionStep == 0 && 
          (tmpRange / calculatedCaptionStep) % 2 == 0) {
        max = tmax;
        sFound = true;
      }
      tmax++;
    }
    
    System.out.println(p.getTimer());
  }
  
  /**
   * Calculates axis caption depending on view width / height.
   * @param space space of view axis available for captions
   */
  void calcCaption(final int space) {
    if(numeric) {
      final int range = (int) (max - min);
      captionStep = calculatedCaptionStep;
      nrCaptions = (int) (range / captionStep) + 1;
      if(nrCaptions * ScatterView.CAPTIONWHITESPACE > space) { 
        captionStep *= 2;
        nrCaptions = (int) (range / captionStep) + 1;
      }
      
    } else {
      nrCaptions = nrCats;
    }
  }
}