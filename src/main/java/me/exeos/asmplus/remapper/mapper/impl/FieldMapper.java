package me.exeos.asmplus.remapper.mapper.impl;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import me.exeos.asmplus.remapper.mapper.MappingContext;
import me.exeos.asmplus.utils.MapperUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.*;
import java.util.function.Function;

public class FieldMapper {

    public static Map<String, String> map(JarArchive jar, Function<Integer, String> nameGen) {
        return map(jar, HierarchyAnalyzer.analyze(jar), nameGen, mappingContext -> false);
    }

    public static Map<String, String> map(JarArchive jar, Map<ClassNode, ClassEdge> hierarchy, Function<Integer, String> nameGen) {
        return map(jar, HierarchyAnalyzer.analyze(jar), nameGen, _ -> false);
    }

    public static Map<String, String> map(JarArchive jar, Map<ClassNode, ClassEdge> hierarchy, Function<Integer, String> nameGen,  Function<MappingContext, Boolean> shouldExclude) {
        Map<String, String> mapping = new HashMap<>();

        Map<ClassEdge, Set<String>> usedNamesByClass = new HashMap<>();

        for (ClassNode classNode : jar.getClasses().values()) {
            if (!hierarchy.containsKey(classNode) || shouldExclude.apply(new MappingContext(classNode, Optional.empty(), Optional.empty()))) {
                continue;
            }

            Set<String> usedNames = MapperUtil.mergeUsedFromHierarchy(hierarchy.get(classNode), usedNamesByClass);
            for (FieldNode fieldNode : classNode.fields) {
                if (shouldExclude.apply(new MappingContext(classNode, Optional.of(fieldNode), Optional.empty()))) {
                    usedNames.add(fieldNode.name);
                }
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (shouldExclude.apply(new MappingContext(classNode, Optional.of(fieldNode), Optional.empty()))) {
                    continue;
                }

                String name = MapperUtil.genName(nameGen, usedNames);
                usedNames.add(name);
                usedNamesByClass.computeIfAbsent(hierarchy.get(classNode), _ -> new HashSet<>()).add(name);

                mapping.put(classNode.name + fieldNode.name + fieldNode.desc, name);
            }
        }

        return mapping;
    }
}
