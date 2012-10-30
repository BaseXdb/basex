package org.basex.query.value.type;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery data types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Type {
  /**
   * Casts the specified item to the XQuery data type.
   * @param it item to be converted
   * @param ctx query context
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Casts the specified Java object to the XQuery data type.
   * @param o Java object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item cast(final Object o, final InputInfo ii) throws QueryException;

  /**
   * Casts the specified string to the XQuery data type.
   * @param s string object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item castString(final String s, final InputInfo ii) throws QueryException;

  /**
   * Returns the sequence type of this data type.
   * @return sequence type
   */
  SeqType seqType();

  // PUBLIC AND STATIC METHODS ================================================

  /**
   * Checks if the current type is an instance of the specified type.
   * @param t type to be checked
   * @return result of check
   */
  boolean instanceOf(final Type t);

  /**
   * Checks if the type refers to a node.
   * @return result of check
   */
  boolean isNode();

  /**
   * Checks if the type refers to a number.
   * @return result of check
   */
  boolean isNumber();

  /**
   * Checks if the type refers to an untyped item.
   * @return result of check
   */
  boolean isUntyped();

  /**
   * Checks if the type refers to a number or an untyped item.
   * @return result of check
   */
  boolean isNumberOrUntyped();

  /**
   * Checks if the type refers to a number or a string.
   * Returns if this item is untyped or a string.
   * @return result of check
   */
  boolean isStringOrUntyped();

  /**
   * Returns the string representation of this type.
   * @return name
   */
  byte[] string();

  /**
   * Returns a type id to differentiate all types.
   * @return id
   */
  int id();

  @Override
  String toString();
}
