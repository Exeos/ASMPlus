package me.exeos.asmplus.analysis.hierarchy.edge;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import me.exeos.asmplus.utils.MethodUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

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

        HierarchyAnalyzer.recurseChildren(owner.children, classEdge -> classEdge
                .getMethod(methodNode.name, methodNode.desc)
                .filter(methodEdge -> !MethodUtil.hasAccess(methodEdge.methodNode, Opcodes.ACC_STATIC))
                .ifPresent(found::add));

        return found;
    }

    /**
     * Returns the top declaration methods in the override group for this method.
     *
     * @return A Set of declaring methods of this method, at a minimum: this method
     */
    public Set<MethodEdge> findTopDeclarations() {
        Set<MethodEdge> topDeclarations = new HashSet<>();
        for (MethodEdge methodEdge : getOverrideGroup()) {
            boolean isTop = true;

            if (methodEdge.owner.superClass != null) {
                Optional<MethodEdge> viaSuper = methodEdge.owner.superClass.findNearestMethod(methodNode);
                if (viaSuper.isPresent() && viaSuper.get().canBeOverridden()) {
                    isTop = false;
                }
            }

            for (ClassEdge interfaceEdge : methodEdge.owner.interfaces) {
                Optional<MethodEdge> viaInterface = interfaceEdge.findNearestMethod(methodNode);
                if (viaInterface.isPresent() && viaInterface.get().canBeOverridden()) {
                    isTop = false;
                }
            }

            if (isTop) {
                topDeclarations.add(methodEdge);
            }
        }

        return topDeclarations;
    }

    /**
     * Computes the override group for this method.
     * Every {@link MethodEdge} in the hierarchy that is bound to the same method identity as this one,
     * and therefore must share this method's name and descriptor for the hierarchy to remain valid.
     *
     * @return the set of all method edges linked to this one. Always contains at least {@code this} method
     */
    public Set<MethodEdge> getOverrideGroup() {
        Set<MethodEdge> group = new HashSet<>();
        Deque<MethodEdge> worklist = new ArrayDeque<>();
        worklist.add(this);

        while (!worklist.isEmpty()) {
            MethodEdge current = worklist.poll();
            if (!group.add(current)) {
                continue;
            }

            ClassEdge owner = current.owner();
            MethodNode methodNode = current.methodNode();

            // upward
            if (owner.superClass != null) {
                owner.superClass.findNearestMethod(methodNode)
                        .filter(MethodEdge::canBeOverridden)
                        .ifPresent(worklist::add);
            }

            for (ClassEdge interfaceEdge : owner.interfaces) {
                interfaceEdge.findNearestMethod(methodNode)
                        .filter(MethodEdge::canBeOverridden)
                        .ifPresent(worklist::add);
            }

            // downward
            worklist.addAll(current.getOverriders());
            // downward declared in interface
            collectInterfaceDeclarations(owner, methodNode, worklist);
        }

        return group;
    }

    private void collectInterfaceDeclarations(ClassEdge owner, MethodNode methodNode, Deque<MethodEdge> worklist) {
        collectInterfaceDeclarations(owner, methodNode.name, methodNode.desc, worklist);
    }

    /**
     * Walks the descendants of {@code owner} looking for classes that inherit the
     * given method (do not redeclare it themselves) but additionally implement
     * some interface declaring a method with the same name and descriptor.
     * <p>
     * Example:  {@code class A extends B implements C}, where {@code B}
     * declares {@code String a()} and {@code C} declares
     * {@code String a()}, {@code A} satisfies {@code C.a()} by inheriting
     * {@code B.a()} without redeclaring anything. Walking upward from {@code B}
     * would never discover {@code C}, since {@code C} is not an ancestor of
     * {@code B} it only becomes relevant through {@code A}, a descendant.
     * <p>
     *
     * @param owner    the class edge whose descendants should be searched
     * @param name     the method name being matched
     * @param desc     the method descriptor being matched
     * @param worklist matches found are appended here for further traversal
     */
    private void collectInterfaceDeclarations(ClassEdge owner, String name, String desc, Deque<MethodEdge> worklist) {
        for (ClassEdge child : owner.children) {
            if (child.getMethod(name, desc).isPresent()) {
                continue;
            }

            for (ClassEdge interfaceEdge : child.interfaces) {
                interfaceEdge.findNearestMethod(name, desc)
                        .filter(MethodEdge::canBeOverridden)
                        .ifPresent(worklist::add);
            }

            collectInterfaceDeclarations(child, name, desc, worklist);
        }
    }

    /**
     * @return Whether it is possible to override this method (not static and not private)
     */
    public boolean canBeOverridden() {
        return !MethodUtil.hasAccess(methodNode, Opcodes.ACC_STATIC) && !MethodUtil.hasAccess(methodNode, Opcodes.ACC_PRIVATE);
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
