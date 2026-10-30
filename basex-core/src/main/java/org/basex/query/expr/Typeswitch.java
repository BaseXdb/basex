package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Typeswitch expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Typeswitch extends ASwitch<TypeswitchGroup> {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param cond condition
   * @param groups case groups (last one is default)
   */
  public Typeswitch(final InputInfo info, final Expr cond, final TypeswitchGroup[] groups) {
    super(info, cond, groups);
  }

  @Override
  Expr branch(final TypeswitchGroup group) {
    return group.expr;
  }

  @Override
  void branch(final TypeswitchGroup group, final Expr expr) {
    group.expr = expr;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoUp(cond);
    checkAllUp(branches());
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // typeswitch(1) case xs:integer return A default return B → A
    if(cond instanceof final Value value) {
      for(final TypeswitchGroup group : groups) {
        if(group.match(value, null)) {
          group.inline(value, cc);
          return cc.replaceWith(this, group.expr);
        }
      }
    }

    // remove checks that will never match
    final SeqType ct = cond.seqType();
    final ArrayList<SeqType> types = new ArrayList<>();
    final ArrayList<TypeswitchGroup> newGroups = new ArrayList<>(groups.length);
    for(final TypeswitchGroup group : groups) {
      if(group.removeTypes(ct, types, cc)) newGroups.add(group);
    }
    groups = newGroups.toArray(TypeswitchGroup[]::new);

    // determine common type
    exprType.assign(SeqType.union(groups, true)).data(groups);

    // check if always the same branch will be evaluated
    Expr expr = this;
    TypeswitchGroup tg = null;
    final int gl = groups.length;
    for(int g = 0; g < gl - 1; g++) {
      final ArrayList<SeqType> matching = groups[g].matchingTypes(ct);
      if(!matching.isEmpty()) {
        // make sure that preceding groups do not branch on subtypes
        boolean branch = false;
        for(int h = 0; !branch && h < g; h++) {
          branch = Checks.any(groups[h].seqTypes, st -> Checks.any(matching, st::instanceOf));
        }
        // typeswitch($i) case xs:integer return A default return B → A
        if(!branch) tg = groups[g];
        break;
      }
    }
    // choose default branch if none of the branches will be chosen
    if(tg == null) {
      boolean opt = true;
      for(int g = 0; opt && g < gl - 1; g++) opt = groups[g].noMatches(ct);
      // typeswitch($i) case xs:string return A default return B → B
      if(opt) tg = groups[gl - 1];
    }
    // choose first branch if all return expressions are equal
    if(tg == null) {
      boolean opt = true;
      for(int g = 1; opt && g < gl; g++) opt = groups[0].expr.equals(groups[g].expr);
      // typeswitch(E) case xs:string return A default return A → void(E), A
      if(opt) tg = groups[0];
    }

    if(tg != null) {
      // rewrite chosen branch
      expr = tg.rewrite(cond, cc);
    } else if(gl < 3 && groups[0].seqTypes.length == 1 && !cond.has(Flag.NDT)) {
      // otherwise, rewrite to 'if' expression if one or two branches are left
      // typeswitch(E) case xs:string return A default return B →
      // if(E instance of xs:string) then A else B
      final Expr iff = new Instance(info, cond, groups[0].seqTypes[0]).optimize(cc);
      final Expr thn = groups[0].rewrite(cond, cc), els = groups[1].rewrite(cond, cc);
      expr = new If(info, iff, thn, els).optimize(cc);
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return group(qc).iter(qc);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return group(qc).value(qc);
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    return group(qc).ebv(qc, info);
  }

  @Override
  public boolean eager() {
    return Checks.all(groups, TypeswitchGroup::eager);
  }

  /**
   * Returns the typeswitch group with a matching expression.
   * @param qc query context
   * @return group
   * @throws QueryException query exception
   */
  private TypeswitchGroup group(final QueryContext qc) throws QueryException {
    final Value value = cond.value(qc);
    for(final TypeswitchGroup group : groups) {
      if(group.match(value, qc)) return group;
    }
    throw Util.notExpected();
  }

  @Override
  public VarUsage count(final Var var) {
    return cond.count(var).plus(VarUsage.maximum(var, groups));
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Typeswitch(info, cond.copy(cc, vm), Arr.copyAll(cc, vm, groups)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Typeswitch && super.equals(obj);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.open().token(TYPESWITCH).paren(cond).tokens(groups).close();
  }
}
