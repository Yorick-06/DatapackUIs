package cz.yorick.ui.templates;

import com.mojang.serialization.MapCodec;
import cz.yorick.api.codec.ClassFieldsCodec;
import cz.yorick.api.codec.annotations.IncludeParent;
import cz.yorick.ui.UiTemplate;
import cz.yorick.ui.uis.MerchantUi;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;

import java.util.List;

@IncludeParent
public class MerchantUiTemplate extends UiTemplate {
    public static final MapCodec<MerchantUiTemplate> CODEC = addCodecsAndValidator(ClassFieldsCodec.builder(MerchantUiTemplate.class).withCodec(TradeOffer.CODEC.listOf(), "trades")).buildMap();

    private final List<TradeOffer> trades = List.of();

    @Override
    public SimpleGui getNewGui(ServerPlayerEntity player, NbtCompound nbtArgument) {
        return new MerchantUi(this, player, nbtArgument);
    }

    public List<TradeOffer> getTrades() {
        return this.trades;
    }

    @Override
    public ScreenHandlerType<?> getHandlerType() {
        return ScreenHandlerType.MERCHANT;
    }

    @Override
    public MapCodec<? extends UiTemplate> getCodec() {
        return CODEC;
    }
}
