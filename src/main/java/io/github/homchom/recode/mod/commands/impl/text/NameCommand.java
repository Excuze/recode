package io.github.homchom.recode.mod.commands.impl.text;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import io.github.homchom.recode.LegacyRecode;
import io.github.homchom.recode.mod.commands.Command;
import io.github.homchom.recode.mod.commands.arguments.ArgBuilder;
import io.github.homchom.recode.mod.commands.arguments.types.PlayerArgumentType;
import io.github.homchom.recode.sys.networking.LegacyState;
import io.github.homchom.recode.sys.player.DFInfo;
import io.github.homchom.recode.sys.player.chat.ChatType;
import io.github.homchom.recode.sys.player.chat.ChatUtil;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class NameCommand extends Command {

    @Override
    public void register(Minecraft mc, CommandDispatcher<FabricClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("name")
                .then(ArgBuilder.argument("uuid", PlayerArgumentType.player())
                        .executes(ctx -> {
                            LegacyRecode.EXECUTOR.submit(() -> {
                                String uuid = ctx.getArgument("uuid", String.class);
                                String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
                                try {
                                    String NameJson = IOUtils
                                            .toString(new URL(url), StandardCharsets.UTF_8);
                                    if (NameJson.isEmpty()) {
                                        ChatUtil.sendMessage("Player with that UUID was not found! Please check if you misspelled it and try again.", ChatType.FAIL);
                                        return;
                                    }
                                    List<Map> json = new Gson().fromJson(NameJson, List.class);
                                    String nameData = json.get(json.size()-1).get("name").toString();
                                    String fullName = nameData;

                                    Component text = new TextComponent("§eName of §6" + uuid + " §eis §b" + fullName + "§e!")
                                            .withStyle(s -> s.withHoverEvent(
                                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("§eClick to copy to clipboard."))
                                            ).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, fullName)));
                                    this.sendMessage(mc, text);

                                    if (this.isCreative(mc) && DFInfo.isOnDF() && DFInfo.currentState.getMode() == LegacyState.Mode.DEV) {
                                        this.sendChatMessage(mc, "/txt " + fullName);
                                    }
                                } catch (IOException e) {
                                    ChatUtil.sendMessage("§cUUID §6" + uuid + "§c was not found. Please check if you misspelled it and try again.");
                                    e.printStackTrace();
                                }
                            });
                            return 1;
                        })
                )
        );
    }

    @Override
    public String getDescription() {
        return "[blue]/name <uuid>[reset]\n"
            + "\n"
            + "Copies the name of the player assigned to the uuid to your clipboard and if you're in dev mode also gives you the name as a text item.";
    }

    @Override
    public String getName() {
        return "/name";
    }
}
