package org.basex.query.util;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
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
  public static final QNm PRIVATE = new QNm(Token.token(QueryText.PRIVATE), FNURI);
  /** Annotation "public". */
  public static final QNm PUBLIC = new QNm(Token.token(QueryText.PUBLIC), FNURI);

  /** QNames. */
  public QNm[] names = new QNm[1];
  /** Values. */
  public Value[] values = new Value[1];

  /**
   * Adds a QName/Value pair.
   * @param name QName
   * @param value value
   * @return success flag
   */
  public boolean add(final QNm name, final Value value) {
    // annotation must only be specified once
    final int i = indexOf(name);
    if(i != -1) {
      // add value to existing entry
      final ItemCache ic = new ItemCache();
      ic.add(values[i]);
      ic.add(value);
      values[i] = ic.value();
    } else {
      // create new entry
      if(size == names.length) {
        final int s = newSize();
        names = Arrays.copyOf(names, s);
        values = Arrays.copyOf(values, s);
      }
      names[size] = name;
      values[size] = value;
      size++;
    }
    return true;
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be found
   * @return result of check
   */
  public boolean contains(final QNm e) {
    return indexOf(e) != -1;
  }

  /**
   * Returns the offset of an existing element, or {@code -1}.
   * @param e element to be found
   * @return offset
   */
  public int indexOf(final QNm e) {
    for(int i = 0; i < size; ++i) if(names[i].eq(e)) return i;
    return -1;
  }

  /**
   * Returns the value for the specified name, or {@code null}.
   * @param e element to be found
   * @return value
   */
  public Value value(final QNm e) {
    final int i = indexOf(e);
    return i != -1 ? values[i] : null;
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
