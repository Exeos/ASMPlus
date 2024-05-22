package me.exeos.asmplus.codegen.proxyjump;

import me.exeos.asmplus.codegen.block.CodeBlock;
import me.exeos.asmplus.utils.ASMUtils;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;

public class ProxyJump {

    public final LabelNode entry = new LabelNode();
    public final LabelNode destination;

    public ProxyJump(LabelNode destination) {
        this.destination = destination;
    }

    public List<AbstractInsnNode> proxyCode() {
        ArrayList<AbstractInsnNode> insns = new ArrayList<>();
        LabelNode exit = new LabelNode();

        insns.addAll(ASMUtils.getJumpInsns(exit));
        insns.addAll(new CodeBlock()
                .entry(entry)
                .add(ASMUtils.getJumpInsns(new LabelNode())) // This label will be replaced but is needed by ASMUtils
                .genBlockCode(ASMUtils.getJump(new LabelNode()), false));
        insns.add(exit);

        return insns;
    }
}
