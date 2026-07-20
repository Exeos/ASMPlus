package me.exeos.asmplus.jar;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

public class JarArchive {

    private Map<String, ClassNode> classes;
    private Map<String, ClassNode> dependencies;
    private Map<String, byte[]> resources;
    private Manifest manifest;

    public JarArchive(Map<String, ClassNode> classes, Map<String, ClassNode> dependencies, Map<String, byte[]> resources, Manifest manifest) {
        this.classes = classes;
        this.dependencies = dependencies;
        this.resources = resources;
        this.manifest = manifest;
    }

    public boolean isDependency(String name) {
        return !classes.containsKey(name) && dependencies.containsKey(name);
    }

    public Optional<ClassNode> getClassNode(String className) {
        return getClassNode(className, true);
    }

    public Optional<ClassNode> getClassNode(String className, boolean includeDependencies) {
        if (classes.containsKey(className)) {
            return Optional.of(classes.get(className));
        }

        if (includeDependencies && dependencies.containsKey(className)) {
            return Optional.of(dependencies.get(className));
        }

        System.out.println("Class " + className + " not found in archive. Missing some dependencies?");
        return Optional.empty();
    }

    public Map<String, ClassNode> getClasses() {
        return classes;
    }

    public Map<String, ClassNode> getClassesAndDependencies() {
        Map<String, ClassNode> combined = new HashMap<>(getClasses());
        getDependencies().forEach(combined::putIfAbsent);

        return combined;
    }

    public void setClasses(Map<String, ClassNode> classes) {
        this.classes = classes;
    }

    public Map<String, ClassNode> getDependencies() {
        return dependencies;
    }

    public Map<String, byte[]> getResources() {
        return resources;
    }

    public Manifest getManifest() {
        return manifest;
    }
}
