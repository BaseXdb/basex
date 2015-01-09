package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import org.junit.*;

/**
 * EXPath package API: Version tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class VersionTest {
  /**
   * Tests method isCompatible.
   */
  @Test
  public void compatible() {
    // case 1: template = 3
    final Version semVer1 = new Version(token("3"));
    assertTrue(new Version(token("3.0")).isCompatible(semVer1));
    assertTrue(new Version(token("3.1")).isCompatible(semVer1));
    assertTrue(new Version(token("3.9")).isCompatible(semVer1));
    assertTrue(new Version(token("3.2.4")).isCompatible(semVer1));
    assertTrue(new Version(token("3.9.89")).isCompatible(semVer1));
    assertFalse(new Version(token("4")).isCompatible(semVer1));
    assertFalse(new Version(token("2.7.4")).isCompatible(semVer1));

    // case 2: template = 3.5
    final Version semVer2 = new Version(token("3.5"));
    assertTrue(new Version(token("3.5")).isCompatible(semVer2));
    assertTrue(new Version(token("3.5.45")).isCompatible(semVer2));
    assertFalse(new Version(token("3.6")).isCompatible(semVer2));
    assertFalse(new Version(token("2.7.4")).isCompatible(semVer2));
    assertFalse(new Version(token("3.4")).isCompatible(semVer2));

    // case 3: template = 3.4.7
    final Version semVer3 = new Version(token("3.4.7"));
    assertTrue(new Version(token("3.4.7")).isCompatible(semVer3));
    assertFalse(new Version(token("3.4")).isCompatible(semVer3));
    assertFalse(new Version(token("3.4.8")).isCompatible(semVer3));
    assertFalse(new Version(token("3")).isCompatible(semVer3));
  }

  /**
   * Tests comparison between version, which is compatible to and greater than a
   * given version template.
   */
  @Test
  public void compatGreater() {
    // case 1: template = 3
    final Version semVer1 = new Version(token("3"));
    final Version v1 = new Version(token("3"));
    assertTrue(v1.isCompatible(semVer1) || v1.compareTo(semVer1) > 0);
    final Version v2 = new Version(token("3.4"));
    assertTrue(v2.isCompatible(semVer1) || v2.compareTo(semVer1) > 0);
    final Version v3 = new Version(token("3.4.55"));
    assertTrue(v3.isCompatible(semVer1) || v3.compareTo(semVer1) > 0);
    final Version v4 = new Version(token("2.6"));
    assertFalse(v4.isCompatible(semVer1) || v4.compareTo(semVer1) > 0);

    // case 2: template = 3.5
    final Version semVer2 = new Version(token("3.5"));
    final Version vv1 = new Version(token("3.5"));
    assertTrue(vv1.isCompatible(semVer2) || vv1.compareTo(semVer2) > 0);
    final Version vv2 = new Version(token("3.5.6"));
    assertTrue(vv2.isCompatible(semVer2) || vv2.compareTo(semVer2) > 0);
    final Version vv3 = new Version(token("9"));
    assertTrue(vv3.isCompatible(semVer2) || vv3.compareTo(semVer2) > 0);
    final Version vv4 = new Version(token("3.4"));
    assertFalse(vv4.isCompatible(semVer2) || vv4.compareTo(semVer2) > 0);
    final Version vv5 = new Version(token("3.6"));
    assertTrue(vv5.isCompatible(semVer2) || vv5.compareTo(semVer2) > 0);

    // case 3: template = 3.4.7
    final Version semVer3 = new Version(token("3.4.7"));
    final Version vvv1 = new Version(token("3.4.7"));
    assertTrue(vvv1.isCompatible(semVer3) || vvv1.compareTo(semVer3) > 0);
    final Version vvv2 = new Version(token("3.4.77"));
    assertTrue(vvv2.isCompatible(semVer3) || vvv2.compareTo(semVer3) > 0);
    final Version vvv3 = new Version(token("3.6"));
    assertTrue(vvv3.isCompatible(semVer3) || vvv3.compareTo(semVer3) > 0);
    final Version vvv4 = new Version(token("101"));
    assertTrue(vvv4.isCompatible(semVer3) || vvv4.compareTo(semVer3) > 0);
    final Version vvv5 = new Version(token("3.4"));
    assertFalse(vvv5.isCompatible(semVer3) || vvv5.compareTo(semVer3) > 0);
    final Version vvv6 = new Version(token("3.4.0"));
    assertFalse(vvv6.isCompatible(semVer3) || vvv6.compareTo(semVer3) > 0);
    final Version vvv7 = new Version(token("3.2"));
    assertFalse(vvv7.isCompatible(semVer3) || vvv7.compareTo(semVer3) > 0);
    final Version vvv8 = new Version(token("1.0"));
    assertFalse(vvv8.isCompatible(semVer3) || vvv8.compareTo(semVer3) > 0);
  }

  /**
   * Tests comparison between version, which is compatible to and smaller than a
   * given version template.
   */
  @Test
  public void compatSmaller() {
    // case 1: template = 3
    final Version semVer1 = new Version(token("3"));
    final Version v1 = new Version(token("3"));
    assertTrue(v1.isCompatible(semVer1) || v1.compareTo(semVer1) < 0);
    final Version v2 = new Version(token("2.4"));
    assertTrue(v2.isCompatible(semVer1) || v2.compareTo(semVer1) < 0);
    final Version v3 = new Version(token("2"));
    assertTrue(v3.isCompatible(semVer1) || v3.compareTo(semVer1) < 0);
    final Version v4 = new Version(token("4.6.55"));
    assertFalse(v4.isCompatible(semVer1) || v4.compareTo(semVer1) < 0);

    // case 2: template = 3.5
    final Version semVer2 = new Version(token("3.5"));
    final Version vv1 = new Version(token("3.5"));
    assertTrue(vv1.isCompatible(semVer2) || vv1.compareTo(semVer2) < 0);
    final Version vv2 = new Version(token("3.5.0"));
    assertTrue(vv2.isCompatible(semVer2) || vv2.compareTo(semVer2) < 0);
    final Version vv3 = new Version(token("2"));
    assertTrue(vv3.isCompatible(semVer2) || vv3.compareTo(semVer2) < 0);
    final Version vv4 = new Version(token("3.6"));
    assertFalse(vv4.isCompatible(semVer2) || vv4.compareTo(semVer2) < 0);
    final Version vv5 = new Version(token("3.4.66"));
    assertTrue(vv5.isCompatible(semVer2) || vv5.compareTo(semVer2) < 0);
    final Version vv6 = new Version(token("6"));
    assertFalse(vv6.isCompatible(semVer2) || vv6.compareTo(semVer2) < 0);

    // case 3: template = 3.4.7
    final Version semVer3 = new Version(token("3.4.7"));
    final Version vvv1 = new Version(token("3.4.7"));
    assertTrue(vvv1.isCompatible(semVer3) || vvv1.compareTo(semVer3) < 0);
    final Version vvv2 = new Version(token("3.4.6"));
    assertTrue(vvv2.isCompatible(semVer3) || vvv2.compareTo(semVer3) < 0);
    final Version vvv3 = new Version(token("3.3"));
    assertTrue(vvv3.isCompatible(semVer3) || vvv3.compareTo(semVer3) < 0);
    final Version vvv4 = new Version(token("1"));
    assertTrue(vvv4.isCompatible(semVer3) || vvv4.compareTo(semVer3) < 0);
    final Version vvv5 = new Version(token("3.4.8"));
    assertFalse(vvv5.isCompatible(semVer3) || vvv5.compareTo(semVer3) < 0);
    final Version vvv6 = new Version(token("3.5.0"));
    assertFalse(vvv6.isCompatible(semVer3) || vvv6.compareTo(semVer3) < 0);
    final Version vvv8 = new Version(token("3.4.0"));
    assertTrue(vvv8.isCompatible(semVer3) || vvv8.compareTo(semVer3) < 0);
  }

  /**
   * Tests if a version is in a given interval.
   */
  @Test
  public void interval() {
    final Version semVerMin1 = new Version(token("3"));
    final Version semVerMax1 = new Version(token("7"));
    final Version test1 = new Version(token("3.0"));
    assertTrue(test1.compareTo(semVerMin1) > 0
        && test1.compareTo(semVerMax1) < 0);
    final Version test2 = new Version(token("3.5"));
    assertTrue(test2.compareTo(semVerMin1) > 0
        && test2.compareTo(semVerMax1) < 0);
    final Version test3 = new Version(token("6.9"));
    assertTrue(test3.compareTo(semVerMin1) > 0
        && test3.compareTo(semVerMax1) < 0);
    final Version test4 = new Version(token("7.0"));
    assertFalse(test4.compareTo(semVerMin1) > 0
        && test4.compareTo(semVerMax1) < 0);
    final Version test5 = new Version(token("7.6.0"));
    assertFalse(test5.compareTo(semVerMin1) > 0
        && test5.compareTo(semVerMax1) < 0);
    final Version test6 = new Version(token("2.0"));
    assertFalse(test6.compareTo(semVerMin1) > 0
        && test6.compareTo(semVerMax1) < 0);
  }

}
