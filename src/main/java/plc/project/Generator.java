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
        return null;    }

    @Override
    public Void visit(Ast.Global ast) {
        return null;    }

    @Override
    public Void visit(Ast.Function ast) {
        return null;    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        print(ast.getExpression(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getVariable().getType().getJvmName(), " ", ast.getVariable().getJvmName());

        if(ast.getValue().isPresent()){
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
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Switch ast) {
        return null;    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        return null;    }

    @Override
    public Void visit(Ast.Statement.Return ast) {
        print("return ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if(ast.getType() == Environment.Type.CHARACTER || ast.getType() == Environment.Type.STRING)
        {
            print("\"", ast.getLiteral(), "\"");
        }
        else if(ast.getType() == Environment.Type.INTEGER)
        {
            print(new BigInteger(ast.getLiteral().toString()));
        }
        else if(ast.getType() == Environment.Type.DECIMAL)
        {
            print(new BigDecimal(ast.getLiteral().toString()));
        }
        else {
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
        return null;    }

    @Override
    public Void visit(Ast.Expression.Function ast) {
        return null;
    }

    @Override
    public Void visit(Ast.Expression.PlcList ast) {
        return null;    }

}
