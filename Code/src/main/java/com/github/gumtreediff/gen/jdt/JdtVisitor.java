/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com> *
 */


package com.github.gumtreediff.gen.jdt;


import com.github.gumtreediff.gen.SyntaxException;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Type;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

import static com.github.gumtreediff.tree.TypeSet.type;

public class JdtVisitor  extends AbstractJdtVisitor {
    
    private static final Type INFIX_EXPRESSION_OPERATOR = type("INFIX_EXPRESSION_OPERATOR");
    private static final Type METHOD_INVOCATION_RECEIVER = type("METHOD_INVOCATION_RECEIVER");
    private static final Type METHOD_INVOCATION_ARGUMENTS = type("METHOD_INVOCATION_ARGUMENTS");
    private static final Type TYPE_DECLARATION_KIND = type("TYPE_DECLARATION_KIND");
    private static final Type ASSIGNMENT_OPERATOR = type("ASSIGNMENT_OPERATOR");
    private static final Type PREFIX_EXPRESSION_OPERATOR = type("PREFIX_EXPRESSION_OPERATOR");
    private static final Type POSTFIX_EXPRESSION_OPERATOR = type("POSTFIX_EXPRESSION_OPERATOR");

    //增加代码
    private static final Type INSTANCE_OF_OPERATOR = type("INSTANCE_OF_OPERATOR");
    private static final Type INFIX_EXPRESSION = type("InfixExpression");

    private static final Type ARRAY_INITIALIZER = nodeAsSymbol(ASTNode.ARRAY_INITIALIZER);
    private static final Type SIMPLE_NAME = nodeAsSymbol(ASTNode.SIMPLE_NAME);

    private IScanner scanner;

    public JdtVisitor(IScanner scanner) {
        super();
        this.scanner = scanner;
    }

    @Override
    public void preVisit(ASTNode n) {
        pushNode(n, getLabel(n));
    }

    public boolean visit(MethodInvocation i)  {
        if (i.getExpression() !=  null) {
            push(METHOD_INVOCATION_RECEIVER, "", i.getExpression().getStartPosition(),
                    i.getExpression().getLength());
            i.getExpression().accept(this);
            popNode();
        }
        pushNode(i.getName(), getLabel(i.getName()));
        popNode();
        if (i.arguments().size() >  0) {
            int startPos = ((ASTNode) i.arguments().get(0)).getStartPosition();
            int length = ((ASTNode) i.arguments().get(i.arguments().size() - 1)).getStartPosition()
                    + ((ASTNode) i.arguments().get(i.arguments().size() - 1)).getLength() -  startPos;
            push(METHOD_INVOCATION_ARGUMENTS,"", startPos , length);
            for (Object o : i.arguments()) {
                ((ASTNode) o).accept(this);

            }
            popNode();
        }
        return false;
    }

    protected String getLabel(ASTNode n) {
        if (n instanceof Name)
            return ((Name) n).getFullyQualifiedName();
        else if (n instanceof PrimitiveType)
            return n.toString();
        else if (n instanceof Modifier)
            return n.toString();
        else if (n instanceof StringLiteral)
            return ((StringLiteral) n).getEscapedValue();
        else if (n instanceof NumberLiteral)
            return ((NumberLiteral) n).getToken();
        else if (n instanceof CharacterLiteral)
            return ((CharacterLiteral) n).getEscapedValue();
        else if (n instanceof BooleanLiteral)
            return n.toString();
        else if (n instanceof TextElement)
            return n.toString();
        else
            return "";
    }

    @Override
    public boolean visit(TypeDeclaration d) {
        return true;
    }

    @Override
    public boolean visit(TagElement e) {
        return true;
    }

    @Override
    public boolean visit(QualifiedName name) {
        return false;
    }

    @Override
    public void postVisit(ASTNode n) {
        if (n instanceof TypeDeclaration)
            handlePostVisit((TypeDeclaration) n);
        else if (n instanceof InfixExpression)
            handlePostVisit((InfixExpression) n);
        else if (n instanceof Assignment)
            handlePostVisit((Assignment)  n);
        else if (n instanceof PrefixExpression)
            handlePostVisit((PrefixExpression) n);
        else if (n instanceof PostfixExpression)
            handlePostVisit((PostfixExpression) n);
        else if (n instanceof ArrayCreation)
            handlePostVisit((ArrayCreation) n);
        // 增加代码
        else if (n instanceof InstanceofExpression)
            handlePostVisit((InstanceofExpression) n);

        popNode();
    }

    // 增加代码
    private void handlePostVisit(InstanceofExpression c){
        ITree t = this.trees.peek();
        String label  = "instanceof";
        ITree s = context.createTree(INSTANCE_OF_OPERATOR, label);
        PosAndLength pl = searchInstanceofExpressionOperator(c);
        s.setPos(pl.pos);
        s.setLength(pl.length);
        t.getChildren().add(1, s);
        s.setParent(t);
    }

    // 增加代码
    private PosAndLength searchInstanceofExpressionOperator(InstanceofExpression e) {
        ITree t = this.trees.peek();
        scanner.resetTo(t.getChild(0).getEndPos(), t.getChild(1).getPos());
        int pos = 0;
        int length = 0;
        try {
            int token = scanner.getNextToken();
            while (token != ITerminalSymbols.TokenNameEOF) {
                pos = scanner.getCurrentTokenStartPosition();
                length = scanner.getCurrentTokenEndPosition() - pos + 1;
                break;
            }
        }
        catch (InvalidInputException ex) {
            throw new SyntaxException(ex.getMessage(), ex);
        }

        return new PosAndLength(pos, length);
    }

    private void handlePostVisit(ArrayCreation c) {
        ITree t = this.trees.peek();
        if (t.getChild(1).getType() == ARRAY_INITIALIZER)
            return;
        for (int i = 1; i < t.getChild(0).getChildren().size(); i++) {
            ITree dim = t.getChild(0).getChild(i);
            if (t.getChildren().size() < 2)
                break;
            ITree expr = t.getChildren().remove(1);
            dim.addChild(expr);
        }
    }

    private void handlePostVisit(PostfixExpression e) {
        ITree t = this.trees.peek();
        String label  = e.getOperator().toString();
        ITree s = context.createTree(POSTFIX_EXPRESSION_OPERATOR, label);
        PosAndLength pl = searchPostfixExpressionPosition(e);
        s.setPos(pl.pos);
        s.setLength(pl.length);
        t.getChildren().add(1, s);
        s.setParent(t);
    }

    private PosAndLength searchPostfixExpressionPosition(PostfixExpression e) {
        ITree t = this.trees.peek();
        scanner.resetTo(t.getChild(0).getEndPos(), t.getEndPos());
        int pos = 0;
        int length = 0;
        try {
            int token = scanner.getNextToken();
            while (token != ITerminalSymbols.TokenNameEOF) {
                pos = scanner.getCurrentTokenStartPosition();
                length = scanner.getCurrentTokenEndPosition() - pos + 1;
                break;
            }
        }
        catch (InvalidInputException ex) {
            throw new SyntaxException(ex.getMessage(), ex);
        }

        return new PosAndLength(pos, length);
    }

    private void handlePostVisit(PrefixExpression e) {
        ITree t = this.trees.peek();
        String label  = e.getOperator().toString();
        ITree s = context.createTree(PREFIX_EXPRESSION_OPERATOR, label);
        PosAndLength pl = searchPrefixExpressionPosition(e);
        s.setPos(pl.pos);
        s.setLength(pl.length);
        t.getChildren().add(0, s);
        s.setParent(t);
    }

    private PosAndLength searchPrefixExpressionPosition(PrefixExpression e) {
        ITree t = this.trees.peek();
        scanner.resetTo(t.getPos(), t.getChild(0).getPos());
        int pos = 0;
        int length = 0;
        try {
            int token = scanner.getNextToken();
            while (token != ITerminalSymbols.TokenNameEOF) {
                pos = scanner.getCurrentTokenStartPosition();
                length = scanner.getCurrentTokenEndPosition() - pos + 1;
                break;
            }
        }
        catch (InvalidInputException ex) {
            throw new SyntaxException(ex.getMessage(), ex);
        }

        return new PosAndLength(pos, length);
    }

    private void handlePostVisit(Assignment a) {
        ITree t = this.trees.peek();
        String label  = a.getOperator().toString();
        ITree s = context.createTree(ASSIGNMENT_OPERATOR, label);
        PosAndLength pl = searchAssignmentOperatorPosition(a);
        s.setPos(pl.pos);
        s.setLength(pl.length);
        t.getChildren().add(1, s);
        s.setParent(t);
    }

    private PosAndLength searchAssignmentOperatorPosition(Assignment a) {
        ITree t = this.trees.peek();
        scanner.resetTo(t.getChild(0).getEndPos(), t.getChild(1).getPos());
        int pos = 0;
        int length = 0;
        try {
            int token = scanner.getNextToken();
            while (token != ITerminalSymbols.TokenNameEOF) {
                pos = scanner.getCurrentTokenStartPosition();
                length = scanner.getCurrentTokenEndPosition() - pos + 1;
                break;
            }
        }
        catch (InvalidInputException ex) {
            throw new SyntaxException(ex.getMessage(), ex);
        }

        return new PosAndLength(pos, length);
    }

    private void handlePostVisit(InfixExpression e) {
        // 增加代码
        if (e.hasExtendedOperands()){
            ITree t = this.trees.peek();
            String label = e.getOperator().toString();
            List<PosAndLength> plList = searchInfixOperatorPositionOfExtendedOperands(e);
            ITree curParent = t;
            for (int i = plList.size() - 2; i >= 0; i--){
                ITree startChild = curParent.getChild(0);
                ITree endChild = curParent.getChild(i + 1);
                ITree infixExpr = createInfixExpression(startChild, endChild);
                List<ITree> tmpChildren = new ArrayList<>();
                for (int j = 0; j <= i + 1; j++){
                    ITree tmp = curParent.getChildren().remove(0);
                    tmp.setParent(infixExpr);
                    tmpChildren.add(tmp);
                }
                infixExpr.setChildren(tmpChildren);
                curParent.getChildren().add(0, infixExpr);
                infixExpr.setParent(curParent);
                ITree s = context.createTree(INFIX_EXPRESSION_OPERATOR, label);
                PosAndLength pl = plList.get(i + 1);
                s.setPos(pl.pos);
                s.setLength(pl.length);
                curParent.getChildren().add(1, s);
                s.setParent(curParent);
                if (i == 0){
                    ITree s2 = context.createTree(INFIX_EXPRESSION_OPERATOR, label);
                    PosAndLength pl2 = plList.get(0);
                    s2.setPos(pl2.pos);
                    s2.setLength(pl2.length);
                    infixExpr.getChildren().add(1, s2);
                    s2.setParent(infixExpr);
                }
                curParent = infixExpr;
            }
        } else {
            ITree t = this.trees.peek();
            String label = e.getOperator().toString();
            ITree s = context.createTree(INFIX_EXPRESSION_OPERATOR, label);
            PosAndLength pl = searchInfixOperatorPosition(e);
            s.setPos(pl.pos);
            s.setLength(pl.length);
            t.getChildren().add(1, s);
            s.setParent(t);
        }
    }

    private ITree createInfixExpression(ITree startChild, ITree endChild){
        ITree tmp = context.createTree(INFIX_EXPRESSION, "");
        int pos = startChild.getPos();
        int length = endChild.getEndPos() - pos + 1;
        tmp.setPos(pos);
        tmp.setLength(length);
        return tmp;
    }

    private List<PosAndLength> searchInfixOperatorPositionOfExtendedOperands(InfixExpression e){
        ITree t = this.trees.peek();
        int childrenSize = t.getChildren().size();
        List<PosAndLength> ret = new ArrayList<>();
        int pos = 0;
        int length = 0;
        for (int i =0; i < childrenSize - 1; i++){
            scanner.resetTo(t.getChild(i).getEndPos(), t.getChild(i+1).getPos());
            try {
                int token = scanner.getNextToken();
                while (token != ITerminalSymbols.TokenNameEOF){
                    pos = scanner.getCurrentTokenStartPosition();
                    length = scanner.getCurrentTokenEndPosition() - pos + 1;
                    break;
                }
            } catch (InvalidInputException ex){
                throw new SyntaxException(ex.getMessage(), ex);
            }
            ret.add(new PosAndLength(pos, length));
        }
        return ret;
    }

    private PosAndLength searchInfixOperatorPosition(InfixExpression e) {
        ITree t = this.trees.peek();
        scanner.resetTo(t.getChild(0).getEndPos(), t.getChild(1).getPos());
        int pos = 0;
        int length = 0;
        try {
            int token = scanner.getNextToken();
            while (token != ITerminalSymbols.TokenNameEOF) {
                pos = scanner.getCurrentTokenStartPosition();
                length = scanner.getCurrentTokenEndPosition() - pos + 1;
                break;
            }
        }
        catch (InvalidInputException ex) {
            throw new SyntaxException(ex.getMessage(), ex);
        }

        return new PosAndLength(pos, length);
    }

    private void handlePostVisit(TypeDeclaration d) {
        String label = "class";
        if (d.isInterface())
            label = "interface";

        ITree s = context.createTree(TYPE_DECLARATION_KIND, label);
        PosAndLength pl = searchTypeDeclarationKindPosition(d);
        s.setPos(pl.pos);
        s.setLength(pl.length);
        int index = 0;
        ITree t = this.trees.peek();
        for (ITree c : t.getChildren()) {
            if (c.getType() != SIMPLE_NAME)
                index++;
            else
                break;
        }
        t.insertChild(s, index);
    }

    private PosAndLength searchTypeDeclarationKindPosition(TypeDeclaration d) {
        int start = d.getStartPosition();
        int end = start + d.getLength();
        scanner.resetTo(start, end);
        int pos = 0;
        int length = 0;
        try {
            int prevToken = -1;
            while (true) {
                int token = scanner.getNextToken();
                scanner.getCurrentTokenSource();
                if ((token == ITerminalSymbols.TokenNameclass || token == ITerminalSymbols.TokenNameinterface)
                        && prevToken != ITerminalSymbols.TokenNameDOT) {
                    pos = scanner.getCurrentTokenStartPosition();
                    length = scanner.getCurrentTokenEndPosition() - pos + 1;
                    break;
                }
                prevToken = token;
            }
        }
        catch (InvalidInputException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
        return new PosAndLength(pos, length);
    }

    public static class PosAndLength {
        public int pos;

        public int length;

        public PosAndLength(int pos, int length) {
            this.pos = pos;
            this.length = length;
        }
    }

}
