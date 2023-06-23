package me.exeos.asmplus.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class ASMUtils implements Opcodes {

    /* ___ START: get x by / based on y ___ */

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

    /**
     * @return Random label from method
     */
    public static LabelNode getRandomLabel(MethodNode from) {
        List<LabelNode> labels = getMethodLabels(from);

        if (labels.isEmpty())
            return null;
        else
            return labels.get(RandomUtil.getInt(0, labels.size() - 1));
    }

    public static List<LabelNode> getMethodLabels(MethodNode from) {
        List<LabelNode> labels = new ArrayList<>();
        for (AbstractInsnNode insnNode : from.instructions) {
            if (insnNode instanceof LabelNode labelNode)
                labels.add(labelNode);
        }

        return labels;
    }

    public static AbstractInsnNode getMethodEnd(MethodNode from) {
        if (from.instructions.size() == 0)
            throw new IllegalStateException("Method has no instructions!");
        
        AbstractInsnNode end = from.instructions.get(from.instructions.size() - 1);
        while (end != null && end.getOpcode() < IRETURN || end.getOpcode() > RETURN) {
            end = end.getPrevious();
        }
        
        if (end == null)
            throw new IllegalStateException("Method does not return!");
        
        return end;
    }

    /* ___ START: value pushes___ */
    public static AbstractInsnNode getValuePush(Object value) {
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
    public static AbstractInsnNode getIConstPush(int value) {
        if (value < -1 || value > 5)
            throw new IllegalStateException("Value: " + value + " isn't in required bound: -1 to +5");

        return new InsnNode(ICONST_0 + value);
    }

    /**
     * @param value Int to be pushed. MUST BE EITHER 0 or 1!
     * @return Insn pushing the int
     */
    public static AbstractInsnNode getLConstPush(long value) {
        if (value != 0 && value != 1)
            throw new IllegalStateException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(LCONST_0 + (int) value);
    }

    /**
     * @param value Int to be pushed. MUST BE IN BOUND OF: 0 TO 2 !
     * @return Insn pushing the int
     */
    public static AbstractInsnNode getFConstPush(float value) {
        if (value < 0 || value > 2)
            throw new IllegalStateException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(FCONST_0 + (int) value);
    }
    /**
     * @param value Int to be pushed. MUST BE EITHER 0 or 1!
     * @return Insn pushing the int
     */
    public static AbstractInsnNode getDConstPush(double value) {
        if (value != 0 && value != 1)
            throw new IllegalStateException("Invalid value: " + value + ". Value must be 0 or 1");

        return new InsnNode(DCONST_0 + (int) value);
    }

    public static AbstractInsnNode getBytePush(byte value) {
        if (isIConstPush(ICONST_0 + value))
            return getIConstPush(value);

        return new IntInsnNode(BIPUSH, value);
    }

    public static AbstractInsnNode getIntPush(int value) {
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            return getShortPush((short) value);

        /* value is exceeds bound of byte and short */
        return new LdcInsnNode(value);
    }

    public static AbstractInsnNode getShortPush(short value) {
        if (isIConstPush(ICONST_0 + value))
            return getIConstPush(value);

        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
            return new IntInsnNode(BIPUSH, value);

        return new IntInsnNode(SIPUSH, value);
    }

    public static AbstractInsnNode getLongPush(long value) {
        if (value == 0 || value == 1)
            return getLConstPush(value);

        return new LdcInsnNode(value);
    }

    public static AbstractInsnNode getFloatPush(float value) {
        if (value >= 0 && value <= 2)
            return getFConstPush(value);

        return new LdcInsnNode(value);
    }

    public static AbstractInsnNode getDoublePush(double value) {
        if (value == 0 || value == 2)
            return getDConstPush(value);

        return new LdcInsnNode(value);
    }
    /* ___ END: value pushes ___ */


    /* ___ START: jumps ___ */

    /**
     * @param to Label to jump to
     * @return InsnList for a random jump
     */
    public InsnList getJump(LabelNode to) {
        return getJump(RandomUtil.getInt(IFEQ, GOTO), to);
    }

    /**
     * @param jumpOpcode Jump insn to use
     * @param to Label to jump to
     * @return InsnList for jump
     */
    public InsnList getJump(int jumpOpcode, LabelNode to) {
        InsnList jump = new InsnList();
        switch (jumpOpcode) {
            /* val == 0 */
            case IFEQ -> {
                jump.add(new InsnNode(ICONST_0));
                jump.add(new JumpInsnNode(IFEQ, to));
            }
            /* val != 0 */
            case IFNE -> {
                jump.add(new InsnNode(ICONST_1));
                jump.add(new JumpInsnNode(IFNE, to));
            }
            /* val < 0 */
            case IFLT -> {
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, -1)));
                jump.add(new JumpInsnNode(IFLT, to));
            }
            /* val >= 0 */
            case IFGE -> {
                jump.add(getIntPush(RandomUtil.getInt(0, Integer.MAX_VALUE)));
                jump.add(new JumpInsnNode(IFGE, to));
            }
            /* val > 0 */
            case IFGT -> {
                jump.add(getIntPush(RandomUtil.getInt(1, Integer.MAX_VALUE)));
                jump.add(new JumpInsnNode(IFGT, to));
            }
            /* val <= 0 */
            case IFLE -> {
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, 0)));
                jump.add(new JumpInsnNode(IFLE, to));
            }
            /* int0 == int1 */
            case IF_ICMPEQ -> {
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE)));
                jump.add(new InsnNode(DUP));
                jump.add(new JumpInsnNode(IF_ICMPEQ, to));
            }
            /* int0 != int1 */
            case IF_ICMPNE -> {
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, 0)));
                jump.add(getIntPush(RandomUtil.getInt(1, Integer.MAX_VALUE)));

                jump.add(new JumpInsnNode(IF_ICMPNE, to));
            }
            /* int0 < int 1*/
            case IF_ICMPLT -> {
                int less = RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE - 1);

                jump.add(getIntPush(less));
                jump.add(getIntPush(RandomUtil.getInt(less + 1, Integer.MAX_VALUE)));

                jump.add(new JumpInsnNode(IF_ICMPLT, to));
            }
            /* int0 >= int1 */
            case IF_ICMPGE -> {
                int more = RandomUtil.getInt(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);

                jump.add(getIntPush(more));
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, more)));
                jump.add(new JumpInsnNode(IF_ICMPGE, to));
            }
            /* int0 > int1 */
            case IF_ICMPGT -> {
                int more = RandomUtil.getInt(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);

                jump.add(getIntPush(more));
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, more - 1)));
                jump.add(new JumpInsnNode(IF_ICMPGT, to));
            }
            /* int0 <= int1 */
            case IF_ICMPLE -> {
                int less = RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE - 1);

                jump.add(getIntPush(less));
                jump.add(getIntPush(RandomUtil.getInt(less, Integer.MAX_VALUE)));

                jump.add(new JumpInsnNode(IF_ICMPLE, to));
            }
            /* object0 == object1 */
            case IF_ACMPEQ -> {
                jump.add(new TypeInsnNode(NEW, "java/lang/String"));
                jump.add(new InsnNode(DUP));

                jump.add(new JumpInsnNode(IF_ACMPEQ, to));
            }
            /* object0 != object1 */
            case IF_ACMPNE -> {
                jump.add(new TypeInsnNode(NEW, "java/lang/String"));
                jump.add(new TypeInsnNode(NEW, "java/lang/Integer"));

                jump.add(new JumpInsnNode(IF_ACMPNE, to));
            }
            /* direct jump to label */
            case GOTO -> jump.add(new JumpInsnNode(GOTO, to));
        }
        return jump;
    }
    /* ___ END: jumps ___ */

    /* ___ END: get x by / based on y ___ */

    /* ___ START: checks & conditions ___ */

    public static boolean isValuePush(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode || insnNode instanceof TypeInsnNode || isNumberPush(insnNode);
    }

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
    /* ___ END: num check ___*/

    public static boolean isString(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof String;
    }

    /* ___ END: checks & conditions ___ */
}
