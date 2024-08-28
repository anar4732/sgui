package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.virtual.FakeContainerMenu;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SOpenSignMenuPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Sign Gui Implementation
 * <p>
 * The Vanilla sign GUI does not use a {@link net.minecraft.world.inventory.AbstractContainerMenu} and thus
 * it gets its data directly from a block in the world. Due to this, before opening the
 * screen the server must send a fake 'ghost' sign block to the player which contains the data
 * we want the sign to show. We send the block at the players location at max world height
 * so it hopefully goes unnoticed. The fake block is removed when the GUI is closed.
 * This also means in order to refresh the data on the sign, we must close and re-open the GUI,
 * as only handled screens have property support.
 * On the server side however, this sign GUI uses a custom {@link FakeContainerMenu} so the server
 * can manage and trigger methods like onTIck, onClose, ect.
 * <p>
 * SignGui has lots of deprecated methods which have no function, mainly due to the lack of
 * item slots and a client ScreenHandler.
 */
public class SignGui implements GuiInterface {
	
	protected final SignTileEntity signEntity = new SignTileEntity();
    protected BlockState type = Blocks.OAK_SIGN.defaultBlockState();
    protected boolean autoUpdate = true;

    protected List<Integer> sendLineUpdate = new ArrayList<>(4);
    protected final ServerPlayerEntity player;
    protected boolean open = false;
    protected boolean reOpen = false;
    protected FakeContainerMenu screenHandler;
    private final ITextComponent[] texts = new ITextComponent[4];

    /**
     * Constructs a new SignGui for the provided player
     *
     * @param player the player to serve this gui to
     */
    public SignGui(ServerPlayerEntity player)  {
        this.player = player;
        this.signEntity.setPosition(new BlockPos(player.blockPosition().getX(), Math.min(player.level.getMaxBuildHeight() - 1, player.blockPosition().getY() + 5), player.blockPosition().getZ()));
    }

    /**
     * Sets a line of {@link Component} on the sign.
     *
     * @param line the line index, from 0
     * @param text the Text for the line, note that all formatting is stripped when the player closes the sign
     */
    public void setLine(int line, ITextComponent text) {
        this.signEntity.setMessage(line, text);
        this.sendLineUpdate.add(line);
        this.texts[line] = text;

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Returns the {@link Component} from a line on the sign.
     *
     * @param line the line number
     * @return the text on the line
     */
    public ITextComponent getLine(int line) {
        return this.texts[line];
    }

    /**
     * Sets default color for the sign text.
     *
     * @param color the default sign color
     */
    public void setColor(DyeColor color) {
        this.signEntity.color = color;

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Sets the block model used for the sign background.
     *
     * @param type a block in the {@link BlockTags#SIGNS} tag
     */
    public void setSignType(Block type) {
        if (!type.is(BlockTags.SIGNS)) {
            throw new IllegalArgumentException("The type must be a sign");
        }

        this.type = type.defaultBlockState();

        if (this.open && this.autoUpdate) {
            this.updateSign();
        }
    }

    /**
     * Sends sign updates to the player. <br>
     * This requires closing and reopening the gui, causing a flicker.
     */
    public void updateSign() {
        if (this.player.containerMenu == this.screenHandler) {
            this.reOpen = true;
            this.player.connection.send(new SCloseWindowPacket(this.screenHandler.containerId));
        } else {
            this.open();
        }
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return this.player;
    }
	
    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    public boolean open() {
        this.reOpen = true;

        if (this.player.containerMenu != this.player.inventoryMenu && this.player.containerMenu != this.screenHandler) {
            this.player.closeContainer();
        }
        if (screenHandler == null) {
            this.screenHandler = new FakeContainerMenu(this);
        }
        this.player.containerMenu = this.screenHandler;

        this.player.connection.send(new SChangeBlockPacket(this.signEntity.getBlockPos(), this.type));
        this.player.connection.send(this.signEntity.getUpdatePacket());
        this.player.connection.send(new SOpenSignMenuPacket(this.signEntity.getBlockPos()));

        this.reOpen = false;
        this.open = true;

        return true;
    }

    @Override
    public void close(boolean alreadyClosed) {
        if (this.open && !this.reOpen) {
            this.open = false;
            this.reOpen = false;

            this.player.connection.send(new SChangeBlockPacket(player.level, signEntity.getBlockPos()));

            if (alreadyClosed && this.player.containerMenu == this.screenHandler) {
                this.player.doCloseContainer();
            } else {
                this.player.closeContainer();
            }

            this.onClose();
        } else {
            this.reOpen = false;
            this.open();
        }
    }

    @Override
    public boolean getAutoUpdate() {
        return this.autoUpdate;
    }

    @Override
    public void setAutoUpdate(boolean value) {
        this.autoUpdate = value;
    }

    /**
     * Used internally to receive input from the client
     */
    
    public void setLineInternal(int line, ITextComponent text) {
        if (this.reOpen && this.sendLineUpdate.contains(line)) {
            this.sendLineUpdate.remove((Integer) line);
        } else {
            this.signEntity.setMessage(line, text);
            this.texts[line] = text;
        }
    }

    @Deprecated
    @Override
    public void setTitle(ITextComponent title) {
    }

    @Deprecated
    @Override
    public ITextComponent getTitle() {
        return null;
    }

    @Deprecated
    @Override
    public ContainerType<?> getType() {
        return null;
    }

    @Deprecated
    @Override
    public int getSyncId() {
        return -1;
    }
}