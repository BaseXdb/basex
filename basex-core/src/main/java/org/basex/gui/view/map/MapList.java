package org.basex.gui.view.map;

import org.basex.data.*;
import org.basex.gui.view.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Stores an integer array of pre values and their corresponding weights.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Joerg Hauser
 */
final class MapList {
  /** Pre values. */
  final IntList pres;
  /** Weights array. */
  double[] weight;

  /**
   * Constructor.
   */
  MapList() {
    pres = new IntList();
  }

  /**
   * Constructor, assigning the specified array.
   * @param v initial list values
   */
  MapList(final int[] v) {
    pres = new IntList(v);
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public int size() {
    return pres.size();
  }

  /**
   * Returns the specified pre value.
   * @param index index
   * @return pre value
   */
  public int get(final int index) {
    return pres.get(index);
  }

  /**
   * Adds a pre value.
   * @param pre pre value to be added
   */
  public void add(final int pre) {
    pres.add(pre);
  }

  /**
   * Sorts the pre values.
   */
  public void sort() {
    pres.sort(weight, false);
  }

  /**
   * Initializes the weights of each list entry, using the text length of
   * nodes or (if the array reference is null) the size attributes.
   * @param textLen array holding pre values to text lengths
   * @param nchildren reference number of nodes
   * @param data reference
   * @param w weight
   */
  void initWeights(final int[] textLen, final int nchildren, final Data data, final int w) {
    final int size = size();
    weight = new double[size];

    // only children
    if(w == 0) {
      for(int i = 0; i < size; ++i) {
        weight[i] = (double) ViewData.size(data, pres.get(i)) / nchildren;
      }
      return;
    }

    // summarize sizes
    final double sizeP = w / 100.0d;
    double sum = 0;
    for(int i = 0; i < size; ++i) sum += weight(textLen, data, i);

    if(sizeP < 1) {
      // use #children and size for weight
      for(int i = 0; i < size; ++i) {
        weight[i] = sizeP * weight(textLen, data, i) / sum +
          (1 - sizeP) * ViewData.size(data, pres.get(i)) / nchildren;
      }
    } else {
      // only sizes
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
    final double d;
    if(textLen != null) {
      d = textLen[pres.get(i)];
    } else {
      final byte[] val = data.attValue(ViewData.sizeID(data), pres.get(i));
      d = val != null ? Token.toLong(val) : 0;
    }
    return d > 1 ? d : 1;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Util.className(this) + '[');
    final int ps = size();
    for(int p = 0; p < ps; ++p) {
      sb.append(p == 0 ? "" : ", ").append(pres.get(p));
      if(weight != null) sb.append('/').append(weight[p]);
    }
    return sb.append(']').toString();
  }
}