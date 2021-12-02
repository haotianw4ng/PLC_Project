package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Function ast) {
        print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getName(), "(");

        for (int i = 0; i < ast.getParameters().size(); i++) {
            if (i != ast.getParameters().size() - 1) {
                print(ast.getParameterTypeNames().get(i), " ", ast.getParameters().get(i));
                print(", ");
            }
            else{
                print(ast.getParameterTypeNames().get(i), " ", ast.getParameters().get(i));
            }
        }
        print(") {");

        indent++;
        for (Ast.Statement statement : ast.getStatements()) {
            newline(indent);
            print(statement);
        }

        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        print(ast.getExpression(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());

        if (ast.getValue().isPresent()) {
            print(" = ");
            print(ast.getValue().get());
        }

        print(";");
        return null;
    }


    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        print(ast.getReceiver(), " = ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        print("if (", ast.getCondition(), ") {");

        indent++;
        for (Ast.Statement statement : ast.getThenStatements()) {
            newline(indent);
            print(statement);
        }

        if (!ast.getElseStatements().isEmpty()) {
            indent--;
            newline(indent);
            print("} else {");

            indent++;
            for (Ast.Statement statement : ast.getElseStatements()) {
                newline(indent);
                print(statement);
            }
        }

        indent--;
        newline(indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        if(ast.getStatements().isEmpty()){
            print("while", " (", ast.getCondition(), ") ", "{", "}");
        } else{
            print("while", " (", ast.getCondition(), ") ", "{");
            indent++;
            for (Ast.Statement statement : ast.getStatements()) {
                newline(indent);
                print(statement);
            }

            indent--;
            newline(indent);
            print("}");
        }
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        print("return ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if (ast.getType() == Environment.Type.CHARACTER || ast.getType() == Environment.Type.STRING) {
            print("\"", ast.getLiteral(), "\"");
        } else if (ast.getType() == Environment.Type.INTEGER) {
            print(new BigInteger(ast.getLiteral().toString()));
        } else if (ast.getType() == Environment.Type.DECIMAL) {
            print(new BigDecimal(ast.getLiteral().toString()));
        } else {
            print(ast.getLiteral());
        }

        return null;
    }


    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(", ast.getExpression(), ")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        print(ast.getLeft(), " ", ast.getOperator(), " ", ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Access ast) {
        print(ast.getVariable().getJvmName());

        // TODO: List case
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        print(ast.getFunction().getJvmName(), "(");

        for (int i = 0; i < ast.getArguments().size() ; i++) {
            if (i != ast.getArguments().size() - 1) {
                print(ast.getArguments().get(i), ", ");
            }
            else{
                print(ast.getArguments().get(i));
            }
        }

        print(")");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        return null;
    }

}
