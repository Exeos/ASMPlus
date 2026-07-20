package me.exeos.asmplus.codegen.value.impl;

import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ConstantPusher implements Opcodes {

    private static AbstractInsnNode getIConstPush(int value) {
        if (value < -1 || value > 5)
            throw new IllegalStateException("Value: " + value + " isn't in required bound: -1 to +5");

        return new InsnNode(ICONST_0 + value);
    }

    public static AbstractInsnNode getBytePush(byte value) {
        if (InsnUtil.isIConstPush(ICONST_0 + value)) {
            return getIConstPush(value);
        }

        return new IntInsnNode(BIPUSH, value);
    }

    public static AbstractInsnNode getShortPush(short value) {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return getBytePush((byte) value);
        }

        return new IntInsnNode(SIPUSH, value);
    }

    public static AbstractInsnNode getIntPush(int value) {
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            return getShortPush((short) value);

        return new LdcInsnNode(value);
    }

    public static InsnList getIntPushList(int value) {
        InsnList push = new InsnList();
        push.add(getIntPush(value));

        return push;
    }

    public static AbstractInsnNode getLongPush(long value) {
        if (value == 0 || value == 1) {
            return new InsnNode((int) (LCONST_0 + value));
        }

        return new LdcInsnNode(value);
    }
}
