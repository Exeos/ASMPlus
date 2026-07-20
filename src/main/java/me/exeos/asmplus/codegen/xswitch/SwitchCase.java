package me.exeos.asmplus.codegen.xswitch;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

public class SwitchCase {

    public final int key;

    public final LabelNode caseStart;
    public final InsnList instructions;

    public SwitchCase(Object key, InsnList instructions) {
        this.instructions = instructions;
        caseStart = new LabelNode();

        switch (key.getClass().getName()) {
            case "java.lang.Integer":
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Character":
                this.key = (int) key;
                break;
            case "java.lang.String":
                this.key = key.hashCode();
                break;
            case "java.lang.Enum":
                this.key = ((Enum<?>) key).ordinal();
                break;
            default:
                throw new IllegalStateException("Key must be type of: char, byte, short, int, String or enum");
        }
    }
}