package me.exeos.asmplus.utils;

import me.exeos.asmplus.descriptor.DescriptorMember;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class TypeUtil implements Opcodes {

    /**
     * Get the class name representing a given primitive
     *
     * @param primitive The primitive to get the class for
     * @return The class name if matched
     * @throws IllegalArgumentException If provided char doesn't represent a primitive
     */
    public static String primitiveToClass(char primitive) {
        return switch (primitive) {
            case 'B' -> "java/lang/Byte";
            case 'C' -> "java/lang/Character";
            case 'D' -> "java/lang/Double";
            case 'F' -> "java/lang/Float";
            case 'I' -> "java/lang/Integer";
            case 'J' -> "java/lang/Long";
            case 'S' -> "java/lang/Short";
            case 'Z' -> "java/lang/Boolean";
            default -> throw new IllegalArgumentException("Provided char isn't valid primitive: " + primitive);
        };
    }

    /**
     * Get the method name responsible for converting an instance class to its primitive type
     *
     * @param primitive The primitive target
     * @return Method name if matched
     */
    public static String clsInstanceToPrimMethodName(char primitive) {
        return switch (primitive) {
            case 'B' -> "byteValue";
            case 'C' -> "charValue";
            case 'D' -> "doubleValue";
            case 'F' -> "floatValue";
            case 'I' -> "intValue";
            case 'J' -> "longValue";
            case 'S' -> "shortValue";
            case 'Z' -> "booleanValue";
            default -> throw new IllegalArgumentException("Provided char isn't valid primitive: " + primitive);
        };
    }

    public static int opcodeForType(DescriptorMember member, int baseOpcode) {
        int offset;
        if (member.isPrimitive() && !member.isArray()) {
            offset = switch (member.getValue().charAt(0)) {
                case 'B', 'C', 'I', 'S', 'Z' -> 0;
                case 'D' -> DRETURN - IRETURN;
                case 'F' -> FRETURN - IRETURN;
                case 'J' -> LRETURN - IRETURN;
                case 'V' -> RETURN - IRETURN;
                default -> throw new IllegalStateException("Invalid primitive" + member.getValue().charAt(0));
            };
        } else {
            offset = ARETURN - IRETURN;
        }

        return baseOpcode + offset;
    }

    /**
     * Get opcode for loading a local based on a given description member
     *
     * @param member The member to get the opcode for
     * @return The load opcode for the type provided
     */
    @Deprecated(forRemoval = true)
    public static int loadOpcodeForType(DescriptorMember member) {
        if (!member.isPrimitive() || member.isArray()) {
            return ALOAD;
        }

        return switch (member.getValue().charAt(0)) {
            case 'J' -> LLOAD;
            case 'D' -> DLOAD;
            case 'F' -> FLOAD;
            case 'I' -> ILOAD;
            default -> throw new IllegalArgumentException("Value does not map to load opcode");
        };
    }

    /**
     * Get opcode for storing a local based on a given description member
     *
     * @param member The member to get the opcode for
     * @return The store opcode for the type provided
     */
    @Deprecated(forRemoval = true)
    public static int storeOpcodeForType(DescriptorMember member) {
        if (!member.isPrimitive() || member.isArray()) {
            return ASTORE;
        }

        return switch (member.getValue().charAt(0)) {
            case 'J' -> LSTORE;
            case 'D' -> DSTORE;
            case 'F' -> FSTORE;
            case 'I' -> ISTORE;
            default -> throw new IllegalArgumentException("Value does not map to store opcode");
        };
    }

    public static AbstractInsnNode getTypeClassPushInsn(Type type) {
        return switch (type.getSort()) {
            case Type.VOID -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
            case Type.BOOLEAN -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
            case Type.CHAR -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
            case Type.BYTE -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
            case Type.SHORT -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
            case Type.INT -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case Type.FLOAT -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case Type.LONG -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
            case Type.DOUBLE -> new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
            default -> new LdcInsnNode(type);
        };
    }
}
