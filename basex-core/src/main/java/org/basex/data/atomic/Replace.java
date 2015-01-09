package org.basex.data.atomic;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;

/**
 * Replaces a node in the database with an insertion sequence.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
final class Replace extends StructuralUpdate {
  /** Insertion sequence. */
  private final DataClip clip;

  /**
   * Constructor.
   * @param location PRE value of the target node location
   * @param clip insertion sequence data clip
   * @param shifts PRE value shifts introduced by update
   * @param acc accumulated shifts
   * @param first PRE value of the first node which distance has to be updated
   * @param parent parent node PRE
   */
  Replace(final int location, final int shifts, final int acc, final int first, final DataClip clip,
      final int parent) {
    super(location, shifts, acc, first, parent);
    this.clip = clip;
  }

  /**
   * Factory.
   * @param data data reference
   * @param pre target node PRE
   * @param clip insertion sequence
   * @return instance
   */
  static Replace getInstance(final Data data, final int pre, final DataClip clip) {
    final int kind = data.kind(pre);
    final int par = data.parent(pre, kind);
    final int oldsize = data.size(pre, kind);
    final int newsize = clip.size();
    final int sh = newsize - oldsize;
    return new Replace(pre, sh, sh, pre + oldsize, clip, par);
  }

  @Override
  void apply(final Data data) {
    // [LK] replace optimizations only work without namespaces..
    if(data.nspaces.size() == 0 && clip.data.nspaces.size() == 0) {
      // Lazy Replace: rewrite to value updates if structure has not changed
      if(lazyReplace(data)) return;
      // Rapid Replace: in-place update, overwrite existing table entries
      data.replace(location, clip);
    } else {
      // fallback: delete old entries, add new ones
      final int kind = data.kind(location);
      final int par = data.parent(location, kind);
      // delete first - otherwise insert must be at location+1
      data.delete(location);
      if(kind == Data.ATTR) {
        data.insertAttr(location, par, clip);
      } else {
        data.insert(location, par, clip);
      }
    }
  }

  /**
   * Lazy Replace implementation. Checks if the replace operation can be substituted with
   * cheaper value updates. If structural changes have to be made no substitution takes place.
   * @param data destination data reference
   * @return true if substitution successful
   */
  private boolean lazyReplace(final Data data) {
    final Data src = clip.data;
    final int srcSize = clip.size();
    // check for equal subtree size
    if(srcSize != data.size(location, data.kind(location))) return false;

    final List<BasicUpdate> valueUpdates = new ArrayList<>();
    for(int c = 0; c < srcSize; c++) {
      final int s = clip.start + c;
      final int t = location + c;
      final int sk = src.kind(s);
      final int tk = data.kind(t);

      if(sk != tk)
        return false;
      // distance can differ for first two tuples
      if(c > 0 && src.dist(s, sk) != data.dist(t, tk))
        return false;
      // check texts, comments and documents
      if(sk == Data.TEXT || sk == Data.COMM || sk == Data.DOC) {
        final byte[] srcText = src.text(s, true);
        if(!eq(data.text(t, true), srcText))
          valueUpdates.add(UpdateValue.getInstance(data, t, srcText));
      } else {
        // check elements, attributes and processing instructions
        final byte[] srcName = src.name(s, sk);
        final byte[] trgName = data.name(t, tk);
        if(!eq(srcName, trgName)) valueUpdates.add(Rename.getInstance(data, t, srcName, EMPTY));
        switch(sk) {
          case Data.ELEM:
            // check size of elements
            if(src.attSize(s, sk) != data.attSize(t, tk) || src.size(s, sk) != data.size(t, tk))
              return false;
            break;
          case Data.ATTR:
            // check attribute values
            byte[] srcValue = src.text(s, false);
            if(!eq(data.text(t, false), srcValue))
              valueUpdates.add(UpdateValue.getInstance(data, t, srcValue));
            break;
          case Data.PI:
            // check processing instruction value
            final byte[] srcText = src.text(s, true);
            final byte[] trgText = data.text(t, true);
            final int i = indexOf(srcText, ' ');
            srcValue = i == -1 ? EMPTY : substring(srcText, i + 1);
            if(!eq(srcValue, indexOf(trgText, ' ') == -1 ? EMPTY :
              substring(trgText, i + 1))) {
              valueUpdates.add(UpdateValue.getInstance(data, t, srcValue));
            }
            break;
        }
      }
    }
    for(final BasicUpdate bu : valueUpdates) bu.apply(data);
    return true;
  }

  @Override
  DataClip getInsertionData() {
    return clip;
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
