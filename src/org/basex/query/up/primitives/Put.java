package org.basex.query.up.primitives;

import static org.basex.query.QueryText.*;

import java.io.IOException;

import org.basex.core.Main;
import org.basex.data.XMLSerializer;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.Uri;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Put primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class Put extends UpdatePrimitive {
  /** Put location. */
  final Uri u;
  /** Output stream. */
  private PrintOutput out;

  /**
   * Constructor.
   * @param n node to put
   * @param uri location uri
   */
  public Put(final Nod n, final Uri uri) {
    super(n);
    u = uri;
  }

  @Override
  public void apply(final int add) {
    try {
      final XMLSerializer ser = new XMLSerializer(out);
      node.serialize(ser);
      ser.close();
      out.close();

    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void prepare() throws QueryException {
    try {
      out = new PrintOutput(path());
    } catch(final IOException ex) {
      Main.debug(ex);
      Err.or(UPFOURI, path());
    }
  }

  @Override
  public PrimitiveType type() {
    return PrimitiveType.PUT;
  }

  @Override
  public void merge(final UpdatePrimitive p) {
  }

  /**
   * Returns uri as string.
   * @return string uri
   */
  public String path() {
    return Token.string(u.str());
  }
}