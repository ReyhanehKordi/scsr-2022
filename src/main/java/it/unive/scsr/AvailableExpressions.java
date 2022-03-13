package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.Variable;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.ExpressionVisitor;
import it.unive.lisa.symbolic.heap.AccessChild;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.symbolic.heap.HeapDereference;
import it.unive.lisa.symbolic.heap.HeapReference;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;

public class AvailableExpressions
		implements DataflowElement<DefiniteForwardDataflowDomain<AvailableExpressions>, AvailableExpressions> {

	private final ValueExpression expression;
	private final ProgramPoint point;

	public AvailableExpressions() {
		this.expression = null;
		this.point = null;
	}

	public AvailableExpressions(ValueExpression expression) {
		this.expression = expression;
		this.point = null;
	}

	public AvailableExpressions(ValueExpression expression, ProgramPoint pp) {
		this.expression = expression;
		this.point = pp;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		if (this.expression != null) {
			try {
				this.expression.accept(new ExpressionVisitor<Collection<Identifier>>() {

					@Override
					public Collection<Identifier> visit(AccessChild expression, Collection<Identifier> receiver,
							Collection<Identifier> child, Object... params) throws SemanticException {

						return result;

					}

					@Override
					public Collection<Identifier> visit(HeapAllocation expression, Object... params)
							throws SemanticException {

						return result;

					}

					@Override
					public Collection<Identifier> visit(HeapReference expression, Collection<Identifier> arg,
							Object... params) throws SemanticException {

						return result;

					}

					@Override
					public Collection<Identifier> visit(HeapDereference expression, Collection<Identifier> arg,
							Object... params) throws SemanticException {

						return result;

					}

					@Override
					public Collection<Identifier> visit(UnaryExpression expression, Collection<Identifier> arg,
							Object... params) throws SemanticException {

						// Check inner expression
						if (expression.getExpression().getClass() == Identifier.class || expression.getExpression().getClass() == Variable.class) {
							result.add((Identifier) expression.getExpression());
						}

						return result;

					}

					@Override
					public Collection<Identifier> visit(BinaryExpression expression, Collection<Identifier> left,
							Collection<Identifier> right, Object... params) throws SemanticException {

						// Check left hand side operand
						if (expression.getLeft().getClass() == Identifier.class || expression.getLeft().getClass() == Variable.class) {
							result.add((Identifier) expression.getLeft());
						}

						// Check right hand side operand
						if (expression.getRight().getClass() == Identifier.class || expression.getRight().getClass() == Variable.class) {
							result.add((Identifier) expression.getRight());
						}

						return result;
					}

					@Override
					public Collection<Identifier> visit(TernaryExpression expression, Collection<Identifier> left,
							Collection<Identifier> middle, Collection<Identifier> right, Object... params)
							throws SemanticException {

						// Check left hand side operand
						if (expression.getLeft().getClass() == Identifier.class || expression.getLeft().getClass() == Variable.class) {
							result.add((Identifier) expression.getLeft());
						}

						// Check right hand side operand
						if (expression.getRight().getClass() == Identifier.class || expression.getRight().getClass() == Variable.class) {
							result.add((Identifier) expression.getRight());
						}

						// Check middle operand
						if (expression.getMiddle().getClass() == Identifier.class || expression.getMiddle().getClass() == Variable.class) {
							result.add((Identifier) expression.getMiddle());
						}
						return result;
					}

					@Override
					public Collection<Identifier> visit(Skip expression, Object... params) throws SemanticException {
						return result;
					}

					@Override
					public Collection<Identifier> visit(PushAny expression, Object... params) throws SemanticException {
						return result;
					}

					@Override
					public Collection<Identifier> visit(Constant expression, Object... params)
							throws SemanticException {
						return result;
					}

					@Override
					public Collection<Identifier> visit(Identifier expression, Object... params)
							throws SemanticException {
						// Just identifier, add it
						result.add(expression);
						return result;
					}

				}, result);
			} catch (SemanticException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return Objects.hash(expression);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		AvailableExpressions other = (AvailableExpressions) obj;

		return Objects.equals(expression, other.expression) && Objects.equals(point, other.point);
	}


	@Override
	public Collection<AvailableExpressions> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		Set<AvailableExpressions> result = new HashSet<>();

		if (expression.getClass() != Identifier.class && expression.getClass() != Constant.class
				&& expression.getClass() != Variable.class) {
			AvailableExpressions ae = new AvailableExpressions(expression, pp);
			result.add(ae);
		}
		// if (ae.getInvolvedIdentifiers().size() > 0) {
		// result.add(ae);
		// }

		return result;
	}

	@Override
	public Collection<AvailableExpressions> gen(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		Set<AvailableExpressions> result = new HashSet<>();

		AvailableExpressions ae = new AvailableExpressions(expression, pp);

		if (ae.getInvolvedIdentifiers().size() > 0 && (expression.getClass() == BinaryExpression.class
				|| expression.getClass() == TernaryExpression.class)) {
			result.add(ae);
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		Set<AvailableExpressions> result = new HashSet<>();

		for (AvailableExpressions ae : domain.getDataflowElements()) {
			if (ae.getInvolvedIdentifiers().contains(id)) {
				result.add(ae);
			}
		}

		return result;
	}

	@Override
	public Collection<AvailableExpressions> kill(ValueExpression expression, ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailableExpressions> domain) throws SemanticException {

		return new HashSet<>();
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailableExpressions pushScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.pushScope(scope));
	}

	@Override
	public AvailableExpressions popScope(ScopeToken scope) throws SemanticException {
		return new AvailableExpressions((ValueExpression) expression.popScope(scope));
	}
}
