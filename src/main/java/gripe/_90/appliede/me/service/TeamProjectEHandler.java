package gripe._90.appliede.me.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;

import gripe._90.appliede.AppliedE;

import cn.leomc.teamprojecte.TPTeam;
import cn.leomc.teamprojecte.TeamChangeEvent;
import cn.leomc.teamprojecte.TeamKnowledgeProvider;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.ITransmutationProxy;

class TeamProjectEHandler {
    private final Map<TPTeam, Supplier<IKnowledgeProvider>> sharingProviders = new HashMap<>();

    private TeamProjectEHandler() {
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> clear());
        MinecraftForge.EVENT_BUS.addListener((TeamChangeEvent event) -> {
            var team = event.getTeam();

            if (team != null && !team.isSharingEMC()) {
                sharingProviders.remove(team);
            }
        });
    }

    private boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
        if (!(provider.getValue().get() instanceof TeamKnowledgeProvider)) {
            return true;
        }

        var uuid = provider.getKey();
        var team = TPTeam.getOrCreateTeam(uuid);

        if (team.isSharingEMC() && !sharingProviders.containsKey(team)) {
            sharingProviders.put(team, () -> ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(team.getOwner()));
        }

        return !sharingProviders.containsKey(team);
    }

    private void clear() {
        sharingProviders.clear();
    }

    static class Proxy {
        private Object handler;

        Proxy() {
            if (AppliedE.isModLoaded("teamprojecte")) {
                handler = new TeamProjectEHandler();
            }
        }

        boolean notSharingEmc(Map.Entry<UUID, Supplier<IKnowledgeProvider>> provider) {
            return handler == null || ((TeamProjectEHandler) handler).notSharingEmc(provider);
        }

        void clear() {
            if (handler != null) {
                ((TeamProjectEHandler) handler).clear();
            }
        }
    }
}
