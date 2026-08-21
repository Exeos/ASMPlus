package me.exeos.asmplus.descriptor.descriptors.method;

import me.exeos.asmplus.descriptor.DescriptorMember;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodDescriptor {

    private List<DescriptorMember> params;
    private DescriptorMember returnType;

    public MethodDescriptor(List<DescriptorMember> params, DescriptorMember returnType) {
        this.params = params;
        this.returnType = returnType;
    }

    public String toDesc() {
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("(");
        for (DescriptorMember param : params) {
            descBuilder.append(param.toDesc());
        }
        descBuilder.append(")");
        descBuilder.append(returnType.toDesc());

        return descBuilder.toString();
    }

    public int getParamsSize() {
        int size = 0;
        for (DescriptorMember param : params) {
            size += param.getSlotWidth();
        }
        return size;
    }

    public int getRelativeSlot(DescriptorMember of) {
        return getAbsoluteSlot(of, 0);
    }

    public int getAbsoluteSlot(DescriptorMember of, int offset) {
        return getAbsoluteSlot(params.indexOf(of), offset);
    }

    public int getAbsoluteSlot(int of, int offset) {
        int slot = offset;
        for (int i = 0; i < params.size(); i++) {
            if (i == of) {
                break;
            }
            slot += params.get(i).getSlotWidth();
        }

        return slot;
    }

    public Map<Integer, Integer> mapLocalToParamIndex() {
        return mapLocalToParamIndex(0);
    }

    public Map<Integer, Integer> mapLocalToParamIndex(int offset) {
        Map<Integer, Integer> indexByLocal = new HashMap<>();

        int local = offset;
        for (int i = 0; i < params.size(); i++) {
            indexByLocal.put(local, i);
            local += params.get(i).getSlotWidth();
        }

        return indexByLocal;
    }

    public MethodDescriptor erase() {
        params.forEach(DescriptorMember::erase);
        returnType.erase();

        return this;
    }

    public MethodDescriptor insertParam(int index, DescriptorMember param) {
        params.add(index, param);

        return this;
    }

    public MethodDescriptor addParam(DescriptorMember param) {
        params.add(param);

        return this;
    }

    public List<DescriptorMember> getParams() {
        return params;
    }

    public void setParams(List<DescriptorMember> params) {
        this.params = params;
    }

    public DescriptorMember getReturnType() {
        return returnType;
    }

    public void setReturnType(DescriptorMember returnType) {
        this.returnType = returnType;
    }
}
