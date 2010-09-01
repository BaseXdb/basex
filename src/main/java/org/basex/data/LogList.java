package org.basex.data;

/**
 * A pseudo-linear logging list to realize the ID->Pre mapping. It saves
 * the inserts and deletes into the database and calculates the mapping.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Philipp Ziemer
 */
final class LogList {
  /** First Node in the list. */
  LogNode first;

  /**
   * Logs a the insert of a new item into the database.
   * @param id value of the entry
   * @param pre value of the entry
   */
  void insert(final int id, final int pre) {
    if(id == pre) return;

    // list is empty
    if(first == null) {
      // calculation of the addends
      final int mainAddend = 1;
      final int subAddend = pre - id - 1;

      // insert the mainNode and then the subNode
      first = new LogNode(pre, mainAddend);
      first.addSub(id, subAddend);
    // list has entries
    } else {
      // search right position for insert through linear search
      LogNode pointer = first;
      LogNode b4pointer = null; // node before pointer
      int addend = 0; // needed to calculate correct value for the sub-node
      while(pointer != null && pointer.id < pre) {
        addend += pointer.addend;
        b4pointer = pointer;
        pointer = pointer.next;
      }

      // handle node with pre exists
      if(pointer != null && pointer.id == pre) {
        ++pointer.addend;
        // addend of subnode is pre - id - addend - addend of pointer
        // (which is not counted yet)
        pointer.addSub(id, pre - id - addend - pointer.addend);
      // new node needs to be created
      } else {
        // built up new node and subnode
        final LogNode newNode = new LogNode(pre, 1);
        // addend of subnode is pre - id - addend - addend of pointer
        // (which is not counted yet)
        newNode.addSub(id, pre - id - addend - 1);

        // insert into list
        // check if item will be first item
        if(b4pointer == null) // yes
          first = newNode;
        else // no
          b4pointer.next = newNode;
        newNode.next = pointer;
      }
    }
  }

  /**
   * Logs a the delete of a node.
   * @param pre value of the entry
   * @param size of deleted root-node
   */
  void delete(final int pre, final int size) {
    // list is empty
    if(first == null)
      // insert the mainNode
      first = new LogNode(pre, -size);
    // list has entries
    else {
      // search right position for insert through linear search
      LogNode pointer = first;
      LogNode b4pointer = null; // node before pointer
      while(pointer != null && pointer.id < pre) {
        b4pointer = pointer;
        pointer = pointer.next;
      }

      // handle node with pre exists
      if(pointer != null && pointer.id == pre)
        pointer.addend -= size;
      // new node needs to be created
      else {
        // built up new node and subnode
        final LogNode newNode = new LogNode(pre, -size);

        // insert into list
        // check if item will be first item
        if(b4pointer == null) // yes
          first = newNode;
        else // no
          b4pointer.next = newNode;
        newNode.next = pointer;
      }
    }
  }

  /**
   * Calculates the Pre-value for a given ID.
   * @param id value of the entry
   * @return pre value
   */
  int pre(final int id) {
    int addend = 0;
    LogNode pointer = first;

    while(pointer != null) {
      // check if id is still affected
      if(pointer.id <= id) {
        addend += pointer.addend;

        // check for subitem
        if(pointer.hasSub()) {
          final int subaddend = pointer.getSubAddend(id);
          if(subaddend != Integer.MIN_VALUE) {
            addend += subaddend;
            break; // subitem found => end
          }
        }
        pointer = pointer.next;
      } else {
        pointer = null;
      }
    }
    return id + addend;
  }
}
