package me.exeos.asmplus.remapper.mapper;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Optional;

public record MappingContext(ClassNode classNode, Optional<FieldNode> fieldNode, Optional<MethodNode> methodNode) {}
