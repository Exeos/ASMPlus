package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class ClassResult {

    public final ClassNode classNode;
    public final List<MethodResult> methodResults;

    public ClassResult(ClassNode classNode, List<MethodResult> methodResults) {
        this.classNode = classNode;
        this.methodResults = methodResults;
    }
}
