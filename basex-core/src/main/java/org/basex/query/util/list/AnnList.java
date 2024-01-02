package org.basex.query.util.list;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.util.*;

/**
 * List of annotations.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class AnnList implements Iterable<Ann> {
  /** Empty annotations. */
  public static final AnnList EMPTY = new AnnList();
  /** Annotations. */
  private final Ann[] anns;

  /**
   * Constructor.
   * @param anns annotations
   */
  private AnnList(final Ann... anns) {
    this.anns = anns;
  }

  /**
   * Checks if the specified signature is found in the list.
   * @param def signature to be found
   * @return result of check
   */
  public boolean contains(final Annotation def) {
    return get(def) != null;
  }

  /**
   * Checks if the specified annotation is found in the list.
   * @param ann annotation to be found
   * @return result of check
   */
  public boolean contains(final Ann ann) {
    for(final Ann a : anns) {
      if(a.equals(ann)) return true;
    }
    return false;
  }

  /**
   * Returns an annotation with the specified signature.
   * @param def annotation to be found
   * @return annotation or {@code null}
   */
  public Ann get(final Annotation def) {
    for(final Ann ann : anns) {
      if(ann.definition == def) return ann;
    }
    return null;
  }

  /**
   * Adds an annotation.
   * @param ann annotation to be added
   * @return a new instance
   */
  public AnnList attach(final Ann ann) {
    final int al = anns.length;
    final Ann[] tmp = Arrays.copyOf(anns, al + 1);
    tmp[al] = ann;
    return new AnnList(tmp);
  }

  /**
   * Tests whether the container has no elements.
   * @return result of check
   */
  public boolean isEmpty() {
    return this == EMPTY;
  }

  /**
   * Returns the number of elements.
   * @return number of elements
   */
  public int size() {
    return anns.length;
  }

  /**
   * Returns the intersection of these annotations and the given ones.
   * @param list other annotations
   * @return a new instance with all annotations, or {@code null} if intersection is not possible
   */
  public AnnList intersect(final AnnList list) {
    final ArrayList<Ann> tmp = new ArrayList<>();
    boolean pub = false, priv = false, up = false;
    for(final Ann ann : anns) {
      final Annotation def = ann.definition;
      if(def == Annotation.PUBLIC) pub = true;
      else if(def == Annotation.PRIVATE) priv = true;
      else if(def == Annotation.UPDATING) up = true;
      tmp.add(ann);
    }

    for(final Ann ann : list.anns) {
      final Annotation def = ann.definition;
      if(def == Annotation.PUBLIC) {
        if(pub) continue;
        if(priv) return null;
      } else if(def == Annotation.PRIVATE) {
        if(pub) return null;
        if(priv) continue;
      } else if(def == Annotation.UPDATING && up) {
        continue;
      }
      tmp.add(ann);
    }
    return new AnnList(tmp.toArray(Ann[]::new));
  }

  /**
   * Returns the unions of these annotations and the given ones.
   * @param list annotations
   * @return a new instance with annotations that are present in both lists
   */
  public AnnList union(final AnnList list) {
    final ArrayList<Ann> tmp = new ArrayList<>();
    for(final Ann ann : anns) {
      for(final Ann ann2 : list.anns) {
        if(ann.equals(ann2)) tmp.add(ann);
      }
    }
    return new AnnList(tmp.toArray(Ann[]::new));
  }

  /**
   * Checks all annotations for parsing errors.
   * @param variable variable flag (triggers different error codes)
   * @param visible check visibility annotations
   * @return self reference
   * @throws QueryException query exception
   */
  public AnnList check(final boolean variable, final boolean visible) throws QueryException {
    boolean up = false, vis = false;
    for(final Ann ann : anns) {
      final Annotation def = ann.definition;
      if(def == Annotation.UPDATING) {
        if(up) throw DUPLUPD.get(ann.info);
        up = true;
      } else if(visible && (def == Annotation.PUBLIC || def == Annotation.PRIVATE)) {
        // only one visibility modifier allowed
        if(vis) throw (variable ? DUPLVARVIS : DUPLFUNVIS).get(ann.info);
        vis = true;
      }
    }
    return this;
  }

  /**
   * Adds the annotations to a query string.
   * @param qs query string builder
   */
  public void toString(final QueryString qs) {
    for(final Ann ann : anns) ann.toString(qs);
  }

  @Override
  public Iterator<Ann> iterator() {
    return new ArrayIterator<>(anns, anns.length);
  }
}
