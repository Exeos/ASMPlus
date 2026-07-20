package me.exeos.asmplus.remapper;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class FieldRemapper {

    private final Map<String, String> mapping;

    public FieldRemapper(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public void remap(JarArchive jar) {
        remap(jar, HierarchyAnalyzer.analyzeNameMapped(jar));
    }

    public void remap(JarArchive jar, Map<String, ClassEdge> hierarchy) {
        for (ClassNode classNode : jar.getClasses().values()) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (!(insnNode instanceof FieldInsnNode fieldInsnNode)) {
                        continue;
                    }

                    fieldInsnNode.name = getMapped(
                            getDeclaringClass(fieldInsnNode, hierarchy),
                            fieldInsnNode.name,
                            fieldInsnNode.desc
                    );
                }
            }
        }

        // update field names after because hierarchy holds reference to them and this affects finding declared fields
        for (ClassNode classNode : jar.getClasses().values()) {
            for (FieldNode fieldNode : classNode.fields) {
                fieldNode.name = getMapped(classNode.name, fieldNode);
            }
        }
    }

    private String getDeclaringClass(FieldInsnNode of, Map<String, ClassEdge> hierarchy) {
        if (!hierarchy.containsKey(of.owner)) {
            return of.owner;
        }

        AtomicReference<String> declaringClass = new AtomicReference<>(of.owner);
        hierarchy.get(of.owner)
                .findDeclaringClassOfField(of.name, of.desc)
                .ifPresent(declaringEdge -> {
                    declaringClass.set(declaringEdge.classNode.name);
                });

        return declaringClass.get();
    }

    private String getMapped(String owner, FieldNode fieldNode) {
        return getMapped(owner, fieldNode.name, fieldNode.desc);
    }

    private String getMapped(String owner, String name, String desc) {
        return mapping.getOrDefault(owner + name + desc, name);
    }
}
