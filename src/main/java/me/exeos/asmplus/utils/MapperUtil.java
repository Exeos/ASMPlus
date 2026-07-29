package me.exeos.asmplus.utils;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.analysis.hierarchy.edge.MethodEdge;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapperUtil {

    public static String genName(Function<Integer, String> nameGen, Set<String> excluded) {
        String name;
        int tries = 0;
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
                Set<MethodEdge> overrideGroup = hierarchy.get(classNode.name).getOverrideGroup(methodNode);
                if (overrideGroup.isEmpty()) {
                    continue;
                }

                MethodEdge first = overrideGroup.iterator().next();
                String firstMapped = mapping.get(first.getOwnerName() + first.getName() + first.getDesc());

                for (MethodEdge overrideEdge : overrideGroup) {
                    String edgeMethodId = overrideEdge.getOwnerName() + overrideEdge.getName() + overrideEdge.getDesc();
                    if (firstMapped == null) {
                        mapping.put(edgeMethodId, overrideEdge.getName());
                    } else {
                        mapping.put(edgeMethodId, firstMapped);
                    }
                }
            }
        }
    }

    public static Map<String, Set<String>> mergeUsedFromHierarchy(ClassEdge edge, Map<ClassEdge, Map<String, Set<String>>> usedByEdge) {
        Map<String, Set<String>> merged = new HashMap<>();
        if (usedByEdge.containsKey(edge)) {
            for (Map.Entry<String, Set<String>> entry : usedByEdge.get(edge).entrySet()) {
                merged.computeIfAbsent(entry.getKey(), _ -> new HashSet<>()).addAll(entry.getValue());
            }
        }

        HierarchyAnalyzer.recurseParents(edge.parents, parent -> {
            if (usedByEdge.containsKey(parent)) {
                for (Map.Entry<String, Set<String>> entry : usedByEdge.get(parent).entrySet()) {
                    merged.computeIfAbsent(entry.getKey(), _ -> new HashSet<>()).addAll(entry.getValue());
                }
            }
        });

        HierarchyAnalyzer.recurseChildren(edge.children, child -> {
            if (usedByEdge.containsKey(child)) {
                for (Map.Entry<String, Set<String>> entry : usedByEdge.get(child).entrySet()) {
                    merged.computeIfAbsent(entry.getKey(), _ -> new HashSet<>()).addAll(entry.getValue());
                }
            }
        });

        return merged;
    }
}
