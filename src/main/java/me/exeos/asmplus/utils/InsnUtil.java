package me.exeos.asmplus.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.function.Consumer;

public class InsnUtil implements Opcodes {

    public static boolean isStore(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() >= ISTORE && insnNode.getOpcode() <= ASTORE;
    }

    public static boolean isLoad(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() >= ILOAD && insnNode.getOpcode() <= ALOAD;
    }

    public static boolean isReturn(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() >= Opcodes.IRETURN && insnNode.getOpcode() <= Opcodes.RETURN;
    }

    public static boolean isTerminal(AbstractInsnNode insnNode) {
        return insnNode.getOpcode() == ATHROW || isReturn(insnNode);
    }

    public static boolean isBranch(AbstractInsnNode insnNode) {
        return insnNode instanceof JumpInsnNode;
    }

    public static boolean isIConstPush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode.getOpcode());
    }

    public static boolean isIConstPush(int opcode) {
        return opcode >= ICONST_M1 && opcode <= ICONST_5;
    }

    public static boolean isBytePush(AbstractInsnNode insnNode) {
        return isIConstPush(insnNode) || insnNode.getOpcode() == BIPUSH;
    }

    public static boolean isShortPush(AbstractInsnNode insnNode) {
        return isBytePush(insnNode) || insnNode.getOpcode() == SIPUSH;
    }

    public static boolean isIntPush(AbstractInsnNode insnNode) {
        return isShortPush(insnNode) ||
                (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Integer);
    }

    public static boolean isLongPush(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode ldc && ldc.cst instanceof Long;
    }

    public static boolean isDoublePush(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode ldc && ldc.cst instanceof Double;
    }

    public static boolean isFloatPush(AbstractInsnNode insnNode) {
        return insnNode instanceof LdcInsnNode ldc && ldc.cst instanceof Float;
    }

    public static Optional<Long> getLongValue(AbstractInsnNode insnNode) {
        if (isLongPush(insnNode)) {
            return Optional.of((Long) ((LdcInsnNode) insnNode).cst);
        }

        return Optional.empty();
    }

    public static Optional<Double> getDoubleValue(AbstractInsnNode insnNode) {
        if (isDoublePush(insnNode)) {
            return Optional.of((Double) ((LdcInsnNode) insnNode).cst);
        }

        return Optional.empty();
    }

    public static Optional<Float> getFloatValue(AbstractInsnNode insnNode) {
        if (isFloatPush(insnNode)) {
            return Optional.of((Float) ((LdcInsnNode) insnNode).cst);
        }

        return Optional.empty();
    }

    public static Optional<Integer> getIntValue(AbstractInsnNode insnNode) {
        if (isIConstPush(insnNode.getOpcode())) {
            return Optional.of(insnNode.getOpcode() - 3);
        }

        switch (insnNode.getOpcode()) {
            case BIPUSH, SIPUSH -> {
                return Optional.of(((IntInsnNode) insnNode).operand);
            }
            case LDC -> {
                LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
                if (ldcInsnNode.cst instanceof Integer value) {
                    return Optional.of(value);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Number> getNumberValue(AbstractInsnNode insnNode) {
        Optional<Integer> intVal = getIntValue(insnNode);
        if (intVal.isPresent()) {
            return Optional.of(intVal.get());
        }

        Optional<Long> longVal = getLongValue(insnNode);
        if (longVal.isPresent()) {
            return Optional.of(longVal.get());
        }

        Optional<Double> doubleVal = getDoubleValue(insnNode);
        if (doubleVal.isPresent()) {
            return Optional.of(doubleVal.get());
        }

        Optional<Float> floatVal = getFloatValue(insnNode);
        if (floatVal.isPresent()) {
            return Optional.of(floatVal.get());
        }

        return Optional.empty();
    }

    public static void addToInsnList(List<AbstractInsnNode> source, InsnList target) {
        for (AbstractInsnNode insnNode : source) {
            target.add(insnNode);
        }
    }

    public static void addFromInsnList(InsnList source, List<AbstractInsnNode> target) {
        for (AbstractInsnNode insnNode : source) {
            target.add(insnNode);
        }
    }

    public static List<AbstractInsnNode> fromInsnList(InsnList from) {
        List<AbstractInsnNode> list = new ArrayList<>();
        for (AbstractInsnNode insnNode : from) {
            list.add(insnNode);
        }

        return list;
    }

    public static InsnList fromInsnList(List<AbstractInsnNode> from) {
        InsnList list = new InsnList();
        addToInsnList(from, list);

        return list;
    }

    /**
     * Safely loop trough instructions, you can insert, delete, etc without breaking iteration
     *
     * @param insnList
     * @param visitor
     */
    public static void loop(InsnList insnList, Consumer<AbstractInsnNode> visitor) {
        AbstractInsnNode current = insnList.getFirst();

        while (current != null) {
            AbstractInsnNode next = current.getNext();
            visitor.accept(current);
            current = next;
        }
    }

    public static void loop(List<AbstractInsnNode> insnList, Consumer<AbstractInsnNode> visitor) {
        AbstractInsnNode current = insnList.getFirst();

        while (current != null) {
            AbstractInsnNode next = current.getNext();
            visitor.accept(current);
            current = next;
        }
    }

    public static boolean isLambdaMetaFactory(InvokeDynamicInsnNode indy) {
        return indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")
                && indy.bsm.getName().equals("metafactory")
                && indy.bsm.getDesc().equals("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
    }

    public static Map<LabelNode, LabelNode> mapLabels(InsnList source) {
        Map<LabelNode, LabelNode> labelMap = new HashMap<>();
        for (AbstractInsnNode insnNode : source) {
            if (insnNode instanceof LabelNode labelNode) {
                labelMap.put(labelNode, new LabelNode());
            }
        }

        return labelMap;
    }

    public static InsnList copy(InsnList source) {
        InsnList copy = new InsnList();
        Map<LabelNode, LabelNode> labelMap = mapLabels(source);

        for (AbstractInsnNode insnNode : source) {
            copy.add(insnNode.clone(labelMap));
        }

        return copy;
    }

    public static boolean isWide(int opcode) {
        return switch (opcode) {
            case Opcodes.LLOAD, Opcodes.LSTORE, Opcodes.DLOAD, Opcodes.DSTORE -> true;
            default -> false;
        };
    }

    public static Optional<String> getStingPushValue(AbstractInsnNode insnNode) {
        if (insnNode instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
            return Optional.of(s);
        }

        return Optional.empty();
    }
}
