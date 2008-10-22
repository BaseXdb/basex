package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class IndexIterator {
  /** Empty iterator. */
  public static final IndexIterator EMPTY = new IndexIterator() {
    @Override
    public boolean more() { return false; };
    @Override
    public int next() { return 0; };
    @Override
    public int size() { return 0; }; 
    @Override 
    public FTNode nextFTNodeFD() { return new FTNode(); };
  };

  /**
   * Checks if more results are available.
   * @return result
   */
  public abstract boolean more();

  /**
   * Returns the next result.
   * @return result
   */
  public abstract int next();
  
  /**
   * Returns the number of index results.
   * @return size
   */
  public abstract int size();
  
  /**
   * Returns the next result as FTNode object.
   * @return next FTNode
   */
  public abstract FTNode nextFTNodeFD(); 
}
