package meitweaks.mixin;

import meitweaks.BookFix;
import meitweaks.Tooltips;
import meitweaks.antichunkban.Saver;
import meitweaks.modules.AntiChunkbanModule;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

/** Adds the Anti-Chunkban Sign/Drop toggles and the 75KB/150KB/200KB oversized-book stress buttons. */
@Mixin(value = BookEditScreen.class, priority = 1500)
public class BookEditScreenMixin extends Screen {
    protected BookEditScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void MaxButtonCooked(CallbackInfo ci) {
        if (AntiChunkbanModule.I == null || !AntiChunkbanModule.I.show()) return;

        Random random = new Random();
        int seed = random.nextInt(Integer.MAX_VALUE);

        // Random printable character, avoiding the surrogate range.
        java.util.function.Supplier<Character> randChar = () -> (char) (random.nextInt(55263) + 32);

        List<String> words75 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            try {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 256; j++) sb.append(randChar.get());
                words75.add(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                words75.add("");
            }
        }

        List<String> words150 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            try {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 512; j++) sb.append(randChar.get());
                words150.add(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                words150.add("");
            }
        }

        // Mixed-script, heavier pages for the 200KB button (ASCII, extended, CJK-ish, and surrogate pairs).
        PrimitiveIterator.OfInt ascii = random.ints(33, 128).iterator();
        PrimitiveIterator.OfInt extended = random.ints(128, 2048).iterator();
        PrimitiveIterator.OfInt wide = random.ints(2048, 55296).iterator();

        List<String> words200 = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i < 50) {
                sb.appendCodePoint(wide.nextInt());
                for (int j = 1; j < 1024; j++) sb.appendCodePoint(ascii.nextInt());
            } else if (i == 50) {
                for (int j = 0; j < 110; j++) sb.appendCodePoint(ascii.nextInt());
                sb.appendCodePoint(extended.nextInt());
                for (int j = 0; j < 913; j++) sb.appendCodePoint(ascii.nextInt());
            } else {
                for (int j = 0; j < 1024; j++) sb.appendCodePoint(ascii.nextInt());
            }
            words200.add(sb.toString());
            sb.setLength(0);
        }

        Button signToggle = Tooltips.build(
            Button.builder(
                Component.nullToEmpty("Sign: " + (Saver.signEnabled ? "§atrue" : "§cfalse")),
                button -> {
                    Saver.signEnabled = !Saver.signEnabled;
                    button.setMessage(Component.nullToEmpty("Sign: " + (Saver.signEnabled ? "§atrue" : "§cfalse")));
                    AntiChunkbanModule.pull();
                }
            ).pos(4, 76).size(150, 16)
        );
        this.addRenderableWidget(signToggle);

        Button dropToggle = Tooltips.build(
            Button.builder(
                Component.nullToEmpty("Drop: " + (Saver.dropsEnabled ? "§atrue" : "§cfalse")),
                button -> {
                    Saver.dropsEnabled = !Saver.dropsEnabled;
                    button.setMessage(Component.nullToEmpty("Drop: " + (Saver.dropsEnabled ? "§atrue" : "§cfalse")));
                    AntiChunkbanModule.pull();
                }
            ).pos(4, 58).size(150, 16)
        );
        this.addRenderableWidget(dropToggle);

        Button small = Tooltips.build(
            Button.builder(Component.nullToEmpty("75KB"), button -> sendBook(words75, "Book #" + seed))
                .pos(4, 4).size(150, 16)
        );
        this.addRenderableWidget(small);

        Button medium = Tooltips.build(
            Button.builder(Component.nullToEmpty("150KB"), button -> sendBook(words150, "512 book #" + seed))
                .pos(4, 22).size(150, 16)
        );
        this.addRenderableWidget(medium);

        Button large = Tooltips.build(
            Button.builder(Component.nullToEmpty("200KB"), button -> sendBook(words200, "Max book #" + seed))
                .pos(4, 40).size(150, 16)
        );
        this.addRenderableWidget(large);

        BookFix.moveCopyPaste(this);
    }

    private void sendBook(List<String> pages, String title) {
        int selectedSlot = this.minecraft.player.getInventory().getSelectedSlot();

        ServerboundEditBookPacket packet = Saver.signEnabled
            ? new ServerboundEditBookPacket(selectedSlot, pages, Optional.of(title))
            : new ServerboundEditBookPacket(selectedSlot, pages, Optional.empty());

        this.minecraft.getConnection().send(packet);
        this.minecraft.setScreenAndShow(null);

        if (Saver.dropsEnabled) {
            int hotbarSlot = selectedSlot + 36;
            MultiPlayerGameMode gameMode = this.minecraft.gameMode;
            gameMode.handleContainerInput(
                this.minecraft.player.containerMenu.containerId,
                hotbarSlot,
                1,
                ContainerInput.THROW,
                this.minecraft.player
            );
        }

        AntiChunkbanModule.pull();
    }
}
