package cz.yorick.ui.uis;

import cz.yorick.ui.storage.UiStorage;
import cz.yorick.ui.templates.MerchantUiTemplate;
import eu.pb4.sgui.api.ScreenProperty;
import eu.pb4.sgui.api.gui.MerchantGui;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;

public class MerchantUi extends MerchantGui implements UiStorage.Holder {
    private final MerchantUiTemplate template;
    private final UiStorage storage;
    public MerchantUi(MerchantUiTemplate template, ServerPlayerEntity player, NbtCompound nbtArgument) {
        super(player, template.includesPlayerInventory());
        this.template = template;
        this.storage = template.storage.create(template, this, nbtArgument, this::writeTrades, this::readTrades);
    }

    @Override
    public boolean open() {
        boolean open = super.open();
        this.storage.postOpen();
        return open;
    }

    @Override
    public void sendProperty(ScreenProperty property, int value) {
        super.sendProperty(property, value);
        this.storage.onValidPropertySend(property, value);
    }

    @Override
    public void onClose() {
        this.storage.onClose();
    }

    private void writeTrades(NbtCompound nbt) {
        //try to write offers to nbt
        TradeOffer.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.merchant.getOffers()).ifSuccess(tradesNbt -> nbt.put("trades", nbt));
    }

    private void readTrades(NbtCompound nbt) {
        //if the key is not present (first open, non-saving ui without arguments)
        if(!nbt.contains("trades")) {
            this.template.getTrades().forEach(this::addTrade);
            return;
        }

        //try to parse offers from nbt
        TradeOffer.CODEC.listOf().parse(NbtOps.INSTANCE, nbt.get("trades")).ifSuccess(tradeOffers -> tradeOffers.forEach(this::addTrade));
    }

    @Override
    public UiStorage getStorage() {
        return this.storage;
    }
}
