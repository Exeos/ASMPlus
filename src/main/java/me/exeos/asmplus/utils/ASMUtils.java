package me.exeos.asmplus.utils;

import me.exeos.asmplus.JarLoader;
import me.exeos.asmplus.codegen.code.Jump;
import me.exeos.asmplus.descarg.DescArg;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ASMUtils implements Opcodes {

    private static JarLoader currentJar = null;

    public static void setJar(JarLoader jar) {
        currentJar = jar;
    }

    /* ___ START: Actions ___*/

    public static void addInstructions(AbstractInsnNode[] add, MethodNode from) {
        removeInstructions(Arrays.asList(add), from);
    }

    public static void addInstructions(List<AbstractInsnNode> add, MethodNode from) {
        for (AbstractInsnNode toAdd : add) {
            from.instructions.add(toAdd);
        }
    }

    public static void removeInstructions(AbstractInsnNode[] remove, MethodNode from) {
        removeInstructions(Arrays.asList(remove), from);
    }

    /**
     * This may look stupid but is necessary to safely remove instructions
     */
    public static void removeInstructions(List<AbstractInsnNode> remove, MethodNode from) {
        for (AbstractInsnNode toRemove : remove) {
            if (isPresent(toRemove, from))
                from.instructions.remove(toRemove);
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

    public static FieldNode getField(FieldInsnNode fieldInsn) {
        return getField(fieldInsn.owner, fieldInsn.name, fieldInsn.desc);
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
     * @return array of args from method. coming from " (BIZLme/exeos/Main;S)V " this may look like:
     * B | false
     * I | false
     * Z | false
     * me/exeos/Main | true
     * S | false
     */
    public static DescArg[] getMethodArgs(MethodNode methodNode) {
        return getMethodArgs(methodNode.desc);
    }

    public static DescArg[] getMethodArgs(String methodDesc) {
        ArrayList<DescArg> args = new ArrayList<>();

        boolean phrasing = false;
        boolean phrasingArg = false;
        StringBuilder argBuilder = new StringBuilder();

        for (char c : methodDesc.toCharArray()) {
            if (c == '(')  {
                phrasing = true;
                continue;
            }
            if (c == ')')  {
                break;
            }
            if (!phrasing) {
                continue;
            }

            if (c == 'L') {
                phrasingArg = true;
                continue;
            }
            if (c == ';') {
                args.add(new DescArg(argBuilder.toString(), true));
                argBuilder = new StringBuilder();
                phrasingArg = false;
                continue;
            }
            if (phrasingArg) {
                argBuilder.append(c);
            } else
                args.add(new DescArg(String.valueOf(c), false));
        }

        return args.toArray(args.toArray(new DescArg[0]));
    }

    /**
     * This is here because InsnList is stupid and buggy
     * @return converted to List<AbstractInsnNode>
     */

    public static List<AbstractInsnNode> convertToJList(InsnList insnList) {
        List<AbstractInsnNode> converted = new ArrayList<>();
        for (AbstractInsnNode insnNode : insnList) {
            converted.add(insnNode);
        }

        return converted;
    }

    /**
     * This is here because InsnList is stupid and buggy
     * @return converted to InsnList
     */

    public static InsnList convertToIList(List<AbstractInsnNode> list) {
        InsnList converted = new InsnList();
        for (AbstractInsnNode insnNode : list) {
            converted.add(insnNode);
        }

        return converted;
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
            if (insnNode instanceof LabelNode) {
                labels.add((LabelNode) insnNode);
            }
        }

        return labels;
    }

    public static AbstractInsnNode getMethodEnd(MethodNode from) {
        if (from.instructions.size() == 0)
            return null;
        
        AbstractInsnNode end = from.instructions.get(from.instructions.size() - 1);
        if (end != null) {
            while (end.getOpcode() < IRETURN || end.getOpcode() > RETURN) {
                end = end.getPrevious();

                if (end == null)
                    break;
            }
        }
        
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

        switch (insnNode.getOpcode()) {
            case BIPUSH:
            case SIPUSH:
                 return ((IntInsnNode) insnNode).operand;
            case LDC:
                if (insnNode instanceof LdcInsnNode) {
                    LdcInsnNode ldcInsn = (LdcInsnNode) insnNode;
                    if (ldcInsn.cst instanceof Integer) {
                        return (int)ldcInsn.cst;
                    }
                }
                throw new IllegalStateException("Instruction doesn't represent int");
            default:
                throw new IllegalStateException("Instruction doesn't represent int");
        }
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

        if (insnNode instanceof LdcInsnNode) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insnNode;
            if (ldcInsn.cst instanceof Long) {
                return (long) ldcInsn.cst;
            }
        }

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static double getDoubleValue(AbstractInsnNode insnNode) {
        if (isDConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 14;

        if (insnNode instanceof LdcInsnNode) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insnNode;
            if (ldcInsn.cst instanceof Double) {
                return (double) ldcInsn.cst;
            }
        }

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static float getFloatValue(AbstractInsnNode insnNode) {
        if (isFConstPush(insnNode.getOpcode()))
            return insnNode.getOpcode() - 11;

        if (insnNode instanceof LdcInsnNode) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insnNode;
            if (ldcInsn.cst instanceof Float) {
                return (float) ldcInsn.cst;
            }
        }

        throw new IllegalStateException("Instruction doesn't represent long");
    }

    public static String getStringValue(AbstractInsnNode insnNode) {
        if (insnNode instanceof LdcInsnNode) {
            LdcInsnNode ldcInsn = (LdcInsnNode) insnNode;
            if (ldcInsn.cst instanceof String) {
                return (String) ldcInsn.cst;
            }
        }

        throw new IllegalStateException("Instruction doesn't represent long");
    }
    /* ___ END: value by insn ___*/

    /* ___ START: value pushes___ */

    public static AbstractInsnNode getValuePush(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Byte":
                return getBytePush((byte) value);
            case "Integer":
                return getIntPush((int) value);
            case "Long":
                return getLongPush((long) value);
            case "Short":
                return getShortPush((short) value);
            case "Float":
                return getFloatPush((float) value);
            case "Double":
                return getDoublePush((double) value);
            case "String":
                return new LdcInsnNode(value);
            default:
                throw new IllegalStateException("Unexpected value type: " + value.getClass().getSimpleName());
        }
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
     * @return List<AbstractInsnNode> for a random jump
     */
    public static List<AbstractInsnNode> getJumpInsns(LabelNode to) {
        return getJumpInsns(RandomUtil.getInt(IFEQ, IF_ICMPLT), to);
    }

    /**
     * @param jumpOpcode Jump insn to use
     * @param to Label to jump to
     * @return List<AbstractInsnNode></> for jump
     */
    public static List<AbstractInsnNode> getJumpInsns(int jumpOpcode, LabelNode to) {
        return getJump(jumpOpcode, to).getJump();
    }

    /**
     * @param to Label to jump to
     * @return List<AbstractInsnNode> for a random jump
     */
    public static Jump getJump(LabelNode to) {
        return getJump(RandomUtil.getInt(IFEQ, IF_ICMPLT), to);
    }

    /**
     * @param jumpOpcode Jump insn to use
     * @param to Label to jump to
     * @return List<AbstractInsnNode></> for jump
     */
    public static Jump getJump(int jumpOpcode, LabelNode to) {
        if (true) {
//        if (jumpOpcode >= IF_ICMPGE) {
            jumpOpcode = GOTO;
        }
        Jump jump = new Jump();
        switch (jumpOpcode) {
            /* val == 0 */
            case IFEQ:
                jump.add(new InsnNode(ICONST_0));
                break;
            /* val != 0 */
            case IFNE:
                jump.add(new InsnNode(ICONST_1));
                break;
            /* val < 0 */
            case IFLT:
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, -1)));
                break;
            /* val >= 0 */
            case IFGE:
                jump.add(getIntPush(RandomUtil.getInt(0, Integer.MAX_VALUE)));
                break;
            /* val > 0 */
            case IFGT:
                jump.add(getIntPush(RandomUtil.getInt(1, Integer.MAX_VALUE)));
                break;
            /* val <= 0 */
            case IFLE:
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, 0)));
                break;
            /* int0 == int1 */
            case IF_ICMPEQ:
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE)));
                jump.add(new InsnNode(DUP));
                break;
            /* int0 != int1 */
            case IF_ICMPNE:
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, 0)));
                jump.add(getIntPush(RandomUtil.getInt(1, Integer.MAX_VALUE)));
                break;
            /* int0 < int 1*/
            case IF_ICMPLT:
            {
                int less = RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE - 10);

                jump.add(getIntPush(RandomUtil.getInt(less + 1, Integer.MAX_VALUE)));
                jump.add(getIntPush(less));
            }
            break;
            /* int0 >= int1 */
            case IF_ICMPGE: // bis hier
            {
                int more = RandomUtil.getInt(0, Integer.MAX_VALUE);

                jump.add(getIntPush(more));
                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, more)));
            }
            break;
            /* int0 > int1 */
            case IF_ICMPGT:
            {
                int more = RandomUtil.getInt(Integer.MIN_VALUE + 10, Integer.MAX_VALUE);

                jump.add(getIntPush(RandomUtil.getInt(Integer.MIN_VALUE, more - 1)));
                jump.add(getIntPush(more));
            }
            break;
            /* int0 <= int1 */
            case IF_ICMPLE:
            {
                int less = RandomUtil.getInt(Integer.MIN_VALUE, Integer.MAX_VALUE - 1);

                jump.add(getIntPush(less));
                jump.add(getIntPush(RandomUtil.getInt(less, Integer.MAX_VALUE)));
            }
            break;
            /* object0 == object1 */
            case IF_ACMPEQ:
                jump.add(new TypeInsnNode(NEW, "java/lang/String"));
                jump.add(new InsnNode(DUP));
                break;
            /* object0 != object1 */
            case IF_ACMPNE:
                jump.add(new TypeInsnNode(NEW, "java/lang/String"));
                jump.add(new TypeInsnNode(NEW, "java/lang/Integer"));
                break;
            /* direct jump to label */
            case GOTO:
                /* Goto is the default opcode of jump */
                break;
            default:
                System.out.println("This branch should never be reached. Opcode: " + jumpOpcode);
        }

        return jump.setOpcode(jumpOpcode).setLabel(to);
    }

    /* ___ END: jumps ___ */

    /**
     * @param debugMessage String in LDC
     * @return LDC Insn with debugMessage,which gets poped right after
     */

    public static List<AbstractInsnNode> getDebugInsn(String debugMessage) {
        ArrayList<AbstractInsnNode> insnNodes = new ArrayList<>();
        insnNodes.add(new LdcInsnNode(debugMessage));
        insnNodes.add(new InsnNode(POP));

        return insnNodes;
    }

    public static List<AbstractInsnNode> getCheckCastMessage(String message) {
        return getCheckCastMessage(message, true);
    }

    public static List<AbstractInsnNode> getCheckCastMessage(String message, boolean pop) {
        ArrayList<AbstractInsnNode> insns = new ArrayList<>();

        insns.add(new InsnNode(ACONST_NULL));
        insns.add(new TypeInsnNode(CHECKCAST, "L" + message.replace(" ", "") + ";"));
        if (pop)
            insns.add(new InsnNode(POP));

        return insns;
    }

    /* ___ END: get x by / based on y ___ */

    /**
     * @return New clean method returning the same type as arg methodNode
     */
    public static MethodNode getCleanMethod(MethodNode methodNode) {
        AbstractInsnNode end = getMethodEnd(methodNode);
        if (end == null)
            throw new IllegalArgumentException("Invalid method: " + methodNode.name);

        methodNode.instructions.clear();
        methodNode.tryCatchBlocks.clear();
        methodNode.localVariables.clear();
        methodNode.maxLocals = 0;
        methodNode.maxStack = 1;

        List<AbstractInsnNode> clean = new ArrayList<>();

        if (end.getOpcode() != RETURN) {
            switch (end.getOpcode()) {
                case IRETURN:
                    clean.add(getIConstPush(0));
                    break;
                case LRETURN:
                    clean.add(getLConstPush(0));
                    break;
                case FRETURN:
                    clean.add(getFConstPush(0));
                    break;
                case DRETURN:
                    clean.add(getDConstPush(0));
                    break;
                case ARETURN:
                    clean.add(new InsnNode(ACONST_NULL));
                    break;
                default: throw new IllegalArgumentException("Invalid return type: " + end.getOpcode());
            }
        }
        clean.add(end);
        addInstructions(clean, methodNode);

        return methodNode;
    }

    /**
     * Returns if insn is present in method, this can be used to avoid concurrent-modification exceptions or nullpointer exceptions when removing insns
     * @param insnNode insn to check for
     * @param in method that should contai insn
     * @return present or nut
     */
    public static boolean isPresent(AbstractInsnNode insnNode, MethodNode in) {
        return in.instructions.indexOf(insnNode) >= 0;
    }

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
        return isDConstPush(insnNode.getOpcode()) || (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof Double);
    }

    public static boolean isFloatPush(AbstractInsnNode insnNode) {
        return isFConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof Float);
    }

    public static boolean isIntPush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode()) || insnNode instanceof IntInsnNode ||
                (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof Integer);
    }

    public static boolean isLongPush(AbstractInsnNode insnNode) {
        return isLConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof Long);
    }

    public static boolean isShortPush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode()) ||
                (insnNode instanceof IntInsnNode && ((IntInsnNode) insnNode).operand >= Short.MIN_VALUE && ((IntInsnNode) insnNode).operand <= Short.MAX_VALUE);
    }
    /* ___ END: num check ___*/

    public static boolean isString(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode && ((LdcInsnNode) insnNode).cst instanceof String;
    }

    public static boolean isJumpOrCondition(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() >= IFEQ && insnNode.getOpcode() <= GOTO;
    }

    /* ___ END: checks & conditions ___ */
}
