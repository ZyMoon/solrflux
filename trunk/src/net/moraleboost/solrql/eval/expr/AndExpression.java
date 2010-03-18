package net.moraleboost.solrql.eval.expr;

import net.moraleboost.solrql.eval.EvalContext;
import net.moraleboost.solrql.eval.EvalException;

public class AndExpression extends BinaryOperatorExpression
{
    public AndExpression()
    {
        super();
    }

    @Override
    protected Boolean evaluateInner(Object lho, Object rho, EvalContext ctx)
    {
        return (convertToBoolean(lho) && convertToBoolean(rho));
    }

    public String toSolrQuery(EvalContext ctx) throws EvalException
    {
        return ("(" + getLeftHandOperand().toSolrQuery(ctx) +
                ") AND (" + getRightHandOperand().toSolrQuery(ctx) + ")");
    }
}
