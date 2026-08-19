package me.exeos.asmplus.matcher.method;

import me.exeos.asmplus.analysis.hierarchy.edge.MethodEdge;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This ugly code is responsible for matching method nodes with each other while providing information about what fields are to be matched
 * @param owner Optional owner (must not be null)
 * @param name Name (only required thing for matching)
 * @param desc Optional desc (must not be null
 * @param matchMode Determines what fields are to be compared
 */
public record MethodMatchEntry(String owner, String name, String desc, MethodMatcher.Mode matchMode) {

    public static MethodMatchEntry of(MethodEdge edge) {
        return of(edge, MethodMatcher.Mode.OWNER_NAME_DESC);
    }

    public static MethodMatchEntry of(MethodEdge edge, MethodMatcher.Mode mode) {
        return new MethodMatchEntry(edge.owner().classNode.name, edge.methodNode().name, edge.methodNode().desc, mode);
    }

    public static MethodMatchEntry of(String owner, MethodNode methodNode) {
        return of(owner, methodNode, MethodMatcher.Mode.OWNER_NAME_DESC);
    }

    public static MethodMatchEntry of(String owner, MethodNode methodNode, MethodMatcher.Mode matchMode) {
        return new MethodMatchEntry(owner, methodNode.name, methodNode.desc, matchMode);
    }

    public static MethodMatchEntry of(MethodInsnNode insnNode) {
        return of(insnNode, MethodMatcher.Mode.OWNER_NAME_DESC);
    }

    public static MethodMatchEntry of(MethodInsnNode insnNode, MethodMatcher.Mode matchMode) {
        return new MethodMatchEntry(insnNode.owner, insnNode.name, insnNode.desc, matchMode);
    }

    public static MethodMatchEntry of(String owner, String name, String desc) {
        return new MethodMatchEntry(owner, name, desc, MethodMatcher.Mode.OWNER_NAME_DESC);
    }

    public static MethodMatchEntry of(String owner, String name) {
        return new MethodMatchEntry(owner, name, "", MethodMatcher.Mode.OWNER_NAME);
    }

    public static MethodMatchEntry of(String name) {
        return new MethodMatchEntry("", name, "", MethodMatcher.Mode.NAME);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        return other == this ||
                (other instanceof MethodMatchEntry(String otherOwner, String otherName, String otherDesc,
                                                   MethodMatcher.Mode _
                                                   )
                        && (matchMode() == MethodMatcher.Mode.NAME || otherOwner.equals(owner()))
                        && (otherName.equals(name()))
                        && (matchMode() == MethodMatcher.Mode.NAME || matchMode() == MethodMatcher.Mode.OWNER_NAME || otherDesc.equals(desc()))
                );
    }
}
