package org.basex.data.atomic;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Replaces a node in the database with an insertion sequence.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class Replace extends StructuralUpdate {
  /** Insertion sequence. */
  final DataClip insseq;

  /**
   * Constructor.
   * @param l PRE value of the target node location
   * @param c insertion sequence data clip
   * @param s PRE value shifts introduced by update
   * @param a accumulated shifts
   * @param f PRE value of the first node which distance has to be updated
   * @param p parent node PRE
   */
  Replace(final int l, final int s, final int a, final int f, final DataClip c,
      final int p) {
    super(l, s, a, f, p);
    insseq = c;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre target node PRE
   * @param clip insertion sequence
   * @return instance
   */
  static Replace getInstance(final Data data, final int pre, final DataClip clip) {
    final int oldsize = data.size(pre, data.kind(pre));
    final int newsize = clip.size();
    final int sh = newsize - oldsize;
    return new Replace(pre, sh, sh, pre + oldsize, clip,
        data.parent(pre, data.kind(pre)));
  }

  @Override
  void apply(final Data dest) {

    if(fastReplace(dest)) {
      // [LK] activate lazy replace (some TC not running then.. / not yet finished)
//      if(lazyReplace(dest)) return;
      // Rapid Replace
      dest.replace(location, insseq);
    } else {
      final int targetKind = dest.kind(location);
      final int targetParent = dest.parent(location, targetKind);
      // delete first - otherwise insert must be at location+1
      dest.delete(location);
      if(targetKind == Data.ATTR)
        dest.insertAttr(location, targetParent, insseq);
      else
        dest.insert(location, targetParent, insseq);
    }
  }

  /**
   * Lazy Replace implementation. Checks if the replace operation can be substituted with
   * cheaper value updates. If structural changes have to be made no substitution takes
   * place.
   * @param dst destination data reference
   * @return true if substitution successful
   */
  boolean lazyReplace(final Data dst) {
    final Data src = insseq.data;
    final int sourceSize = insseq.size();
    if(sourceSize != dst.size(location, dst.kind(location))) return false;

    final List<BasicUpdate> valueUpdates = new ArrayList<BasicUpdate>();
    // [LK] check for equal size of both! src.size==dst.size
    for(int c = 0; c < sourceSize; c++) {
      final int s = insseq.start + c;
      final int d = location + c;
      final int sk = src.kind(s);
      final int dk = dst.kind(d);
      if(sk != dk)
        return false;
      final int sdis = src.dist(s, sk);
      final int ddis = dst.dist(d, dk);
      if(sdis != ddis)
        return false;

      switch(sk) {
        case Data.ELEM:
          if(src.attSize(s, sk) != dst.attSize(d, dk) ||
            src.size(s, sk) != dst.size(d, dk))
            return false;
          break;

        case Data.ATTR:
//          Util.notexpected("replace by value: update ATTR ?");
          break;

        case Data.TEXT:
        case Data.COMM:
        case Data.PI:
          final byte[] stxt = src.text(s, true);
          // [LK] compare potentially huge texts?
          if(dst.textLen(d, true) != src.textLen(s, true) ||
              !Token.eq(dst.text(d, true), stxt))
            valueUpdates.add(UpdateValue.getInstance(dst, d, stxt));
      }
    }

    for(final BasicUpdate u : valueUpdates) {
      u.apply(dst);
    }
    return true;
  }

  /**
   * Checks whether an optimization for a faster replace can be leveraged. Only available
   * if no {@link Namespaces} exist in both the target and the source database.
   * @param d target database
   * @return true if fast replace possible
   */
  private boolean fastReplace(final Data d) {
    return d.nspaces.size() == 0 && insseq.data.nspaces.size() == 0 &&
        d.kind(location) != Data.ATTR;
  }

  @Override
  DataClip getInsertionData() {
    return insseq;
  }

  @Override
  boolean destructive() {
    return true;
  }

  @Override
  public String toString() {
    return "\nReplace: " + super.toString();
  }
}
