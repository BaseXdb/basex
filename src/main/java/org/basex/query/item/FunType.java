package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;

/**
 * XQuery 3.0 function data types.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class FunType implements Type {

  @Override
  public boolean dat() {
    return false;
  }

  @Override
  public boolean dur() {
    return false;
  }

  @Override
  public Item e(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Item e(final Object o, final InputInfo ii) throws QueryException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean instance(final Type t) {
    return false;
  }

  @Override
  public byte[] nam() {
    return null;
  }

  @Override
  public boolean node() {
    return false;
  }

  @Override
  public boolean num() {
    return false;
  }

  @Override
  public Type par() {
    return null;
  }

  @Override
  public SeqType seq() {
    return null;
  }

  @Override
  public boolean str() {
    return false;
  }

  @Override
  public boolean unt() {
    return false;
  }

  @Override
  public byte[] uri() {
    return null;
  }

  @Override
  public boolean func() {
    return true;
  }

  /**
   * Getter for function types.
   * @param args argument types
   * @param ret return type
   * @return function type
   */
  public static FunType instance(final Type[] args, final Type ret) {
    return null;
  }

}
