package org.basex.query.util.collation;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This collation is based on a standard Java collator.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class BaseXCollation extends Collation {
  /** Collator. */
  private final Comparator<Object> collator;

  /**
   * Constructor.
   * @param collator collator
   * @param uri collation URI
   */
  BaseXCollation(final Comparator<Object> collator, final byte[] uri) {
    super(uri);
    this.collator = collator;
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return collator.compare(string(string), string(compare));
  }

  @Override
  CollationElements elements(final String string, final InputInfo info) throws QueryException {
    if(!(collator instanceof final RuleBasedCollator rbc)) throw CHARCOLL.get(info);
    final CollationElementIterator it = rbc.getCollationElementIterator(string);
    return new CollationElements(it::next, it::getOffset);
  }

  @Override
  public byte[] key(final byte[] string, final InputInfo info) throws QueryException {
    if(!(collator instanceof final RuleBasedCollator rbc)) throw CHARCOLL.get(info);
    return rbc.getCollationKey(Token.string(string)).toByteArray();
  }

  @Override
  boolean keys() {
    return collator instanceof RuleBasedCollator;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final BaseXCollation bxc && collator.equals(bxc.collator);
  }
}
