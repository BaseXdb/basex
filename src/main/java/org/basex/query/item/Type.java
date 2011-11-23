package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * XQuery data types.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface Type {
  /**
   * Constructs a new item from the specified item.
   * @param it item to be converted
   * @param ctx query context
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Constructs a new item from the specified Java object.
   * The Java object is supposed to have a correct mapping type.
   * @param o Java object
   * @param ii input info
   * @return new item
   * @throws QueryException query exception
   */
  Item e(final Object o, final InputInfo ii) throws QueryException;

  /**
   * Returns the sequence type of this type.
   * @return sequence type
   */
  SeqType seq();

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
   * Checks if the type refers to an untyped value.
   * @return result of check
   */
  boolean isUntyped();

  /**
   * Checks if the type refers to a string.
   * @return result of check
   */
  boolean isString();

  /**
   * Checks if the type refers to a duration.
   * @return result of check
   */
  boolean isDuration();

  /**
   * Checks if the type refers to a date.
   * @return result of check
   */
  boolean isDate();

  /**
   * Checks if the type refers to a function item.
   * @return result of check
   */
  boolean isFunction();

  /**
   * Checks if the type refers to a map.
   * @return result of check
   */
  boolean isMap();

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
