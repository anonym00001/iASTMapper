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
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.gen.jdt;

import com.github.gumtreediff.gen.SyntaxException;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.TreeContext;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.dom.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractJdtTreeGenerator extends TreeGenerator {

    private static char[] readerToCharArray(Reader r) throws IOException {
        StringBuilder fileData = new StringBuilder();
        try (BufferedReader br = new BufferedReader(r)) {
            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = br.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
        }
        return  fileData.toString().toCharArray();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TreeContext generate(Reader r) throws IOException {
        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map pOptions = JavaCore.getOptions();
        pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
        pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
        pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
        pOptions.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
        parser.setCompilerOptions(pOptions);
        char[] source = readerToCharArray(r);
        parser.setSource(source);
        IScanner scanner = ToolFactory.createScanner(false, false, false, false);
        scanner.setSource(source);
        AbstractJdtVisitor v = createVisitor(scanner);
        ASTNode node = parser.createAST(null);

//        BufferedWriter srcContent = new BufferedWriter(new FileWriter("D:\\SE-Mapping\\files\\activemq\\activemq-broker\\src\\main\\java\\org\\apache\\activemq\\broker\\PublishedAddressPolicy_4800a7a1a46dd248b130b4289cf9f26787fe5612\\org_SrcNode.java"));
//        print(node,srcContent,0);
//        srcContent.close();
        if ((node.getFlags() & ASTNode.MALFORMED) != 0) // bitwise flag to check if the node has a syntax error
            throw new SyntaxException(this, r);
        node.accept(v);
        return v.getTreeContext();
    }

    protected abstract AbstractJdtVisitor createVisitor(IScanner scanner);

    private static void print(ASTNode node, BufferedWriter f, int nums) throws IOException {
        List properties = node.structuralPropertiesForType();
        for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
            Object descriptor = iterator.next();
            if (descriptor instanceof SimplePropertyDescriptor) {
                SimplePropertyDescriptor simple = (SimplePropertyDescriptor) descriptor;
                Object value = node.getStructuralProperty(simple);
                for(int i = 0;i < nums;i++)
                    f.write("    |");
                if(value != null)
                    f.write(simple.getId() + " : " + value.toString() + "\n");
                else
                    f.write(simple.getId() + " : Null\n");
//                System.out.println(simple.getId() + " (" + value.toString() + ")");
            } else if (descriptor instanceof ChildPropertyDescriptor) {
                ChildPropertyDescriptor child = (ChildPropertyDescriptor) descriptor;
                ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
                if (childNode != null) {
                    for(int i = 0;i < nums;i++)
                        f.write("    |");
//                    f.write("Child : " + child.getId() + "\n");
                    f.write(child.getId() + "\n");
                    print(childNode,f,nums+1);
                }
            } else {
                ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) descriptor;
                for(int i = 0;i < nums;i++)
                    f.write("    |");
                f.write(list.getId() + "\n");
                print((List) node.getStructuralProperty(list),f,nums+1);
            }
        }
    }

    private static void print(List nodes, BufferedWriter f, int nums) throws IOException {
        for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
            print((ASTNode) iterator.next(),f,nums+1);
        }
    }

}
