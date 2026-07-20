package me.exeos.asmplus.analysis.flow.block;

import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.*;

public class BasicBlock {

    public List<AbstractInsnNode> instructions = new ArrayList<>();

    /**
     * Cached InsnList, is populated when insnList() is called
     */
    private InsnList cachedInsnList = null;

    /**
     * All possible immediate following blocks after this Block
     */
    public Set<BasicBlock> successors = new HashSet<>();

    /**
     * Immediate following blocks, excluding exceptions
     */
    public Set<BasicBlock> normalSuccessors = new HashSet<>();

    /**
     * All immediate blocks before this Block
     */
    public Set<BasicBlock> predecessors = new HashSet<>();

    /**
     * Maps instructions that are inside try catch block protected regions to their handler blocks
     */
    public Map<AbstractInsnNode, HashSet<BasicBlock>> exceptionDispatchMap = new HashMap<>();

    /**
     * Should only be used when instructions are detached see FlowAnalyzer.detachBasicBlocks
     *
     * @return Converted InsnList
     */
    public InsnList insnList() {
        return insnList(true);
    }

    /**
     * Should only be used when instructions are detached see FlowAnalyzer.detachBasicBlocks
     *
     * @param useCache Should it used cached InsnList, (cached when this method is called)
     * @return Converted InsnList
     */
    public InsnList insnList(boolean useCache) {
        if (useCache && cachedInsnList != null) {
            return cachedInsnList;
        }

        InsnList list = InsnUtil.fromInsnList(instructions);
        cachedInsnList = list;

        return list;
    }
}
