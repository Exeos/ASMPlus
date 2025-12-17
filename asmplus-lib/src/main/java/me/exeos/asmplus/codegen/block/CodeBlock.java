package me.exeos.asmplus.codegen.block;

import me.exeos.asmplus.codegen.code.Jump;
import me.exeos.asmplus.utils.ASMUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock {

    private LabelNode entry = new LabelNode();
    public final ArrayList<AbstractInsnNode> code = new ArrayList<>();

    private CodeBlock prev;
    private CodeBlock next;

    public CodeBlock(CodeBlock next, CodeBlock prev) {
        this.next = next;
        this.prev = prev;
    }

    public CodeBlock() {
        this(null, null);
    }

    public ArrayList<AbstractInsnNode> genBlockCode() {
        return genBlockCode(null, false);
    }

    public ArrayList<AbstractInsnNode> genBlockCode(Jump jumpTemplate) {
        return genBlockCode(jumpTemplate, false);
    }

    public ArrayList<AbstractInsnNode> genBlockCode(boolean encapsulate) {
        return genBlockCode(null, encapsulate);
    }

    public ArrayList<AbstractInsnNode> genBlockCode(Jump jumpTemplateE, boolean encapsulate) {
        ArrayList<AbstractInsnNode> blockCode = new ArrayList<>();
        LabelNode exit = null;

        /* Jump to block exit, so you can only access block with entry */
        if (encapsulate) {
            exit = new LabelNode();
            blockCode.addAll(ASMUtils.getJumpInsns(exit));
        }

        blockCode.add(entry);
        blockCode.addAll(code);
        if (next != null) {
            blockCode.addAll(ASMUtils.getJumpInsns(Opcodes.GOTO, next.entry));
        }

        if (encapsulate) {
            blockCode.add(exit);
            blockCode.add(new InsnNode(Opcodes.NOP));
        }

        return blockCode;
    }

    /**
     * Add list of instructions to block's code
     * @param insns List of instructions to add
     * @return Instance of current CodeBlock
     */
    public CodeBlock add(List<AbstractInsnNode> insns) {
        if (insns == null) {
            throw new IllegalArgumentException("insns can't be null");
        }
        code.addAll(insns);

        return this;
    }

    /**
     * Add instruction
     * @param insnNode instruction to add
     * @return Instance of current CodeBlock
     */
    public CodeBlock add(AbstractInsnNode insnNode) {
        if (insnNode == null) {
            throw new IllegalArgumentException("insnNode can't be null");
        }
        code.add(insnNode);

        return this;
    }

    /* getter and setter for prev and next block */

    /**
     * Set the next entry of the CodeBlock. NOTE: Entry MUST be a new Entry, not existing anywhere you are trying to add block to
     * @return Instance of CodeBlock
     */
    public CodeBlock entry(LabelNode newEntry) {
        if (newEntry == null) {
            throw new IllegalArgumentException("Entry of CodeBlock can't be null");
        }
        this.entry = newEntry;

        return this;
    }

    public LabelNode entry() {
        return entry;
    }

    public CodeBlock next() {
        return next;
    }

    /**
     * Set the next CodeBlock of this CodeBlock
     * @return Instance of next CodeBlock
     */
    public CodeBlock next(CodeBlock next) {
        if (next == null) {
            throw new IllegalArgumentException("next can't be null");
        }
        this.next = next;

        return next;
    }

    public CodeBlock prev() {
        return prev;
    }

    /**
     * Set the prev CodeBlock of this CodeBlock
     * @return Instance of prev CodeBlock
     */
    public CodeBlock prev(CodeBlock prev) {
        if (prev == null) {
            throw new IllegalArgumentException("prev can't be null");
        }
        this.prev = prev;

        return prev;
    }
}