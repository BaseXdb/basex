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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNHash extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHash(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
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
  private B64 hash(final QueryContext ctx) throws QueryException {
    return hash(Token.string(checkStr(expr[1], ctx)), ctx);
  }

  /**
   * Creates the hash of a string, using the given algorithm.
   * @param algo hashing algorithm
   * @param ctx query context
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private B64 hash(final String algo, final QueryContext ctx) throws QueryException {
    return hashBinary(checkStrBin(checkItem(expr[0], ctx)), algo);
  }

  /**
   * Creates the hash of the given xs:string, using the algorithm {@code algo}.
   * @param val value to be hashed
   * @param algo hashing algorithm
   * @return xs:hexBinary instance containing the hash
   * @throws QueryException exception
   */
  private B64 hashBinary(final byte[] val, final String algo) throws QueryException {
    try {
      return new B64(MessageDigest.getInstance(algo).digest(val));
    } catch(final NoSuchAlgorithmException ex) {
      throw HASH_ALG.thrw(info, algo);
    }
  }
}
