package me.exeos.asmplus.utils;

import me.exeos.asmplus.descriptor.DescriptorMember;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TypeUtil implements Opcodes {

    /**
     * Get the class name representing a given primitive
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

    /**
     * Get opcode for loading a local based on a given description member
     * @param member The member to get the opcode for
     * @return The load opcode for the type provided
     */
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
     * @param member The member to get the opcode for
     * @return The store opcode for the type provided
     */
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
}
