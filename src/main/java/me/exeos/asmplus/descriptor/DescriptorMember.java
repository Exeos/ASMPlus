package me.exeos.asmplus.descriptor;

import me.exeos.asmplus.utils.TypeUtil;

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
