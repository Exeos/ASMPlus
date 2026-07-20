package me.exeos.asmplus.analysis.flow.block.impl;

import me.exeos.asmplus.analysis.flow.block.BasicBlock;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.Optional;

public class JumpBlock extends BasicBlock {

    public JumpInsnNode dispatcher;
    public BasicBlock trueBranchBlock;
    public Optional<BasicBlock> falseBranchBlock = Optional.empty();

    public JumpBlock(JumpInsnNode dispatcher) {
        this.dispatcher = dispatcher;
    }
}
