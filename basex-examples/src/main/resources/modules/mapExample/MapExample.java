package test;

import org.basex.util.*;
import java.util.HashMap;

public class MapExample {
  public static String test(final HashMap<Object, Object> map, final String key) {
    return map.get(key).toString();
  }
}
