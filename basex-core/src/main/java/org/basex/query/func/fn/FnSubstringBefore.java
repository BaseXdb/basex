package org.basex.query.func.fn;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringBefore extends SubstringFn {
  @Override
  boolean before() {
    return true;
  }

  @Override
  boolean last() {
    return false;
  }
}
