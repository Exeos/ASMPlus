package me.exeos.asmplus.utils;

import me.exeos.asmplus.jar.JarArchive;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassUtil implements Opcodes {

    public static String getNoneCollidingClassName(JarArchive archive, Function<Integer, String> nameGeneration) {
        String name;
        int tryCount = 0;
        do {
            name = nameGeneration.apply(tryCount);
            tryCount++;
        } while (archive.getClasses().containsKey(name));

        return name;
    }

    public static MethodNode getOrCreateStaticInitializer(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("<clinit>")) return method;
        }

        MethodNode methodNode = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
        methodNode.instructions.add(new InsnNode(RETURN));
        classNode.methods.add(methodNode);
        return methodNode;
    }

    public static Set<MethodNode> getConstructors(ClassNode classNode) {
        Set<MethodNode> constructors = new HashSet<>();
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("<init>")) {
                constructors.add(method);
            }
        }

        return constructors;
    }

    public static Optional<MethodNode> findMethod(ClassNode owner, String methodName, String methodDesc) {
        for (MethodNode methodNode : owner.methods) {
            if (methodNode.name.equals(methodName) && methodNode.desc.equals(methodDesc)) {
                return Optional.of(methodNode);
            }
        }

        return Optional.empty();
    }

    public static boolean isEnum(ClassNode classNode) {
        return classNode.superName != null && classNode.superName.equals("java/lang/Enum");
    }

    /**
     * Returns a map, mapping the methods name and how often a method with that name is declared. Ignores descriptor and hierarchy methods.
     *
     * @param owner Class containing methods
     * @return A map, mapping the methods name and how often a method with that name is declared
     */
    public static Map<String, Integer> methodCountByName(ClassNode owner) {
        return owner.methods.stream().collect(Collectors.toMap(methodNode -> methodNode.name, e -> 1, Math::addExact));
    }
}
