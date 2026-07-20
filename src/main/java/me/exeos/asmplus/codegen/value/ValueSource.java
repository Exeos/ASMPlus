package me.exeos.asmplus.codegen.value;

import me.exeos.asmplus.InsnProvider;

public record ValueSource<T>(T value, InsnProvider pushValueInsn) {
}
