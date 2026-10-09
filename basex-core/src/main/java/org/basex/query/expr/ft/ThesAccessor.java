package org.basex.query.expr.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.index.thes.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.ft.thesaurus.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * Thesaurus accessor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ThesAccessor {
  /** Default maximum level. */
  public static final int LEVELS = 1;

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
   * Checks if this accessor was created for the specified thesaurus node and options.
   * @param nd thesaurus root node
   * @param rel requested relation
   * @param mx requested maximum level
   * @return result of check
   */
  public boolean matches(final XNode nd, final byte[] rel, final long mx) {
    return node != null && node.is(nd) && eq(relation, rel) && max == mx;
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
    FTOpt qopt = source.opt();
    if(opt.is(WC)) {
      qopt = qopt.copy();
      qopt.set(WC, true);
    }
    final TokenList list = new TokenList();
    final byte[] key = Thesaurus.normalize(term, new FTLexer(qopt));
    final int label = key.length != 0 ? source.thesaurus().label(key) : -1;
    if(label != -1) find(list, label, source.thesaurus());
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
    final Data thesData = db != null ? qc.resources.database(db, qc, false, info) : null;
    DiskData data = null;
    if(thesData instanceof final DiskData dd) {
      data = dd;
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
          source = new Source(norm, index);
          qc.evalInfo("Thesaurus \"" + name + "\": index");
        } else {
          qc.evalInfo("Thesaurus \"" + name + "\": main memory" + (data == null ? "" :
            !data.meta.createthes ? " (no index)" : index == null ? " (index is outdated)" :
            !index.current() ? " (index format is outdated)" : " (full-text options differ)"));
          final Value roots = node != null ? node : thesData != null ?
            DBNodeSeq.get(thesData.resources.docs(), thesData, true, true) : new DBNode(file);
          source = new Source(norm, new Thesaurus(norm, roots));
        }
      } catch(final IOException ex) {
        throw QueryError.NOTHES_X.get(info, db != null ? db : file).cause(ex);
      }
      sources.put(key, source);
    }
    return source;
  }

  /**
   * Collects the labels of the concepts of the query term and of the requested levels.
   * @param list result list
   * @param label id of the query term
   * @param source thesaurus source
   */
  private void find(final TokenList list, final int label, final ThesSource source) {
    // the other labels of the concepts of the term are matched like the term itself
    final BitSet labels = new BitSet(), concepts = new BitSet();
    labels.set(label);
    final IntConsumer add = concept -> {
      for(final int l : source.labels(concept)) {
        if(!labels.get(l)) {
          labels.set(l);
          list.add(source.term(l));
        }
      }
    };
    IntList current = new IntList();
    for(final int concept : source.concepts(label)) {
      concepts.set(concept);
      current.add(concept);
      add.accept(concept);
    }
    // the level of a concept is the length of the shortest path from the concepts of the term
    int rel = 0;
    if(relation.length != 0) {
      rel = source.relation(relation);
      if(rel == 0) return;
    }
    for(long level = 1; level <= max && !current.isEmpty(); level++) {
      final IntList next = new IntList();
      for(final int concept : current.finish()) {
        final int[] relations = source.relations(concept);
        for(int r = 0; r < relations.length; r += 2) {
          final int target = relations[r];
          if((rel == 0 || relations[r + 1] == rel) && !concepts.get(target)) {
            concepts.set(target);
            if(level >= min) add.accept(target);
            if(level < max) next.add(target);
          }
        }
      }
      current = next;
    }
  }

  /**
   * Passes on the thesaurus database to the visitor.
   * @param visitor visitor
   * @return result of check
   */
  boolean accept(final ASTVisitor visitor) {
    return db == null || visitor.lock(db, false);
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

  /**
   * Thesaurus source: an in-memory structure or an index.
   * @param opt full-text options for normalizing terms
   * @param thesaurus thesaurus structure or index
   */
  private record Source(FTOpt opt, ThesSource thesaurus) { }
}
