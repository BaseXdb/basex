package org.basex.util.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.node.*;
import org.basex.util.list.*;

/**
 * This class contains all full-text options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTOpt extends ExprInfo {
  /** Flag values. */
  private final EnumMap<FTFlag, Boolean> map = new EnumMap<>(FTFlag.class);
  /** Case (can be {@code null}). */
  public FTCase cs;
  /** Stemming dictionary (can be {@code null}). */
  public StemDir sd;
  /** Stop words (can be {@code null}). */
  public StopWords sw;
  /** Thesaurus (can be {@code null}). */
  public ThesQuery th;
  /** Language (can be {@code null}). */
  public Language ln;
  /** Levenshtein error (ignored if {@code -1}). */
  public int errors = -1;

  /**
   * Adopts the options of the specified argument.
   * @param opt parent full-text options
   * @return self reference
   */
  public FTOpt assign(final FTOpt opt) {
    opt.map.forEach((key, value) -> map.computeIfAbsent(key, k -> value));
    if(cs == null) cs = opt.cs;
    if(sw == null) sw = opt.sw;
    if(sd == null) sd = opt.sd;
    if(ln == null) ln = opt.ln;
    if(th == null) th = opt.th;
    else if(opt.th != null) th.merge(opt.th);
    if(errors == -1) errors = opt.errors;
    return this;
  }

  /**
   * Assigns the full-text options from the specified database meta data.
   * @param md meta data
   * @return self reference
   */
  public FTOpt assign(final MetaData md) {
    set(DC, md.diacritics);
    set(ST, md.stemming);
    cs = md.casesens ? FTCase.SENSITIVE : FTCase.INSENSITIVE;
    ln = md.language;
    return this;
  }

  /**
   * Sets the specified flag.
   * @param flag flag to be set
   * @param value value
   */
  public void set(final FTFlag flag, final boolean value) {
    map.put(flag, value);
  }

  /**
   * Tests if the specified flag has been set.
   * @param flag flag index
   * @return true if flag has been set
   */
  public boolean isSet(final FTFlag flag) {
    return map.containsKey(flag);
  }

  /**
   * Returns the specified flag.
   * @param flag flag index
   * @return flag
   */
  public boolean is(final FTFlag flag) {
    final Boolean b = map.get(flag);
    return b != null && b;
  }

  /**
   * Creates a copy of the full-text options.
   * @return copy
   */
  public FTOpt copy() {
    return new FTOpt().assign(this);
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTOpt)) return false;
    final FTOpt f = (FTOpt) obj;
    return map.equals(f.map) && cs == f.cs && Objects.equals(sd, f.sd) && errors == f.errors &&
        Objects.equals(sw, f.sw) && Objects.equals(th, f.th) && Objects.equals(ln, f.ln);
  }

  @Override
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.create(this,
      WILDCARDS, is(WC) ? TRUE : null, FUZZY, is(FZ) ? TRUE : null,
      ERRORS, errors != -1 ? errors : null, CASE, cs,
      STEMMING, is(ST) || sd != null ? TRUE : null, LANGUAGE, ln,
      THESAURUS, th != null ? TRUE : null);
    if(elem.attributeIter().next() != null) plan.add(elem);
  }

  @Override
  public void plan(final QueryString qs) {
    final StringList list = new StringList();
    if(is(WC)) list.add(WILDCARDS);
    if(is(FZ)) list.add(errors != -1 ? FUZZY + ' ' + errors + ' ' + ERRORS : FUZZY);
    if(cs == FTCase.LOWER) list.add(LOWERCASE);
    else if(cs == FTCase.UPPER) list.add(UPPERCASE);
    else if(cs == FTCase.SENSITIVE) list.add(CASE + ' ' + SENSITIVE);
    if(is(DC)) list.add(DIACRITICS + ' ' + SENSITIVE);
    if(is(ST) || sd != null) list.add(STEMMING);
    if(ln != null) list.add(LANGUAGE + " \"" + ln + '"');
    if(th != null) list.add(THESAURUS);

    for(final String opt : list) qs.token(USING).token(opt);
  }
}
