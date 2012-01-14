package org.basex.util.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;
import org.basex.data.ExprInfo;
import org.basex.data.MetaData;
import org.basex.io.serial.Serializer;
import org.basex.query.ft.ThesQuery;

/**
 * This class contains all full-text options.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class FTOpt extends ExprInfo {
  /** Flag values. */
  private final EnumMap<FTFlag, Boolean> map =
    new EnumMap<FTFlag, Boolean>(FTFlag.class);

  /** Stemming dictionary. */
  public StemDir sd;
  /** Stopwords. */
  public StopWords sw;
  /** Thesaurus. */
  public ThesQuery th;
  /** Language. */
  public Language ln;

  /**
   * Initializes the full-text options, inheriting the options of the argument.
   * @param opt parent full-text options
   * @return self reference
   */
  public FTOpt copy(final FTOpt opt) {
    for(final Entry<FTFlag, Boolean> f : opt.map.entrySet()) {
      final FTFlag fl = f.getKey();
      if(map.get(fl) == null) map.put(fl, f.getValue());
    }
    if(sw == null) sw = opt.sw;
    if(sd == null) sd = opt.sd;
    if(ln == null) ln = opt.ln;
    if(th == null) th = opt.th;
    else if(opt.th != null) th.merge(opt.th);
    return this;
  }

  /**
   * Copies the full-text options from the specified database meta data.
   * @param md meta data
   * @return self reference
   */
  public FTOpt copy(final MetaData md) {
    set(CS, md.casesens);
    set(DC, md.diacritics);
    set(ST, md.stemming);
    ln = md.language;
    return this;
  }

  /**
   * Sets the specified flag.
   * @param f flag to be set
   * @param v value
   * @return self reference
   */
  public FTOpt set(final FTFlag f, final boolean v) {
    map.put(f, Boolean.valueOf(v));
    return this;
  }

  /**
   * Tests if the specified flag has been set.
   * @param f flag index
   * @return true if flag has been set
   */
  public boolean isSet(final FTFlag f) {
    return map.get(f) != null;
  }

  /**
   * Returns the specified flag.
   * @param f flag index
   * @return flag
   */
  public boolean is(final FTFlag f) {
    final Boolean b = map.get(f);
    return b != null && b.booleanValue();
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(is(WC)) ser.attribute(token(WILDCARDS), TRUE);
    if(is(FZ)) ser.attribute(token(FUZZY), TRUE);
    if(is(UC)) ser.attribute(token(UPPERCASE), TRUE);
    if(is(LC)) ser.attribute(token(LOWERCASE), TRUE);
    if(is(DC)) ser.attribute(token(DIACRITICS), TRUE);
    if(is(ST)) ser.attribute(token(STEMMING), TRUE);
    if(ln != null) ser.attribute(token(LANGUAGE), token(ln.toString()));
    if(th != null) ser.attribute(token(THESAURUS), TRUE);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    if(is(WC)) s.append(' ' + USING + ' ' + WILDCARDS);
    if(is(FZ)) s.append(' ' + USING + ' ' + FUZZY);
    if(is(UC)) s.append(' ' + USING + ' ' + UPPERCASE);
    if(is(LC)) s.append(' ' + USING + ' ' + LOWERCASE);
    if(is(DC)) s.append(' ' + USING + ' ' + DIACRITICS + ' ' + SENSITIVE);
    if(is(ST) || sd != null) s.append(' ' + USING + ' ' + STEMMING);
    if(ln != null) s.append(' ' + USING + ' ' + LANGUAGE + " '" + ln + '\'');
    if(th != null) s.append(' ' + USING + ' ' + THESAURUS);
    return s.toString();
  }
}
