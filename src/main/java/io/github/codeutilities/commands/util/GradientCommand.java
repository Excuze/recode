package io.github.codeutilities.commands.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.codeutilities.commands.Command;
import io.github.codeutilities.commands.arguments.ArgBuilder;
import io.github.codeutilities.commands.arguments.types.FreeStringArgumentType;
import io.github.codeutilities.util.*;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;

import java.awt.*;

public class GradientCommand extends Command {
    @Override
    public void register(MinecraftClient mc, CommandDispatcher<CottonClientCommandSource> cd) {
        cd.register(ArgBuilder.literal("gradient")
            .then(ArgBuilder.argument("startColor", FreeStringArgumentType.string())
                .then(ArgBuilder.argument("endColor", FreeStringArgumentType.string())
                    .then(ArgBuilder.argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String text = ctx.getArgument("text", String.class);
                            String strStartColor = ctx.getArgument("startColor", String.class);
                            String strEndColor = ctx.getArgument("endColor", String.class);

                            HSLColor startColor;
                            HSLColor endColor;

                            try {
                                if (strStartColor.startsWith("#")) {
                                    startColor = new HSLColor(Color.decode(strStartColor));
                                }else if (strStartColor.matches("^[0-9a-f]*")) {
                                    startColor = new HSLColor(Color.decode("#" + strStartColor));
                                }else if (strStartColor.startsWith("&x")) {
                                    startColor = new HSLColor(Color.decode(strStartColor.replaceAll("&", "").replaceAll("x", "#")));
                                }else if (strStartColor.matches("^&[0-9a-f]")) {
                                    char[] chars = strStartColor.replaceAll("&", "").toCharArray();
                                    MinecraftColors mcColor = MinecraftColors.fromCode(chars[0]);
                                    startColor = new HSLColor(mcColor.getColor());
                                }else {
                                    ChatUtil.sendMessage("Invalid color!", ChatType.FAIL);
                                    return -1;
                                }

                                if (strEndColor.startsWith("#")) {
                                    endColor = new HSLColor(Color.decode(strEndColor));
                                }else if (strEndColor.matches("^[0-9a-f]*")) {
                                    endColor = new HSLColor(Color.decode("#" + strEndColor));
                                }else if (strEndColor.startsWith("&x")) {
                                    endColor = new HSLColor(Color.decode(strEndColor.replaceAll("&", "").replaceAll("x", "#")));
                                }else if (strEndColor.matches("^&[0-9a-f]")) {
                                    char[] chars = strEndColor.replaceAll("&", "").toCharArray();
                                    MinecraftColors mcColor = MinecraftColors.fromCode(chars[0]);
                                    endColor = new HSLColor(mcColor.getColor());
                                }else {
                                    ChatUtil.sendMessage("Invalid color!", ChatType.FAIL);
                                    return -1;
                                }
                            }catch (Exception e) {
                                ChatUtil.sendMessage("Invalid color!", ChatType.FAIL);
                                return -1;
                            }

                            StringBuilder sb = new StringBuilder();
                            char[] chars = text.toCharArray();
                            float i = 0;
                            String lastHex = "";
                            int spaces = 0;

                            LiteralText base = new LiteralText("§a→ §r");

                            for (char c: chars) {
                                if (c == ' ') {
                                    spaces++;
                                    base.getSiblings().add(new LiteralText(" "));
                                    sb.append(c);
                                    continue;
                                }
                                HSLColor temp = new HSLColor((float)lerp(startColor.getHue(), endColor.getHue(), i/(float)(text.length()-1-spaces)),
                                        (float)lerp(startColor.getSaturation(), endColor.getSaturation(), i/(float)(text.length()-1-spaces)),
                                        (float)lerp(startColor.getLuminance(), endColor.getLuminance(), i/(float)(text.length()-1-spaces)));
                                Color temp2 = HSLColor.toRGB(temp.getHue(), temp.getSaturation(), temp.getLuminance());
                                String hex = String.format("%02x%02x%02x", temp2.getRed(), temp2.getGreen(), temp2.getBlue());

                                System.out.println("COLOR INFO:");
                                System.out.println("" + i/(text.length()-1-spaces));
                                System.out.println("" + startColor.getHue() + " " + endColor.getHue() + " | " + lerp(startColor.getHue(), endColor.getHue(), i/(text.length()-1-spaces)));
                                System.out.println("" + startColor.getSaturation() + " " + endColor.getSaturation() + " | " + lerp(startColor.getSaturation(), endColor.getSaturation(), i/(text.length()-1-spaces)));
                                System.out.println("" + startColor.getLuminance() + " " + endColor.getLuminance() + " | " + lerp(startColor.getLuminance(), endColor.getLuminance(), i/(text.length()-1-spaces)));
                                System.out.println("hex: " + hex + " (last: " + lastHex + ")");

                                if (lastHex != hex) {
                                    lastHex = hex;
                                    String dfHex = "&x&" + String.join("&", hex.split(""));
                                    sb.append(dfHex);
                                }

                                Style colorStyle = Style.EMPTY.withColor(TextColor.fromRgb(temp2.getRGB()));
                                String colorName = "#" + hex;
                                LiteralText extra = new LiteralText(String.valueOf(c));
                                LiteralText hover = new LiteralText(colorName);
                                hover.append("\n§7Click to copy!");
                                extra.setStyle(colorStyle);
                                hover.setStyle(colorStyle);
                                extra.styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color hex " + colorName)));
                                extra.styled((style) -> style.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(hover)));
                                base.getSiblings().add(extra);

                                sb.append(c);
                                i++;
                            }
                            MinecraftClient.getInstance().keyboard.setClipboard(sb.toString());
                            ChatUtil.sendMessage("Copied text!", ChatType.SUCCESS);
                            mc.player.sendMessage(base, false);

                            if (DFInfo.currentState == DFInfo.State.DEV) {
                                mc.player.sendChatMessage("/txt " + sb.toString());
                            }

                            return 1;
                        })
                    )
                )
            )
        );
    }

    private double lerp(float x, float y, float p)
    {
        return x + (y - x) * p;
    }
}
