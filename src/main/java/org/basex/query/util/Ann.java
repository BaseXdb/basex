package org.basex.query.util;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Annotations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Ann extends ElementList {
  /** Annotation "private". */
  public static final QNm Q_PRIVATE = new QNm(QueryText.PRIVATE, XQURI);
  /** Annotation "public". */
  public static final QNm Q_PUBLIC = new QNm(QueryText.PUBLIC, XQURI);
  /** Annotation "updating". */
  public static final QNm Q_UPDATING = new QNm(QueryText.UPDATING, XQURI);

  /** Supported REST annotations. */
  private static final byte[][] ANN_REST = tokens("error", "path", "produces", "consumes",
      "query-param", "form-param", "header-param", "cookie-param", "error-param",
      "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");
  /** Supported UNIT annotations. */
  private static final byte[][] ANN_UNIT = tokens("test", "ignore", "before", "after",
      "before-module", "after-module", "expected");

  /** Input Info. */
  public InputInfo[] infos = new InputInfo[1];
  /** QNames. */
  public QNm[] names = new QNm[1];
  /** Values. */
  public Value[] values = new Value[1];

  /**
   * Adds a QName/value pair.
   * @param name QName
   * @param value value
   * @param info input info
   */
  public void add(final QNm name, final Value value, final InputInfo info) {
    // create new entry
    if(size == names.length) {
      final int s = newSize();
      names = Array.copy(names, new QNm[s]);
      values = Array.copy(values, new Value[s]);
      infos = Array.copy(infos, new InputInfo[s]);
    }
    names[size] = name;
    values[size] = value;
    infos[size] = info;
    size++;
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public boolean contains(final QNm e) {
    for(int i = 0; i < size; ++i) if(names[i].eq(e)) return true;
    return false;
  }

  /**
   * Checks if the specified key/value pair is found in the list.
   * @param k name of the entry
   * @param v value of the entry
   * @return result of check
   */
  public boolean contains(final QNm k, final Value v) {
    try {
      for(int i = 0; i < size; ++i) {
        if(names[i].eq(k) && Compare.deep(v, values[i], null)) return true;
      }
      return false;
    } catch(final QueryException e) {
      // should never happen because annotations can only contain simple literals
      throw Util.notexpected(e);
    }
  }

  /**
   * Returns the union of these annotations and the given ones.
   * @param ann other annotations
   * @return a n {@link Ann} instance containing all annotations
   */
  public Ann union(final Ann ann) {
    final Ann o = new Ann();
    boolean pub = false, priv = false, up = false;
    for(int i = 0; i < size; i++) {
      if(names[i].eq(Ann.Q_PUBLIC)) pub = true;
      else if(names[i].eq(Ann.Q_PRIVATE)) priv = true;
      else if(names[i].eq(Ann.Q_UPDATING)) up = true;
      o.add(names[i], values[i], infos[i]);
    }

    for(int i = 0; i < ann.size; i++) {
      final QNm name = ann.names[i];
      if(name.eq(Ann.Q_PUBLIC)) {
        if(pub) continue;
        if(priv) return null;
      } else if(name.eq(Ann.Q_PRIVATE)) {
        if(pub) return null;
        if(priv) continue;
      } else if(name.eq(Ann.Q_UPDATING) && up) {
        continue;
      }
      o.add(ann.names[i], ann.values[i], ann.infos[i]);
    }
    return o;
  }

  /**
   * Returns the intersection of these annotations and the given ones.
   * @param ann annotations
   * @return those annotations that are present in both collections
   */
  public Ann intersect(final Ann ann) {
    final Ann o = new Ann();
    for(int i = 0; i < size; i++) {
      final QNm name = names[i];
      final Value val = values[i];
      try {
        for(int j = 0; j < ann.size; j++) {
          if(name.eq(ann.names[j]) && Compare.deep(val, ann.values[j], null))
            o.add(name, val, infos[i]);
        }
      } catch(final QueryException ex) {
        // should never happen because annotations can only contain simple literals
        Util.notexpected(ex);
      }
    }
    return o;
  }

  /**
   * Checks all annotations for parsing errors.
   * @param var variable flag
   * @throws QueryException query exception
   */
  public void check(final boolean var) throws QueryException {
    boolean up = false, vis = false;
    for(int a = 0; a < size(); a++) {
      final QNm name = names[a];
      final byte[] local = name.local();
      final byte[] uri = name.uri();
      if(name.eq(Q_UPDATING)) {
        if(up) DUPLUPD.thrw(infos[a]);
        up = true;
      } else if(name.eq(Q_PUBLIC) || name.eq(Q_PRIVATE)) {
        // only one visibility modifier allowed
        if(vis) (var ? DUPLVARVIS : DUPLVIS).thrw(infos[a]);
        vis = true;
      } else if(NSGlobal.reserved(name.uri())) {
        // no global namespaces allowed
        ANNRES.thrw(infos[a], '%', name.string());
      } else if(eq(uri, OUTPUTURI)) {
        if(Serializer.PROPS.get(string(local)) == null)
          BASX_ANNOT.thrw(infos[a], '%', name.string());
        if(values[a].size() != 1 || !values[a].itemAt(0).type.isStringOrUntyped()) {
          BASX_ANNOTARGS.thrw(infos[a], '%', name.string());
        }
      } else if(eq(uri, RESTURI)) {
        if(!eq(local, ANN_REST)) BASX_ANNOT.thrw(infos[a], '%', name.string());
      } else if(eq(uri, UNITURI)) {
        if(!eq(local, ANN_UNIT)) BASX_ANNOT.thrw(infos[a], '%', name.string());
      }
    }
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    for(int i = 0; i < size; ++i) {
      tb.add('%').add(names[i].string());
      final long s = values[i].size();
      if(s != 0) {
        tb.add('(');
        for(int a = 0; a < s; a++) {
          if(a != 0) tb.add(',');
          tb.add(values[i].itemAt(a).toString());
        }
        tb.add(')');
      }
      tb.add(' ');
    }
    return tb.toString();
  }
}
