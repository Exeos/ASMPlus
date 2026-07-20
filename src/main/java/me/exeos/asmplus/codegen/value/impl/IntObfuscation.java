package me.exeos.asmplus.codegen.value.impl;

import me.exeos.asmplus.codegen.Arithmetic;
import me.exeos.asmplus.codegen.value.ValueSource;
import me.exeos.asmplus.utils.RandomUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Optional;

public class IntObfuscation implements Opcodes {

    @SafeVarargs
    public static InsnList getIntPush(int value, ValueSource<Integer>... valueSources) {
        return RandomUtil.chance(70)
                ? IntObfuscation.xorIntPush(value, valueSources)
                : IntObfuscation.rotateIntPush(value, valueSources);
    }

    @SafeVarargs
    public static InsnList xorIntPush(int value, ValueSource<Integer>... valueSources) {
        InsnList pushInsn = new InsnList();

        if (valueSources.length == 0) {
            pushInsn.add(ConstantPusher.getIntPush(value));
            return pushInsn;
        }

        // additional key required of valueSources are not even so the last value source can be xored with something
        Optional<Integer> additionalKey = Optional.empty();
        if (valueSources.length % 2 != 0) {
            additionalKey = Optional.of(RandomUtil.getInt(10, 10000));
        }

        int key = valueSources[0].value();
        for (int i = 1; i < valueSources.length; i++) {
            key ^= valueSources[i].value();
        }

        if (additionalKey.isPresent()) {
            key ^= additionalKey.get();
        }

        pushInsn.add(ConstantPusher.getIntPush(value ^ key));
        for (int i = 0; i < valueSources.length; i += 2) {
            pushInsn.add(valueSources[i].pushValueInsn().get());
            if (i + 1 < valueSources.length) {
                pushInsn.add(valueSources[i + 1].pushValueInsn().get());
            } else additionalKey.ifPresent(addKeyValue -> pushInsn.add(ConstantPusher.getIntPush(addKeyValue)));
            pushInsn.add(new InsnNode(IXOR));
        }
        pushInsn.add(new InsnNode(IXOR));

        return pushInsn;
    }

    @SafeVarargs
    public static InsnList rotateIntPush(int value, ValueSource<Integer>... valueSources) {
        if (valueSources.length == 0) {
            return ConstantPusher.getIntPushList(value);
        }

        boolean direction = RandomUtil.chance(50);
        InsnList pushInsn = new InsnList();

        int enc = value;
        for (ValueSource<Integer> valueSource : valueSources) {
            int r = valueSource.value() & 31;
            enc = direction
                    ? Integer.rotateLeft(enc ^ valueSource.value(), r)
                    : Integer.rotateRight(enc ^ valueSource.value(), r);
        }

        pushInsn.add(ConstantPusher.getIntPush(enc));

        for (int i = valueSources.length - 1; i >= 0; i--) {
            pushInsn.add(valueSources[i].pushValueInsn().get());
            pushInsn.add(ConstantPusher.getIntPush(31));
            pushInsn.add(new InsnNode(IAND));

            if (RandomUtil.chance(50)) {
                pushInsn.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", direction ? "rotateRight" : "rotateLeft", "(II)I"));
            } else {
                pushInsn.add(direction ? Arithmetic.rotateRight() : Arithmetic.rotateLeft());
            }
            pushInsn.add(valueSources[i].pushValueInsn().get());
            pushInsn.add(new InsnNode(IXOR));
        }

        return pushInsn;
    }
}
