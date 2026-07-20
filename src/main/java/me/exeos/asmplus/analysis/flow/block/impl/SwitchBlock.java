package me.exeos.asmplus.analysis.flow.block.impl;

import me.exeos.asmplus.analysis.flow.block.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.HashMap;
import java.util.Map;

public class SwitchBlock extends BasicBlock {

    public AbstractInsnNode dispatcher;
    public final Map<Integer, BasicBlock> keyCaseMap = new HashMap<>();
    public final Map<LabelNode, BasicBlock> labelCaseMap = new HashMap<>();
    public BasicBlock defaultBlock;

    public SwitchBlock(AbstractInsnNode dispatcher) {
        this.dispatcher = dispatcher;
    }
}
