package github.mc.storage.utils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockMapping {

    private static final List<Block> TARGET_BLOCKS = new ArrayList<>();

    static {
        // 0-15: 16种羊毛
        TARGET_BLOCKS.add(Blocks.WHITE_WOOL);
        TARGET_BLOCKS.add(Blocks.ORANGE_WOOL);
        TARGET_BLOCKS.add(Blocks.MAGENTA_WOOL);
        TARGET_BLOCKS.add(Blocks.LIGHT_BLUE_WOOL);
        TARGET_BLOCKS.add(Blocks.YELLOW_WOOL);
        TARGET_BLOCKS.add(Blocks.LIME_WOOL);
        TARGET_BLOCKS.add(Blocks.PINK_WOOL);
        TARGET_BLOCKS.add(Blocks.GRAY_WOOL);
        TARGET_BLOCKS.add(Blocks.LIGHT_GRAY_WOOL);
        TARGET_BLOCKS.add(Blocks.CYAN_WOOL);
        TARGET_BLOCKS.add(Blocks.PURPLE_WOOL);
        TARGET_BLOCKS.add(Blocks.BLUE_WOOL);
        TARGET_BLOCKS.add(Blocks.BROWN_WOOL);
        TARGET_BLOCKS.add(Blocks.GREEN_WOOL);
        TARGET_BLOCKS.add(Blocks.RED_WOOL);
        TARGET_BLOCKS.add(Blocks.BLACK_WOOL);

        // 16-31: 16种玻璃
        TARGET_BLOCKS.add(Blocks.WHITE_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.ORANGE_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.MAGENTA_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.LIGHT_BLUE_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.YELLOW_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.LIME_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.PINK_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.GRAY_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.LIGHT_GRAY_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.CYAN_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.PURPLE_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.BLUE_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.BROWN_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.GREEN_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.RED_STAINED_GLASS);
        TARGET_BLOCKS.add(Blocks.BLACK_STAINED_GLASS);

        // 32-47: 16种混凝土
        TARGET_BLOCKS.add(Blocks.WHITE_CONCRETE);
        TARGET_BLOCKS.add(Blocks.ORANGE_CONCRETE);
        TARGET_BLOCKS.add(Blocks.MAGENTA_CONCRETE);
        TARGET_BLOCKS.add(Blocks.LIGHT_BLUE_CONCRETE);
        TARGET_BLOCKS.add(Blocks.YELLOW_CONCRETE);
        TARGET_BLOCKS.add(Blocks.LIME_CONCRETE);
        TARGET_BLOCKS.add(Blocks.PINK_CONCRETE);
        TARGET_BLOCKS.add(Blocks.GRAY_CONCRETE);
        TARGET_BLOCKS.add(Blocks.LIGHT_GRAY_CONCRETE);
        TARGET_BLOCKS.add(Blocks.CYAN_CONCRETE);
        TARGET_BLOCKS.add(Blocks.PURPLE_CONCRETE);
        TARGET_BLOCKS.add(Blocks.BLUE_CONCRETE);
        TARGET_BLOCKS.add(Blocks.BROWN_CONCRETE);
        TARGET_BLOCKS.add(Blocks.GREEN_CONCRETE);
        TARGET_BLOCKS.add(Blocks.RED_CONCRETE);
        TARGET_BLOCKS.add(Blocks.BLACK_CONCRETE);

        // 48-63: 16种陶瓦
        TARGET_BLOCKS.add(Blocks.WHITE_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.ORANGE_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.MAGENTA_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.LIGHT_BLUE_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.YELLOW_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.LIME_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.PINK_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.GRAY_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.LIGHT_GRAY_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.CYAN_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.PURPLE_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.BLUE_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.BROWN_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.GREEN_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.RED_TERRACOTTA);
        TARGET_BLOCKS.add(Blocks.BLACK_TERRACOTTA);
    }

    /**
     * Returns the block at the specified index.
     *
     * @param index The index of the block (0-63).
     * @return The Block at the given index, or null if the index is out of bounds.
     */
    public static Optional<Block> getBlock(int index) {
        if (index >= 0 && index < TARGET_BLOCKS.size()) {
            return Optional.ofNullable(TARGET_BLOCKS.get(index));
        }
        return Optional.empty();
    }

    /**
     * Returns the index of the given block.
     *
     * @param block The block to find the index for.
     * @return The index of the block, or -1 if not found.
     */
    public static int getBlockIndex(Block block) {
        return TARGET_BLOCKS.indexOf(block);
    }
}