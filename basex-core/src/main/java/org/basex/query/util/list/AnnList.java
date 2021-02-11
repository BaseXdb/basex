package org.basex.query.util.list;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.util.list.*;

/**
 * List of annotations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class AnnList extends ObjectList<Ann, AnnList> {
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
   * @return annotation or {@code null}
   */
  public Ann get(final Annotation sig) {
    for(final Ann ann : this) {
      if(ann.sig == sig) return ann;
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
      final Annotation sig = ann.sig;
      if(sig == Annotation.PUBLIC) pub = true;
      else if(sig == Annotation.PRIVATE) priv = true;
      else if(sig == Annotation.UPDATING) up = true;
      tmp.add(ann);
    }

    for(final Ann ann : anns) {
      final Annotation sig = ann.sig;
      if(sig == Annotation.PUBLIC) {
        if(pub) continue;
        if(priv) return null;
      } else if(sig == Annotation.PRIVATE) {
        if(pub) return null;
        if(priv) continue;
      } else if(sig == Annotation.UPDATING && up) {
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
      final Annotation sig = ann.sig;
      if(sig == Annotation.UPDATING) {
        if(up) throw DUPLUPD.get(ann.info);
        up = true;
      } else if(visible && (sig == Annotation.PUBLIC || sig == Annotation.PRIVATE)) {
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
  public void plan(final QueryString qs) {
    for(final Ann ann : this) ann.plan(qs);
  }
}
