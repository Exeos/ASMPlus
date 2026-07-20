package me.exeos.asmplus.analysis.init;

import me.exeos.asmplus.jar.JarArchive;
import me.exeos.asmplus.utils.ClassUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Analyzer responsible for determining the initialization
 * within a JAR archive by statically analyzing method invocation instructions.
 */
public class ClassInitAnalyzer {


    /**
     * @return Key = Class that gets initialized AFTER Values
     */
    public static Map<String, Set<String>> analyzeInitOrder(JarArchive jar, ClassNode startClass, MethodNode startMethod) {
        Map<String, Set<String>> initOrder = new HashMap<>();
        analyzeInitOrder(jar, startClass, startMethod, initOrder, new HashSet<>());
        return invertGraph(initOrder);
    }

    private static void analyzeInitOrder(JarArchive jar, ClassNode startClass, MethodNode startMethod,
                                         Map<String, Set<String>> initOrder, Set<String> visitedMethods) {
        String methodSignature = startClass.name + startMethod.name + startMethod.desc;
        if (!visitedMethods.add(methodSignature)) {
            return;
        }

        for (AbstractInsnNode insn : startMethod.instructions) {
            if (insn instanceof MethodInsnNode methodInsn) {
                if (!startClass.name.equals(methodInsn.owner)) {
                    initOrder.computeIfAbsent(startClass.name, _ -> new HashSet<>()).add(methodInsn.owner);
                }

                jar.getClassNode(methodInsn.owner, false).ifPresent(targetClass -> {
                    ClassUtil.findMethod(targetClass, methodInsn.name, methodInsn.desc).ifPresent(targetMethod ->
                            analyzeInitOrder(jar, targetClass, targetMethod, initOrder, visitedMethods)
                    );
                });
            }
        }
    }

    /**
     * Inverts a directed graph representation.
     * <p>
     * Converts a "Class A inits [Class B, Class C]" map into a
     * "Class B is initialized by [Class A]" map.
     * </p>
     *
     * @param sourceGraph The original map.
     * @return A new map containing the structurally inverted graph.
     */
    private static Map<String, Set<String>> invertGraph(Map<String, Set<String>> sourceGraph) {
        Map<String, Set<String>> invertedGraph = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : sourceGraph.entrySet()) {
            for (String value : entry.getValue()) {
                invertedGraph.computeIfAbsent(value, _ -> new HashSet<>()).add(entry.getKey());
            }
        }
        return invertedGraph;
    }
}
