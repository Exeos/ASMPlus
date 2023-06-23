package me.exeos.asmplus.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

class ASMUtilTest implements Opcodes {

    @Test
    void isNumberPush() {
        IntInsnNode bIntPush = new IntInsnNode(BIPUSH, Byte.MAX_VALUE);
        IntInsnNode sIntPush = new IntInsnNode(SIPUSH, Short.MAX_VALUE);
        LdcInsnNode ldcIntPush = new LdcInsnNode(Short.MAX_VALUE + 50);

        assertTrue(ASMUtil.isNumberPush(bIntPush));
        assertTrue(ASMUtil.isNumberPush(sIntPush));
        assertTrue(ASMUtil.isNumberPush(ldcIntPush));
    }

    @Test
    void isLowNumPush() {
    }

    @Test
    void isBytePush() {
    }

    @Test
    void isShortPush() {
    }

    @Test
    void isIntPush() {

    }

    @Test
    void isLongPush() {
    }

    @Test
    void isFloatPush() {
    }

    @Test
    void isDoublePush() {
    }
}