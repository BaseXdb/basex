package org.basex.query.up;

import static org.basex.query.up.primitives.PrimitiveType.*;

import java.util.*;

import org.basex.data.*;
import org.basex.query.up.primitives.*;
import org.basex.util.*;
/**
 * Comparator for {@link UpdatePrimitive}.
 *
 * In general, a list of updates is applied from the highest to the lowest PRE value in
 * BaseX. The higher the actual location of an update the sooner it is applied, hence
 * the 'greater' {@link UpdatePrimitive} is applied first. The order further relies on
 * the definition of {@link PrimitiveType}.
 *
 * {@link UpdatePrimitive}s are identified by their target node's PRE values (T).
 * Depending on the {@link PrimitiveType} of the update, a specific PRE value on the
 * table is affected (L). Hence it is not sufficient to order primitives based on T
 * (see case 2,3 below). It is also not sufficient to order them based on L (see case
 * 2,3 below).
 *
 * The first {@link UpdatePrimitive} is referred to as P1, the target node of P1 is
 * referred to as T1, the (optional) insertion sequence for P1 is S1, the actually
 * affected PRE value on disk is L1.
 * For the second P2, T2, S2, L2.
 *
 * The result of the comparison depends on several things:
 *
 * 1) The PRE values of T1 and T2.
 * Consider D the document <DOC><N1/><N2/><DOC>. If P1 is a {@link DeleteNode} on N1
 * and P2 is a {@link DeleteNode} on N2, it follows that T2>T1. P2 wins the comparison
 * and is therefore executed first.
 *
 *
 * 2) Whether the order of P1, P2 must be switched to get the desired result.
 * Consider D the document <DOC><N1><N2/><\/N1><DOC>. If P1 is an {@link InsertInto} on
 * N1 and P2 is an {@link InsertInto} on N2, it follows that T2>T1. Yet, executing P2
 * first would lead to the following problem:
 *
 * The actual affected PRE value location on disk for both updates is L1=L2=L=4. P2
 * inserts a sequence of nodes S2 at L. After this P1 inserts a sequence of nodes S1 at
 * the same location L and shifts S2 further back on disk. S1 and S2 are now ordered
 * incorrectly (S1,S2) and invalidate the document.
 *
 * Hence the correct order (S2,S1) can only be achieved if P1 is executed first and S1
 * subsequently shifted by inserting S2.
 *
 * The problem can exist if P1 and/or P2 are of the kind {@link InsertInto} or
 * {@link InsertAfter} and T1+size(T1) is equal T2+size(T2), hence T1 and T2 have the
 * same following node. The correct order is realized by executing the update first, that
 * is on the ancestor axis of the other. In this case P1>P2.
 *
 * Another case similar to this is if P1 is an {@link InsertIntoAsFirst} on an element
 * T1 and P2 is i.e. a {@link ReplaceNode} on an attribute T2 of T1. To get the desired
 * order of insertion sequences (S2,S1) P1 must be executed first, hence P1>P2.
 *
 *
 * 3) The {@link PrimitiveType} of P1, P2.
 * Consider D the document <DOC><N1/><DOC> with T=N1. P1 is an {@link InsertBefore}
 * on T and P2 is an {@link InsertIntoAsFirst} on the same target T. As they both operate
 * on the same target, both have not to be re-located because of their type (see case 2),
 * hence only differ in their {@link PrimitiveType}, it follows that P2>P1 as nodes
 * S2 are to be inserted on the following axis of S1.
 *
 * Another case: P2 a {@link DeleteNode} on T and P1 an {@link InsertBefore} on T. As
 * L2=L1, P2, the execution sequence must be (P2,P1). For (P1,P2) S1 would be deleted by
 * P2. The correct order is also determined by the order of {@link PrimitiveType}. Here
 * we see that ordering updates simply by the actually affected PRE value L is not
 * sufficient.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public class UpdatePrimitiveComparator implements Comparator<UpdatePrimitive> {

  @Override
  public int compare(final UpdatePrimitive a, final UpdatePrimitive b) {
    // data the same for both primitives
    final Data data = a.data;

    /* Step 1: Calculate actual locations correctly. Location is only changed for
     * - InsertInto, InsertAfter, as they must be executed before anything on the
     *   descendant axis of T
     * - InsertIntoAsFirst, as this must be executed before anything on the attribute
     *   axis of T
     */
    final boolean aIsInsertInto = a.type == INSERTINTO || a.type == INSERTAFTER;
    final boolean bIsInsertInto = b.type == INSERTINTO || b.type == INSERTAFTER;
    final boolean aIsInsertIntoAsFirst = a.type == INSERTINTOFIRST;
    final boolean bIsInsertIntoAsFirst = b.type == INSERTINTOFIRST;
    final int aPre = a.targetPre;
    final int bPre = b.targetPre;
    final int aKind = data.kind(aPre);
    final int bKind = data.kind(bPre);
    final int aSize = data.size(aPre, aKind);
    final int bSize = data.size(bPre, bKind);
    final int aActualLocation = aPre +
        (aIsInsertInto ? aSize : 0) +
        (aIsInsertIntoAsFirst ? data.attSize(aPre, aKind) : 0);
    final int bActualLocation = bPre +
        (bIsInsertInto ? bSize : 0) +
        (bIsInsertIntoAsFirst ? data.attSize(bPre, bKind) : 0);

    // Step 2: Compare Locations.
    if(aActualLocation > bActualLocation) return 1;
    if(bActualLocation > aActualLocation) return -1;

    /* Step 3: Subtree check. If one update A adds a node to the end of its subtree and
     * the other update takes place in this subtree of A, A wins. */
    if(aActualLocation > bPre && aPre < bPre) return 1;
    if(bActualLocation > aPre && bPre < aPre) return -1;

    /* Step 4: Compare target PRE values if the actual locations are equal and the
     * subtree check yields no decision. */
    if(aPre > bPre) return 1;
    if(bPre > aPre) return -1;

    // Step 5: Compare type of primitives for a final decision.
    if(a.type.ordinal() > b.type.ordinal()) return 1;
    if(b.type.ordinal() > a.type.ordinal()) return -1;


    // Two UpdatePrimitives cannot be equal!
    Util.notexpected("Ambiguous order of UpdatePrimitives: " + a + ", " + b);

    return 0;
  }
}
