package me.exeos.asmplus.pattern;

import org.objectweb.asm.tree.AbstractInsnNode;

public interface PatternParts {

    /**
     * Use if part is unknown
     */
    int P_ANY = -2;
    /**
     * Will skip to part in pattern after itself
     */
    int P_SKIPTO = -3;
    /**
     * Any number push NOTE: this does not include method invocations
     */
    int P_NUMBER = -4;
    int P_STRING = -5;
    int P_VALUE = -6;


    /**
     * Because the PatternScanner doesn't support Objects it uses these ints
     */
    int INSN = AbstractInsnNode.INSN + 300;
    int INT_INSN = AbstractInsnNode.INT_INSN + 300;
    int VAR_INSN = AbstractInsnNode.VAR_INSN + 300;
    int TYPE_INSN = AbstractInsnNode.TYPE_INSN + 300;
    int FIELD_INSN = AbstractInsnNode.FIELD_INSN + 300;
    int METHOD_INSN = AbstractInsnNode.METHOD_INSN + 300;
    int INVOKE_DYNAMIC_INSN = AbstractInsnNode.INVOKE_DYNAMIC_INSN + 300;
    int JUMP_INSN = AbstractInsnNode.JUMP_INSN + 300;
    int LABEL = AbstractInsnNode.LABEL + 300;
    int LDC_INSN = AbstractInsnNode.LDC_INSN + 300;
    int IINC_INSN = AbstractInsnNode.IINC_INSN + 300;
    int TABLESWITCH_INSN = AbstractInsnNode.TABLESWITCH_INSN + 300;
    int LOOKUPSWITCH_INSN = AbstractInsnNode.LOOKUPSWITCH_INSN + 300;
    int MULTIANEWARRAY_INSN = AbstractInsnNode.MULTIANEWARRAY_INSN + 300;
    int FRAME = AbstractInsnNode.FRAME + 300;
    int LINE = AbstractInsnNode.LINE + 300;
}