package org.example;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        String workingDirectory = System.getProperty("user.dir");

        JavaParser parser = new JavaParser();

        Collection<TypeSolver> solvers = new ArrayList<>();
        solvers.add(new ReflectionTypeSolver());
        solvers.add(new JavaParserTypeSolver(Path.of(workingDirectory, "src", "main", "java")));

        parser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(new CombinedTypeSolver(solvers)));

        ParseResult<CompilationUnit> result = parser.parse(Path.of(workingDirectory, "src", "main", "java", "org", "example", "Descendant.java"));

        if (result.isSuccessful() && result.getResult().isPresent()) {
            CompilationUnit unit = result.getResult().get();

            List<Type> types = unit.findAll(Type.class);

            for (Type type : types) {
                Optional<Node> parentNode = type.getParentNode();
                // just so we do not resolve namespaces like java.util of java.util.Iterator
                if (parentNode.isPresent() && parentNode.get() instanceof ClassOrInterfaceType && type instanceof ClassOrInterfaceType) {
                    continue;
                }
                System.out.println("Resolving type: " + type);
                ResolvedType resolvedType = type.resolve();
                System.out.printf("Type: %s; Resolved: %s%n", type, resolvedType);
            }
        }

        System.out.println();
    }
}