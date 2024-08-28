package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ScreenProperty;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.play.server.SWindowPropertyPacket;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

@SuppressWarnings({"unused"})
public interface GuiInterface {

    /**
     * Sets the title of the gui.
     *
     * @param title the new title
     */
    void setTitle(ITextComponent title);

    /**
     * Returns the title of the gui.
     *
     * @return the title of the gui or <code>null</code> if not set
     */
    @Nullable
    ITextComponent getTitle();

    /**
     * Returns the {@link net.minecraft.world.inventory.AbstractContainerMenu} type that will be sent to the client. <br>
     * The other GUI data should match what the client would expect for this handler (slot count, ect).
     *
     * @return the screen handler type
     */
    ContainerType<?> getType();

    /**
     * Returns the player this gui was constructed for.
     *
     * @return the player
     */
    ServerPlayerEntity getPlayer();

    /**
     * Returns the sync id used for communicating information about this screen between the server and client.
     *
     * @return the sync id or <code>-1</code> if the screen has not been opened
     */
    int getSyncId();

    /**
     * Returns <code>true</code> the screen is currently open on te players screen
     *
     * @return <code>true</code> the screen is open
     */
    boolean isOpen();

    /**
     * Opens the screen for the player.
     *
     * @return <code>true</code> if the screen successfully opened
     * @see GuiInterface#onOpen()
     */
    boolean open();

    boolean getAutoUpdate();

    void setAutoUpdate(boolean value);

    /**
     * Used internally for closing the gui.
     *
     * @param alreadyClosed Is set to true, if gui's ScreenHandler is already closed
     * @see GuiInterface#onClose()
     */
    void close(boolean alreadyClosed);

    /**
     * Closes the current gui
     *
     * @see GuiInterface#onClose()
     */
    default void close() {
        this.close(false);
    }

    /**
     * Executes when the screen is opened
     */
    default void beforeOpen() {
    }

    /**
     * Executes when the screen is opened
     */
    default void afterOpen() {
    }

    /**
     * Executes when the screen is opened
     */
    default void onOpen() {
    }

    /**
     * Executes when the screen is closed
     */
    default void onClose() {
    }

    /**
     * Executes each tick while the screen is open
     */
    default void onTick() {
    }

    default boolean canPlayerClose() {
        return true;
    }

    default void handleException(Throwable throwable) {
        throwable.printStackTrace();
    }


    /**
     * Send additional properties to the GUI.
     *
     * @param property the property to adjust
     * @param value    the value of the property to send
     * @throws IllegalArgumentException if the property is not valid for this GUI
     * @since 0.4.0
     */
    default void sendProperty(ScreenProperty property, int value) {
        if (!property.validFor(this.getType())) {
            throw new IllegalArgumentException(String.format("The property '%s' is not valid for the handler '%s'", property.name(), Registry.MENU.getKey(this.getType())));
        }
        if (this.isOpen()) {
            this.getPlayer().connection.send(new SWindowPropertyPacket(this.getSyncId(), property.id(), value));
        }
    }

    default void sendRawProperty(int id, int value) {
        if (this.isOpen()) {
            this.getPlayer().connection.send(new SWindowPropertyPacket(this.getSyncId(), id, value));
        }
    }

    default boolean resetMousePosition() {
        return false;
    }
}