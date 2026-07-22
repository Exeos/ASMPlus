package me.exeos.asmplus.remapper.mapper.impl;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import me.exeos.asmplus.remapper.mapper.MappingContext;
import me.exeos.asmplus.utils.MapperUtil;
import me.exeos.asmplus.utils.MethodUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodMapper {

    public static Map<String, String> map(JarArchive jar, Function<Integer, String> nameGen, Function<MappingContext, Boolean> shouldExclude) {
        return map(jar, HierarchyAnalyzer.analyzeNameMapped(jar), nameGen, shouldExclude);
    }

    public static Map<String, String> map(JarArchive jar, Map<String, ClassEdge> hierarchy, Function<Integer, String> nameGen, Function<MappingContext, Boolean> shouldExclude) {
        Map<String, String> mapping = new HashMap<>();

        Map<ClassEdge, Set<String>> usedNamesByClass = new HashMap<>();

        for (ClassNode classNode : jar.getClasses().values()) {
            if (!hierarchy.containsKey(classNode.name) || !hierarchy.get(classNode.name).unresolvedParents.isEmpty() || shouldExclude.apply(new MappingContext(classNode, Optional.empty(), Optional.empty()))) {
                continue;
            }

            Set<String> hierarchyExcludedMethods = new HashSet<>();
            AtomicBoolean containsUnresolved = new AtomicBoolean(false);
            HierarchyAnalyzer.recurseParents(hierarchy.get(classNode.name).parents, edge -> {
                if (edge.hasUnresolved()) {
                    containsUnresolved.set(true);
                }

                if (jar.isDependency(edge.classNode.name)) {
                    hierarchyExcludedMethods.addAll(edge.methods.stream().map(
                            methodEdge -> methodEdge.methodNode().name + methodEdge.methodNode().desc
                    ).collect(Collectors.toSet()));
                }
            });

            if (containsUnresolved.get()) {
                continue;
            }

            Set<String> usedNames = MapperUtil.mergeUsedFromHierarchy(hierarchy.get(classNode.name), usedNamesByClass);
            for (MethodNode methodNode : classNode.methods) {
                if (shouldExclude.apply(new MappingContext(classNode, Optional.empty(), Optional.of(methodNode)))) {
                    usedNames.add(methodNode.name);
                }
            }

            for (MethodNode methodNode : classNode.methods) {
                if (hierarchyExcludedMethods.contains(methodNode.name + methodNode.desc) || MethodUtil.isSpecial(methodNode) || shouldExclude.apply(new MappingContext(classNode, Optional.empty(), Optional.of(methodNode)))) {
                    continue;
                }

                String name = MapperUtil.genName(nameGen, usedNames);
                usedNames.add(name);
                usedNamesByClass.computeIfAbsent(hierarchy.get(classNode.name), _ -> new HashSet<>()).add(name);

                mapping.put(classNode.name + methodNode.name + methodNode.desc, name);
            }
        }
        MapperUtil.hierarchyMergeMapping(jar, mapping, hierarchy);

        return mapping;
    }
}
