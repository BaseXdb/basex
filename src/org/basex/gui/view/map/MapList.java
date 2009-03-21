package org.basex.gui.view.map;

import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.gui.view.ViewData;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Stores an integer array of pre values and their corresponding weights.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
class MapList extends IntList {
  /** Weights array. */
  double[] weight;
  
  /**
   * Constructor.
   */
  MapList() {
  }
  
  /**
   * Constructor, specifying an initial array.
   * @param v initial list values
   */
  MapList(final int[] v) {
    super(v);
  }
  
  @Override
  public void sort() {
    sort(weight, false);
  }

  /**
   * Initializes the weights giving each node of this level same weight.
   */
  void initWeights() {
    weight = new double[size];
    for(int i = 0; i < size; i++) weight[i] = 1d / size;
  }
  
  /**
   * Initializes the weights of each list entry, using the text length of
   * nodes or (if the array reference is null) the size attributes.
   * @param textLen array holding pre values to text lengths
   * @param nchildren reference number of nodes
   * @param data reference
   */
  void initWeights(final int[] textLen, final int nchildren, final Data data) {
    weight = new double[size];

    // only children
    if(GUIProp.mapweight == 0) {
      for(int i = 0; i < size; i++) {
        weight[i] = (double) ViewData.size(data, list[i]) / nchildren;
      }
      return;
    }

    // summarize sizes
    final double sizeP = GUIProp.mapweight / 100d;
    long sum = 0;
    for(int i = 0; i < size; i++) sum += weight(textLen, data, i);

    // use #children and size for weight
    if(sizeP < 1) {
      for(int i = 0; i < size; i++) {
        weight[i] = sizeP * weight(textLen, data, i) / sum + 
          (1 - sizeP) * ViewData.size(data, list[i]) / nchildren;
      }
    // only sizes
    } else {
      for(int i = 0; i < size; i++) {
        weight[i] = weight(textLen, data, i) / sum;
      }
    }
  }

  /***
   * Returns the numeric weight for the specified input, or 1 as minimum.
   * @param textLen array holding pre values to text lengths
   * @param data data reference
   * @param i array index
   * @return calculated weight
   */
  private double weight(final int[] textLen, final Data data, final int i) {
    double d = 0;
    if(textLen != null) {
      d = textLen[list[i]];
    } else {
      final byte[] val = data.attValue(data.sizeID, list[i]);
      d = val != null ? Token.toLong(val) : 0;
    }
    return d > 1 ? d : 1;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "[");
    for(int i = 0; i < size; i++) {
      sb.append((i == 0 ? "" : ", ") + list[i]);
      if(weight != null) sb.append("/" + weight[i]);
    }
    return sb.append("]").toString();
  }
}