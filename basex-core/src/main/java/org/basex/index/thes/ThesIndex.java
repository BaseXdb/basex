package org.basex.index.thes;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.node.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Thesaurus index, stored in the files starting with {@link DataText#DATATHS}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ThesIndex implements Closeable {
  /** Format of the index. */
  private static final byte[] FORMAT = token("thesaurus 1");

  /** Indicates if the index has the current format. */
  private final boolean current;
  /** Offsets (can be {@code null}). */
  private DataAccess offsets;
  /** Terms (can be {@code null}). */
  private DataAccess terms;
  /** Synonyms (can be {@code null}). */
  private DataAccess synonyms;
  /** Groups of equivalent terms (can be {@code null}). */
  private DataAccess groups;
  /** Relationships (can be {@code null}). */
  private TokenSet relations;
  /** Number of terms. */
  private int size;
  /** Case sensitivity. */
  private boolean casesens;
  /** Diacritics sensitivity. */
  private boolean diacritics;
  /** Stemming. */
  private boolean stemming;
  /** Language (can be {@code null}). */
  private Language language;

  /**
   * Constructor, opening an existing index.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public ThesIndex(final Data data) throws IOException {
    final MetaData meta = data.meta;
    // indexes of other formats are ignored
    final IOFile file = meta.dbFile(DATATHS + 'r');
    current = file.exists() && startsWith(file.read(), FORMAT);
    if(!current) return;

    try(DataInput in = new DataInput(file)) {
      for(int f = 0; f < FORMAT.length; f++) in.read();
      relations = new TokenSet(in);
      casesens = in.readBool();
      diacritics = in.readBool();
      stemming = in.readBool();
      final String ln = string(in.readToken());
      language = ln.isEmpty() ? null : Language.get(ln);
    }
    offsets = new DataAccess(meta.dbFile(DATATHS + 'l'));
    terms = new DataAccess(meta.dbFile(DATATHS + 't'));
    synonyms = new DataAccess(meta.dbFile(DATATHS + 'e'));
    groups = new DataAccess(meta.dbFile(DATATHS + 'g'));
    size = offsets.read4(0);
  }

  /**
   * Builds the index from the thesaurus documents of a database.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public static void create(final Data data) throws IOException {
    final IntList docs = data.resources.docs();
    final int ds = docs.size();
    final DBNode[] roots = new DBNode[ds];
    for(int d = 0; d < ds; d++) roots[d] = new DBNode(data, docs.get(d));
    final MetaData meta = data.meta;
    final Thesaurus thesaurus = new Thesaurus(new FTOpt().assign(meta), roots);

    final byte[][] keys = thesaurus.keys();
    Arrays.sort(keys, (k1, k2) -> compare(k1, k2));
    final int ks = keys.length;
    final TokenIntMap ids = new TokenIntMap(ks);
    for(int k = 0; k < ks; k++) ids.put(keys[k], k);

    // g: groups of equivalent terms (ids of the members)
    final byte[][][] grps = thesaurus.groups();
    final int gs = grps.length;
    final long[] positions = new long[gs];
    final IntList[] memberships = new IntList[ks];
    try(DataOutput outg = new DataOutput(meta.dbFile(DATATHS + 'g'))) {
      for(int g = 0; g < gs; g++) {
        positions[g] = outg.size();
        outg.writeNum(grps[g].length);
        for(final byte[] member : grps[g]) {
          final int id = ids.get(member);
          outg.writeNum(id);
          if(memberships[id] == null) memberships[id] = new IntList(1);
          memberships[id].add(g);
        }
      }
    }

    // l: offsets of terms and synonyms, t: terms, e: synonyms and positions of groups,
    // r: relationships and options
    final TokenSet rels = new TokenSet();
    try(DataOutput outl = new DataOutput(meta.dbFile(DATATHS + 'l'));
        DataOutput outt = new DataOutput(meta.dbFile(DATATHS + 't'));
        DataOutput oute = new DataOutput(meta.dbFile(DATATHS + 'e'))) {
      outl.write4(ks);
      for(int k = 0; k < ks; k++) {
        final byte[] key = keys[k];
        outl.write5(outt.size());
        outl.write5(oute.size());
        outt.writeToken(key);
        outt.writeToken(thesaurus.term(key));
        final IntList list = new IntList();
        thesaurus.synonyms(key, (synonym, relation) -> {
          list.add(ids.get(synonym));
          list.add(rels.put(relation));
        });
        oute.writeNum(list.size() >>> 1);
        for(final int value : list.finish()) oute.writeNum(value);
        final IntList groupIds = memberships[k];
        oute.writeNum(groupIds != null ? groupIds.size() : 0);
        if(groupIds != null) {
          for(final int g : groupIds.finish()) oute.write5(positions[g]);
        }
      }
    }
    try(DataOutput outr = new DataOutput(meta.dbFile(DATATHS + 'r'))) {
      outr.writeBytes(FORMAT);
      rels.write(outr);
      outr.writeBool(meta.casesens);
      outr.writeBool(meta.diacritics);
      outr.writeBool(meta.stemming);
      final Language ln = meta.language();
      outr.writeToken(token(ln != null ? ln.toString() : ""));
    }
  }

  /**
   * Indicates if the index has the current format.
   * @return result of check
   */
  public boolean current() {
    return current;
  }

  /**
   * Checks if the index is current and was built with the specified full-text options.
   * @param opt full-text options
   * @return result of check
   */
  public boolean compatible(final FTOpt opt) {
    return current && opt.cs == (casesens ? FTCase.SENSITIVE : FTCase.INSENSITIVE) &&
        opt.is(DC) == diacritics && opt.is(ST) == stemming && opt.sd == null &&
        Objects.equals(opt.ln, language);
  }

  /**
   * Returns the id of a normalized term.
   * @param key normalized term
   * @return id, or {@code -1} if the term is not found
   */
  public synchronized int id(final byte[] key) {
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1, c = compare(key(m), key);
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -1;
  }

  /**
   * Returns the normalized term with the specified id.
   * @param id id
   * @return normalized term
   */
  public synchronized byte[] key(final int id) {
    return terms.readToken(offsets.read5(4 + id * 10L));
  }

  /**
   * Returns the original term with the specified id.
   * @param id id
   * @return term
   */
  public synchronized byte[] term(final int id) {
    terms.readToken(offsets.read5(4 + id * 10L));
    return terms.readToken();
  }

  /**
   * Passes the synonyms and the other members of the groups of a term to the specified action.
   * @param id id of the term
   * @param action action, receiving relationship and id of the synonym
   */
  public synchronized void synonyms(final int id, final ObjIntConsumer<byte[]> action) {
    final int ss = synonyms.readNum(offsets.read5(4 + id * 10L + 5));
    final IntList list = new IntList(ss << 1);
    for(int s = 0; s < ss << 1; s++) list.add(synonyms.readNum());
    final int gs = synonyms.readNum();
    final long[] positions = new long[gs];
    for(int g = 0; g < gs; g++) positions[g] = synonyms.read5();

    for(int s = 0; s < ss << 1; s += 2) action.accept(relations.key(list.get(s + 1)), list.get(s));
    for(final long position : positions) {
      final int ms = groups.readNum(position);
      final int[] members = new int[ms];
      for(int m = 0; m < ms; m++) members[m] = groups.readNum();
      for(final int member : members) {
        if(member != id) action.accept(Thesaurus.EQ, member);
      }
    }
  }

  @Override
  public synchronized void close() {
    if(!current) return;
    offsets.close();
    terms.close();
    synonyms.close();
    groups.close();
  }
}
