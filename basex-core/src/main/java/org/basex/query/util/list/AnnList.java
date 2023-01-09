package org.basex.query.util.list;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.util.list.*;

/**
 * List of annotations.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class AnnList extends ObjectList<Ann, AnnList> {
  /**
   * Checks if the specified signature is found in the list.
   * @param def signature to be found
   * @return result of check
   */
  public boolean contains(final Annotation def) {
    return get(def) != null;
  }

  /**
   * Returns an annotation with the specified signature.
   * @param def annotation to be found
   * @return annotation or {@code null}
   */
  public Ann get(final Annotation def) {
    for(final Ann ann : this) {
      if(ann.definition == def) return ann;
    }
    return null;
  }

  /**
   * Returns the intersection of these annotations and the given ones.
   * @param anns other annotations
   * @return a new instance with all annotations, or {@code null} if intersection is not possible
   */
  public AnnList intersect(final AnnList anns) {
    final AnnList tmp = new AnnList();
    boolean pub = false, priv = false, up = false;
    for(final Ann ann : this) {
      final Annotation def = ann.definition;
      if(def == Annotation.PUBLIC) pub = true;
      else if(def == Annotation.PRIVATE) priv = true;
      else if(def == Annotation.UPDATING) up = true;
      tmp.add(ann);
    }

    for(final Ann ann : anns) {
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
    return tmp;
  }

  /**
   * Returns the unions of these annotations and the given ones.
   * @param anns annotations
   * @return a new instance with annotations that are present in both lists
   */
  public AnnList union(final AnnList anns) {
    final AnnList tmp = new AnnList();
    for(final Ann ann : this) {
      for(final Ann ann2 : anns) {
        if(ann.equals(ann2)) tmp.add(ann);
      }
    }
    return tmp;
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
    for(final Ann ann : this) {
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

  @Override
  protected Ann[] newArray(final int s) {
    return new Ann[s];
  }

  /**
   * Adds the annotations to a query string.
   * @param qs query string builder
   */
  public void toString(final QueryString qs) {
    for(final Ann ann : this) ann.toString(qs);
  }
}
