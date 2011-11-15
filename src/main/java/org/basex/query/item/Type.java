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
   * Checks if the specified type is an instance of the current type.
   * @param t type to be checked
   * @return result of check
   */
  boolean instance(final Type t);

  /**
   * Checks if the type refers to a node.
   * @return result of check
   */
  boolean node();

  /**
   * Checks if the type refers to a number.
   * @return result of check
   */
  boolean num();

  /**
   * Checks if the type refers to an untyped value.
   * @return result of check
   */
  boolean unt();

  /**
   * Checks if the type refers to a string.
   * @return result of check
   */
  boolean str();

  /**
   * Checks if the type refers to a duration.
   * @return result of check
   */
  boolean dur();

  /**
   * Checks if the type refers to a date.
   * @return result of check
   */
  boolean dat();

  /**
   * Checks if the type refers to a function item.
   * @return result of check
   */
  boolean func();

  /**
   * Checks if the type refers to a function item.
   * @return result of check
   */
  boolean map();

  /**
   * Returns the name of this type.
   * @return name
   */
  byte[] nam();

  /**
   * Returns a type id to differentiate all types.
   * @return id
   */
  int id();

  @Override
  String toString();
}
