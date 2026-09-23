package id.seria.gacha.api.sdk.plugin.commands;

import java.util.List;

public interface Command {
   String getName();

   String getDescription();

   List<String> getAliases();

   List<? extends Node> getNodes();
}
