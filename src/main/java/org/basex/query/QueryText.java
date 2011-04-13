package org.basex.query;

/**
 * This class assembles textual information of the XQuery package.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface QueryText {

  /** Example for a Date format. */
  String XDATE = "2000-12-31";
  /** Example for a Time format. */
  String XTIME = "23:59:59";
  /** Example for a DateTime format. */
  String XDTM = XDATE + "T" + XTIME;
  /** Example for a DayTimeDuration format. */
  String XDTD = "P23DT12M34S";
  /** Example for a YearMonthDuration format. */
  String XYMD = "P2000Y12M";
  /** Example for a Duration format. */
  String XDURR = "P2000Y12MT23H12M34S";
  /** Example for a YearMonth format. */
  String XYMO = "2000-12";
  /** Example for a Year format. */
  String XYEA = "2000";
  /** Example for a MonthDay format. */
  String XMDA = "--12-31";
  /** Example for a Day format. */
  String XDAY = "---31";
  /** Example for a Month format. */
  String XMON = "--12";

  // OPTIMIZATIONS

  /** Optimization info. */
  String OPTDESC = "optimizing descendant-or-self step(s)";
  /** Optimization info. */
  String OPTPATH = "merging axis paths";
  /** Optimization info. */
  String OPTPRE = "pre-evaluating %";
  /** Optimization info. */
  String OPTWRITE = "rewriting %";
  /** Optimization info. */
  String OPTREMOVE = "%: removing %";
  /** Optimization info. */
  String OPTFORLET = "moving for/let clauses";
  /** Optimization info. */
  String OPTSWAP = "operands swapped: %";
  /** Optimization info. */
  String OPTTEXT = "adding text() step";
  /** Optimization info. */
  String OPTFLWOR = "simplifying flwor expression";
  /** Optimization info. */
  String OPTINLINE = "inlining function %(...)";
  /** Optimization info. */
  String OPTWHERE = "rewriting where clause to predicate(s)";
  /** Optimization info. */
  String OPTCAST = "removing redundant % cast.";
  /** Optimization info. */
  String OPTVAR = "removing variable %";
  /** Optimization info. */
  String OPTPREF = "skipping namespace test for \"%\"";
  /** Optimization info. */
  String OPTNAME = "removing unknown tag/attribute %";
  /** Optimization info. */
  String OPTTXTINDEX = "applying text index";
  /** Optimization info. */
  String OPTATVINDEX = "applying attribute index";
  /** Optimization info. */
  String OPTFTXINDEX = "applying full-text index";
  /** Optimization info. */
  String OPTRNGINDEX = "applying range index";
  /** Optimization info. */
  String OPTNOINDEX = "removing path with no index results";
  /** Optimization info. */
  String OPTBIND = "binding static variable %";
  /** Optimization info. */
  String OPTCHILD = "converting % to child steps";
}
