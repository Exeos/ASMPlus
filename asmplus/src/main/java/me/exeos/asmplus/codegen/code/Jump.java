package me.exeos.asmplus.codegen.code;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;

public class Jump {

    public final ArrayList<AbstractInsnNode> condition;
    private int opcode;
    private LabelNode label = null;

    public Jump(ArrayList<AbstractInsnNode> condition, int opcode, LabelNode label) {
        this.condition = condition;
        setOpcode(opcode);
        this.label = label;
    }

    public Jump(int opcode, LabelNode label) {
        this(new ArrayList<>(), opcode, label);
    }

    public Jump(LabelNode label) {
        this(Opcodes.GOTO, label);
    }

    public Jump() {
        this(null);
    }

    /**
     * combine condition and jump and return
     * @return List of instructions required for the jump
     * */
    public ArrayList<AbstractInsnNode> getJump() {
        if (label == null) {
            throw new IllegalStateException("Jump label can't be null");
        }
        ArrayList<AbstractInsnNode> combine = new ArrayList<>();
        combine.addAll(condition);
        combine.add(new JumpInsnNode(opcode, label));

        return combine;
    }

    /**
     * Add an Instruction to the conditions
     * @param insnNode Instruction to be added to conditions
     * */
    public void add(AbstractInsnNode insnNode) {
        condition.add(insnNode);
    }

    /**
     * Set the jump opcode
     * @param opcode new jump opcode
     * @return Instance of current Object
    */
    public Jump setOpcode(int opcode) {
        if (opcode < Opcodes.IFEQ || opcode > Opcodes.JSR) {
            throw new IllegalArgumentException("Opcode must be a valid jump opcode");
        }
        this.opcode = opcode;

        return this;
    }

    /**
     * Set the jump target label
     * @param label new label
     * @return Instance of current Object
     */
    public Jump setLabel(LabelNode label) {
        if (label == null) {
            throw new IllegalArgumentException("Label can't be null");
        }
        this.label = label;

        return this;
    }
}
