package eu.pb4.sgui.testmod;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.UUID;

public class SGuiTest implements ModInitializer {
	private static int test(CommandContext<CommandSourceStack> context) {
		try {
			ServerPlayer player = context.getSource().getPlayer();
			SimpleGui gui = new SimpleGui(MenuType.GENERIC_3x3, player, false) {
				@Override
				public boolean onClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action, GuiElementInterface element) {
					this.player.sendSystemMessage(Component.literal(type.toString()), false);
					return super.onClick(index, type, action, element);
				}
				
				@Override
				public void onTick() {
					this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.level().getGameTime() % 127)));
					super.onTick();
				}
				
				@Override
				public boolean canPlayerClose() {
					return false;
				}
			};
			
			gui.setTitle(Component.literal("Nice"));
			gui.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount(100));
			gui.setSlot(1,
					new AnimatedGuiElement(new ItemStack[] { Items.NETHERITE_PICKAXE.getDefaultInstance(), Items.DIAMOND_PICKAXE.getDefaultInstance(), Items.GOLDEN_PICKAXE.getDefaultInstance(), Items.IRON_PICKAXE.getDefaultInstance(), Items.STONE_PICKAXE.getDefaultInstance(), Items.WOODEN_PICKAXE.getDefaultInstance() },
							10,
							false,
							(x, y, z) -> {}
					)
			           );
			
			gui.setSlot(2,
					new AnimatedGuiElementBuilder().setItem(Items.NETHERITE_AXE)
					                               .setDamage(150)
					                               .saveItemStack()
					                               .setItem(Items.DIAMOND_AXE)
					                               .setDamage(150)
					                               .unbreakable()
					                               .saveItemStack()
					                               .setItem(Items.GOLDEN_AXE)
					                               .glow()
					                               .saveItemStack()
					                               .setItem(Items.IRON_AXE)
					                               .enchant(Enchantments.AQUA_AFFINITY, 1)
					                               .hideFlags()
					                               .saveItemStack()
					                               .setItem(Items.STONE_AXE)
					                               .saveItemStack()
					                               .setItem(Items.WOODEN_AXE)
					                               .saveItemStack()
					                               .setInterval(10)
					                               .setRandom(true)
			           );
			
			for (int x = 3; x < gui.getSize(); x++) {
				ItemStack itemStack = Items.STONE.getDefaultInstance();
				itemStack.setCount(x);
				gui.setSlot(x, new GuiElement(itemStack, (index, clickType, actionType) -> {}));
			}
			
			gui.setSlot(5,
					new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(
							"ewogICJ0aW1lc3RhbXAiIDogMTYxOTk3MDIyMjQzOCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI0OGVhYTQxNGNjZjA1NmJhOTY5ZTdkODAxZmI2YTkyNzhkMGZlYWUxOGUyMTczNTZjYzhhOTQ2NTY0MzU1ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
							null,
							null
					                                                      ).setName(Component.literal("Battery")).glow()
			           );
			
			gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server).setName(Component.literal("Patbox's Head")).glow());
			
			gui.setSlot(7,
					new GuiElementBuilder().setItem(Items.BARRIER)
					                       .glow()
					                       .setName(Component.literal("Bye").setStyle(Style.EMPTY.withItalic(false).withBold(true)))
					                       .addLoreLine(Component.literal("Some lore"))
					                       .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
					                       .setCount(3)
					                       .setCallback((index, clickType, actionType) -> gui.close())
			           );
			
			gui.setSlot(8,
					new GuiElementBuilder().setItem(Items.TNT)
					                       .glow()
					                       .setName(Component.literal("Test :)").setStyle(Style.EMPTY.withItalic(false).withBold(true)))
					                       .addLoreLine(Component.literal("Some lore"))
					                       .addLoreLine(Component.literal("More lore").withStyle(ChatFormatting.RED))
					                       .setCount(1)
					                       .setCallback((index, clickType, actionType) -> {
						                       player.sendSystemMessage(Component.literal("derg "), false);
						                       ItemStack item = gui.getSlot(index).getItemStack();
						                       if (clickType == ClickType.MOUSE_LEFT) {
							                       item.setCount(item.getCount() == 1 ? item.getCount() : item.getCount() - 1);
						                       } else if (clickType == ClickType.MOUSE_RIGHT) {
							                       item.setCount(item.getCount() + 1);
						                       }
						                       ((GuiElement) gui.getSlot(index)).setItemStack(item);
						                       
						                       if (item.getCount() <= player.getEnderChestInventory().getContainerSize()) {
							                       gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), item.getCount() - 1, 0, 0));
						                       }
					                       })
			           );
			gui.setSlotRedirect(4, new Slot(player.getEnderChestInventory(), 0, 0, 0));
			
			gui.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("test").executes(SGuiTest::test));
		});
	}
}