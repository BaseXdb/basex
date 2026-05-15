package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
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
    final Item input = arg(0).atomItem(qc, info);
    if(input.isEmpty()) return Empty.VALUE;

    final String value = string(input.string(info));
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final Matcher matcher = pattern(pattern, flags).matcher(value);
    final ValueBuilder vb = new ValueBuilder(qc);
    while(matcher.find()) {
      final MapBuilder groups = new MapBuilder();
      final int gc = matcher.groupCount();
      for(int g = 1; g <= gc; g++) {
        final int start = matcher.start(g);
        if(start >= 0) {
          final MapBuilder group = new MapBuilder();
          group.put("group", matcher.group(g));
          group.put("position", Itr.get(start + 1));
          groups.put(Itr.get(g), group.map());
        }
      }
      final MapBuilder segment = new MapBuilder();
      segment.put("substring", matcher.group());
      segment.put("position", Itr.get(matcher.start() + 1));
      segment.put("groups", groups.map());
      vb.add(segment.map());
    }
    return vb.value();
  }
}
