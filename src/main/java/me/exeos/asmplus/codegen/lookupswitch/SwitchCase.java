package me.exeos.asmplus.codegen.lookupswitch;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.List;

public class SwitchCase {

    public final int key;

    public final LabelNode caseStart;
    public final List<AbstractInsnNode> instructions;

    public SwitchCase(Object key, List<AbstractInsnNode> instructions) {
        this.instructions = instructions;
        caseStart = new LabelNode();

        switch (key.getClass().getName()) {
            case "java.lang.Integer", "java.lang.Byte", "java.lang.Short", "java.lang.Character" -> this.key = (int) key;
            case "java.lang.String" -> this.key = key.hashCode();
            case "java.lang.Enum" -> this.key = ((Enum<?>) key).ordinal();
            default -> throw new IllegalStateException("Key must be type of: char, byte, short, int, String or enum");
        }
    }
}
