package org.basex.gui.view.map;

import org.basex.data.Data;
import org.basex.gui.GUIProp;
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
  /** indicates if List has been sorted. */
  boolean sorted = false;
  
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
    sorted = true;
  }
  
  /**
   * Initializes the weights giving each node of this level same weight.
   */
  void initWeights() {
    weight = new double[size];
    for(int i = 0; i < size; i++) {
      System.out.println(size);
      weight[i] = 1d / size; 
    }
  }
  
  /**
   * Initializes the weights only using number of descendants.
   * @param parchildren reference number of nodes
   * @param data reference
   */
  void initWeights(final int parchildren, final Data data) {
    int[] nrchildren = new int[size];
    for(int i = 0; i < size; i++) {
      nrchildren[i] = data.size(list[i], data.kind(list[i]));
      weight[i] = nrchildren[i] * 1d / parchildren;
    }
  }
  
  /**
   * Initializes the weights of each list entry and stores it in an extra list.
   * @param parsize reference size
   * @param parchildren reference number of nodes
   * @param data reference
   * [JH] some weight problems occur displaying folders without any files and 
   * children
   */
  void initWeights(final long parsize, final int parchildren, final Data data) {
    if(size == 1) {
      initWeights();
      return;
    }
    
    weight = new double[size];
    int[] nrchildren = new int[size];
    long[] sizes = new long[size];
    int sizeP = GUIProp.mapweight;

    
    // only children
    if (GUIProp.mapweight == 0 || data.fs == null) {
      initWeights(parchildren, data);
    // use #children and size for weight
    } else if (0 < GUIProp.mapweight && GUIProp.mapweight < 100 && 
        data.fs != null) {
      for(int i = 0; i < size; i++) {
        if(data.attValue(data.sizeID, list[i]) != null) 
          sizes[i] = Token.toLong(data.attValue(data.sizeID, list[i]));
        else {
          sizes[i] = 0;
        }
        nrchildren[i] = data.size(list[i], data.kind(list[i]));
        weight[i] = sizeP / 100d * sizes[i] / parsize + 
            (1 - sizeP / 100d) * nrchildren[i] / parchildren;
      }
    // only sizes
    } else if (GUIProp.mapweight == 100 && data.fs != null) {
      for(int i = 0; i < size; i++) {
        if(data.attValue(data.sizeID, list[i]) != null) 
          sizes[i] = Token.toLong(data.attValue(data.sizeID, list[i]));
        else  {
          sizes[i] = 0;
        }
        weight[i] = sizes[i] * 1d / parsize;
      }
    }
  }
  
  /**
   * Initializes the weights of each list using text lengths of nodes.
   * @param textLen array holding pre vals to textlengths
   * @param children number of children
   * @param data reference
   */
  void initWeights(final int[] textLen, final int children, final Data data) {
    weight = new double[size];
    int[] nrchildren = new int[size];
    int textSum = 0;
    for(int i = 0; i < size; i++) {
      textSum += textLen[list[i]];
    }
    // only children
    for(int i = 0; i < size; i++) {
      nrchildren[i] = data.size(list[i], data.kind(list[i]));
      if(nrchildren[i] < 0) System.out.println(nrchildren[i]);
      weight[i] = GUIProp.mapweight / 100d * textLen[list[i]] / textSum + 
          (1 - GUIProp.mapweight / 100d) * nrchildren[i] / children;
    }
  }
  
  @Override
  public String toString() {
    if(weight == null) {
      StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "[");
      for(int i = 0; i < size; i++) {
        sb.append((i == 0 ? "" : ", ") + list[i]);
      }
      return sb.append("]").toString();
    } else {
      StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "[");
      for(int i = 0; i < size; i++) {
        sb.append((i == 0 ? "" : ", ") + list[i] + "/" + weight[i]);
      }
      return sb.append("]").toString();
    }
  }
}