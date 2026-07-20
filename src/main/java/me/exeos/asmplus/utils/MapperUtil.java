package me.exeos.asmplus.utils;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapperUtil {

    public static String genName(Function<Integer, String> nameGen, Set<String> excluded) {
        String name;
        int tries = 1;
        do {
            name = nameGen.apply(tries++);
        } while (excluded.contains(name));

        return name;
    }

    public static void hierarchyMergeMapping(JarArchive jar, Map<String, String> mapping) {
        hierarchyMergeMapping(jar, mapping, HierarchyAnalyzer.analyzeNameMapped(jar));
    }

    public static void hierarchyMergeMapping(JarArchive jar, Map<String, String> mapping, Map<String, ClassEdge> hierarchy) {
        for (ClassNode classNode : jar.getClasses().values()) {
            if (!hierarchy.containsKey(classNode.name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                hierarchy.get(classNode.name).getMethodRoot(methodNode).ifPresent(root -> {
                    String rootKey = root.owner().classNode.name + root.methodNode().name + root.methodNode().desc;
                    if (mapping.containsKey(rootKey)) {
                        mapping.put(
                                classNode.name + methodNode.name + methodNode.desc,
                                mapping.get(rootKey)
                        );
                    }
                });
            }
        }
    }

    public static Set<String> mergeUsedFromHierarchy(ClassEdge edge, Map<ClassEdge, Set<String>> usedByEdge) {
        Set<String> merged = new HashSet<>();
        if (usedByEdge.containsKey(edge)) {
            merged.addAll(usedByEdge.get(edge));
        }

        HierarchyAnalyzer.recurseParents(edge.parents, parent -> {
            if (usedByEdge.containsKey(parent)) {
                merged.addAll(usedByEdge.get(parent));
            }
        });

        HierarchyAnalyzer.recurseChildren(edge.children, child -> {
            if (usedByEdge.containsKey(child)) {
                merged.addAll(usedByEdge.get(child));
            }
        });

        return merged;
    }
}
