package org.basex.query.func.fn;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

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

    final Matcher matcher = pattern(pattern, flags, qc).matcher(value);
    final ValueBuilder vb = new ValueBuilder(qc);
    while(matcher.find()) {
      final MapBuilder groups = new MapBuilder();
      final int gc = matcher.groupCount();
      for(int g = 1; g <= gc; g++) {
        final int s = matcher.start(g);
        if(s >= 0) groups.put(Itr.get(g), new XQRecordMap(Records.MATCHING_GROUP.get(),
            Str.get(matcher.group(g)), Itr.get(s + 1)));
      }
      vb.add(new XQRecordMap(Records.MATCHING_SEGMENT.get(),
          Str.get(matcher.group()), Itr.get(matcher.start() + 1), groups.map()));
    }
    return vb.value();
  }
}
