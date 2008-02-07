package org.basex.query.xpath.locpath;

/**
 * Defined Axes in XPath.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public enum Axis {
  /** Anc.        */ ANC(StepAnc.class, "ancestor"),
  /** AnceOrSelf. */ ANCORSELF(StepAncOrSelf.class, "ancestor-or-self"),
  /** Attr.       */ ATTR(StepAttr.class, "attribute"),
  /** Child.      */ CHILD(StepChild.class, "child"),
  /** Desc.       */ DESC(StepDesc.class, "descendant"),
  /** DescOrSelf. */ DESCORSELF(StepDescOrSelf.class, "descendant-or-self"),
  /** Foll.       */ FOLL(StepFoll.class, "following"),
  /** FollSibl.   */ FOLLSIBL(StepFollSibl.class, "following-sibling"),
  /** NameSpace.  */ NAMESPACE(null, "namespace"),
  /** Parent.     */ PARENT(StepParent.class, "parent"),
  /** Prec.       */ PREC(StepPrec.class, "preceding"),
  /** PrecSibl.   */ PRECSIBL(StepPrecSibl.class, "preceding-sibling"),
  /** Step.       */ SELF(StepSelf.class, "self");

  /** Axis string. */
  private Class<? extends Step> step;

  /** Axis string. */
  protected final String name;

  /**
   * Constructor, initializing the enum constants.
   * @param s step class
   * @param n axis string
   */
  Axis(final Class<? extends Step> s, final String n) {
    step = s;
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
  public static Step get(final Axis a, final Test test) {
    try {
      final Step step = a.step.newInstance();
      step.axis = a;
      step.test = test;
      step.preds = new Preds();
      return step;
    } catch(final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Returns a location step instance.
   * @param a axis
   * @param test node test
   * @param preds predicates
   * @return step
   */
  public static Step get(final Axis a, final Test test, final Preds preds) {
    try {
      final Step step = a.step.newInstance();
      step.axis = a;
      step.test = test;
      step.preds = preds;
      return step;
    } catch(final Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
