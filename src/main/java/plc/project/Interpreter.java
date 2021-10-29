package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        for (Ast.Global global : ast.getGlobals()) {
            visit(global);
        }
        for (Ast.Function function : ast.getFunctions()) {
            visit(function);
        }
        Environment.Function result = scope.lookupFunction("main", 0);
        List<Environment.PlcObject> l = new ArrayList<>();
        return result.invoke(l);


        //throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Global ast) {
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(), ast.getMutable(), visit(ast.getValue().get()));
        } else {
            scope.defineVariable(ast.getName(), ast.getMutable(), Environment.NIL);
        }
        return Environment.NIL;
        //throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Function ast) {
        Scope temp = scope;
        scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {
            Scope before = temp;
            scope = new Scope(temp);
            int index = 0;
            for (String s : ast.getParameters()) {
                // how to determine whether parameter is mutable?
                scope.defineVariable(s, false, args.get(index));
                index++;
            }
            try {
                ast.getStatements().forEach(this::visit);
            } catch (Return returnExcept) {
                return returnExcept.value;
            }
            finally {
                scope = before;
            }
            return Environment.NIL;
        });
        return Environment.NIL;
        //throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        return Environment.NIL;
        //throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Declaration ast) {
        // Provided in lecture
        if (ast.getValue().isPresent()) {
            scope.defineVariable(ast.getName(), true, visit(ast.getValue().get()));
        } else {
            scope.defineVariable(ast.getName(), true, Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Assignment ast) {
        if (ast.getReceiver() instanceof Ast.Expression.Access) {
            if (!scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getMutable()) {
                throw new RuntimeException("Not mutable");
            } else {
                // if variable is a list, i.e. if has offset?
                if (((Ast.Expression.Access) ast.getReceiver()).getOffset().isPresent()) {
                    System.out.println("yes list");
                    //(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).setValue()
                  //  scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).setValue(visit(ast.getValue()));
                   // scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue()

                } else {
                   // visit(((Ast.Expression.Access)ast.getReceiver()).getName()).setValue(ast.getValue());
                    scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).setValue(visit(ast.getValue()));

                }
                System.out.println("Print");

                System.out.println(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()));
                System.out.println (scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue());// {
                System.out.println(((Ast.Expression.Access) ast.getReceiver()).getOffset().get());
                System.out.println(ast.getValue());
                System.out.println(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue());
                System.out.println("receiver");
                System.out.println((Ast.Expression.Access) ast.getReceiver());
                System.out.println("SCOPE");
                System.out.println(scope);

               // List<?> list = new ArrayList<>();
                System.out.println("Class");
                System.out.println((scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue()).getClass());
                System.out.println((scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue()).getClass());
                System.out.println((scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue()) instanceof List);

                //(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue()).get(0);



                System.out.println((Arrays.asList(scope.lookupVariable(((Ast.Expression.Access) ast.getReceiver()).getName()).getValue().getValue())).get(0));

                System.out.println(ast);


                //}



                // otherwise
            }

        }
        return Environment.NIL;
        //throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.If ast) {
        if (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Statement statement : ast.getThenStatements()) {
                    visit(statement);
                }
            }finally {
                scope = scope.getParent();
            }
        } else {
            try {
                scope = new Scope(scope);
                for (Ast.Statement statement : ast.getElseStatements()) {
                    visit(statement);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;

       // throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Switch ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Case ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.While ast) {
        // Provided in lecture
        while (requireType(Boolean.class, visit(ast.getCondition()))) {
            try {
                scope = new Scope(scope);
                for (Ast.Statement stmt : ast.getStatements()) {
                    visit(stmt);
                }
            } finally {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Statement.Return ast) {
        throw new Return(visit(ast.getValue()));
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Literal ast) {
        if (ast.getLiteral() != null)
            return Environment.create(ast.getLiteral());
        else
            return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Group ast) {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Binary ast) {
        String op = ast.getOperator();

        if (op.equals("&&")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());

            if (requireType(Boolean.class, left) && left.getValue().equals(true)) {
                if (requireType(Boolean.class, right) && right.getValue().equals(true)) {
                    return Environment.create(true);
                } else {
                    return Environment.create(false);
                }
            } else {
                return Environment.create(false);
            }
        }

        else if (op.equals("||")) {
            Environment.PlcObject left = visit(ast.getLeft());

            if (requireType(Boolean.class, left) && left.getValue().equals(true)) {
                return Environment.create(true);
            } else {
                if (requireType(Boolean.class, visit(ast.getRight())) && visit(ast.getRight()).getValue().equals(true)) {
                    return Environment.create(true);
                } else {
                    return Environment.create(false);
                }
            }
        }

        else if (op.equals("<")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());
            return Environment.create(requireType(Comparable.class, left).compareTo(requireType(left.getValue().getClass(), right)) < 0);
        }
        else if (op.equals(">")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());
            return Environment.create(requireType(Comparable.class, left).compareTo(requireType(left.getValue().getClass(), right)) > 0);
        }
        else if (op.equals("==")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());
            return Environment.create(left.equals(right));
        }
        else if (op.equals("!=")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());
            return Environment.create(!left.equals(right));
        }
        else if (op.equals("+")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());

            if (left.getValue() instanceof String || right.getValue() instanceof String) {
                return Environment.create(left.getValue().toString() + right.getValue().toString());
            }
            else if (left.getValue() instanceof BigInteger) {
                if (right.getValue() instanceof BigInteger) {
                    return Environment.create(((BigInteger) left.getValue()).add(requireType(BigInteger.class, right)));
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
            else if (left.getValue() instanceof BigDecimal) {
                if (right.getValue() instanceof BigDecimal) {
                    return Environment.create(((BigDecimal) left.getValue()).add(requireType(BigDecimal.class, right)));
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
        }

        else if (op.equals("/")) {
            Environment.PlcObject left = visit(ast.getLeft());
            Environment.PlcObject right = visit(ast.getRight());

            if (right.getValue().equals(0)){
                throw new RuntimeException("The denominator is zero");
            }

            if (left.getValue() instanceof BigInteger) {
                if (right.getValue() instanceof BigInteger) {
                    return Environment.create(((BigInteger) left.getValue()).divide(requireType(BigInteger.class, right)));
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
            else if (left.getValue() instanceof BigDecimal) {
                if (right.getValue() instanceof BigDecimal) {
                    BigDecimal result = ((BigDecimal) left.getValue()).divide((BigDecimal) right.getValue(), RoundingMode.HALF_EVEN);
                    return Environment.create(result);
                }
                else{
                    throw new RuntimeException("RHS is not the same type as LHS");
                }
            }
        }


        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expression.Access ast) {
        if (ast.getOffset().isPresent()) {
            if (!(Environment.create(visit(ast.getOffset().get()).getValue()).getValue() instanceof BigInteger)) {
                throw new RuntimeException("Offset not big integer");
            }
            List value = (List)(scope.lookupVariable(ast.getName()).getValue()).getValue();

            Object x = Environment.create(visit(ast.getOffset().get()).getValue()).getValue();
            BigInteger y = (BigInteger) x;

            return Environment.create(value.get(y.intValue()));

            //System.out.println(value.get(y.intValue()));
/**
            BigInteger index = BigInteger.ZERO;
            for (Object i : value) {
                if (index == x) {
                   // System.out.println(Environment.create(i));
                    return Environment.create(i);
                }
                index = index.add(BigInteger.ONE);
            }*/
        }
        return scope.lookupVariable(ast.getName()).getValue();
    }


    @Override
    public Environment.PlcObject visit(Ast.Expression.Function ast) {
        List<Environment.PlcObject> result = new ArrayList<>();

        for (int i = 0; i < ast.getArguments().size(); i++) {
            result.add(visit(ast.getArguments().get(i)));
        }
        return scope.lookupFunction(ast.getName(),ast.getArguments().size()).invoke(result);
    }

    
    @Override
    public Environment.PlcObject visit(Ast.Expression.PlcList ast) {

        ArrayList<Object> result = new ArrayList<>();

        for (Ast.Expression elem : ast.getValues()) {
            result.add(visit(elem).getValue());
        }

        return Environment.create(result);

    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
