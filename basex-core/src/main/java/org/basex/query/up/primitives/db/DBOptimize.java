package org.basex.query.up.primitives.db;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.options.*;

/**
 * Update primitive for the optimize function.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Dimitar Popov
 */
public final class DBOptimize extends DBUpdate {
  /** Options supplied with the function call. */
  private final DBOptions options;
  /** Query context. */
  private final QueryContext qc;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param data data
   * @param all optimize all database structures flag
   * @param opts database options
   * @param qc database context
   * @param info input info
   * @throws QueryException query exception
   */
  public DBOptimize(final Data data, final boolean all, final Options opts, final QueryContext qc,
      final InputInfo info) throws QueryException {

    super(UpdateType.DBOPTIMIZE, data, info);
    this.all = all;
    this.qc = qc;

    final ArrayList<Option<?>> supported = new ArrayList<>();
    for(final Option<?> option : DBOptions.INDEXING) {
      if(all || option != MainOptions.UPDINDEX) supported.add(option);
    }
    options = new DBOptions(opts, supported, info);
  }

  @Override
  public void merge(final Update update) {
    all |= ((DBOptimize) update).all;
  }

  @Override
  public void prepare() { }

  @Override
  public void apply() throws QueryException {
    // create new options, based on global defaults, and overwrite with database options
    final MainOptions opts = new MainOptions(qc.context.options, true);
    final MetaData meta = data.meta;
    options.assignIfEmpty(MainOptions.TEXTINDEX, meta.createtext);
    options.assignIfEmpty(MainOptions.ATTRINDEX, meta.createattr);
    options.assignIfEmpty(MainOptions.TOKENINDEX, meta.createtoken);
    options.assignIfEmpty(MainOptions.FTINDEX, meta.createft);
    options.assignIfEmpty(MainOptions.TEXTINCLUDE, meta.textinclude);
    options.assignIfEmpty(MainOptions.ATTRINCLUDE, meta.attrinclude);
    options.assignIfEmpty(MainOptions.TOKENINCLUDE, meta.tokeninclude);
    options.assignIfEmpty(MainOptions.FTINCLUDE, meta.ftinclude);
    options.assignIfEmpty(MainOptions.INDEXSPLITSIZE, meta.splitsize);
    options.assignIfEmpty(MainOptions.FTINDEXSPLITSIZE, meta.ftsplitsize);
    options.assignIfEmpty(MainOptions.UPDINDEX, meta.updindex);
    options.assignIfEmpty(MainOptions.AUTOOPTIMIZE, meta.autoopt);
    options.assignTo(opts);

    // adopt options to database meta data
    meta.autoopt = opts.get(MainOptions.AUTOOPTIMIZE);
    meta.createtext = opts.get(MainOptions.TEXTINDEX);
    meta.createattr = opts.get(MainOptions.ATTRINDEX);
    meta.createtoken = opts.get(MainOptions.TOKENINDEX);
    meta.createft = opts.get(MainOptions.FTINDEX);
    meta.updindex = opts.get(MainOptions.UPDINDEX);

    // check if other indexing options have changed
    final int maxcats = opts.get(MainOptions.MAXCATS);
    final int maxlen = opts.get(MainOptions.MAXLEN);
    final String textinclude = opts.get(MainOptions.TEXTINCLUDE);
    final String attrinclude = opts.get(MainOptions.ATTRINCLUDE);
    final String tokeninclude = opts.get(MainOptions.TOKENINCLUDE);
    final boolean rebuild = maxlen != meta.maxlen;
    final boolean rebuildText = !meta.textinclude.equals(textinclude) || rebuild;
    final boolean rebuildAttr = !meta.attrinclude.equals(attrinclude) || rebuild;
    final boolean rebuildToken = !meta.tokeninclude.equals(tokeninclude);
    meta.textinclude = textinclude;
    meta.attrinclude = attrinclude;
    meta.tokeninclude = tokeninclude;
    meta.maxcats = maxcats;
    meta.maxlen = maxlen;
    meta.splitsize = opts.get(MainOptions.INDEXSPLITSIZE);

    // check if fulltext indexing options have changed
    final String ftinclude = opts.get(MainOptions.FTINCLUDE);
    final boolean stemming = opts.get(MainOptions.STEMMING);
    final boolean casesens = opts.get(MainOptions.CASESENS);
    final boolean diacritics = opts.get(MainOptions.DIACRITICS);
    final Language language = Language.get(opts);
    final String stopwords = opts.get(MainOptions.STOPWORDS);
    final boolean rebuildFt = !meta.ftinclude.equals(ftinclude) || rebuild ||
        stemming != meta.stemming || casesens != meta.casesens || diacritics != meta.diacritics ||
        !language.equals(meta.language) || !stopwords.equals(meta.stopwords);
    meta.ftinclude = ftinclude;
    meta.stemming   = stemming;
    meta.casesens   = casesens;
    meta.diacritics = diacritics;
    meta.language   = language;
    meta.stopwords  = stopwords;
    meta.ftsplitsize = opts.get(MainOptions.FTINDEXSPLITSIZE);

    try {
      if(all) OptimizeAll.optimizeAll(data, qc.context, opts, null);
      else Optimize.optimize(data, rebuildText, rebuildAttr, rebuildToken, rebuildFt, null);
    } catch(final IOException ex) {
      throw UPDBOPTERR_X.get(info, ex);
    }

    // remove old database reference
    if(all) qc.resources.remove(meta.name);
  }

  @Override
  public int size() {
    return 1;
  }
}
