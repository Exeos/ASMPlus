package me.exeos.asmplus.codegen;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

public class Arithmetic implements Opcodes {

    public static InsnList rotateRight() {
        InsnList rr = new InsnList();
        rr.add(new InsnNode(DUP2)); //   0, 1, 0, 1
        rr.add(new InsnNode(IUSHR)); //  0, 1, ushr
        rr.add(new InsnNode(DUP_X2)); // ushr, 0, 1, ushr
        rr.add(new InsnNode(POP)); //    ushr, 0, 1
        rr.add(new InsnNode(INEG));
        rr.add(new InsnNode(ISHL));
        rr.add(new InsnNode(IOR));
        return rr;
    }

    public static InsnList rotateLeft() {
        InsnList rr = new InsnList();
        rr.add(new InsnNode(DUP2)); //   0, 1, 0, 1
        rr.add(new InsnNode(ISHL)); //  0, 1, shl
        rr.add(new InsnNode(DUP_X2)); // shl, 0, 1, shl
        rr.add(new InsnNode(POP)); //    shl, 0, 1
        rr.add(new InsnNode(INEG));
        rr.add(new InsnNode(IUSHR));
        rr.add(new InsnNode(IOR));
        return rr;
    }
}
