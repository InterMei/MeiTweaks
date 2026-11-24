package meitweaks.gui;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meitweaks.MeiTweaksAddon;
import meitweaks.Tooltips;
import meitweaks.modules.UiUtilsModule;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;

/** A small window for building and sending/queueing a manual container click or button-click packet. */
public class FabricateScreen extends WindowScreen {
    private boolean click = true;
    private ContainerInput action = ContainerInput.PICKUP;

    private WTextBox sync;
    private WTextBox rev;
    private WTextBox slot;
    private WTextBox button;

    public FabricateScreen(GuiTheme theme) {
        super(theme, "Fabricate packet");
    }

    @Override
    public void initWidgets() {
        AbstractContainerMenu menu = MeteorClient.mc.player.containerMenu;

        WButton typeButton = add(Tooltips.wbutton(theme, "Type: Click Slot")).expandX().widget();
        typeButton.action = () -> {
            click = !click;
            typeButton.set(click ? "Type: Click Slot" : "Type: Button Click");
        };

        sync = field("Sync ID", menu.containerId);
        rev = field("Revision", menu.getStateId());
        slot = field("Slot", 0);
        button = field("Button", 0);

        WButton actionButton = add(Tooltips.wbutton(theme, "Action: " + action.name())).expandX().widget();
        actionButton.action = () -> {
            ContainerInput[] values = ContainerInput.values();
            action = values[(action.ordinal() + 1) % values.length];
            actionButton.set("Action: " + action.name());
        };

        WHorizontalList row = add(theme.horizontalList()).expandX().widget();

        WButton send = row.add(Tooltips.wbutton(theme, "Send")).expandX().widget();
        send.action = () -> UiUtilsModule.I.raw(build());

        WButton queue = row.add(Tooltips.wbutton(theme, "Queue")).expandX().widget();
        queue.action = () -> UiUtilsModule.I.queue.add(build());
    }

    private WTextBox field(String label, int defaultValue) {
        WHorizontalList row = add(theme.horizontalList()).expandX().widget();
        row.add(theme.label(label));
        WTextBox box = row.add(theme.textBox(String.valueOf(defaultValue),
            (text, c) -> Character.isDigit(c) || c == '-')).expandX().widget();
        return box;
    }

    private static int num(WTextBox box) {
        try {
            return Integer.parseInt(box.get().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Packet<?> build() {
        if (!click) {
            return new ServerboundContainerButtonClickPacket(num(sync), num(button));
        }

        ClientPacketListener connection = MeteorClient.mc.getConnection();
        return new ServerboundContainerClickPacket(
            num(sync),
            num(rev),
            (short) num(slot),
            (byte) num(button),
            action,
            new Int2ObjectArrayMap<>(),
            HashedStack.create(
                MeteorClient.mc.player.containerMenu.getCarried(),
                connection.decoratedHashOpsGenenerator()
            )
        );
    }
}
