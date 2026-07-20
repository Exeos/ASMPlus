package me.exeos.asmplus.remapper;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.analysis.hierarchy.edge.ClassEdge;
import me.exeos.asmplus.analysis.hierarchy.edge.MethodEdge;
import me.exeos.asmplus.descriptor.DescriptorMember;
import me.exeos.asmplus.descriptor.DescriptorParser;
import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.Optional;

public class MethodRemapper {

    private final Map<String, String> mapping;

    public MethodRemapper(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public void remap(JarArchive jar) {
        remap(jar, HierarchyAnalyzer.analyzeNameMapped(jar));
    }

    public void remap(JarArchive jar, Map<String, ClassEdge> hierarchy) {
        jar.getClasses().values().forEach(classNode -> remapCallsites(hierarchy, classNode));
        jar.getClasses().values().forEach(this::remapMethodNames);
    }

    private void remapCallsites(Map<String, ClassEdge> hierarchy, ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            for (AbstractInsnNode insnNode : methodNode.instructions) {
                switch (insnNode) {
                    case MethodInsnNode methodInsnNode -> {
                        methodInsnNode.name = getMapped(
                                findRoot(hierarchy, methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc),
                                methodInsnNode.name,
                                methodInsnNode.desc);
                    }
                    case InvokeDynamicInsnNode indy -> {
                        DescriptorMember returnType = DescriptorParser.parseMethodDesc(indy.desc).getReturnType();
                        if (!returnType.isPrimitive() && !returnType.isArray() && indy.bsmArgs.length > 0 && indy.bsmArgs[0] instanceof Type type) {
                            indy.name = getMapped(returnType.getValue(), indy.name, type.getDescriptor());
                        }

                        indy.bsm = remapHandle(indy.bsm);
                        for (int i = 0; i < indy.bsmArgs.length; i++) {
                            Object bsmArg = indy.bsmArgs[i];
                            if (bsmArg instanceof Handle handle) {
                                indy.bsmArgs[i] = remapHandle(handle);
                            }
                        }
                    }
                    default -> {}
                }
            }
        }
    }

    public void remapMethodNames(ClassNode owner) {
        for (MethodNode methodNode : owner.methods) {
            methodNode.name = getMapped(owner.name, methodNode);
        }
    }

    private String findRoot(Map<String, ClassEdge> hierarchy, String owner, String name, String desc) {
        if (!hierarchy.containsKey(owner)) {
            return owner;
        }

        Optional<MethodEdge> rootMethod = hierarchy.get(owner).findMethodRoot(name, desc);
        return rootMethod.isPresent() ? rootMethod.get().owner().classNode.name : owner;
    }

    private Handle remapHandle(Handle handle) {
        return new Handle(
                handle.getTag(),
                handle.getOwner(),
                getMapped(handle.getOwner(), handle.getName(), handle.getDesc()),
                handle.getDesc(),
                handle.isInterface()
        );
    }

    private String getMapped(String owner, MethodNode methodNode) {
        return getMapped(owner, methodNode.name, methodNode.desc);
    }

    private String getMapped(MethodInsnNode methodInsnNode) {
        return getMapped(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
    }

    private String getMapped(String owner, String name, String desc) {
        return mapping.getOrDefault(owner + name + desc, name);
    }
}
