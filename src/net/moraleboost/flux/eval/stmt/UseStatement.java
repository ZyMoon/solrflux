package net.moraleboost.flux.eval.stmt;

import java.net.MalformedURLException;

import net.moraleboost.flux.eval.EvalContext;
import net.moraleboost.flux.eval.EvalException;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

public class UseStatement extends BaseStatement
{
    private String url;
    private String name;
    
    public UseStatement()
    {
        super();
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }

    public Object execute(EvalContext ctx) throws EvalException
    {
        try {
            if (url == null) {
                throw new EvalException("Url is null.");
            }
            
            CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);
            if (name == null) {
                ctx.setDefaultServer(server);
            } else {
                ctx.putGlobal(name, server);
            }
        } catch (MalformedURLException e) {
            throw new EvalException("Invalid Solr URL: " + url, e);
        }
        
        return null;
    }
}
