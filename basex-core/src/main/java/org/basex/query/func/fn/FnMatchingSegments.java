package org.basex.query.func.fn;

import java.util.function.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.regex.*;
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
  public Iter iter(final QueryContext qc) throws QueryException {
    final String value = toZeroString(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final RegExpr regExpr = regExpr(pattern, flags, qc);
    final String[] names = regExpr.getGroupNames();
    final Matcher matcher = regExpr.matcher(value, qc);
    final int gc = regExpr.groupCount(matcher);
    final IntUnaryOperator positions = positions(value);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        if(!matcher.find()) return null;
        final MapBuilder groups = new MapBuilder();
        for(int g = 1; g <= gc; g++) {
          final int j = regExpr.group(g), s = matcher.start(j);
          if(s >= 0) {
            final Str name = g <= names.length && names[g - 1] != null ?
              Str.get(names[g - 1]) : null;
            final XQMap group = XQMap.get(Records.CAPTURED_GROUP.get(),
                Str.get(matcher.group(j)), Itr.get(positions.applyAsInt(s)), Itr.get(g),
                name != null ? name : Empty.VALUE);
            groups.put(name != null ? name : Itr.get(g), group);
          }
        }
        return XQMap.get(Records.MATCHING_SEGMENT.get(),
            Str.get(matcher.group()), Itr.get(positions.applyAsInt(matcher.start())), groups.map());
      }
    };
  }
}
