package me.exeos.asmplus.analysis.flow.block.impl;

import me.exeos.asmplus.analysis.flow.block.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class SwitchBlock extends BasicBlock {

    public final Map<Integer, BasicBlock> keyCaseMap = new LinkedHashMap<>();
    public final Map<LabelNode, BasicBlock> labelCaseMap = new LinkedHashMap<>();
    public AbstractInsnNode dispatcher;
    public BasicBlock defaultBlock;

    public SwitchBlock(AbstractInsnNode dispatcher) {
        this.dispatcher = dispatcher;
    }
}
