package org.basex.query.value.item;

import static org.basex.data.DataText.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract string item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AStr extends Item {
  /**
   * Constructor.
   */
  AStr() {
    super(AtomType.STR);
  }

  /**
   * Constructor, specifying a type.
   * @param type atomic type
   */
  AStr(final AtomType type) {
    super(type);
  }

  @Override
  public final boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return coll == null ? Token.eq(string(ii), it.string(ii)) :
      coll.compare(string(ii), it.string(ii)) == 0;
  }

  @Override
  public final int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return coll == null ? Token.diff(string(ii), it.string(ii)) :
      coll.compare(string(ii), it.string(ii));
  }

  @Override
  public String toString() {
    try {
      final ByteList tb = new ByteList();
      tb.add('"');
      for(final byte v : string(null)) {
        if(v == '&') tb.add(E_AMP);
        else if(v == '\r') tb.add(E_0D);
        else if(v == '\n') tb.add(E_0A);
        else tb.add(v);
        if(v == '"') tb.add('"');
      }
      return tb.add('"').toString();
    } catch(final QueryException ex) {
      Util.debug(ex);
      return "";
    }
  }
}
