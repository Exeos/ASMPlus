package me.exeos.asmplus.analysis.flow.block.impl;

import me.exeos.asmplus.analysis.flow.block.BasicBlock;
import org.objectweb.asm.tree.AbstractInsnNode;

public class TerminalBlock extends BasicBlock {

    public AbstractInsnNode dispatcher;

    public TerminalBlock(AbstractInsnNode dispatcher) {
        this.dispatcher = dispatcher;
    }
}
