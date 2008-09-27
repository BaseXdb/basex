package org.basex.gui.view.scatter;

import java.math.BigDecimal;

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
  private int nrCats;
  /** Type of data. */
  int numType;
  /** Data type integer. */
  static final int TYPEINT = 0;
  /** Data type double. */
  static final int TYPEDBL = 1;

  /** Coordinates of items. */
  double[] co;
  /** Number of captions to display. */
  int nrCaptions;
  /** Step for axis caption. */
  double captionStep;
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
    if(attr == null) return;
    if(Token.eq(attr, Token.token("")))
      return;
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
      nrCats = cats.length;
//      nrCaptions = nrCats;
    }

    final int[] items = scatterData.pres;
    co = new double[items.length];
    for(int i = 0; i < items.length; i++) {
      int p = items[i];
      final int limit = p + data.size(p, Data.ELEM);
      double currentValue = -1;
      p++;
      while(p < limit) {
        final int kind = data.kind(p);
        if(kind == Data.ELEM) {
          final byte[] currName = data.tag(p);
          if((Token.eq(attr, currName)) && isTag) {
            final int attSize = data.attSize(p, kind);
            final byte[] value = data.text(p + attSize);
            currentValue = calcPosition(value);
            break;
          }
        } else if(kind == Data.ATTR) {
          final byte[] currName = data.attName(p);
          if((Token.eq(attr, currName)) && !isTag) {
            final byte[] value = data.attValue(p);
            currentValue = calcPosition(value);
            break;
          }
        }
        p++;
      }
      co[i] = currentValue;
    }
  }
  
  /**
   * Calculates the relative position of an item in the plot for a given value.
   * @param value item value
   * @return relative x or y value of the item
   */
  private double calcPosition(final byte[] value) {
    final double d = Token.toDouble(value);
    double percentage = 0d;
    if(numeric) {
      final double range = max - min;
      if(range == 0) {
        percentage = 0.5d;
      } else {
        percentage = 1 / range * (d - min);
      }
    } else {
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
      if(Math.abs(pos - posI) <= 0.3d) {
        return Token.string(cats[posI]);
      }
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
          int tmpMax = (maxI + tmpStep - 1) / tmpStep * tmpStep;
          nrCaptions = (tmpMax - tmpMin) / tmpStep + 1;
          if(String.valueOf(tmpStep).startsWith("1"))
            tmpStep *= tmpStep == 1 ? 5 : 2.5;
          else
            tmpStep *= 2;
        } while(nrCaptions * ScatterView.CAPTIONWHITESPACE > space);
        
      } else {
        nrCaptions = 5;
        captionStep = .5d;
      }
    } else {
      nrCaptions = nrCats;
    }
  }
}