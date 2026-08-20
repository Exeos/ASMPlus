package me.exeos.asmplus.analysis.hierarchy.edge;

import me.exeos.asmplus.analysis.hierarchy.HierarchyAnalyzer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a class in the analyzed hierarchy.
 * <p>
 * A {@code ClassEdge} wraps an ASM {@link ClassNode} and exposes direct parent/child
 * relationships as well as the fields and methods declared by the class.
 * </p>
 */
public class ClassEdge {

    public final List<ClassEdge> interfaces = new ArrayList<>();
    public final ClassNode classNode;
    /**
     * A set of all the parents not found in the jar archive or provided dependencies
     */
    public final Set<String> unresolvedParents = new HashSet<>();
    /**
     * Direct parent classes and interfaces in the hierarchy.
     */
    public final List<ClassEdge> parents = new ArrayList<>();
    /**
     * Direct child classes and interfaces in the hierarchy.
     */
    public final List<ClassEdge> children = new ArrayList<>();
    /**
     * Fields declared directly on this class.
     */
    public final List<FieldEdge> fields = new ArrayList<>();
    /**
     * Methods declared directly on this class.
     */
    public final List<MethodEdge> methods = new ArrayList<>();
    public ClassEdge superClass;

    public ClassEdge(ClassNode classNode) {
        this.classNode = classNode;

        for (FieldNode fieldNode : classNode.fields) {
            fields.add(new FieldEdge(this, fieldNode));
        }

        for (MethodNode methodNode : classNode.methods) {
            methods.add(new MethodEdge(this, methodNode));
        }
    }

    /**
     * Check if this Class or any of its parents have unresolved parents
     *
     * @return true if this Class or any of its parents have unresolved parents
     */
    public boolean hasUnresolved() {
        if (!unresolvedParents.isEmpty()) {
            return true;
        }

        AtomicBoolean parentsHaveUnresolved = new AtomicBoolean(false);
        HierarchyAnalyzer.recurseParents(parents, parent -> {
            if (!parent.unresolvedParents.isEmpty()) {
                parentsHaveUnresolved.set(true);
            }
        });

        return parentsHaveUnresolved.get();
    }

    /**
     * Finds the class that declares the field matching the given field node.
     *
     * @param fieldNode The field node to match
     * @return The declaring class, if found
     */
    public Optional<ClassEdge> findDeclaringClassOfField(FieldNode fieldNode) {
        return findDeclaringClassOfField(fieldNode.name, fieldNode.desc);
    }

    /**
     * Finds the class that declares the field matching the given name and descriptor.
     *
     * @param name The field name
     * @param desc The field descriptor
     * @return The declaring class, if found
     */
    public Optional<ClassEdge> findDeclaringClassOfField(String name, String desc) {
        return findNearestField(name, desc).map(FieldEdge::owner);
    }

    /**
     * Finds the nearest field with the same name and descriptor as the given field node.
     *
     * @param fieldNode The field node to match
     * @return The nearest matching field, if one exists
     */
    public Optional<FieldEdge> findNearestField(FieldNode fieldNode) {
        return findNearestField(fieldNode.name, fieldNode.desc);
    }

    /**
     * Finds the nearest field with the given name and descriptor in this class hierarchy.
     *
     * @param name The field name
     * @param desc The field descriptor
     * @return The nearest matching field, if one exists
     */
    public Optional<FieldEdge> findNearestField(String name, String desc) {
        Optional<FieldEdge> firstLevel = getField(name, desc);
        if (firstLevel.isPresent()) {
            return firstLevel;
        }

        for (ClassEdge interfaceEdge : interfaces) {
            Optional<FieldEdge> viaInterface = interfaceEdge.findNearestField(name, desc);

            if (viaInterface.isPresent()) {
                return viaInterface;
            }
        }

        if (superClass != null) {
            Optional<FieldEdge> viaSuper = superClass.findNearestField(name, desc);
            if (viaSuper.isPresent()) {
                return viaSuper;
            }
        }

        return Optional.empty();
    }

    /**
     * Finds the nearest field with the given name in this class hierarchy.
     *
     * @param name The field name
     * @return The nearest matching field, if one exists
     */
    public Optional<FieldEdge> findNearestField(String name) {
        Optional<FieldEdge> firstLevel = getField(name);
        if (firstLevel.isPresent()) {
            return firstLevel;
        }

        for (ClassEdge interfaceEdge : interfaces) {
            Optional<FieldEdge> viaInterface = interfaceEdge.findNearestField(name);
            if (viaInterface.isPresent()) {
                return viaInterface;
            }
        }

        if (superClass != null) {
            Optional<FieldEdge> viaSuper = superClass.findNearestField(name);
            if (viaSuper.isPresent()) {
                return viaSuper;
            }
        }

        return Optional.empty();
    }

    /**
     * Discovers the method matching the given name and descriptor on this class or its
     * overridden ancestor methods, and passes each matched edge to the provided consumer.
     *
     * @param name     The method name
     * @param desc     The method descriptor
     * @param consumer Callback invoked for each discovered method edge
     */
    public void discoverMethods(String name, String desc, Consumer<MethodEdge> consumer) {
        Optional<MethodEdge> nearest = findNearestMethod(name, desc);
        if (nearest.isEmpty()) {
            return;
        }

        MethodEdge start = nearest.get();
        consumer.accept(start);

        HierarchyAnalyzer.recurseParents(start.owner().parents, parentEdge -> {
            parentEdge.getMethod(name, desc).ifPresent(parentMethod -> {
                if (start.overrides(parentMethod)) {
                    consumer.accept(parentMethod);
                }
            });
        });
    }

    /**
     * Finds the class that declares the method matching the given method node.
     *
     * @param methodNode The method node to match
     * @return The declaring class, if found
     */
    public Optional<ClassEdge> findDeclaringClassOfMethod(MethodNode methodNode) {
        return findDeclaringClassOfMethod(methodNode.name, methodNode.desc);
    }

    /**
     * Finds the class that declares the method matching the given name and descriptor.
     *
     * @param name The method name
     * @param desc The method descriptor
     * @return The declaring class, if found
     */
    public Optional<ClassEdge> findDeclaringClassOfMethod(String name, String desc) {
        return findNearestMethod(name, desc).map(MethodEdge::owner);
    }

    /**
     * Finds the class that is declaring the method root.
     *
     * @param methodNode The method node to match
     * @return The class declaring the method root, if found
     */
    public Set<MethodEdge> findTopDeclarations(MethodNode methodNode) {
        return findTopDeclarations(methodNode.name, methodNode.desc);
    }

    /**
     * Finds the class that is declaring the method root.
     *
     * @param name The method name
     * @param desc The method descriptor
     * @return The class declaring the method root, if found
     */
    public Set<MethodEdge> findTopDeclarations(String name, String desc) {
        Set<MethodEdge> topDeclarations = new HashSet<>();
        findNearestMethod(name, desc).ifPresent(nearest -> topDeclarations.addAll(nearest.findTopDeclarations()));

        return topDeclarations;
    }

    public Set<MethodEdge> findMethods(String name) {
        Set<MethodEdge> methods = getMethods(name);
        for (ClassEdge parent : parents) {
            methods.addAll(parent.findMethods(name));
        }

        return methods;
    }

    public Set<MethodEdge> findMethodsDownward(String name) {
        Set<MethodEdge> methods = getMethods(name);
        for (ClassEdge child : children) {
            methods.addAll(child.findMethodsDownward(name));
        }

        return methods;
    }

    public Set<MethodEdge> findAllMethods(String name) {
        Set<MethodEdge> methods = new HashSet<>();
        methods.addAll(findMethods(name));
        methods.addAll(findMethodsDownward(name));

        return methods;
    }

    public Set<MethodEdge> findMethods(String name, String desc) {
        Set<MethodEdge> methods = new HashSet<>();
        getMethod(name, desc).ifPresent(methods::add);
        for (ClassEdge parent : parents) {
            methods.addAll(parent.findMethods(name, desc));
        }

        return methods;
    }

    public Set<MethodEdge> findMethodsDownward(String name, String desc) {
        Set<MethodEdge> methods = new HashSet<>();
        getMethod(name, desc).ifPresent(methods::add);
        for (ClassEdge child : children) {
            methods.addAll(child.findMethodsDownward(name, desc));
        }

        return methods;
    }

    public Set<MethodEdge> findAllMethods(String name, String desc) {
        Set<MethodEdge> methods = new HashSet<>();
        methods.addAll(findMethods(name, desc));
        methods.addAll(findMethodsDownward(name, desc));

        return methods;
    }

    /**
     * Finds the nearest method with the same name and descriptor as the given method node.
     *
     * @param methodNode the method node to match
     * @return the nearest matching method, if one exists
     */
    public Optional<MethodEdge> findNearestMethod(MethodNode methodNode) {
        return findNearestMethod(methodNode.name, methodNode.desc);
    }

    /**
     * Finds the nearest method declaration with the given name and descriptor in this class hierarchy.
     * <p>
     * This first checks the current class, then recursively searches parent classes/interfaces
     * until a matching method is found.
     * </p>
     *
     * @param name The method name
     * @param desc The method descriptor
     * @return The nearest matching method, if one exists
     */
    public Optional<MethodEdge> findNearestMethod(String name, String desc) {
        Optional<MethodEdge> firstLevel = getMethod(name, desc);
        if (firstLevel.isPresent()) {
            return firstLevel;
        }

        if (superClass != null) {
            Optional<MethodEdge> viaSuper = superClass.findNearestMethod(name, desc);
            if (viaSuper.isPresent()) {
                return viaSuper;
            }
        }

        for (ClassEdge interfaceEdge : interfaces) {
            Optional<MethodEdge> viaInterface = interfaceEdge.findNearestMethod(name, desc);
            if (viaInterface.isPresent()) {
                return viaInterface;
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the field in this class matching the given field node.
     *
     * @param fieldNode The field node to match
     * @return The matching field, if one exists
     */
    public Optional<FieldEdge> getField(FieldNode fieldNode) {
        return getField(fieldNode.name, fieldNode.desc);
    }

    /**
     * Returns a field declared directly in this class, if one matches the given name and descriptor.
     *
     * @param name The field name
     * @param desc The field descriptor
     * @return The matching field, if one exists
     */
    public Optional<FieldEdge> getField(String name, String desc) {
        for (FieldEdge fieldEdge : fields) {
            if (fieldEdge.fieldNode().name.equals(name) && fieldEdge.fieldNode().desc.equals(desc)) {
                return Optional.of(fieldEdge);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a field declared directly in this class, if one matches the given name.
     *
     * @param name The field name
     * @return The matching field, if one exists
     */
    public Optional<FieldEdge> getField(String name) {
        for (FieldEdge fieldEdge : fields) {
            if (fieldEdge.fieldNode().name.equals(name)) {
                return Optional.of(fieldEdge);
            }
        }

        return Optional.empty();
    }

    public Set<MethodEdge> getOverrideGroup(MethodNode methodNode) {
        Set<MethodEdge> overrideGroup = new HashSet<>();
        getMethod(methodNode).ifPresent(methodEdge -> overrideGroup.addAll(methodEdge.getOverrideGroup()));

        return overrideGroup;
    }

    public Set<MethodEdge> getTopDeclarations(MethodNode methodNode) {
        return getTopDeclarations(methodNode.name, methodNode.desc);
    }

    public Set<MethodEdge> getTopDeclarations(String name, String desc) {
        Set<MethodEdge> topDeclarations = new HashSet<>();
        getMethod(name, desc).ifPresent(methodEdge -> topDeclarations.addAll(methodEdge.findTopDeclarations()));

        return topDeclarations;
    }

    /**
     * Returns the method in this class matching the given method node's name and descriptor.
     *
     * @param methodNode the method node to match
     * @return the matching method, if one exists
     */
    public Optional<MethodEdge> getMethod(MethodNode methodNode) {
        return getMethod(methodNode.name, methodNode.desc);
    }

    /**
     * Returns a method declared directly in this class, if one matches the given name and descriptor.
     *
     * @param name The method name
     * @param desc The method descriptor
     * @return The matching method, if one exists
     */
    public Optional<MethodEdge> getMethod(String name, String desc) {
        for (MethodEdge methodEdge : methods) {
            if (methodEdge.methodNode().name.equals(name) && methodEdge.methodNode().desc.equals(desc)) {
                return Optional.of(methodEdge);
            }
        }

        return Optional.empty();
    }

    public Set<MethodEdge> getMethods(String name) {
        return methods
                .stream()
                .filter(methodEdge -> methodEdge.methodNode().name.equals(name))
                .collect(Collectors.toSet());
    }

    public List<MethodEdge> getMethods() {
        return methods;
    }
}
