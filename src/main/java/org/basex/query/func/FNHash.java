package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.security.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Hashing functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNHash extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHash(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _HASH_MD5:         return hash("MD5", ctx);
      case _HASH_SHA1:        return hash("SHA", ctx);
      case _HASH_SHA256:      return hash("SHA-256", ctx);
      case _HASH_HASH:        return hash(ctx);
      default:                return super.item(ctx, ii);
    }
  }

  /**
   * Creates the hash of the given xs:string.
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex hash(final QueryContext ctx) throws QueryException {
    return hash(Token.string(checkStr(expr[1], ctx)), ctx);
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param algo hashing algorithm
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex hash(final String algo, final QueryContext ctx) throws QueryException {
    final Item it = checkItem(expr[0], ctx);
    final byte[] val;
    if(it.type.isString()) {
      val = it.string(info);
    } else if(it instanceof Bin) {
      val = ((Bin) it).binary(info);
    } else {
      throw BINSTRTYPE.thrw(info, it.type);
    }
    return hashBinary(val, algo);
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param val value to be hashed
   * @param algo hashing algorithm
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private Hex hashBinary(final byte[] val, final String algo) throws QueryException {
    try {
      return new Hex(MessageDigest.getInstance(algo).digest(val));
    } catch(final NoSuchAlgorithmException ex) {
      throw HASH_ALG.thrw(info, algo);
    }
  }
}
