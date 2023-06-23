package me.exeos.asmplus.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class ASMUtil implements Opcodes {

    /* ___START: get x by / based on y___ */

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
     * @return           x prev insn after current or null if not able to reach
     */
    public static AbstractInsnNode getPrev(AbstractInsnNode current, int steps) {
        for (int i = 0; i < steps; i++) {
            if (current.getPrevious() == null)
                return null;

            current = current.getPrevious();
        }
        return current;
    }

    /* ___START: value pushes___ */
    public AbstractInsnNode pushValue(Object value) {
        return switch (value.getClass().getSimpleName()) {
            case "Byte" -> getBytePush((byte) value);
            case "Integer" -> getIntPush((int) value);
            case "Long" -> getLongPush((long) value);
            case "Short" -> getShortPush((short) value);
            case "Float" -> getFloatPush((float) value);
            case "Double" -> getDoublePush((double) value);
            case "String" -> new LdcInsnNode(value);
            default -> throw new IllegalStateException("Unexpected value type: " + value.getClass().getSimpleName());
        };
    }

    /**
     * @param value Int to be pushed. MUST BE IN BOUND OF: -1 TO +5 !
     * @return Insn pushing the int
     */
    public AbstractInsnNode getIConstPush(int value) {
        if (value < -1 || value > 5)
            throw new IllegalArgumentException("Value: " + value + " isn't in required bound: -1 to +5");

        return new InsnNode(ICONST_0 + value);
    }

    /**
     * @param value Int to be pushed. MUST BE EITHER 0 or 1!
     * @return Insn pushing the int
     */
    public AbstractInsnNode getLConstPush(long value) {
        if (value != 0 && value != 1)
            throw new IllegalArgumentException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(LCONST_0 + (int) value);
    }

    /**
     * @param value Int to be pushed. MUST BE IN BOUND OF: 0 TO 2 !
     * @return Insn pushing the int
     */
    public AbstractInsnNode getFConstPush(float value) {
        if (value < 0 || value > 2)
            throw new IllegalArgumentException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(FCONST_0 + (int) value);
    }

    /**
     * @param value Int to be pushed. MUST BE EITHER 0 or 1!
     * @return Insn pushing the int
     */
    public AbstractInsnNode getDConstPush(double value) {
        if (value != 0 && value != 1)
            throw new IllegalArgumentException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(DCONST_0 + (int) value);
    }

    public AbstractInsnNode getBytePush(byte value) {
        if (isIConstPush(ICONST_0 + value))
            return getIConstPush(value);

        return new IntInsnNode(BIPUSH, value);
    }

    public AbstractInsnNode getIntPush(int value) {
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            return getShortPush((short) value);

        /* value is exceeds bound of byte and short */
        return new LdcInsnNode(value);
    }

    public AbstractInsnNode getShortPush(short value) {
        if (isIConstPush(ICONST_0 + value))
            return getIConstPush(value);

        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
            return new IntInsnNode(BIPUSH, value);

        return new IntInsnNode(SIPUSH, value);
    }

    public AbstractInsnNode getLongPush(long value) {
        if (value == 0 || value == 1)
            return getLConstPush(value);

        return new LdcInsnNode(value);
    }

    public AbstractInsnNode getFloatPush(float value) {
        if (value >= 0 && value <= 2)
            return getFConstPush(value);

        return new LdcInsnNode(value);
    }

    public AbstractInsnNode getDoublePush(double value) {
        if (value == 0 || value == 2)
            return getDConstPush(value);

        return new LdcInsnNode(value);
    }
    /* ___END: value pushes___ */

    /* __END: get x by / based on y___ */

    /* ___START: checks & conditions___ */

    /* ___STAR: num check start___*/
    public static boolean isNumberPush(AbstractInsnNode insnNode) {
        return isNumConstPush(insnNode.getOpcode()) ||
                isDoublePush(insnNode) ||
                isBytePush(insnNode) ||
                isFloatPush(insnNode) ||
                isIntPush(insnNode) ||
                isLongPush(insnNode) ||
                isShortPush(insnNode);
    }

    /**
     * @param opcode Insn's opcode
     * @return if insn pushes a CONST and if that CONST is a number
     */
    public static boolean isNumConstPush(int opcode) {
        return  isDConstPush(opcode) ||
                isFConstPush(opcode) ||
                isIConstPush(opcode) ||
                isLConstPush(opcode);
    }

    public static boolean isDConstPush(int opcode) {
        return opcode == DCONST_0 || opcode == DCONST_1;
    }

    public static boolean isFConstPush(int opcode) {
        return opcode >= FCONST_0 && opcode <= FCONST_2;
    }

    public static boolean isIConstPush(int opcode) {
        return opcode >= ICONST_M1 && opcode <= ICONST_5;
    }

    public static boolean isLConstPush(int opcode) {
        return opcode == LCONST_0 || opcode == LCONST_1;
    }

    public static boolean isBytePush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode()) || insnNode.getOpcode() == BIPUSH;
    }

    public static boolean isDoublePush(AbstractInsnNode insnNode) {
        return isDConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Double);
    }

    public static boolean isFloatPush(AbstractInsnNode insnNode) {
        return isFConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Float);
    }

    public static boolean isIntPush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode()) || insnNode instanceof IntInsnNode ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Integer);
    }

    public static boolean isLongPush(AbstractInsnNode insnNode) {
        return isLConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Long);
    }

    public static boolean isShortPush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof IntInsnNode intInsnNode && intInsnNode.operand >= Short.MIN_VALUE && intInsnNode.operand <= Short.MAX_VALUE);
    }
    /* ___END: num check___*/

    /* ___END: checks & conditions___ */
}
