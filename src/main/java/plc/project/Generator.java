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
        print("public class Main {");
        newline(0);
        indent++;
        if (!ast.getGlobals().isEmpty()) {
            for (Ast.Global global : ast.getGlobals()){
                newline(indent);
                print(global);
            }
            newline(0);
        }
        newline(indent);
        print("public static void main(String[] args) {");
        indent++;
        newline(indent);
        print("System.exit(new Main().main());");
        indent--;
        newline(indent);
        print("}");

        for (Ast.Function function : ast.getFunctions()) {
            newline(0);
            newline(indent);
            print(function);
        }
        indent--;
        newline(0);
        newline(indent);
        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Global ast) {
        if (ast.getValue().isPresent() && ast.getValue().get() instanceof Ast.Expression.PlcList) {
            print(Environment.getType(ast.getTypeName()).getJvmName(), "[] ", ast.getName(), " = {");
            Ast.Expression.PlcList plc = (Ast.Expression.PlcList) ast.getValue().get();
            if (plc.getValues().size() == 1) {
                print(plc.getValues().get(0), "};");
            } else {
                for (int i = 0; i < plc.getValues().size(); ++i) {
                    if (i == plc.getValues().size() - 1) {
                        print(plc.getValues().get(i), "};");
                    } else {
                        print(plc.getValues().get(i), ", ");
                    }
                }
            }
        } else if (ast.getMutable()) {
            print(Environment.getType(ast.getTypeName()).getJvmName(), " ", ast.getName());
            if (ast.getValue().isPresent()) {
                print(" = ", ast.getValue().get(), ";");
            } else
                print(";");
        } else {
            print("final ", Environment.getType(ast.getTypeName()).getJvmName(), " ", ast.getName());
            if (ast.getValue().isPresent()) {
                print(" = ", ast.getValue().get(), ";");
            } else
                print(";");
        }
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
        print("switch (", ast.getCondition(), ")", " {");
        indent++;

        for (Ast.Statement.Case caseStmt : ast.getCases()) {
            newline(indent);
            print(caseStmt);
        }
        indent--;
        newline(indent);
        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Statement.Case ast) {
        if (ast.getValue().isPresent()) {
            Ast.Expression.Literal lit = (Ast.Expression.Literal) ast.getValue().get();
            String res = "case '" + lit.getLiteral() + "':";
            print(res);
        } else {
            print("default:");
        }
        indent++;
        for (Ast.Statement stmt : ast.getStatements()) {
            newline(indent);
            print(stmt);
        }
        indent--;
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
        if (ast.getType() == Environment.Type.STRING) {
            print("\"", ast.getLiteral(), "\"");
        } else if (ast.getType() == Environment.Type.CHARACTER) {
            print("'", ast.getLiteral(),"'");
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
