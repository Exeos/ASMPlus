package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public record MethodResult(MethodNode methodNode, List<InsnResult> foundPatterns) { }