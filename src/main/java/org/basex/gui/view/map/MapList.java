package org.basex.gui.view.map;

import org.basex.data.Data;
import org.basex.gui.view.ViewData;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.IntList;

/**
 * Stores an integer array of pre values and their corresponding weights.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Joerg Hauser
 */
final class MapList extends IntList {
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
  public MapList sort() {
    sort(weight, false);
    return this;
  }

  /**
   * Initializes the weights of each list entry, using the text length of
   * nodes or (if the array reference is null) the size attributes.
   * @param textLen array holding pre values to text lengths
   * @param nchildren reference number of nodes
   * @param data reference
   * @param w weight
   */
  void initWeights(final int[] textLen, final int nchildren, final Data data,
      final int w) {
    weight = new double[size];

    // only children
    if(w == 0) {
      for(int i = 0; i < size; ++i) {
        weight[i] = (double) ViewData.size(data, list[i]) / nchildren;
      }
      return;
    }

    // summarize sizes
    final double sizeP = w / 100d;
    long sum = 0;
    for(int i = 0; i < size; ++i) sum += weight(textLen, data, i);

    // use #children and size for weight
    if(sizeP < 1) {
      for(int i = 0; i < size; ++i) {
        weight[i] = sizeP * weight(textLen, data, i) / sum +
          (1 - sizeP) * ViewData.size(data, list[i]) / nchildren;
      }
    // only sizes
    } else {
      for(int i = 0; i < size; ++i) {
        weight[i] = weight(textLen, data, i) / sum;
      }
    }
  }

  /**
   * Returns the numeric weight for the specified input, or 1 as minimum.
   * @param textLen array holding pre values to text lengths
   * @param data data reference
   * @param i array index
   * @return calculated weight
   */
  private double weight(final int[] textLen, final Data data, final int i) {
    double d;
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
    final StringBuilder sb = new StringBuilder(Util.name(this) + '[');
    for(int i = 0; i < size; ++i) {
      sb.append((i == 0 ? "" : ", ") + list[i]);
      if(weight != null) sb.append("/" + weight[i]);
    }
    return sb.append(']').toString();
  }
}