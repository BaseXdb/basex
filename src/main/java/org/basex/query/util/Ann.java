package org.basex.query.util;

import static org.basex.query.QueryText.*;
import java.util.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Annotations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Ann extends ElementList {
  /** Annotation "public". */
  public static final QNm A_PRIVATE = new QNm(Token.token(PRIVATE), FNURI);
  /** Annotation "public". */
  public static final QNm A_PUBLIC = new QNm(Token.token(PUBLIC), FNURI);

  /** QNames. */
  private QNm[] names = new QNm[1];
  /** Values. */
  private Value[] values = new Value[1];

  /**
   * Adds a QName/Value pair.
   * @param name QName
   * @param value value
   * @return success flag
   */
  public boolean add(final QNm name, final Value value) {
    // annotation must only be specified once
    if(contains(name)) return false;

    if(size == names.length) {
      final int s = newSize();
      names = Arrays.copyOf(names, s);
      values = Arrays.copyOf(values, s);
    }
    names[size] = name;
    values[size] = value;
    size++;
    return true;
  }

  /**
   * Checks if the specified element is found in the list.
   * @param e element to be checked
   * @return result of check
   */
  public boolean contains(final QNm e) {
    for(int i = 0; i < size; ++i) if(names[i].eq(e)) return true;
    return false;
  }
}
