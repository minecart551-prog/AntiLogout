package org.samo_lego.antilogout.mixin;

import io.netty.channel.Channel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.samo_lego.antilogout.AntiLogout;
import org.samo_lego.antilogout.datatracker.ILogoutRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.antilogout.AntiLogout.config;


@Mixin(Connection.class)
public abstract class MConnection {

    @Shadow
    private Channel channel;

    @Shadow
    public abstract PacketListener getPacketListener();

    /**
     * This method gets called when PLAYER wants to disconnect
     *
     * @param ci
     */
    @Inject(method = "handleDisconnection", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/chat/Component;)V", ordinal = 1), cancellable = true)
    private void al_handleDisconnection(CallbackInfo ci) {
        if (this.getPacketListener() instanceof ServerGamePacketListenerImpl listener) {
            ILogoutRules rules = (ILogoutRules) listener.getPlayer();
            
            // Check if we should prevent disconnect (combat log or grace period)
            boolean shouldPreventDisconnect = !rules.al_allowDisconnect() || 
                                              (config.general.logoutGracePeriod > 0);
            
            if (shouldPreventDisconnect) {
                this.channel.close();
                rules.al_onRealDisconnect();
                ci.cancel();
            }
        }
    }
}
