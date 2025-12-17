package me.exeos.asmplus.remapper.mapping;

public class ClassMember {

    private final String owner;
    private final String name;
    private final String desc;

    public ClassMember(String owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    public boolean equals(String owner, String name, String desc) {
        return this.owner.equals(owner) && this.name.equals(name) && this.desc.equals(desc);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        return ((ClassMember) obj).equals(owner, name, desc);
    }
}
