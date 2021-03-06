package net.moraleboost.flux.eval.expr.func;

import java.util.List;

import net.moraleboost.flux.eval.EvalContext;
import net.moraleboost.flux.eval.EvalException;

public class DoubleFunction extends BaseFunction
{
    public DoubleFunction()
    {
        super();
    }

    @Override
    public Double call(List<Object> arguments, EvalContext ctx)
            throws EvalException
    {
        if (arguments.size() == 1) {
            Object arg = arguments.get(0);
            if (arg == null) {
                return null;
            } else if (isNumber(arg)) {
                return convertToDouble((Number)arg);
            } else if (isString(arg)) {
                try {
                    return Double.parseDouble((String)arg);
                } catch (Exception e) {
                    throw new EvalException(e);
                }
            } else {
                throw new EvalException(
                        "Can't convert " + arg.getClass().getCanonicalName() +
                        " to a Double.");
            }
        } else {
            throw new EvalException("DOUBLE function must take 1 argument.");
        }
    }
}
