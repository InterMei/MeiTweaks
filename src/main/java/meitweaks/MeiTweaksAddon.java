package meitweaks;

import meitweaks.modules.AntiChunkbanModule;
import meitweaks.modules.SignEditorModule;
import meitweaks.modules.UiUtilsModule;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screens.TitleScreen;

public class MeiTweaksAddon extends MeteorAddon {
    private static final String COLORED = "§4M§4e§ci§cT§ew§ee§ea§ek§es";
    private static final String CLEAN = "MeiTweaks";

    public static final Category CATEGORY = new Category("MeiTweaks");

    @Override
    public void onInitialize() {
        this.name = COLORED;

        Modules.get().add(new UiUtilsModule());
        Modules.get().add(new AntiChunkbanModule());
        Modules.get().add(new SignEditorModule());

        // Subscribe to the Meteor event bus reflectively so the addon still loads
        // if the Orbit API shape changes between Meteor versions.
        try {
            Class<?> iEventBus = Class.forName("meteordevelopment.orbit.IEventBus");
            Object bus = Class.forName("meteordevelopment.meteorclient.MeteorClient")
                .getField("EVENT_BUS").get(null);
            iEventBus.getMethod("subscribe", Object.class).invoke(bus, this);
        } catch (Throwable t) {
            this.name = CLEAN;
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        try {
            this.name = event.screen instanceof TitleScreen ? COLORED : CLEAN;
        } catch (Throwable t) {
            this.name = CLEAN;
        }
    }

    @Override
    public String getPackage() {
        return "meitweaks";
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }
}
