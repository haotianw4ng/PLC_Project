package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Function function;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Global ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {

        if (ast.getLiteral() instanceof Void)
            ast.setType(new Environment.Type("Nil", "Void", scope));
        else if (ast.getLiteral() instanceof Boolean)
            ast.setType(new Environment.Type("Boolean", "boolean", scope));
        else if (ast.getLiteral() instanceof Character)
            ast.setType(new Environment.Type("Character", "char", scope));
        else if (ast.getLiteral() instanceof String) {
            ast.setType(new Environment.Type("String", "String", scope));
        } else if (ast.getLiteral() instanceof BigInteger) {
            if (((BigInteger)ast.getLiteral()).bitCount() > 32) {
                throw new RuntimeException("too many bits!!!!!!");
            }
                ast.setType(new Environment.Type("Integer", "int", scope));
        } else if (ast.getLiteral() instanceof BigDecimal) {
            if (((BigDecimal)ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY ||  ((BigDecimal)ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY) {
                throw new RuntimeException("decimal too big :(");
            }
            ast.setType(new Environment.Type("Boolean", "boolean", scope));
        }
        return null;
        /**
        Environment.Type litType = ast.getType();
        if (litType == Environment.Type.NIL || litType == Environment.Type.BOOLEAN || litType == Environment.Type.CHARACTER || litType == Environment.Type.STRING) {
            return null;
        } else if (litType == Environment.Type.INTEGER) {

            if (((BigInteger)ast.getLiteral()).bitCount() > 32) {
                throw new RuntimeException("too many bits!!!!!!");
            }
        } else if (litType == Environment.Type.DECIMAL) {
            if (((BigDecimal)ast.getLiteral()).doubleValue() == Double.NEGATIVE_INFINITY ||  ((BigDecimal)ast.getLiteral()).doubleValue() == Double.POSITIVE_INFINITY) {
                throw new RuntimeException("decimal too big :(");
            }
        }
        return null;
*/
        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type) {
        throw new UnsupportedOperationException();  // TODO
    }

}
