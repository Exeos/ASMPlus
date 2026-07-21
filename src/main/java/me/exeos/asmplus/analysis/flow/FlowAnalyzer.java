package me.exeos.asmplus.analysis.flow;

import me.exeos.asmplus.analysis.flow.block.BasicBlock;
import me.exeos.asmplus.analysis.flow.block.impl.FallTroughBlock;
import me.exeos.asmplus.analysis.flow.block.impl.JumpBlock;
import me.exeos.asmplus.analysis.flow.block.impl.SwitchBlock;
import me.exeos.asmplus.analysis.flow.block.impl.TerminalBlock;
import me.exeos.asmplus.utils.InsnUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class FlowAnalyzer {

    /**
     * Map method instructions to List of BasicBlock.
     *
     * @param methodNode The method to be analyzed
     * @return List of BasicBlock
     */
    public static List<BasicBlock> getBasicBlocks(MethodNode methodNode) {
        return getBasicBlocks(methodNode, false);
    }

    /**
     * Map method instructions to List of BasicBlock.
     *
     * @param methodNode The method to be analyzed
     * @param detachBlocks Should block instructions be detached from the methodNode's InsnList
     * @return List of BasicBlock
     */
    public static List<BasicBlock> getBasicBlocks(MethodNode methodNode, boolean detachBlocks) {
        return getBasicBlocks(methodNode, detachBlocks, false);
    }

    /**
     * Map method instructions to List of BasicBlock.
     *
     * @param methodNode The method to be analyzed
     * @param detachBlocks Should block instructions be detached from the methodNode's InsnList
     * @param ignoreUnmappedExceptions Indicates if unmapped exception handlers should be treated as an error
     * @return List of BasicBlock
     */
    public static List<BasicBlock> getBasicBlocks(MethodNode methodNode, boolean detachBlocks, boolean ignoreUnmappedExceptions) {
        List<BasicBlock> blocks = new ArrayList<>();
        Map<AbstractInsnNode, BasicBlock> insnBlockMap = new HashMap<>();

        constructBlocks(methodNode, blocks, insnBlockMap);
        linkBlocks(methodNode, blocks, insnBlockMap, ignoreUnmappedExceptions);

        if (detachBlocks) {
            detachBasicBlocks(blocks, methodNode.instructions);
        }

        return blocks;
    }

    /**
     * Detaches all instruction references in BasicBlock from owning InsnList
     *
     * @param basicBlocks BasicBlocks to detach
     * @param from        InsnList to detach from
     * @return Label map used from cloning, computed from provided InsnList.
     */
    public static Map<LabelNode, LabelNode> detachBasicBlocks(List<BasicBlock> basicBlocks, InsnList from) {
        Map<LabelNode, LabelNode> labelMap = InsnUtil.mapLabels(from);

        for (BasicBlock basicBlock : basicBlocks) {
            InsnList detached = new InsnList();
            Map<AbstractInsnNode, AbstractInsnNode> insnMap = new HashMap<>();

            for (AbstractInsnNode attachedInsn : basicBlock.instructions) {
                AbstractInsnNode detachedInsn = attachedInsn.clone(labelMap);
                detached.add(detachedInsn);
                insnMap.put(attachedInsn, detachedInsn);
            }

            switch (basicBlock) {
                case JumpBlock jumpBlock -> {
                    jumpBlock.dispatcher = (JumpInsnNode) insnMap.get(jumpBlock.dispatcher);
                }
                case SwitchBlock switchBlock -> {
                    switchBlock.dispatcher = insnMap.get(switchBlock.dispatcher);
                }
                case TerminalBlock terminalBlock -> {
                    terminalBlock.dispatcher = insnMap.get(terminalBlock.dispatcher);
                }
                default -> {
                }
            }

            basicBlock.instructions = InsnUtil.fromInsnList(detached);
        }

        return labelMap;
    }

    /**
     * Returns a set of all instructions where a new BasicBlock starts
     *
     * @param methodNode The method to be analyzed
     * @return Returns a Set of all instructions where a new BasicBlock starts
     */
    private static Set<AbstractInsnNode> getBlockEntries(MethodNode methodNode) {
        Set<AbstractInsnNode> blockEntries = new HashSet<>();
        blockEntries.add(methodNode.instructions.getFirst());

        for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
            blockEntries.add(tryCatchBlock.handler);
            blockEntries.add(tryCatchBlock.start);
            blockEntries.add(tryCatchBlock.end);
        }

        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (insnNode instanceof JumpInsnNode jumpInsnNode) {
                blockEntries.add(jumpInsnNode.label);
                if (insnNode.getNext() != null) {
                    blockEntries.add(insnNode.getNext());
                }
            } else if (insnNode instanceof TableSwitchInsnNode ts) {
                blockEntries.addAll(ts.labels);
                blockEntries.add(ts.dflt);

                if (insnNode.getNext() != null) {
                    blockEntries.add(insnNode.getNext());
                }
            } else if (insnNode instanceof LookupSwitchInsnNode ls) {
                blockEntries.addAll(ls.labels);
                blockEntries.add(ls.dflt);

                if (insnNode.getNext() != null) {
                    blockEntries.add(insnNode.getNext());
                }
            } else if ((InsnUtil.isReturn(insnNode) || insnNode.getOpcode() == Opcodes.ATHROW) && insnNode.getNext() != null) {
                blockEntries.add(insnNode.getNext());
            }
        }

        return blockEntries;
    }

    /**
     * Loops trough instructions and starts block if blockEntires matches current insn. Blocks don't get linked here
     *
     * @param methodNode   MethodNode to contruct from
     * @param blocks       Output list of blocks from instructions
     * @param insnBlockMap Maps Block-start-instruction -> BasicBlock
     */
    private static void constructBlocks(MethodNode methodNode, List<BasicBlock> blocks, Map<AbstractInsnNode, BasicBlock> insnBlockMap) {
        Set<AbstractInsnNode> blockEntries = getBlockEntries(methodNode);
//        Map<LabelNode, LabelNode> labelMap = InsnUtil.mapLabels(methodNode.instructions);

        BasicBlock currentBlock = null;
        for (AbstractInsnNode insnNode : methodNode.instructions) {
            if (blockEntries.contains(insnNode)) {
                if (currentBlock != null && currentBlock.instructions.size() > 0) {
                    blocks.add(currentBlock);
                    insnBlockMap.put(currentBlock.instructions.getFirst(), currentBlock);
                }

                currentBlock = getNextBlockType(insnNode, blockEntries);
            }

            assert currentBlock != null;
//            currentBlock.instructions.add(insnNode.clone(labelMap));
            currentBlock.instructions.add(insnNode);
        }

        if (currentBlock != null) {
            blocks.add(currentBlock);
            insnBlockMap.put(currentBlock.instructions.getFirst(), currentBlock);
        }
    }

    /**
     * Determines the type of block based on its dispatcher
     *
     * @param nextBlockStart Instruction where the next block starts
     * @param blockEntries   Set of instructions marking block entries
     * @return New instance of BasicBlock with correct child
     */
    private static BasicBlock getNextBlockType(AbstractInsnNode nextBlockStart, Set<AbstractInsnNode> blockEntries) {
        AbstractInsnNode next = nextBlockStart;
        while (next.getNext() != null) {
            next = next.getNext();

            if (blockEntries.contains(next)) {
                return createBlockTypeFromDispatcher(next.getPrevious());
            }
        }

        return createBlockTypeFromDispatcher(next);
    }

    /**
     * Creates the correct impl of BasicBlock based on its dispatcher
     *
     * @param dispatcher The dispatcher of the Block
     * @return Correct subclass of BasicBlock for dispatcher
     */
    private static BasicBlock createBlockTypeFromDispatcher(AbstractInsnNode dispatcher) {
        switch (dispatcher) {
            case JumpInsnNode jumpInsnNode -> {
                return new JumpBlock(jumpInsnNode);
            }
            case LookupSwitchInsnNode ls -> {
                return new SwitchBlock(ls);
            }
            case TableSwitchInsnNode ts -> {
                return new SwitchBlock(ts);
            }
            default -> {
                if (InsnUtil.isReturn(dispatcher) || dispatcher.getOpcode() == Opcodes.ATHROW) {
                    return new TerminalBlock(dispatcher);
                } else {
                    return new FallTroughBlock();
                }
            }
        }
    }

    /**
     * Links Blocks to one another
     *
     * @param methodNode Method node being analyzed, required for exception flow linking
     * @param blocks     List of blocks to be linked
     * @param blockMap   Map, mapping instructions where blocks start to Block
     * @param ignoreUnmappedExceptions Indicates if unmapped exception handlers should be treated as an error
     */
    private static void linkBlocks(MethodNode methodNode, List<BasicBlock> blocks, Map<AbstractInsnNode, BasicBlock> blockMap, boolean ignoreUnmappedExceptions) {
        for (BasicBlock block : blocks) {
            AbstractInsnNode last = block.instructions.getLast();
            switch (block) {
                case JumpBlock jumpBlock -> {
                    // true branch
                    if (blockMap.containsKey(jumpBlock.dispatcher.label)) {
                        BasicBlock branchTarget = blockMap.get(jumpBlock.dispatcher.label);

                        block.successors.add(branchTarget);
                        block.normalSuccessors.add(branchTarget);

                        jumpBlock.trueBranchBlock = branchTarget;
                    } else {
                        throw new IllegalStateException("Block mapping incomplete, could not find Block for dispatcher target");
                    }

                    // false branch
                    if (jumpBlock.dispatcher.getOpcode() != Opcodes.GOTO && jumpBlock.dispatcher.getOpcode() != Opcodes.JSR) {
                        AbstractInsnNode nextBlockStart = last.getNext();
                        if (nextBlockStart != null && blockMap.containsKey(nextBlockStart)) {
                            BasicBlock falseBranchBlock = blockMap.get(nextBlockStart);

                            block.successors.add(falseBranchBlock);
                            block.normalSuccessors.add(falseBranchBlock);

                            jumpBlock.falseBranchBlock = Optional.of(falseBranchBlock);
                        } else {
                            throw new IllegalStateException("Block mapping incomplete, could not find next Block after JumpInstruction. Bytecode might be invalid");
                        }
                    }
                }
                case SwitchBlock switchBlock -> {
                    switch (switchBlock.dispatcher) {
                        case TableSwitchInsnNode ts -> {
                            for (int i = 0; i < ts.labels.size(); i++) {
                                LabelNode caseLabel = ts.labels.get(i);

                                if (blockMap.containsKey(caseLabel)) {
                                    BasicBlock caseBlock = blockMap.get(caseLabel);

                                    block.successors.add(caseBlock);
                                    block.normalSuccessors.add(caseBlock);

                                    switchBlock.keyCaseMap.put(ts.min + i, caseBlock);
                                    switchBlock.labelCaseMap.put(caseLabel, caseBlock);
                                } else {
                                    throw new IllegalStateException("Block mapping incomplete. Could not find Block for case target");
                                }
                            }

                            if (blockMap.containsKey(ts.dflt)) {
                                BasicBlock dfltCaseBlock = blockMap.get(ts.dflt);

                                block.successors.add(dfltCaseBlock);
                                block.normalSuccessors.add(dfltCaseBlock);

                                switchBlock.defaultBlock = dfltCaseBlock;
                            } else {
                                throw new IllegalStateException("Block mapping incomplete. Could not find Block for default case");
                            }
                        }
                        case LookupSwitchInsnNode ls -> {
                            for (int i = 0; i < ls.labels.size(); i++) {
                                LabelNode caseLabel = ls.labels.get(i);

                                if (blockMap.containsKey(caseLabel)) {
                                    BasicBlock caseBlock = blockMap.get(caseLabel);

                                    block.successors.add(caseBlock);
                                    block.normalSuccessors.add(caseBlock);

                                    switchBlock.keyCaseMap.put(ls.keys.get(i), caseBlock);
                                    switchBlock.labelCaseMap.put(caseLabel, caseBlock);
                                } else {
                                    throw new IllegalStateException("Block mapping incomplete. Could not find Block for case target");
                                }
                            }

                            if (blockMap.containsKey(ls.dflt)) {
                                BasicBlock dfltCaseBlock = blockMap.get(ls.dflt);

                                block.successors.add(dfltCaseBlock);
                                block.normalSuccessors.add(dfltCaseBlock);

                                switchBlock.defaultBlock = dfltCaseBlock;
                            } else {
                                throw new IllegalStateException("Block mapping incomplete. Could not find Block for default case");
                            }
                        }
                        default -> {
                            throw new IllegalStateException("SwitchBlock dispatcher instruction is not of type TableSwitchInsnNode or LookupSwitchInsnNode");
                        }
                    }
                }
                case FallTroughBlock fallTroughBlock -> {
                    if (last.getNext() != null) {
                        BasicBlock ftBlock = blockMap.get(last.getNext());
                        if (ftBlock == null) {
                            throw new IllegalStateException(
                                    "Block mapping incomplete. No block mapped for fall-through next instruction: " + last.getNext()
                            );
                        }

                        block.successors.add(ftBlock);
                        block.normalSuccessors.add(ftBlock);

                        fallTroughBlock.fallTroughBlock = ftBlock;
                    } else {
                        throw new IllegalStateException("Block mapping incomplete. No block found after FallTroughBlock");
                    }
                }
                case TerminalBlock jumpBlock -> {
                    // doesn't need to be linked
                }
                default -> {
                    throw new IllegalStateException("Invalid block at index: " + blocks.indexOf(block) + " " + block.getClass().getSimpleName());
                }
            }

            // link exception handlers to block from all matching TryCatchBlocks
            for (Map.Entry<AbstractInsnNode, ArrayList<TryCatchBlockNode>> entry : mapExceptionDispatchersToHandlers(methodNode, block).entrySet()) {
                for (TryCatchBlockNode tryCatchBlockNode : entry.getValue()) {
                    BasicBlock handlerBlock = blockMap.get(tryCatchBlockNode.handler);
                    if (handlerBlock == null) {
                        if (ignoreUnmappedExceptions) {
                            continue;
                        }

                        throw new IllegalStateException("No handler block mapped for try-catch handler label");
                    }

                    block.successors.add(handlerBlock);
                    block.exceptionDispatchMap
                            .computeIfAbsent(entry.getKey(), _ -> new HashSet<>())
                            .add(handlerBlock);
                }
            }

            for (BasicBlock successor : block.successors) {
                successor.predecessors.add(block);
            }
        }
    }

    /**
     * Maps instructions that can result in exceptional control flow transfer to all their possible handlers
     *
     * @param methodNode Method containing block to analyze
     * @param block      Block in method to analyze
     * @return Map, mapping instructions that can result in exceptional control flow transfer to all their possible handlers
     */
    private static Map<AbstractInsnNode, ArrayList<TryCatchBlockNode>> mapExceptionDispatchersToHandlers(MethodNode methodNode, BasicBlock block) {
        Map<AbstractInsnNode, ArrayList<TryCatchBlockNode>> dispatcherHandlerMap = new HashMap<>();

        // method exception table
        for (AbstractInsnNode insnNode : block.instructions) {
            int insnIndex = methodNode.instructions.indexOf(insnNode);
            for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
                int startIndex = methodNode.instructions.indexOf(tryCatchBlock.start);
                int endIndex = methodNode.instructions.indexOf(tryCatchBlock.end);
                if (insnIndex >= startIndex && insnIndex < endIndex) {
                    dispatcherHandlerMap.computeIfAbsent(insnNode, k -> new ArrayList<>()).add(tryCatchBlock);
                }
            }
        }

        return dispatcherHandlerMap;
    }
}
