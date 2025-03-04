package cz.yorick.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import cz.yorick.api.resources.ResourceUtil;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public abstract class Executable {
    public static final Codec<Executable> CODEC = Codecs.NON_EMPTY_STRING.comapFlatMap(Executable::parse, executable -> executable.input);
    private final String input;
    public Executable(String input) {
        this.input = input;
    }

    public String getInput() {
        return this.input;
    }

    public void execute(ServerCommandSource source) {
        execute(source, null, ReturnValueConsumer.EMPTY);
    }

    public void execute(ServerCommandSource source, NbtCompound nbt) {
        execute(source, nbt, ReturnValueConsumer.EMPTY);
    }

    public void execute(ServerCommandSource source, ReturnValueConsumer returnValueConsumer) {
        execute(source, null, returnValueConsumer);
    }

    public abstract void execute(ServerCommandSource source, NbtCompound nbt, ReturnValueConsumer returnValueConsumer);

    public abstract boolean acceptsNbt();

    static DataResult<Executable> parse(String input) {
        if(input.startsWith("/")) {
            return DataResult.success(new Command(input));
        }

        Identifier functionId = Identifier.tryParse(input);
        if(functionId != null) {
            return DataResult.success(new Function(input, functionId));
        }

        return DataResult.error(() -> "Executable needs to start with '/' or be a valid identifier!");
    }

    private static class Command extends Executable {
        public Command(String input) {
            super(input);
        }

        @Override
        public void execute(ServerCommandSource source, NbtCompound nbt, ReturnValueConsumer returnValueConsumer) {
            ResourceUtil.executeCommand(source, this.getInput(), returnValueConsumer);
        }

        @Override
        public boolean acceptsNbt() {
            return false;
        }
    }

    private static class Function extends Executable {
        private final Identifier function;
        public Function(String input, Identifier function) {
            super(input);
            this.function = function;
        }

        @Override
        public void execute(ServerCommandSource source, NbtCompound nbt, ReturnValueConsumer returnValueConsumer) {
            ResourceUtil.executeMacroFunction(source, this.function, nbt, returnValueConsumer);
        }

        @Override
        public boolean acceptsNbt() {
            return true;
        }
    }
}
