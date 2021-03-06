package net.moraleboost.flux.eval.stmt;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

import net.moraleboost.flux.eval.EvalContext;
import net.moraleboost.flux.eval.EvalException;

public class RollbackStatement extends BaseStatement
{
    private String destination;

    public RollbackStatement()
    {
        super();
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }
    
    public String getDestination()
    {
        return destination;
    }

    public Object execute(EvalContext ctx) throws EvalException
    {
        SolrServer server = getSolrServer(destination, ctx);
        try {
            return server.rollback();
        } catch (IOException e) {
            throw new EvalException(e);
        } catch (SolrServerException e) {
            throw new EvalException(e);
        }
    }
}
