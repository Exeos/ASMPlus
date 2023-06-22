package me.exeos.asmplus.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class ASMUtil implements Opcodes {

    /* ___get x by / based on y___ */

    /**
     * Get x next insn after @current insn
     *
     * @param current    InsnNode to start stepping from.
     * @param steps      Steps to take from start
     * @return           x next insn after current
     */
    public static AbstractInsnNode getNext(AbstractInsnNode current, int steps) {
        for (int i = 0; i < steps; i++) {
            if (current.getNext() == null)
                return null;

            current = current.getNext();
        }
        return current;
    }

    /**
     * Get x prev insn after @current insn
     *
     * @param current    InsnNode to start stepping from.
     * @param steps      Steps to revert from start
     * @return           x prev insn after current
     */
    public static AbstractInsnNode getPrev(AbstractInsnNode current, int steps) {
        for (int i = 0; i < steps; i++) {
            if (current.getPrevious() == null)
                return null;

            current = current.getPrevious();
        }
        return current;
    }

    /* ___checks & conditions___ */

    /* ___num check start___*/
    public static boolean isNumberPush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) ||
                isBytePush(insnNode) ||
                isShortPush(insnNode) ||
                isIntPush(insnNode) ||
                isLongPush(insnNode) ||
                isFloatPush(insnNode) ||
                isDoublePush(insnNode);
    }

    public static boolean isLowNumPush(int opcode) {
        return opcode >= ICONST_M1 && opcode <= ICONST_5;
    }

    public static boolean isBytePush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) || insnNode.getOpcode() == BIPUSH;
    }

    public static boolean isShortPush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) ||
                (insnNode instanceof IntInsnNode intInsnNode && intInsnNode.operand >= Short.MIN_VALUE && intInsnNode.operand <= Short.MAX_VALUE);
    }

    public static boolean isIntPush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) || insnNode instanceof IntInsnNode ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Integer);
    }

    public static boolean isLongPush(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() == LCONST_0 || insnNode.getOpcode() == LCONST_1 ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Long);
    }

    public static boolean isFloatPush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) ||
                (insnNode instanceof IntInsnNode intInsnNode && intInsnNode.operand >= Float.MIN_VALUE) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Float);
    }

    public static boolean isDoublePush(AbstractInsnNode insnNode) {
        return isLowNumPush(insnNode.getOpcode()) ||
                (insnNode instanceof IntInsnNode intInsnNode && intInsnNode.operand >= Double.MIN_VALUE) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Double);
    }

    /* ___num check end___*/
}
