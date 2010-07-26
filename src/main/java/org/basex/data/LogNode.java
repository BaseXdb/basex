package org.basex.data;

/**
 * A Node in the pseudo-linear logging List to realize the ID->Pre Mapping. It
 * saves the inserts and deletes into the Database and calculates the mapping.
 * Works together with the LogList-class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Philipp Ziemer
 */
final class LogNode {
  /** Addend is valid for this id and all following. */
  int id;
  /** What to add to calculate the Pre-value from the ID. */
  int addend;
  /** Next node in the list. */
  LogNode next;
  /** Subnodes of the Node. */
  private int[][] subnodes;

  // #################################
  // ######## Private methods ########
  // #################################
  /**
   * Searches the position where the id has to be inserted in
   * the list through binary search.
   * @param subId Position for this value.
   * @return The position of the next bigger id in the list
   * or sublist.length if the searched id is the largest.
   */
  private int binarySearch(final int subId) {
    if(subnodes.length == 0)
      return -1;

    int left = 0;
    int right = subnodes.length - 1;
    while(1 < right - left) {
      final int mid = left + (right - left) / 2;
      if(subnodes[mid][0] <= subId) left = mid;
      else if(subId < subnodes[mid][0]) right = mid;
    }
    if(subId < subnodes[left][0]) return left;
    else if(subId < subnodes[right][0]) return right;
    return subnodes.length;
  }

  // ##############################
  // ######## Constructors ########
  // ##############################
  /**
   * Creates a new node with the given values.
   * @param theId Addend is valid for this id and all following.
   * @param theAddend What to add to calculate the Pre-value from the ID.
   */
  LogNode(final int theId, final int theAddend) {
    id = theId;
    addend = theAddend;
    next = null;
  }

  // ################################
  // ######## Public methods ########
  // ################################
  /**
   * Adds a subnode to the mainnode.
   * @param subId ID of the subnode.
   * @param subAddend What to add to calculate the Pre-value from the ID.
   */
  void addSub(final int subId, final int subAddend) {
    // empty sublist
    if(subnodes == null) {
      subnodes = new int[1][2];
      subnodes[0][0] = subId;
      subnodes[0][1] = subAddend;
    // there are subnodes
    } else {
      final int pos = binarySearch(subId); // where to insert
      final int[][] newsubnodes = new int[subnodes.length + 1][2];

      // at the beginning
      if(pos == 0) {
        System.arraycopy(subnodes, 0, newsubnodes, 1, subnodes.length);
      // in the mid
      } else if(pos < subnodes.length) {
        System.arraycopy(subnodes, 0, newsubnodes, 0, pos);
        System.arraycopy(subnodes, pos, newsubnodes, pos + 1,
            subnodes.length - pos);
      // at the end
      } else {
        System.arraycopy(subnodes, 0, newsubnodes, 0, subnodes.length);
      }

      newsubnodes[pos][0] = subId;
      newsubnodes[pos][1] = subAddend;
      subnodes = newsubnodes;
    }
  }

  /**
   * Returns the addend of the ID.
   * @param subId ID of the entry.
   * @return Addend to calculate the Pre or
   * Integer.MIN_VALUE if the ID is not in the list.
   */
  int getSubAddend(final int subId) {
    final int pos = binarySearch(subId);
    // as binarySearch gives the position of the next
    // bigger calculate position - 1
    if(pos != 0 && subnodes[pos - 1][0] == subId) return subnodes[pos - 1][1];
    return Integer.MIN_VALUE;
  }

  /**
   * Has the mainnode subnodes to check?
   * @return true if there are subnode, else false.
   */
  boolean hasSub() {
    return subnodes != null && subnodes.length != 0;
  }
}