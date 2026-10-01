package org.basex.query.func.fn;

import java.util.function.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnMatchingSegments extends RegExFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String value = toZeroString(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final RegExpr regExpr = regExpr(pattern, flags, qc);
    final String[] names = regExpr.getGroupNames();
    final Matcher matcher = regExpr.pattern.matcher(value);
    final IntUnaryOperator positions = positions(value);
    final ValueBuilder vb = new ValueBuilder(qc);
    while(matcher.find()) {
      final MapBuilder groups = new MapBuilder();
      final int gc = regExpr.groupCount(matcher);
      for(int g = 1; g <= gc; g++) {
        final int j = regExpr.group(g), s = matcher.start(j);
        if(s >= 0) {
          final String name = g <= names.length ? names[g - 1] : null;
          final Value nm = name != null ? Str.get(name) : Empty.VALUE;
          final XQMap group = XQMap.get(Records.CAPTURED_GROUP.get(),
              Str.get(matcher.group(j)), Itr.get(positions.applyAsInt(s)), Itr.get(g), nm);
          groups.put(name != null ? Str.get(name) : Itr.get(g), group);
        }
      }
      vb.add(XQMap.get(Records.MATCHING_SEGMENT.get(),
          Str.get(matcher.group()), Itr.get(positions.applyAsInt(matcher.start())), groups.map()));
    }
    return vb.value();
  }
}
