package net.moraleboost.flux.lang;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;

import net.moraleboost.flux.eval.Expression;
import net.moraleboost.flux.eval.Statement;
import net.moraleboost.flux.eval.expr.IdLiteral;
import net.moraleboost.flux.eval.stmt.CommitStatement;
import net.moraleboost.flux.eval.stmt.DeleteStatement;
import net.moraleboost.flux.eval.stmt.InsertStatement;
import net.moraleboost.flux.eval.stmt.OptimizeStatement;
import net.moraleboost.flux.eval.stmt.RollbackStatement;
import net.moraleboost.flux.eval.stmt.SelectStatement;
import net.moraleboost.flux.eval.stmt.UseStatement;

public class StatementListBuilder
{
    private ExpressionBuilder exprBuilder;
    
    public StatementListBuilder()
    {
        exprBuilder = new ExpressionBuilder();
    }
    
    public List<Statement> build(Tree node)
    throws SyntaxException
    {
        List<Statement> ret = new ArrayList<Statement>();
        
        if (node == null) {
            return ret;
        }
        
        if (node.isNil()) {
            for (int i=0; i<node.getChildCount(); ++i) {
                ret.add(buildStatement(node));
            }
        } else {
            ret.add(buildStatement(node));
        }
        
        return ret;
    }
    
    private Statement buildStatement(Tree node)
    throws SyntaxException
    {
        switch (node.getType()) {

        case SolrqlParser.T_USE:
            return buildUseStatement(node);
            
        case SolrqlParser.T_SELECT:
            return buildSelectStatement(node);
            
        case SolrqlParser.T_INSERT:
            return buildInsertStatement(node);
            
        case SolrqlParser.T_DELETE:
            return buildDeleteStatement(node);
            
        case SolrqlParser.T_COMMIT:
            return buildCommitStatement(node);
            
        case SolrqlParser.T_ROLLBACK:
            return buildRollbackStatement(node);
            
        case SolrqlParser.T_OPTIMIZE:
            return buildOptimizeStatement(node);

        default:
            throw new SyntaxException("Unknown token type: " + node.getType());
            
        }
    }
    
    private UseStatement buildUseStatement(Tree node)
    throws SyntaxException
    {
        UseStatement stmt = new UseStatement();

        Tree child;
        int type;
        
        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_SERVER) {
                // url
                stmt.setUrl(Util.unescapeString(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_AS) {
                // ref
                stmt.setName(Util.unescapeId(child.getChild(0).getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private SelectStatement buildSelectStatement(Tree node)
    throws SyntaxException
    {
        SelectStatement stmt = new SelectStatement();

        Tree child;
        int type;
        
        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_FIELDS) {
                // result expressions
                List<SelectStatement.ResultField> resultFields = getResultFields(child);
                stmt.setResultFields(resultFields);
            } else if (type == SolrqlParser.T_FROM) {
                // source
                stmt.setSource(Util.unescapeId(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_WHERE) {
                // where expression
                Expression expr = exprBuilder.build(child.getChild(0));
                stmt.setCondition(expr);
            } else if (type == SolrqlParser.T_NWHERE) {
                // native query
                stmt.setNativeQuery(Util.unescapeString(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_ORDER) {
                // order by
                List<SelectStatement.Sort> orderings = getOrderings(child);
                stmt.setSortConditions(orderings);
            } else if (type == SolrqlParser.T_LIMIT) {
                // number of rows
                stmt.setLimit(Integer.parseInt(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_OFFSET) {
                // offset
                stmt.setOffset(Integer.parseInt(child.getChild(0).getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private List<SelectStatement.ResultField> getResultFields(Tree node)
    throws SyntaxException
    {
        List<SelectStatement.ResultField> ret =
            new ArrayList<SelectStatement.ResultField>();
        
        Tree child;
        int type;
        
        int autoAliasCount = 0;
        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_FIELD_ALIAS) {
                SelectStatement.ResultField rf = new SelectStatement.ResultField();
                rf.expression = exprBuilder.build(child.getChild(0));
                if (child.getChildCount() > 1) {
                    rf.alias = Util.unescapeId(child.getChild(1).getText());
                } else {
                    if (rf.expression instanceof IdLiteral) {
                        rf.alias = ((IdLiteral)(rf.expression)).getValue();
                    } else {
                        rf.alias = "__field_" + Integer.toString(autoAliasCount) + "__";
                        ++autoAliasCount;
                    }
                }
                ret.add(rf);
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return ret;
    }
    
    private List<SelectStatement.Sort> getOrderings(Tree node)
    throws SyntaxException
    {
        List<SelectStatement.Sort> ret =
            new ArrayList<SelectStatement.Sort>();
        
        Tree child;
        int type;
        
        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_ORDERING) {
                SelectStatement.Sort s = new SelectStatement.Sort();
                s.field = Util.unescapeId(child.getChild(0).getChild(0).getText());
                
                int direction = child.getChild(0).getType();
                if (direction == SolrqlParser.T_ASC) {
                    s.order = SelectStatement.SortOrder.Ascending;
                } else if (direction == SolrqlParser.T_DESC) {
                    s.order = SelectStatement.SortOrder.Descending;
                } else {
                    throw new SyntaxException("Unknonw direction: " + direction);
                }
                
                ret.add(s);
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return ret;
    }
    
    private InsertStatement buildInsertStatement(Tree node)
    throws SyntaxException
    {
        InsertStatement stmt = new InsertStatement();
        
        Tree child;
        int type;
        
        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_INTO) {
                // destination
                stmt.setDestination(Util.unescapeId(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_FIELDS) {
                // field list
                List<String> fields = getFieldList(child);
                stmt.setFields(fields);
            } else if (type == SolrqlParser.T_VALUES) {
                // value list
                List<Expression> values = getValueList(child);
                stmt.setValues(values);
            } else if (type == SolrqlParser.T_SELECT) {
                // select statement
                SelectStatement selectStmt = buildSelectStatement(child);
                stmt.setSelectStatement(selectStmt);
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private List<String> getFieldList(Tree node)
    throws SyntaxException
    {
        List<String> ret = new ArrayList<String>();
        
        for (int i=0; i<node.getChildCount(); ++i) {
            ret.add(Util.unescapeId(node.getChild(i).getText()));
        }
        
        return ret;
    }
    
    private List<Expression> getValueList(Tree node)
    throws SyntaxException
    {
        List<Expression> ret = new ArrayList<Expression>();
        
        for (int i=0; i<node.getChildCount(); ++i) {
            ret.add(exprBuilder.build(node.getChild(i)));
        }
        
        return ret;
    }
    
    private DeleteStatement buildDeleteStatement(Tree node)
    throws SyntaxException
    {
        DeleteStatement stmt = new DeleteStatement();

        Tree child;
        int type;

        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.T_FROM) {
                // source
                stmt.setSource(Util.unescapeId(child.getChild(0).getText()));
            } else if (type == SolrqlParser.T_WHERE) {
                // where expression
                Expression expr = exprBuilder.build(child.getChild(0));
                stmt.setCondition(expr);
            } else if (type == SolrqlParser.T_NWHERE) {
                // native query
                stmt.setNativeQuery(Util.unescapeId(child.getChild(0).getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private CommitStatement buildCommitStatement(Tree node)
    throws SyntaxException
    {
        CommitStatement stmt = new CommitStatement();
        
        Tree child;
        int type;

        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.ID) {
                stmt.setDestination(Util.unescapeId(child.getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private RollbackStatement buildRollbackStatement(Tree node)
    throws SyntaxException
    {
        RollbackStatement stmt = new RollbackStatement();

        Tree child;
        int type;

        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.ID) {
                stmt.setDestination(Util.unescapeId(child.getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
    
    private OptimizeStatement buildOptimizeStatement(Tree node)
    throws SyntaxException
    {
        OptimizeStatement stmt = new OptimizeStatement();

        Tree child;
        int type;

        for (int i=0; i<node.getChildCount(); ++i) {
            child = node.getChild(i);
            type = child.getType();
            
            if (type == SolrqlParser.ID) {
                stmt.setDestination(Util.unescapeId(child.getText()));
            } else {
                throw new SyntaxException("Unknown token type: " + type);
            }
        }
        
        return stmt;
    }
}
