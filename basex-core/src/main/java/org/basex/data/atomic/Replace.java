package org.basex.data.atomic;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;

/**
 * Replaces a node in the database with an insertion sequence.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
final class Replace extends StructuralUpdate {
  /** Insertion sequence. */
  private final DataClip insseq;

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
  void apply(final Data targetData) {
    // [LK] replace optimizations only work without namespaces..
    if(targetData.nspaces.size() == 0 && insseq.data.nspaces.size() == 0) {
      // Lazy Replace
      if(lazyReplace(targetData)) return;
      // Rapid Replace
      targetData.replace(location, insseq);
    } else {
      final int targetKind = targetData.kind(location);
      final int targetParent = targetData.parent(location, targetKind);
      // delete first - otherwise insert must be at location+1
      targetData.delete(location);
      if(targetKind == Data.ATTR)
        targetData.insertAttr(location, targetParent, insseq);
      else
        targetData.insert(location, targetParent, insseq);
    }
  }

  /**
   * Lazy Replace implementation. Checks if the replace operation can be substituted with
   * cheaper value updates. If structural changes have to be made no substitution takes
   * place.
   * @param trg destination data reference
   * @return true if substitution successful
   */
  boolean lazyReplace(final Data trg) {
    final Data src = insseq.data;
    final int srcSize = insseq.size();
    // check for equal subtree size
    if(srcSize != trg.size(location, trg.kind(location))) return false;

    final List<BasicUpdate> valueUpdates = new ArrayList<BasicUpdate>();
    for(int c = 0; c < srcSize; c++) {
      final int s = insseq.start + c;
      final int t = location + c;
      final int sk = src.kind(s);
      final int tk = trg.kind(t);

      if(sk != tk)
        return false;
      // distance can differ for first two tuples
      if(c > 0 && src.dist(s, sk) != trg.dist(t, tk))
        return false;
      // check text / comment values
      if(sk == Data.TEXT || sk == Data.COMM) {
        byte[] srcText = src.text(s, true);
        if(trg.textLen(t, true) != src.textLen(s, true) ||
            !eq(trg.text(t, true), srcText))
          valueUpdates.add(UpdateValue.getInstance(trg, t, srcText));
      } else {
        // check element, attribute, processing instruction name
        final byte[] srcName = src.name(s, sk);
        final byte[] trgName = trg.name(t, tk);
        if(!eq(srcName, trgName))
          valueUpdates.add(Rename.getInstance(trg, t, srcName, EMPTY));
        switch(sk) {
          case Data.ELEM:
            // check size of elements
            if(src.attSize(s, sk) != trg.attSize(t, tk) || src.size(s, sk) != trg.size(t, tk))
              return false;
            break;
          case Data.ATTR:
            // check attribute values
            byte[] srcValue = src.text(s, false);
            if(!eq(trg.text(t, false), srcValue))
              valueUpdates.add(UpdateValue.getInstance(trg, t, srcValue));
            break;
          case Data.PI:
            // check processing instruction value
            final byte[] srcText = src.text(s, true);
            final byte[] trgText = trg.text(t, true);
            final int i = indexOf(srcText, ' ');
            srcValue =  i == -1 ? EMPTY : substring(srcText, i + 1);
            if(!eq(srcValue, indexOf(trgText, ' ') == -1 ? EMPTY :
              substring(trgText, i + 1))) {
              valueUpdates.add(UpdateValue.getInstance(trg, t, srcValue));
            }
            break;
        }
      }
    }
    for(final BasicUpdate u : valueUpdates) {
      u.apply(trg);
    }
    return true;
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
