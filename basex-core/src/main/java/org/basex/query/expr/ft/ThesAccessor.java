package org.basex.query.expr.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.index.thes.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Thesaurus accessor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ThesAccessor {
  /** Thesaurus sources, indexed by the full-text options for normalizing terms. */
  private final HashMap<List<Object>, Source> sources = new HashMap<>();
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Requested relation. */
  private final byte[] relation;
  /** Requested minimum level. */
  private final long min;
  /** Requested maximum level. */
  private final long max;

  /** Thesaurus root node (can be {@code null}). */
  private XNode node;
  /** File reference (can be {@code null}). */
  private IO file;
  /** Name of the thesaurus database (can be {@code null}). */
  private String db;

  /**
   * Constructor.
   * @param file file reference
   */
  public ThesAccessor(final IO file) {
    this(file, null, EMPTY, 0, Long.MAX_VALUE, null);
  }

  /**
   * Constructor.
   * @param file file reference
   * @param db name of the thesaurus database (can be {@code null})
   * @param relation requested relation
   * @param min requested minimum level
   * @param max requested maximum level
   * @param info input info (can be {@code null})
   */
  public ThesAccessor(final IO file, final String db, final byte[] relation, final long min,
      final long max, final InputInfo info) {
    this(relation, min, max, info);
    this.file = file;
    this.db = db;
  }

  /**
   * Constructor.
   * @param node thesaurus root node
   * @param relation requested relation
   * @param max requested maximum level
   * @param info input info (can be {@code null})
   */
  public ThesAccessor(final XNode node, final byte[] relation, final long max,
      final InputInfo info) {
    this(relation, 0, max, info);
    this.node = node;
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
    this.min = min;
    this.max = max;
    this.info = info;
  }

  /**
   * Finds synonyms for the specified term.
   * @param term token
   * @param opt full-text options
   * @param qc query context
   * @return results
   * @throws QueryException query exception
   */
  public byte[][] find(final byte[] term, final FTOpt opt, final QueryContext qc)
      throws QueryException {
    final Source source = source(opt, qc);
    // wildcard characters are retained, so query terms with wildcards are not found
    FTOpt qopt = source.opt;
    if(opt.is(WC)) {
      qopt = qopt.copy();
      qopt.set(WC, true);
    }
    final TokenList list = new TokenList();
    final byte[] key = Thesaurus.normalize(term, new FTLexer(qopt));
    final ThesEntry entry = key.length != 0 ? source.entry(key) : null;
    if(entry != null) find(list, entry, source);
    return list.finish();
  }

  /**
   * Returns the thesaurus source for the specified full-text options.
   * @param opt full-text options
   * @param qc query context
   * @return source
   * @throws QueryException query exception
   */
  private synchronized Source source(final FTOpt opt, final QueryContext qc)
      throws QueryException {
    // database that may contain a thesaurus index
    DiskData data = null;
    if(db != null) {
      if(qc.resources.database(db, qc.user, false, info) instanceof final DiskData dd) data = dd;
    } else if(node instanceof final DBNode dbnode && node.kind() == Kind.DOCUMENT &&
        dbnode.data() instanceof final DiskData dd && dd.resources.docs().size() == 1) {
      data = dd;
    }

    // options for normalizing terms: unassigned options are adopted from the database
    final FTOpt norm = new FTOpt();
    norm.cs = opt.cs;
    norm.ln = opt.ln;
    norm.sd = opt.sd;
    for(final FTFlag flag : new FTFlag[] { DC, ST }) {
      if(opt.isSet(flag)) norm.set(flag, opt.is(flag));
    }
    if(data != null) norm.assign(new FTOpt().assign(data.meta));

    final List<Object> key = Arrays.asList(norm.cs, norm.is(DC), norm.is(ST), norm.ln, norm.sd);
    Source source = sources.get(key);
    if(source == null) {
      try {
        final String name = db != null ? db : data != null ? data.meta.name :
          file != null ? file.path() : "node";
        final ThesIndex index = data != null && data.meta.thesindex ? data.thesIndex() : null;
        if(index != null && index.compatible(norm)) {
          source = new Source(norm, null, index);
          qc.evalInfo("Thesaurus \"" + name + "\": index");
        } else {
          qc.evalInfo("Thesaurus \"" + name + "\": main memory" + (data == null ? "" :
            !data.meta.createthes ? " (no index)" : index == null ? " (index is outdated)" :
            !index.current() ? " (index format is outdated)" : " (full-text options differ)"));
          final XNode[] roots;
          if(node != null) {
            roots = new XNode[] { node };
          } else if(db != null) {
            final Data dt = qc.resources.database(db, qc.user, false, info);
            final IntList docs = dt.resources.docs();
            final int ds = docs.size();
            roots = new XNode[ds];
            for(int d = 0; d < ds; d++) roots[d] = new DBNode(dt, docs.get(d));
          } else {
            roots = new XNode[] { new DBNode(file) };
          }
          source = new Source(norm, new Thesaurus(norm, roots), null);
        }
      } catch(final IOException ex) {
        throw QueryError.NOTHES_X.get(info, db != null ? db : file).cause(ex);
      }
      sources.put(key, source);
    }
    return source;
  }

  /**
   * Collects the terms of the requested levels, level by level.
   * @param list result list
   * @param entry entry of the query term
   * @param source thesaurus source
   */
  private void find(final TokenList list, final ThesEntry entry, final Source source) {
    // the level of a term is the length of the shortest path from the query term
    final TokenSet keys = new TokenSet();
    keys.add(entry.key);
    ArrayList<ThesEntry> entries = new ArrayList<>(1);
    entries.add(entry);
    for(long level = 1; level <= max && !entries.isEmpty(); level++) {
      final boolean add = level >= min, expand = level < max;
      final ArrayList<ThesEntry> next = new ArrayList<>();
      for(final ThesEntry ent : entries) {
        ent.forEach((synonym, rel) -> {
          if((relation.length == 0 || eq(rel, relation)) && keys.add(synonym.key)) {
            if(add) list.add(synonym.term);
            if(expand) next.add(synonym);
          }
        });
      }
      entries = new ArrayList<>(next.size());
      for(final ThesEntry synonym : next) {
        final ThesEntry ent = source.entry(synonym.key);
        if(ent != null) entries.add(ent);
      }
    }
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ThesAccessor ta && Objects.equals(file, ta.file) &&
        Objects.equals(db, ta.db) && Objects.equals(node, ta.node) &&
        eq(relation, ta.relation) && min == ta.min && max == ta.max;
  }

  @Override
  public String toString() {
    return "\"" + (db != null ? db : file) + '"';
  }

  /** Thesaurus source: an in-memory structure or an index. */
  private static final class Source {
    /** Full-text options for normalizing terms. */
    private final FTOpt opt;
    /** Thesaurus structure (can be {@code null}). */
    private final Thesaurus thesaurus;
    /** Thesaurus index (can be {@code null}). */
    private final ThesIndex index;

    /**
     * Constructor.
     * @param opt full-text options for normalizing terms
     * @param thesaurus thesaurus structure (can be {@code null})
     * @param index thesaurus index (can be {@code null})
     */
    private Source(final FTOpt opt, final Thesaurus thesaurus, final ThesIndex index) {
      this.opt = opt;
      this.thesaurus = thesaurus;
      this.index = index;
    }

    /**
     * Returns the entry for the specified normalized term.
     * @param key normalized term
     * @return entry or {@code null}
     */
    private ThesEntry entry(final byte[] key) {
      if(thesaurus != null) return thesaurus.get(key);
      final int id = index.id(key);
      if(id == -1) return null;
      final ThesEntry entry = new ThesEntry(index.term(id), key);
      index.synonyms(id, (rel, syn) ->
        entry.add(new ThesEntry(index.term(syn), index.key(syn)), rel));
      return entry;
    }
  }
}
