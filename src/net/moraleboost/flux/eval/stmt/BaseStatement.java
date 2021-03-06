package net.moraleboost.flux.eval.stmt;

import org.apache.solr.client.solrj.SolrServer;

import net.moraleboost.flux.eval.EvalContext;
import net.moraleboost.flux.eval.EvalException;
import net.moraleboost.flux.eval.Statement;

public abstract class BaseStatement implements Statement
{
    public BaseStatement()
    {
    }
    
    // never returns null
    public SolrServer getSolrServer(String name, EvalContext ctx)
    throws EvalException
    {
        if (name == null) {
            SolrServer server =  ctx.getDefaultServer();
            if (server == null) {
                throw new EvalException("Default connection is not specified.");
            }
            return server;
        } else {
            Object obj = ctx.get(name);
            
            if (obj != null && obj instanceof SolrServer) {
                return (SolrServer)obj;
            } else {
                throw new EvalException("No such connection.");
            }
        }
    }
}
