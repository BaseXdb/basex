package org.basex.query.item;

import static org.basex.query.util.Err.*;

import java.io.IOException;
import java.io.InputStream;

import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Implementation-specific raw item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Raw extends Bin {
  /** File reference. */
  private final IOFile file;
  /** String path. */
  private final String path;

  /**
   * Constructor.
   * @param f file reference
   * @param p path to resource
   */
  public Raw(final IOFile f, final String p) {
    super(null, AtomType.RAW);
    file = f;
    path = p;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it)
      throws QueryException {

    if(it instanceof Raw) return file.eq(((Raw) it).file);
    return Token.eq(toJava(), it instanceof Bin ?
        ((Bin) it).toJava() : it.atom(ii));
  }

  @Override
  public byte[] atom(final InputInfo ii) {
    return Token.token(path);
  }

  @Override
  protected byte[] val(final InputInfo ii) throws QueryException {
    if(val == null) {
      try {
        val = file.read();
      } catch(final IOException ex) {
        throw IOERR.thrw(ii, ex);
      }
    }
    return val;
  }

  @Override
  public InputStream input() throws IOException {
    return new BufferInput(file.file());
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", path);
  }
}
