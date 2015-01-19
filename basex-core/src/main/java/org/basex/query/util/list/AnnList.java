package org.basex.query.util.list;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * List of annotations.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AnnList extends ElementList implements Iterable<Ann> {
  /** Annotations. */
  private Ann[] anns = new Ann[0];

  /**
   * Adds a QName/value pair.
   * @param ann annotation
   */
  public void add(final Ann ann) {
    // create new entry
    final int s = size;
    if(s == anns.length) anns = Array.copy(anns, new Ann[newSize()]);
    anns[s] = ann;
    size = s + 1;
  }

  /**
   * Returns the annotation at the specified position.
   * @param index annotation index
   * @return annotation
   */
  public Ann get(final int index) {
    return anns[index];
  }

  /**
   * Checks if the specified annotation is found in the list.
   * @param ann annotation to be found
   * @return result of check
   */
  public boolean contains(final Ann ann) {
    for(final Ann a : anns) if(a.eq(ann)) return true;
    return false;
  }

  /**
   * Checks if the specified signature is found in the list.
   * @param sig signature to be found
   * @return result of check
   */
  public boolean contains(final Annotation sig) {
    return get(sig) != null;
  }

  /**
   * Returns an annotation with the specified signature.
   * @param sig signature to be found
   * @return result, or {@code null}
   */
  public Ann get(final Annotation sig) {
    for(final Ann ann : this) if(ann.sig == sig) return ann;
    return null;
  }

  /**
   * Returns the union of these annotations and the given ones.
   * @param al other annotations
   * @return a new instance, containing all annotations, or {@code null} if union is not possible
   */
  public AnnList union(final AnnList al) {
    final AnnList tmp = new AnnList();
    boolean pub = false, priv = false, up = false;
    for(final Ann ann : this) {
      if(ann.sig == Annotation.PUBLIC) pub = true;
      else if(ann.sig == Annotation.PRIVATE) priv = true;
      else if(ann.sig == Annotation.UPDATING) up = true;
      tmp.add(ann);
    }

    for(final Ann ann : al) {
      if(ann.sig == Annotation.PUBLIC) {
        if(pub) continue;
        if(priv) return null;
      } else if(ann.sig == Annotation.PRIVATE) {
        if(pub) return null;
        if(priv) continue;
      } else if(ann.sig == Annotation.UPDATING && up) {
        continue;
      }
      tmp.add(ann);
    }
    return tmp;
  }

  /**
   * Returns the intersection of these annotations and the given ones.
   * @param al annotations
   * @return those annotations that are present in both collections
   */
  public AnnList intersect(final AnnList al) {
    final AnnList tmp = new AnnList();
    for(final Ann ann : this) {
      for(final Ann ann2 : al.anns) {
        if(ann.eq(ann2)) tmp.add(ann);
      }
    }
    return tmp;
  }

  /**
   * Checks all annotations for parsing errors.
   * @param var variable flag
   * @return self reference
   * @throws QueryException query exception
   */
  public AnnList check(final boolean var) throws QueryException {
    boolean up = false, vis = false;
    for(final Ann ann : this) {
      final Annotation sig = ann.sig;
      if(sig == Annotation.UPDATING) {
        if(up) throw DUPLUPD.get(ann.info);
        up = true;
      } else if(sig == Annotation.PUBLIC || sig == Annotation.PRIVATE) {
        // only one visibility modifier allowed
        if(vis) throw (var ? DUPLVARVIS : DUPLFUNVIS).get(ann.info);
        vis = true;
      }
    }
    return this;
  }

  @Override
  public Iterator<Ann> iterator() {
    return new ArrayIterator<>(anns, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Ann ann : this) sb.append(ann);
    return sb.toString();
  }
}
