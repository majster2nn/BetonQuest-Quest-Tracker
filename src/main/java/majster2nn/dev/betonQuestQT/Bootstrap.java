package majster2nn.dev.betonQuestQT;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

@SuppressWarnings("UnstableApiUsage")
public class Bootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext bootstrapContext) {
        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            CommandList commandList = new CommandList();
            for(LiteralCommandNode node : commandList.commandBuilders()){
                commands.registrar().register(node);
            }
        });
    }
}
