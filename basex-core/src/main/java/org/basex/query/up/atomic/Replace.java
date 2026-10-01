package org.basex.query.up.atomic;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;

/**
 * Replaces a node in the database with an insertion sequence.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
final class Replace extends StructuralUpdate {
  /** Insertion sequence. */
  private final DataClip clip;

  /**
   * Constructor.
   * @param location PRE value of the target node location
   * @param shifts PRE value shifts introduced by update
   * @param acc accumulated shifts
   * @param first PRE value of the first node which distance has to be updated
   * @param clip insertion sequence data clip
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
    final int kind = data.kind(pre), parent = data.parent(pre, kind);
    final int oldsize = data.size(pre, kind), sh = clip.size() - oldsize;
    return new Replace(pre, sh, sh, pre + oldsize, clip, parent);
  }

  @Override
  void apply(final Data data) {
    try {
      // if possible, replace values or overwrite database entries
      final boolean ns = !data.nspaces.isEmpty() || !clip.data.nspaces.isEmpty();
      if(lazyReplace(data, ns) || !ns && data.replace(location, clip)) return;

      // otherwise, delete old entries and insert new ones
      final int kind = data.kind(location), par = data.parent(location, kind);
      data.delete(location);
      if(kind == Data.ATTR) {
        data.insertAttr(location, par, clip);
      } else {
        data.insert(location, par, clip);
      }
    } finally {
      clip.finish();
    }
  }

  /**
   * Lazy Replace implementation. Checks if the replace operation can be substituted with
   * cheaper value updates. If structural changes have to be made no substitution takes place.
   * @param data destination data reference
   * @param ns indicates if the source or destination data contains namespaces
   * @return true if operation was successful
   */
  private boolean lazyReplace(final Data data, final boolean ns) {
    final Data src = clip.data;
    final int srcSize = clip.size();
    // check for equal subtree size
    if(srcSize != data.size(location, data.kind(location))) return false;

    final List<BasicUpdate> valueUpdates = new ArrayList<>();
    for(int c = 0; c < srcSize; c++) {
      final int s = clip.start + c, t = location + c, sk = src.kind(s), tk = data.kind(t);
      // distance can differ for first two tuples
      if(sk != tk || c > 0 && src.dist(s, sk) != data.dist(t, tk)) return false;
      // check texts, comments and documents
      if(sk == Data.TEXT || sk == Data.COMM || sk == Data.DOC) {
        final byte[] srcText = src.text(s, true);
        if(!eq(data.text(t, true), srcText))
          valueUpdates.add(UpdateValue.getInstance(data, t, srcText));
      } else {
        // check elements, attributes and processing instructions
        final byte[] srcName = src.name(s, sk);
        final byte[] trgName = data.name(t, tk);
        if(!eq(srcName, trgName)) {
          // with namespaces, only processing instructions can be renamed
          if(ns && sk != Data.PI) return false;
          valueUpdates.add(Rename.getInstance(data, t, srcName, EMPTY));
        }
        switch(sk) {
          case Data.ELEM -> {
            // check size and namespaces of elements
            if(src.attSize(s, sk) != data.attSize(t, tk) || src.size(s, sk) != data.size(t, tk) ||
                ns && !namespaces(data, s, t)) return false;
          }
          case Data.ATTR -> {
            // check namespaces and values of attributes
            if(ns && !eq(src.qname(s, sk)[1], data.qname(t, tk)[1])) return false;
            final byte[] av = src.text(s, false);
            if(!eq(data.text(t, false), av))
              valueUpdates.add(UpdateValue.getInstance(data, t, av));
          }
          case Data.PI -> {
            // check processing instruction value
            final byte[] srcText = src.text(s, true);
            final byte[] trgText = data.text(t, true);
            final int i = indexOf(srcText, ' ');
            final byte[] pv = i == -1 ? EMPTY : substring(srcText, i + 1);
            if(!eq(pv, indexOf(trgText, ' ') == -1 ? EMPTY :
              substring(trgText, i + 1))) {
              valueUpdates.add(UpdateValue.getInstance(data, t, pv));
            }
          }
        }
      }
    }
    for(final BasicUpdate update : valueUpdates) update.apply(data);
    return true;
  }

  /**
   * Checks if the target element has the namespace and declarations that an insertion of the
   * source element would yield.
   * @param data destination data reference
   * @param s PRE value of the source element
   * @param t PRE value of the target element
   * @return result of check
   */
  private boolean namespaces(final Data data, final int s, final int t) {
    final Data src = clip.data;
    if(!eq(src.qname(s, Data.ELEM)[1], data.qname(t, Data.ELEM)[1])) return false;
    // without declarations, the names resolve to the same namespace in the same scope
    if(!src.nsFlag(s) && !data.nsFlag(t)) return true;

    // namespaces of the source element that are not in scope of the target parent
    final Atts nsp = src.elemNamespaces(s), ns = new Atts();
    final int par = data.parent(t, Data.ELEM);
    for(int a = 0; a < nsp.size(); a++) {
      final byte[] prefix = nsp.name(a), uri = nsp.value(a);
      final int uriId = data.nspaces.uriIdForPrefix(prefix, par, data);
      if(data.nspaces.declare(uriId, uri)) ns.add(prefix, uri);
    }
    return ns.equals(data.namespaces(t));
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
