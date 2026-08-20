package me.exeos.asmplus.descriptor;

import me.exeos.asmplus.utils.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class DescriptorMember {

    private String value;
    private boolean isPrimitive;
    private boolean isArray;
    private int arrayDepth;

    public DescriptorMember(String value, boolean isPrimitive, boolean isArray, int arrayDepth) {
        this.value = value;
        this.isPrimitive = isPrimitive;
        this.isArray = isArray;
        this.arrayDepth = arrayDepth;
    }

    public DescriptorMember toErased() {
        return new DescriptorMember(value, isPrimitive, isArray, arrayDepth).erase();
    }

    public DescriptorMember erase() {
        if (isPrimitive) {
            return this;
        }

        value = "java/lang/Object";
        return this;
    }

    public int getSlotWidth() {
        if (!isArray && isPrimitive && (value.equals("D") || value.equals("J"))) {
            return 2;
        }

        return 1;
    }

    public String toDesc() {
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        if (isArray) {
            prefix.repeat("[", arrayDepth);
        }

        if (!isPrimitive) {
            prefix.append("L");
            suffix.append(";");
        }

        return prefix + value + suffix;
    }

    public String toType() {
        StringBuilder typeBuilder = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        if (isArray) {
            typeBuilder.repeat("[", arrayDepth);
            if (!isPrimitive) {
                typeBuilder.append("L");
                suffix.append(";");
            }
        }

        typeBuilder.append(value);
        return typeBuilder.toString() + suffix;
    }

    public DescriptorMember toNonePrimitive() {
        if (!isPrimitive) {
            return this;
        }

        return new DescriptorMember(TypeUtil.primitiveToClass(value.toCharArray()[0]), false, isArray, arrayDepth);
    }

    /**
     * Unboxes Object on stack into this {@link DescriptorMember DescriptorMember's} type
     *
     * @return {@link InsnList} that does the unboxing
     */
    public InsnList unbox() {
        InsnList unboxInsn = new InsnList();

        unboxInsn.add(new TypeInsnNode(Opcodes.CHECKCAST, (isArray() ? this : toNonePrimitive()).toType()));
        if (isPrimitive() && !isArray()) {
            char primitive = value.charAt(0);
            unboxInsn.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    TypeUtil.primitiveToClass(primitive),
                    TypeUtil.clsInstanceToPrimMethodName(primitive),
                    "()" + primitive
            ));
        }

        return unboxInsn;
    }

    /**
     * Boxes this {@link DescriptorMember DescriptorMember's} type that's on the stack into Object
     *
     * @return {@link InsnList} that does the boxing
     */
    public InsnList box() {
        InsnList boxInsn = new InsnList();
        if (isPrimitive() && !isArray()) {
            String primClassName = TypeUtil.primitiveToClass(value.charAt(0));
            boxInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, primClassName, "valueOf", "(" + value + ")L" + primClassName + ";"));
        }

        return boxInsn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public void setPrimitive(boolean primitive) {
        isPrimitive = primitive;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public int getArrayDepth() {
        return arrayDepth;
    }

    public void setArrayDepth(int arrayDepth) {
        this.arrayDepth = arrayDepth;
    }
}
