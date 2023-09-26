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
 * @author BaseX Team 2005-23, BSD License
 * @author Dimitar Popov
 */
public final class DBOptimize extends DBUpdate {
  /** Main options. */
  private final MainOptions options;
  /** Query context. */
  private final QueryContext qc;
  /** Flag to optimize all database structures. */
  private boolean all;

  /**
   * Constructor.
   * @param data data
   * @param all optimize all database structures flag
   * @param opts query options
   * @param qc database context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBOptimize(final Data data, final boolean all, final HashMap<String, String> opts,
      final QueryContext qc, final InputInfo info) throws QueryException {

    super(UpdateType.DBOPTIMIZE, data, info);
    this.all = all;
    this.qc = qc;

    final ArrayList<Option<?>> supported = new ArrayList<>();
    for(final Option<?> option : MainOptions.INDEXING) {
      if(all || option != MainOptions.UPDINDEX) supported.add(option);
    }

    // create options, based on global defaults
    final DBOptions dbopts = new DBOptions(opts, supported, info);
    final MetaData meta = data.meta;
    dbopts.assignIfAbsent(MainOptions.TEXTINDEX, meta.createtext);
    dbopts.assignIfAbsent(MainOptions.ATTRINDEX, meta.createattr);
    dbopts.assignIfAbsent(MainOptions.TOKENINDEX, meta.createtoken);
    dbopts.assignIfAbsent(MainOptions.FTINDEX, meta.createft);
    dbopts.assignIfAbsent(MainOptions.TEXTINCLUDE, meta.textinclude);
    dbopts.assignIfAbsent(MainOptions.ATTRINCLUDE, meta.attrinclude);
    dbopts.assignIfAbsent(MainOptions.TOKENINCLUDE, meta.tokeninclude);
    dbopts.assignIfAbsent(MainOptions.FTINCLUDE, meta.ftinclude);
    dbopts.assignIfAbsent(MainOptions.UPDINDEX, meta.updindex);
    dbopts.assignIfAbsent(MainOptions.AUTOOPTIMIZE, meta.autooptimize);
    dbopts.assignIfAbsent(MainOptions.SPLITSIZE, meta.splitsize);
    dbopts.assignIfAbsent(MainOptions.MAXCATS, meta.maxcats);
    dbopts.assignIfAbsent(MainOptions.MAXLEN, meta.maxlen);
    options = dbopts.assignTo(new MainOptions(qc.context.options, false));
  }

  @Override
  public void prepare() {
  }

  @Override
  public void apply() throws QueryException {
    // check which options have changed
    final int maxlen = options.get(MainOptions.MAXLEN);
    final String textinclude = options.get(MainOptions.TEXTINCLUDE);
    final String attrinclude = options.get(MainOptions.ATTRINCLUDE);
    final String tokeninclude = options.get(MainOptions.TOKENINCLUDE);
    final String ftinclude = options.get(MainOptions.FTINCLUDE);
    final boolean stemming = options.get(MainOptions.STEMMING);
    final boolean casesens = options.get(MainOptions.CASESENS);
    final boolean diacritics = options.get(MainOptions.DIACRITICS);
    final Language language = Language.get(options);
    final String stopwords = options.get(MainOptions.STOPWORDS);

    final MetaData meta = data.meta;
    final boolean rebuild = maxlen != meta.maxlen;
    final boolean rebuildText = !meta.textinclude.equals(textinclude) || rebuild;
    final boolean rebuildAttr = !meta.attrinclude.equals(attrinclude) || rebuild;
    final boolean rebuildToken = !meta.tokeninclude.equals(tokeninclude);
    final boolean rebuildFt = !meta.ftinclude.equals(ftinclude) || rebuild ||
        stemming != meta.stemming || casesens != meta.casesens || diacritics != meta.diacritics ||
        !language.equals(meta.language) || !stopwords.equals(meta.stopwords);

    // assign options to meta data
    meta.createtext = options.get(MainOptions.TEXTINDEX);
    meta.createattr = options.get(MainOptions.ATTRINDEX);
    meta.createtoken = options.get(MainOptions.TOKENINDEX);
    meta.createft = options.get(MainOptions.FTINDEX);
    meta.maxcats = options.get(MainOptions.MAXCATS);
    meta.updindex = options.get(MainOptions.UPDINDEX);
    meta.autooptimize = options.get(MainOptions.AUTOOPTIMIZE);
    meta.splitsize = options.get(MainOptions.SPLITSIZE);
    meta.textinclude = textinclude;
    meta.attrinclude = attrinclude;
    meta.tokeninclude = tokeninclude;
    meta.maxlen = maxlen;
    meta.ftinclude = ftinclude;
    meta.stemming   = stemming;
    meta.casesens   = casesens;
    meta.diacritics = diacritics;
    meta.language   = language;
    meta.stopwords  = stopwords;

    try {
      if(all) OptimizeAll.optimizeAll(data, qc.context, options, null);
      else Optimize.optimize(data, rebuildText, rebuildAttr, rebuildToken, rebuildFt, null);
    } catch(final IOException ex) {
      throw UPDBERROR_X.get(info, ex);
    }

    // remove old database reference
    if(all) qc.resources.remove(meta.name);
  }

  @Override
  public void merge(final Update update) {
    all |= ((DBOptimize) update).all;
  }

  @Override
  public int size() {
    return 1;
  }
}
