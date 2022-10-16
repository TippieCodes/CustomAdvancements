package me.tippie.customadvancements.commands;

import lombok.val;
import me.tippie.customadvancements.CustomAdvancements;
import me.tippie.customadvancements.advancement.AdvancementTree;
import me.tippie.customadvancements.advancement.InvalidAdvancementException;
import me.tippie.customadvancements.player.CAPlayer;
import me.tippie.customadvancements.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.tippie.customadvancements.commands.CommandSet.getStrings;

/**
 * Represents the '/ca checkprogress' command.
 * This command checks the progress of an advancement.
 */
public class CommandCheckProgress extends SubCommand {

	CommandCheckProgress() {
		super("checkprogress", "customadvancements.command.checkprogress", Lang.COMMAND_DESC_CHECK_PROGRESS.getConfigValue(null, true), Lang.COMMAND_CHECK_PROGRESS_USAGE.getConfigValue(null, true), new ArrayList<>());
	}

	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
		if (args.length == 2) {
			val trees = CustomAdvancements.getAdvancementManager().getAdvancementTrees();
			final List<String> result = new ArrayList<>();
			for (final AdvancementTree tree : trees) result.add(tree.getLabel());
			return result;
		} else if (args.length == 3) {
			try {
				val treeLabel = args[1];
				return getStrings(treeLabel);
			} catch (final InvalidAdvancementException ex) {
				return new ArrayList<>();
			}
		} else if (args.length == 4) {
			return null;
		}
		return new ArrayList<>();
	}

	@Override
	public void execute(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!((args.length == 3) || (args.length == 4))) {
			sender.sendMessage(Lang.COMMAND_INVALID_USAGE.getConfigValue(new String[]{getUsage()}));
			return;
		}
		val treeLabel = args[1];
		val advancementLabel = args[2];
		val path = treeLabel + "." + advancementLabel;
		var playername = "";
		final CAPlayer player;
		if (args.length == 4 && sender.hasPermission("customadvancements.command.checkprogress.others")) {
			try {
				val uuid = Bukkit.getPlayer(args[3]).getUniqueId();
				player = CustomAdvancements.getCaPlayerManager().getPlayer(uuid);
				if (player == null) throw new NullPointerException();
				playername = Bukkit.getPlayer(uuid).getName();
			} catch (final NullPointerException ex) {
				sender.sendMessage(Lang.COMMAND_INVALID_PLAYER.getConfigValue(null));
				return;
			}
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must provided an username to use this command from the console.");
				return;
			}
			playername = sender.getName();
			player = CustomAdvancements.getCaPlayerManager().getPlayer(((Player) sender).getUniqueId());
		}
		if (player.getAdvancementProgress().get(path) == null) {
			sender.sendMessage(Lang.COMMAND_INVALID_ADVANCEMENT.getConfigValue(null));
			return;
		}
		try {
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_HEADER.getConfigValue(null, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_USER.getConfigValue(new String[]{playername}, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_TREE.getConfigValue(new String[]{treeLabel}, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_ADVANCEMENT.getConfigValue(new String[]{advancementLabel}, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_PROGRESS.getConfigValue(new String[]{String.valueOf(player.getProgress(path)), String.valueOf(CustomAdvancements.getAdvancementManager().getAdvancement(path).getMaxProgress())}, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_ACTIVE.getConfigValue(new String[]{String.valueOf(player.checkIfAdvancementActive(path))}, true));
			sender.sendMessage(Lang.COMMAND_CHECK_PROGRESS_COMPLETED.getConfigValue(new String[]{String.valueOf(player.checkIfAdvancementCompleted(path))}, true));
		} catch (final InvalidAdvancementException ex) {
			sender.sendMessage(Lang.COMMAND_INVALID_ADVANCEMENT.getConfigValue(null));
		}
	}
}
