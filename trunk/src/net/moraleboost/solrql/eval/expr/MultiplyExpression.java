package net.moraleboost.solrql.eval.expr;

import net.moraleboost.solrql.eval.EvalContext;
import net.moraleboost.solrql.eval.EvalException;
import net.moraleboost.solrql.eval.expr.arith.Multiply;

public class MultiplyExpression extends BinaryOperatorExpression
{
    public MultiplyExpression()
    {
        super();
    }

    @Override
    protected Object evaluateInner(Object lho, Object rho, EvalContext ctx)
    throws EvalException
    {
        if (isNumber(lho) && isNumber(rho)) {
            // number
            return evaluateArithmeticCalculation(
                    (Number)lho, (Number)rho, ctx, Multiply.INSTANCE);
        }
        
        throw new EvalException("Operands have incompatible types.");
    }

    public String toSolrQuery(EvalContext ctx) throws EvalException
    {
        throw new EvalException("Operator \"*\" is not supported in WHERE clauses.");
    }
}
