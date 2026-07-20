package me.exeos.asmplus.analysis.hierarchy.edge;

import org.objectweb.asm.tree.FieldNode;

/**
 * Represents a field in the analyzed class hierarchy.
 */
public record FieldEdge(ClassEdge owner, FieldNode fieldNode) {
}
