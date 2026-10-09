package org.basex.index.thes;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.io.random.*;
import org.basex.query.util.ft.thesaurus.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * Thesaurus index, stored in the files starting with {@link DataText#DATATHS}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ThesIndex implements ThesSource, Closeable {
  /** Format of the index. */
  private static final byte[] FORMAT = token("thesaurus 2");

  /** Indicates if the index has the current format. */
  private final boolean current;
  /** Offsets of the labels (can be {@code null}). */
  private DataAccess labelOffsets;
  /** Labels and their concepts (can be {@code null}). */
  private DataAccess labels;
  /** Offsets of the concepts (can be {@code null}). */
  private DataAccess conceptOffsets;
  /** Labels and relationships of the concepts (can be {@code null}). */
  private DataAccess concepts;
  /** Names of relationships (can be {@code null}). */
  private TokenSet relations;
  /** Number of labels. */
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
    labelOffsets = new DataAccess(meta.dbFile(DATATHS + 'l'));
    labels = new DataAccess(meta.dbFile(DATATHS + 't'));
    conceptOffsets = new DataAccess(meta.dbFile(DATATHS + 'o'));
    concepts = new DataAccess(meta.dbFile(DATATHS + 'c'));
    size = labelOffsets.read4(0);
  }

  /**
   * Builds the index from the thesaurus documents of a database.
   * @param data data reference
   * @throws IOException I/O exception
   */
  public static void create(final Data data) throws IOException {
    final MetaData meta = data.meta;
    final Thesaurus thesaurus = new Thesaurus(new FTOpt().assign(meta),
      DBNodeSeq.get(data.resources.docs(), data, true, true));

    // labels are sorted by their normalized keys
    final byte[][] keys = thesaurus.keys();
    final int[] order = Array.createOrder(keys, false, true);
    final int ls = order.length, cs = thesaurus.conceptCount();
    final int[] ids = new int[ls];
    for(int l = 0; l < ls; l++) ids[order[l]] = l;

    // l: offsets of labels, t: labels and their concepts
    try(DataOutput outl = new DataOutput(meta.dbFile(DATATHS + 'l'));
        DataOutput outt = new DataOutput(meta.dbFile(DATATHS + 't'))) {
      outl.write4(ls);
      for(int l = 0; l < ls; l++) {
        outl.write5(outt.size());
        outt.writeToken(keys[l]);
        outt.writeToken(thesaurus.term(order[l]));
        outt.writeNums(thesaurus.concepts(order[l]));
      }
    }
    // o: offsets of concepts, c: labels and relationships of concepts
    try(DataOutput outo = new DataOutput(meta.dbFile(DATATHS + 'o'));
        DataOutput outc = new DataOutput(meta.dbFile(DATATHS + 'c'))) {
      for(int c = 0; c < cs; c++) {
        outo.write5(outc.size());
        final int[] conceptLabels = thesaurus.labels(c);
        for(int l = 0; l < conceptLabels.length; l++) conceptLabels[l] = ids[conceptLabels[l]];
        outc.writeNums(conceptLabels);
        outc.writeNums(thesaurus.relations(c));
      }
    }
    // r: relationships and options
    try(DataOutput outr = new DataOutput(meta.dbFile(DATATHS + 'r'))) {
      outr.writeBytes(FORMAT);
      thesaurus.relations().write(outr);
      outr.writeBool(meta.casesens);
      outr.writeBool(meta.diacritics);
      outr.writeBool(meta.stemming);
      final Language ln = meta.language();
      outr.writeToken(token(ln != null ? ln.toString() : ""));
    }
  }

  /**
   * Reads ids, starting from the current position.
   * @param in input
   * @return ids
   */
  private static int[] read(final DataAccess in) {
    final int[] values = new int[in.readNum()];
    for(int v = 0; v < values.length; v++) values[v] = in.readNum();
    return values;
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

  @Override
  public synchronized int label(final byte[] key) {
    int l = 0, h = size - 1;
    while(l <= h) {
      final int m = l + h >>> 1, c = compare(key(m), key);
      if(c == 0) return m;
      if(c < 0) l = m + 1;
      else h = m - 1;
    }
    return -1;
  }

  @Override
  public synchronized byte[] term(final int label) {
    key(label);
    return labels.readToken();
  }

  @Override
  public synchronized int[] concepts(final int label) {
    key(label);
    labels.readToken();
    return read(labels);
  }

  @Override
  public synchronized int[] labels(final int concept) {
    concepts.cursor(conceptOffsets.read5(concept * 5L));
    return read(concepts);
  }

  @Override
  public synchronized int[] relations(final int concept) {
    // skip the labels
    for(int l = concepts.readNum(conceptOffsets.read5(concept * 5L)); l > 0; l--) {
      concepts.readNum();
    }
    return read(concepts);
  }

  @Override
  public int relation(final byte[] name) {
    return relations.index(Thesaurus.relationship(name));
  }

  /**
   * Returns the normalized label with the specified id.
   * @param label id of the label
   * @return normalized label
   */
  private byte[] key(final int label) {
    return labels.readToken(labelOffsets.read5(4 + label * 5L));
  }

  @Override
  public synchronized void close() {
    if(!current) return;
    labelOffsets.close();
    labels.close();
    conceptOffsets.close();
    concepts.close();
  }
}
