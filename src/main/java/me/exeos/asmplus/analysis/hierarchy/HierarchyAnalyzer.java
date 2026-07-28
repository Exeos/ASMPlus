package me.exeos.asmplus.analysis.hierarchy;

import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            ClassEdge edge = edgeMap.computeIfAbsent(classNode, ClassEdge::new);

            if (classNode.superName != null) {
                jar.getClassNode(classNode.superName).ifPresentOrElse(superClass -> {
                    ClassEdge superEdge = edgeMap.computeIfAbsent(superClass, ClassEdge::new);

                    edge.superClass = superEdge;
                    edge.parents.add(superEdge);
                }, () -> {
                    edge.unresolvedParents.add(classNode.superName);
                });
            }

            for (String interfaceName : classNode.interfaces) {
                jar.getClassNode(interfaceName).ifPresentOrElse(interfaceClass -> {
                    ClassEdge interfaceEdge = edgeMap.computeIfAbsent(interfaceClass, ClassEdge::new);

                    edge.parents.add(interfaceEdge);
                    edge.interfaces.add(interfaceEdge);
                }, () -> {
                    edge.unresolvedParents.add(interfaceName);
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

    public static void recurseInterfaces(List<ClassEdge> edges, Consumer<ClassEdge> edgeConsumer) {
        for (ClassEdge edge : edges) {
            edgeConsumer.accept(edge);
            recurseParents(edge.interfaces, edgeConsumer);
        }
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
