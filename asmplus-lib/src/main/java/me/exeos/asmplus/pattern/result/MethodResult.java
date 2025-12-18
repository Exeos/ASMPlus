package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.MethodNode;

import java.util.List;

@Deprecated
public class MethodResult {

    public final MethodNode methodNode;
    public final List<InsnResult> foundPatterns;

    public MethodResult(MethodNode methodNode, List<InsnResult> foundPatterns) {
        this.methodNode = methodNode;
        this.foundPatterns = foundPatterns;
    }
}