package net.moraleboost.flux.eval.expr.func;

import java.util.Date;
import java.util.List;

import org.apache.solr.common.util.DateUtil;

import net.moraleboost.flux.eval.EvalContext;
import net.moraleboost.flux.eval.EvalException;

public class StringFunction extends BaseFunction
{
    public StringFunction()
    {
        super();
    }

    @Override
    public String call(List<Object> arguments, EvalContext ctx)
            throws EvalException
    {
        if (arguments.size() == 1) {
            Object arg = arguments.get(0);
            if (arg == null) {
                return null;
            } else if (isDate(arg)) {
                return DateUtil.getThreadLocalDateFormat().format((Date)arg);
            } else {
                return arguments.get(0).toString();
            }
        } else {
            throw new EvalException("STRING function must take 1 argument.");
        }
    }
}
