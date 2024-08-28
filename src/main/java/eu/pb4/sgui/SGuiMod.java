package eu.pb4.sgui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.*;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.UUID;

@Mod("sgui")
public class SGuiMod {
	private static int test(CommandContext<CommandSource> context) {
		try {
			ServerPlayerEntity player = context.getSource().getPlayerOrException();
			SimpleGui gui = new SimpleGui(ContainerType.GENERIC_3x3, player, false) {
				@Override
				public boolean onClick(int index, ClickType type, net.minecraft.inventory.container.ClickType action, GuiElementInterface element) {
					this.player.sendMessage(new StringTextComponent(type.toString()), Util.NIL_UUID);
					return super.onClick(index, type, action, element);
				}
				
				@Override
				public void onTick() {
					this.setSlot(0, new GuiElementBuilder(Items.ARROW).setCount((int) (player.level.getGameTime() % 127)));
					super.onTick();
				}
				
				@Override
				public boolean canPlayerClose() {
					return false;
				}
			};
			
			gui.setTitle(new StringTextComponent("Nice"));
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
					                                                      ).setName(new StringTextComponent("Battery")).glow()
			           );
			
			gui.setSlot(6, new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(new GameProfile(UUID.fromString("f5a216d9-d660-4996-8d0f-d49053677676"), "patbox"), player.server).setName(new StringTextComponent("Patbox's Head")).glow());
			
			gui.setSlot(7,
					new GuiElementBuilder().setItem(Items.BARRIER)
					                       .glow()
					                       .setName(new StringTextComponent("Bye").setStyle(Style.EMPTY.withItalic(false).withBold(true)))
					                       .addLoreLine(new StringTextComponent("Some lore"))
					                       .addLoreLine(new StringTextComponent("More lore").withStyle(TextFormatting.RED))
					                       .setCount(3)
					                       .setCallback((index, clickType, actionType) -> gui.close())
			           );
			
			gui.setSlot(8,
					new GuiElementBuilder().setItem(Items.TNT)
					                       .glow()
					                       .setName(new StringTextComponent("Test :)").setStyle(Style.EMPTY.withItalic(false).withBold(true)))
					                       .addLoreLine(new StringTextComponent("Some lore"))
					                       .addLoreLine(new StringTextComponent("More lore").withStyle(TextFormatting.RED))
					                       .setCount(1)
					                       .setCallback((index, clickType, actionType) -> {
						                       player.sendMessage(new StringTextComponent("derg "), Util.NIL_UUID);
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
	
	public SGuiMod() {
		if (!FMLEnvironment.production) {
			MinecraftForge.EVENT_BUS.register(this);
		}
	}
	
	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("test").executes(SGuiMod::test));
	}
}