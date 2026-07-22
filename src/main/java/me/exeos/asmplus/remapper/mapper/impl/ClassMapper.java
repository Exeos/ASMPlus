package me.exeos.asmplus.remapper.mapper.impl;

import me.exeos.asmplus.jar.JarArchive;
import me.exeos.asmplus.utils.MapperUtil;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ClassMapper {

    public static Map<String, String> map(JarArchive jar, Function<Integer, String> nameGen, Function<ClassNode, Boolean> shouldExclude) {
        Map<String, String> mapping = new HashMap<>();
        Set<String> usedNames = new HashSet<>();

        for (ClassNode classNode : jar.getClasses().values()) {
            if (shouldExclude.apply(classNode)) {
                usedNames.add(classNode.name);
            }
        }

        for (ClassNode classNode : jar.getClasses().values()) {
            if (shouldExclude.apply(classNode)) {
                continue;
            }

            String name = MapperUtil.genName(nameGen, usedNames);
            usedNames.add(name);

            mapping.put(classNode.name, name);
        }

        return mapping;
    }
}
