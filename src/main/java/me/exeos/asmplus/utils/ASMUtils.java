package me.exeos.asmplus.utils;

import me.exeos.asmplus.JarLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ASMUtils implements Opcodes {

    public static JarLoader currentJar = null;
    private static AbstractInsnNode insnNode;

    /* ___ START: Actions ___*/

    public static void removeInstructions(InsnList remove, MethodNode from) {
        for (AbstractInsnNode insnNode : remove) {
            from.instructions.remove(insnNode);
        }
    }

    public static void setOpcode(AbstractInsnNode insnNode, int opcode) throws IllegalAccessException {
        for (Field field : insnNode.getClass().getFields()) {
            if (!field.getName().equals("opcode"))
                continue;

            field.setAccessible(true);
            field.set(field, opcode);
            break;
        }
    }
    /* ___ END: Actions ___ */

    /* ___ START: get x by / based on y ___ */

    public static ClassNode getClass(String name) {
        if (currentJar == null)
            throw new IllegalStateException("Current jar can't be null");

        return getClass(currentJar, name);
    }

    public static ClassNode getClass(JarLoader jar, String name) {
        return jar.classes.get(name);
    }

    public static FieldNode getField(String owner, String name, String desc) {
        return getField(owner, name, desc, null);
    }

    public static FieldNode getField(String owner, String name, String desc, String signature) {
        if (currentJar == null)
            throw new IllegalStateException("Current jar can't be null");

        return getField(currentJar, owner, name, desc, signature);
    }

    public static FieldNode getField(JarLoader jar, String owner, String name, String desc) {
        return getField(jar, owner, name, desc, null);
    }

    public static FieldNode getField(JarLoader jar, String owner, String name, String desc, String signature) {
        ClassNode classNode = getClass(owner);
        if (classNode == null)
            throw new IllegalStateException("Owner of field can't be null");
        else
            return getField(classNode, name, desc, signature);
    }

    public static FieldNode getField(ClassNode classNode, String name, String desc, String signature) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals(name) && fieldNode.desc.equals(desc) && (signature == null || fieldNode.signature.equals(signature)))
                return fieldNode;
        }

        return null;
    }

    public static MethodNode getMethod(ClassNode owner, String name, String desc) {
        return getMethod(owner, null, name, desc, null);
    }

    public static MethodNode getMethod(ClassNode owner, Integer access, String name, String desc) {
        return getMethod(owner, access, name, desc, null);
    }

    public static MethodNode getMethod(ClassNode owner, String name, String desc, String signature) {
        return getMethod(owner, null, name, desc, signature);
    }

    public static MethodNode getMethod(ClassNode owner, Integer access, String name, String desc, String signature) {
        for (MethodNode methodNode : owner.methods) {
            if (methodNode.name.equals(name) && methodNode.desc.equals(desc) && (access == null || access == methodNode.access) &&
                    (signature == null || signature.equals(methodNode.signature)))
                return methodNode;
        }

        return null;
    }

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
            throw new IllegalStateException("Method has no instructions");
        
        AbstractInsnNode end = from.instructions.get(from.instructions.size() - 1);
        while (end != null && end.getOpcode() < IRETURN || end.getOpcode() > RETURN) {
            end = end.getPrevious();
        }
        
        if (end == null)
            throw new IllegalStateException("Method does not return");
        
        return end;
    }

    /* ___ START: value by insn ___ */

    public static Object getValue(AbstractInsnNode insnNode) {
        if (isValuePush(insnNode)) {
            if (isIntPush(insnNode))
                return getIntValue(insnNode);
            if (isBytePush(insnNode))
                return getByteValue(insnNode);
            if (isShortPush(insnNode))
                return getShortValue(insnNode);
            if (isLongPush(insnNode))
                return getLongValue(insnNode);
            if (isFloatPush(insnNode))
                return getFloatValue(insnNode);
            if (isDoublePush(insnNode))
                return getDoubleValue(insnNode);
            if (isString(insnNode))
                return getStringValue(insnNode);
        }
        throw new IllegalStateException("Instruction does not push a value");
    }

    public static int getIntValue(AbstractInsnNode insnNode) {
        if (isIConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 3;

        return switch (insnNode.getOpcode()) {
            case BIPUSH, SIPUSH -> ((IntInsnNode) insnNode).operand;
            case LDC -> {
                if (insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof Integer)
                    yield (int) ldcInsn.cst;

                throw new IllegalStateException("Instruction doesn't represent int");
            }
            default -> throw new IllegalStateException("Instruction doesn't represent int");
        };
    }

    public static byte getByteValue(AbstractInsnNode insnNode) {
        if (isIConstPush(insnNode.getOpcode()))
            return (byte) (insnNode.getOpcode() - 3);

        if (insnNode.getOpcode() == BIPUSH)
            return (byte) ((IntInsnNode) insnNode).operand;

        throw new IllegalStateException("Instruction doesn't represent byte");
    }

    public static short getShortValue(AbstractInsnNode insnNode) {
        if (isIConstPush(insnNode.getOpcode()))
            return (short) (insnNode.getOpcode() - 3);

        if (insnNode.getOpcode() == SIPUSH)
            return (short) ((IntInsnNode) insnNode).operand;

        throw new IllegalStateException("Instruction doesn't represent short");
    }

    public static long getLongValue(AbstractInsnNode insnNode) {
        if (isLConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 9;

        if (insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof Long)
            return (long) ldcInsn.cst;

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static double getDoubleValue(AbstractInsnNode insnNode) {
        if (isDConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 14;

        if (insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof Double)
            return (double) ldcInsn.cst;

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static float getFloatValue(AbstractInsnNode insnNode) {
        if (isFConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 11;

        if (insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof Float)
            return (float) ldcInsn.cst;

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static String getStringValue(AbstractInsnNode insnNode) {
        if (insnNode instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof String)
            return (String) ldcInsn.cst;

        throw new IllegalStateException("Instruction doesn't represent long");
    }
    /* ___ END: value by insn ___*/

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
    public static InsnList getJump(LabelNode to) {
        return getJump(RandomUtil.getInt(IFEQ, GOTO), to);
    }

    /**
     * @param jumpOpcode Jump insn to use
     * @param to Label to jump to
     * @return InsnList for jump
     */
    public static InsnList getJump(int jumpOpcode, LabelNode to) {
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
