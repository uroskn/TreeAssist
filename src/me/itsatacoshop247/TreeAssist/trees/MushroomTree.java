package me.itsatacoshop247.TreeAssist.trees;

import java.util.ArrayList;
import java.util.List;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class MushroomTree extends BaseTree {
	public static Debugger debugger;
	private final int type;

	public MushroomTree(int type) {
		this.type = type;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	protected boolean hasPerms(Player player) {
		if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
			return true;
		}
		if (type == 99) {
			return player.hasPermission("treeassist.destroy.brownshroom");
		}
		if (type == 100) {
			return player.hasPermission("treeassist.destroy.redshroom");
		}
		return false;
	}

	@Override
	protected Block getBottom(Block block) {
		int counter = 1;
		do {
			if (block.getRelative(0, 0 - counter, 0).getTypeId() == type) {
				counter++;
			} else {
				bottom = block.getRelative(0, 1 - counter, 0);
				return bottom;
			}
		} while (block.getY() - counter > 0);

		bottom = null;
		return bottom;
	}

	@Override
	protected Block getTop(Block block) {
		int maxY = block.getWorld().getMaxHeight() + 10;
		int counter = 1;
		
		debug.i("getting top; type " + type);

		while (block.getY() + counter < maxY) {
			if (block.getRelative(0, counter, 0).getTypeId() != type || counter > 6) {
				top = block.getRelative(0, counter - 1, 0);
				debug.i("++");
				break;
			} else {
				counter++;
			}
		}
		debug.i("counter == " + counter);
		return (top != null) ? top.getRelative(0, 1, 0) : null;
	}

	@Override
	protected List<Block> calculate(final Block bottom, final Block top) {
		List<Block> list = new ArrayList<Block>();
		checkBlock(list, bottom, top, true, bottom.getData());
		return list;
	}

	@Override
	protected int isLeaf(Block block) {
		return 0;
	}

	@Override
	protected void getTrunks() {
	
	}

	@Override
	protected boolean willBeDestroyed() {
		switch (type) {
		case 99:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Brown Shroom");
		case 100:
			return Utils.plugin.getConfig()
					.getBoolean("Automatic Tree Destruction.Tree Types.Red Shroom");
		default:
			return true; // ugly branch messes
		}
	}

	@Override
	protected boolean willReplant() {
		if (!Utils.replantType((byte) type)) {
			return false;
		}
		return true;
	}

	@Override
	protected void handleSaplingReplace(int delay) {
		replaceSapling(delay, bottom);
	}

	private void replaceSapling(int delay, Block bottom) {
		// make sure that the block is not being removed later
		bottom.setType(Material.AIR);
		removeBlocks.remove(bottom);
		totalBlocks.remove(bottom);
		
		int saplingID = (type == 99) ? 39 : 40;
		
		Runnable b = new TreeAssistReplant(Utils.plugin, bottom, saplingID, (byte) 0);
		Utils.plugin.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(Utils.plugin, b,
						20 * delay);

		if (Utils.plugin.getConfig()
				.getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
			Utils.plugin.blockList.add(bottom.getLocation());
			Runnable X = new TreeAssistProtect(Utils.plugin,
					bottom.getLocation());

			Utils.plugin.getServer()
					.getScheduler()
					.scheduleSyncDelayedTask(
							Utils.plugin,
							X,
							20 * Utils.plugin.getConfig()
									.getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
		}
	}

	@Override
	protected void checkBlock(List<Block> list, Block block,
			Block top, boolean deep, byte origData) {

//		debug.i("cB " + Debugger.parse(block.getLocation()));
		if (block.getTypeId() != type) {
//			debug.i("out!");
			return;
		}
		
		if (block.getX() == top.getX() && block.getZ() == top.getZ()) {
//			debug.i("main trunk!");
			if (!deep) {
				// something else caught the main, return, this will be done later!
//				debug.i("not deep; out!");
				return;
			}
		}

		if (top.getY() < block.getY()) {
			return;
		}
		
		int margin = type == 99 ? 3 : 2;
		
		if (Math.abs(bottom.getX() - block.getX()) > margin
				|| Math.abs(bottom.getZ() - block.getZ()) > margin) {
			// more than 3 off. That's probably the next shroom already.
			return;
		}
		
		if (type == 100 && block.getRelative(0, -1, 0).getTypeId() == type) {
			// overhanging red blabla
			if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");
				if (block.getY() < bottom.getY() || block.getY() > top.getY()) {
					return;
				}
			}
		}

		if (list.contains(block)) {
//			debug.i("already added!");
			return;
		} else {
//			debug.i(">>>>>>>>>> adding! <<<<<<<<<<<");
			list.add(block);
		}
		
		for (BlockFace face : Utils.NEIGHBORFACES) {
			checkBlock(list, block.getRelative(face), top, false, origData);

			checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false, origData);
			checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false, origData);
		}

		if (!deep) {
//			debug.i("not deep, out!");
			return;
		}

		if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
			return;
		}

		checkBlock(list, block.getRelative(0, 1, 0), top, true, origData);
	}
	protected boolean checkFail(Block block) {
		return false;
	}

}
