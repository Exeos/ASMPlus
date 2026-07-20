package me.exeos.asmplus.analysis.hierarchy;

import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;
import java.util.function.Consumer;

public class HierarchyAnalyzer {

    public static Map<String, ClassEdge> analyzeNameMapped(JarArchive jar) {
        Map<ClassNode, ClassEdge> nodeMapped = analyze(jar);
        Map<String, ClassEdge> nameMapped = new HashMap<>();

        nodeMapped.forEach((classNode, classEdge) -> nameMapped.put(classNode.name, classEdge));

        return nameMapped;
    }

    public static Map<ClassNode, ClassEdge> analyze(JarArchive jar) {
        Map<ClassNode, ClassEdge> edgeMap = new HashMap<>();

        for (ClassNode classNode : jar.getClassesAndDependencies().values()) {
            edgeMap.putIfAbsent(classNode, new ClassEdge(classNode));

            Set<String> directParents = new HashSet<>(classNode.interfaces);
            if (classNode.superName != null) {
                directParents.add(classNode.superName);
            }

            for (String parentName : directParents) {
                jar.getClassNode(parentName).ifPresentOrElse(parentClass -> {
                    edgeMap.get(classNode).parents.add(edgeMap.computeIfAbsent(parentClass, ClassEdge::new));
                }, () -> {
                    edgeMap.get(classNode).unresolvedParents.add(parentName);
                });
            }
        }

        for (ClassEdge edge : edgeMap.values()) {
            for (ClassEdge parent : edge.parents) {
                parent.children.add(edge);
            }
        }

        return edgeMap;
    }

    public static void recurseParents(List<ClassEdge> edges, Consumer<ClassEdge> edgeConsumer) {
        for (ClassEdge edge : edges) {
            edgeConsumer.accept(edge);
            recurseParents(edge.parents, edgeConsumer);
        }
    }

    public static void recurseChildren(List<ClassEdge> edges, Consumer<ClassEdge> edgeConsumer) {
        for (ClassEdge edge : edges) {
            edgeConsumer.accept(edge);
            recurseChildren(edge.children, edgeConsumer);
        }
    }
}
