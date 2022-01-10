package org.basex.query.expr.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Thesaurus accessor.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class ThesAccessor {
  /** Input Info (can be {@code null}). */
  private final InputInfo info;
  /** Requested relation. */
  private final byte[] relation;
  /** Requested maximum level. */
  private final long max;

  /** Thesaurus structure. */
  private Thesaurus thesaurus;
  /** File reference. */
  private IO file;

  /**
   * Constructor.
   * @param file file reference
   */
  public ThesAccessor(final IO file) {
    this(file, EMPTY, 0, Long.MAX_VALUE, null);
  }

  /**
   * Constructor.
   * @param file file reference
   * @param relation requested relation
   * @param min requested minimum level
   * @param max requested maximum level
   * @param info input info (can be {@code null})
   */
  public ThesAccessor(final IO file, final byte[] relation, final long min, final long max,
      final InputInfo info) {
    this(relation, min, max, info);
    this.file = file;
  }

  /**
   * Constructor.
   * @param thesaurus thesaurus structure
   * @param relation requested relation
   * @param max requested maximum level
   * @param info input info (can be {@code null})
   */
  public ThesAccessor(final Thesaurus thesaurus, final byte[] relation, final long max,
      final InputInfo info) {
    this(relation, 0, max, info);
    this.thesaurus = thesaurus;
  }

  /**
   * Constructor.
   * @param relation requested relation
   * @param min requested minimum level
   * @param max requested maximum level
   * @param info input info (can be {@code null})
   */
  private ThesAccessor(final byte[] relation, final long min, final long max,
      final InputInfo info) {
    this.relation = relation;
    this.max = Math.min(max, min + 100);
    this.info = info;
  }

  /**
   * Finds synonyms for the specified term.
   * @param term token
   * @return results
   * @throws QueryException query exception
   */
  public byte[][] find(final byte[] term) throws QueryException {
    final TokenList list = new TokenList();
    final ThesEntry entry = thesaurus().get(term);
    if(entry != null) find(list, entry, 0);
    return list.finish();
  }

  /**
   * Initializes the thesaurus structure.
   * @return thesaurus structure
   * @throws QueryException query exception
   */
  private Thesaurus thesaurus() throws QueryException {
    if(thesaurus == null) {
      try {
        thesaurus = new Thesaurus(new DBNode(file));
      } catch(final IOException ex) {
        Util.debug(ex);
        throw QueryError.NOTHES_X.get(info, file);
      }
    }
    return thesaurus;
  }

  /**
   * Recursively collects relevant thesaurus terms.
   * @param list result list
   * @param entry current thesaurus entry
   * @param level current level
   */
  private void find(final TokenList list, final ThesEntry entry, final long level) {
    if(level >= max) return;

    final int ns = entry.size;
    for(int n = 0; n < ns; n++) {
      if(relation.length == 0 || eq(entry.relations[n], relation)) {
        final ThesEntry synonym = entry.synonyms[n];
        final byte[] term = synonym.term;
        if(!list.contains(term)) {
          list.add(term);
          find(list, synonym, level + 1);
        }
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof ThesAccessor)) return false;
    final ThesAccessor ta = (ThesAccessor) obj;
    return Objects.equals(file, ta.file) && Objects.equals(thesaurus, ta.thesaurus) &&
        eq(relation, ta.relation) && max == ta.max;
  }

  @Override
  public String toString() {
    return "\"" + file + '"';
  }
}
