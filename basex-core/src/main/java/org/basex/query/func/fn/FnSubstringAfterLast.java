package org.basex.query.func.fn;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringAfterLast extends SubstringFn {
  @Override
  boolean before() {
    return false;
  }

  @Override
  boolean last() {
    return true;
  }
}
