package org.basex.query.item;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import static org.basex.query.QueryTokens.*;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * XQuery 3.0 function data types.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FunType implements Type {

  /** Any function type. */
  public static final FunType ANY = new FunType(null, null);

  /** Argument types. */
  private final SeqType[] args;
  /** Return type. */
  private final SeqType ret;

  /** This function type's sequence type. */
  private SeqType seq;

  /**
   * Constructor.
   * @param arg argument types
   * @param rt return type
   */
  private FunType(final SeqType[] arg, final SeqType rt) {
    args = arg;
    ret = rt;
  }

  @Override
  public boolean dat() {
    return false;
  }

  @Override
  public boolean dur() {
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
    if(seq == null) seq = new SeqType(this);
    return seq;
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
    // takes care of FunType.ANY
    if(this == t || t == ANY) return true;
    if(this == ANY || !(t instanceof FunType)) return false;

    final FunType ft = (FunType) t;
    if(args.length != ft.args.length || !ret.instance(ft.ret)) return false;
    for(int i = 0; i < args.length; i++)
      if(!args[i].instance(ft.args[i])) return false;
    return true;
  }

  /**
   * Getter for function types.
   * @param args argument types
   * @param ret return type
   * @return function type
   */
  public static FunType get(final SeqType[] args, final SeqType ret) {
    if(args == null && ret == null) return ANY;
    return new FunType(args, ret);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(FUNCTION).add('(');
    if(this == ANY) {
      tb.add('*').add(')');
    } else {
      tb.addSep(args, ", ").add(") as ").add(ret.toString());
    }
    return tb.toString();
  }
}
