package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
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

        try {
            Environment.Function func = scope.lookupFunction("main", 0);
            requireAssignable(Environment.Type.INTEGER, func.getReturnType());
        } catch (RuntimeException except) {
            throw new RuntimeException("Main function incorrect");
        }

        for (Ast.Global global: ast.getGlobals()) {
            visit(global);
        }
        for (Ast.Function function: ast.getFunctions()) {
            visit(function);
        }

        return null;
        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Global ast) {
        if (ast.getValue().isPresent()) {
            visit(ast.getValue().get());

            // TODO Check if value is of subtype of global's type
            if (ast.getVariable().getType() == Environment.Type.ANY) {
            } else if (Environment.create(ast.getValue().get()).getType() == ast.getVariable().getType()){
            } else if (ast.getVariable().getType() == Environment.Type.COMPARABLE) {
                Environment.PlcObject temp = Environment.create(ast.getValue().get());
                if (!((temp.getType() == Environment.Type.INTEGER) || (temp.getType() == Environment.Type.DECIMAL) || (temp.getType() == Environment.Type.CHARACTER) ||(temp.getType() == Environment.Type.STRING))) {
                    throw new RuntimeException("variable mismatch");
                }
            } else {
                //if (Environment.create(ast.getValue().get()).getType() != ast.getVariable().getType()) {
                    throw new RuntimeException("variable mismatch");
                //}
            }
        }

       // scope.defineVariable(ast.getName(), ast.getMutable(), Environment.NIL);
        Environment.Variable var = scope.defineVariable(ast.getName(), ast.getName(),Environment.getType(ast.getTypeName()), ast.getMutable(), Environment.NIL);
       // ast.setVariable(new Environment.Variable(ast.getName(), ast.getMutable(), Environment.NIL));
        ast.setVariable(var);
        return null;

        //throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Function ast) {

        List<Environment.Type> parameterTypes = new ArrayList<>();
        for (int i = 0; i < ast.getParameterTypeNames().size(); ++i) {
            parameterTypes.add(Environment.getType(ast.getParameterTypeNames().get(i)));
        }
        Environment.Type returnType = ast.getReturnTypeName().isPresent() ? Environment.getType(ast.getReturnTypeName().get()) : Environment.Type.NIL;
        Environment.Function func = scope.defineFunction(ast.getName(), ast.getName(), parameterTypes, returnType, args-> Environment.NIL);
        ast.setFunction(func);

        // need to save parent scope in variable to restore after? or no
        try {
            Scope newScope = new Scope(scope);
            ast.getStatements().forEach(this::visit);
            //scope = newScope.getParent();
        } finally {
            scope = getScope();
            return null;
        }

        //return null;

        //throw new UnsupportedOperationException();  // TODO
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
