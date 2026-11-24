package meitweaks.signeditor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class SignEditor implements ModInitializer {
    public static boolean autoSign = false;
    public static boolean noRender = false;
    public static boolean CustomMode = false;
    public static boolean BackMode = false;
    public static boolean MAXMODE = false;
    public static int signLength = 80;
    public static String customText1 = "";
    public static String customText2 = "";
    public static String customText3 = "";
    public static String customText4 = "";

    public static Minecraft mc;

    private static final Queue<Runnable> TICK_TASKS = new ArrayDeque<>();
    private static final List<DelayedTask> DELAYED_TASKS = new ArrayList<>();
    private static long currentTick = 0;

    @Override
    public void onInitialize() {
        System.out.println("INITIALIZED SIGN EDITOR");
        mc = Minecraft.getInstance();
        init();
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            currentTick++;

            int size = TICK_TASKS.size();
            for (int i = 0; i < size; i++) {
                Runnable task = TICK_TASKS.poll();
                if (task != null) task.run();
            }

            List<Runnable> due = new ArrayList<>();
            Iterator<DelayedTask> it = DELAYED_TASKS.iterator();
            while (it.hasNext()) {
                DelayedTask delayed = it.next();
                if (delayed.targetTick <= currentTick) {
                    due.add(delayed.task);
                    it.remove();
                }
            }
            for (Runnable task : due) task.run();
        });
    }

    public static void scheduleNextTick(Runnable task) {
        TICK_TASKS.add(task);
    }

    private static class DelayedTask {
        private final long targetTick;
        private final Runnable task;

        private DelayedTask(long targetTick, Runnable task) {
            this.targetTick = targetTick;
            this.task = task;
        }
    }
}
