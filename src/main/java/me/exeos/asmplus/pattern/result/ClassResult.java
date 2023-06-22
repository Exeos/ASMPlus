package me.exeos.asmplus.pattern.result;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public record ClassResult(ClassNode classNode, List<MethodResult> methodResults) { }
