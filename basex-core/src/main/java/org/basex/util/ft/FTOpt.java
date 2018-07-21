package org.basex.util.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.node.*;

/**
 * This class contains all full-text options.
 *
 * @author BaseX Team 2005-18, BSD License
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

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof FTOpt)) return false;
    final FTOpt f = (FTOpt) obj;
    return map.equals(f.map) && cs == f.cs && Objects.equals(sd, f.sd) &&
        Objects.equals(sw, f.sw) && Objects.equals(th, f.th) && Objects.equals(ln, f.ln);
  }

  @Override
  public void plan(final FElem plan) {
    if(is(WC)) plan.add(planAttr(WILDCARDS, TRUE));
    if(is(FZ)) plan.add(planAttr(FUZZY, TRUE));
    if(cs != FTCase.INSENSITIVE) plan.add(planAttr(CASE, cs));
    if(is(DC)) plan.add(planAttr(DIACRITICS, TRUE));
    if(is(ST)) plan.add(planAttr(STEMMING, TRUE));
    if(ln != null) plan.add(planAttr(LANGUAGE, ln));
    if(th != null) plan.add(planAttr(THESAURUS, TRUE));
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    if(is(WC)) s.append(' ' + USING + ' ' + WILDCARDS);
    if(is(FZ)) s.append(' ' + USING + ' ' + FUZZY);
    if(cs == FTCase.LOWER) s.append(' ' + USING + ' ' + LOWERCASE);
    else if(cs == FTCase.UPPER) s.append(' ' + USING + ' ' + UPPERCASE);
    else if(cs == FTCase.SENSITIVE) s.append(' ' + USING + ' ' + CASE + ' ' + SENSITIVE);
    if(is(DC)) s.append(' ' + USING + ' ' + DIACRITICS + ' ' + SENSITIVE);
    if(is(ST) || sd != null) s.append(' ' + USING + ' ' + STEMMING);
    if(ln != null) s.append(' ' + USING + ' ' + LANGUAGE + " '").append(ln).append('\'');
    if(th != null) s.append(' ' + USING + ' ' + THESAURUS);
    return s.toString();
  }
}
