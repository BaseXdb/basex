package org.basex.query.xpath.path;

/**
 * XPath Axes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public enum Axis {
  /** Ancestor.   */ ANC("ancestor"),
  /** AncOrSelf.  */ ANCORSELF("ancestor-or-self"),
  /** Attribute.  */ ATTR("attribute"),
  /** Child.      */ CHILD("child"),
  /** Descendant. */ DESC("descendant"),
  /** DescOrSelf. */ DESCORSELF("descendant-or-self"),
  /** Following.  */ FOLL("following"),
  /** FollSibl.   */ FOLLSIBL("following-sibling"),
  /** Parent.     */ PARENT("parent"),
  /** Preceding.  */ PREC("preceding"),
  /** PrecSibl.   */ PRECSIBL("preceding-sibling"),
  /** Self.       */ SELF("self");
  // NameSpace.  NAMESPACE(null, "namespace"),

  /** Axis string. */
  final String name;

  /**
   * Constructor.
   * @param n axis string
   */
  Axis(final String n) {
    name = n;
  }

  /**
   * Returns the specified command.
   * @param c command to be found
   * @return command
   */
  public static Axis find(final String c) {
    for(final Axis a : values()) if(a.name.equals(c)) return a;
    return null;
  }

  /**
   * Returns a location step instance.
   * @param a axis
   * @param test node test
   * @return step
   */
  public static Step create(final Axis a, final Test test) {
    return create(a, test, new Preds());
  }

  /**
   * Returns a location step instance.
   * @param a axis
   * @param test node test
   * @param preds predicates
   * @return step
   */
  public static Step create(final Axis a, final Test test, final Preds preds) {
    try {
      Step step = null;
      switch(a) {
        case ANC: step = new StepAnc(); break;
        case ANCORSELF: step = new StepAncOrSelf(); break;
        case ATTR: step = new StepAttr(); break;
        case CHILD: step = new StepChild(); break;
        case DESC: step = new StepDesc(); break;
        case DESCORSELF: step = new StepDescOrSelf(); break;
        case FOLL: step = new StepFoll(); break;
        case FOLLSIBL: step = new StepFollSibl(); break;
        case PARENT: step = new StepParent(); break;
        case PREC: step = new StepPrec(); break;
        case PRECSIBL: step = new StepPrecSibl(); break;
        case SELF: step = new StepSelf(); break;
      }
      step.axis = a;
      step.test = test;
      step.preds = preds;
      return step;
    } catch(final Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Inverts the axis.
   * @return inverted axis
   */
  Axis invert() {
    switch(this) {
      case ANC:        return Axis.DESC;
      case ANCORSELF:  return Axis.DESCORSELF;
      case ATTR:
      case CHILD:      return Axis.PARENT;
      case DESC:       return Axis.ANC;
      case DESCORSELF: return Axis.ANCORSELF;
      case FOLLSIBL:   return Axis.PRECSIBL;
      case FOLL:       return Axis.PREC;
      case PARENT:     return Axis.CHILD;
      case PRECSIBL:   return Axis.FOLLSIBL;
      case PREC:       return Axis.FOLL;
      case SELF:       return Axis.SELF;
      default:         return null;
    }
  }
}
