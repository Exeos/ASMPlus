package me.exeos.asmplus.analysis.hierarchy.edge;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.utils.MethodUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a method in the analyzed class hierarchy.
 * <p>
 * A {@code MethodEdge} wraps a method declared on a specific {@link ClassEdge} and provides helpers
 * </p>
 */
public record MethodEdge(ClassEdge owner, MethodNode methodNode) {

    /**
     * Returns all methods in descendant classes that override this method.
     *
     * @return A list of overriding method edges
     */
    public List<MethodEdge> getOverriders() {
        List<MethodEdge> found = new ArrayList<>();

        if (MethodUtil.hasAccess(methodNode, Opcodes.ACC_FINAL)
                || MethodUtil.hasAccess(methodNode, Opcodes.ACC_STATIC)
                || MethodUtil.hasAccess(methodNode, Opcodes.ACC_PRIVATE)
        ) {
            return found;
        }

        HierarchyAnalyzer.recurseChildren(owner.children, classEdge -> {
            classEdge
                    .getMethod(methodNode.name, methodNode.desc)
                    .filter(methodEdge -> !MethodUtil.hasAccess(methodEdge.methodNode, Opcodes.ACC_STATIC))
                    .ifPresent(found::add);
        });

        return found;
    }

    /**
     * Returns the root method in the override chain for this method.
     * <p>
     * The root is the highest ancestor method that this method overrides. If no parent method
     * is overridden, this method returns itself.
     * </p>
     *
     * @return The root method in the hierarchy chain
     */
    public MethodEdge getRoot() {
        AtomicReference<MethodEdge> root = new AtomicReference<>(this);

        HierarchyAnalyzer.recurseParents(owner.parents, parentEdge -> {
            parentEdge.getMethod(methodNode).ifPresent(root::set);
        });

        return root.get();
    }

    /**
     * Determines whether this method overrides the given method.
     *
     * @param other The method to compare against
     * @return {@code true} If this method overrides {@code other}; otherwise {@code false}
     */
    public boolean overrides(MethodEdge other) {
        if (other == null) {
            return false;
        }

        return other.getOverriders().contains(this);
    }

    public String getOwnerName() {
        return owner.classNode.name;
    }

    public String getName() {
        return methodNode.name;
    }

    public String getDesc() {
        return methodNode.desc;
    }

    public int getAccess() {
        return methodNode.access;
    }
}
