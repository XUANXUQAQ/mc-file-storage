package github.mc.storage.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class BlockGenerator {

    /**
     * 将 Base64 数据编码为方块结构
     * 每个字符用 6 位表示 (Base64: 0-63)
     * 每层是 64x64 的平面，从下往上堆叠
     *
     * @param startPos   起始位置（底层中心）
     * @param base64Data Base64 编码的数据
     */
    public static void generateBlocks(BlockPos startPos, String base64Data) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }

        Level level = client.level;

        // Base64 字符到索引的映射 (0-63)
        int charIndex = 0;
        int layer = 0;

        for (char c : base64Data.toCharArray()) {
            // 将 Base64 字符转换为 0-63 的索引
            int blockTypeIndex = base64CharToIndex(c);
            if (blockTypeIndex < 0) {
                charIndex++;
                continue; // 跳过无效字符
            }

            // 计算在当前层中的位置
            int posInLayer = charIndex % (64 * 64); // 每层最多 64*64 = 4096 个方块
            int x = posInLayer % 64;
            int z = (posInLayer / 64) % 64;

            // 如果填满一层，移到下一层
            if (charIndex > 0 && charIndex % (64 * 64) == 0) {
                layer++;
            }

            // 获取对应的方块类型
            Optional<Block> blockOpt = BlockMapping.getBlock(blockTypeIndex);
            if (blockOpt.isEmpty()) {
                charIndex++;
                continue;
            }

            Block block = blockOpt.get();

            // 计算实际位置（从底层中心向外扩展）
            BlockPos pos = startPos.offset(x - 32, layer, z - 32);

            // 放置方块
            level.setBlock(pos, block.defaultBlockState(), 3);

            charIndex++;
        }
    }

    /**
     * 从方块结构还原 Base64 数据
     *
     * @param startPos 起始位置（底层中心）
     * @param layers   层数
     * @return Base64 编码的字符串
     */
    public static String restoreFromBlocks(BlockPos startPos, int layers) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return "";
        }

        Level level = client.level;
        StringBuilder base64Data = new StringBuilder();

        // 从下往上逐层读取
        for (int layer = 0; layer < layers; layer++) {
            for (int z = 0; z < 64; z++) {
                for (int x = 0; x < 64; x++) {
                    BlockPos pos = startPos.offset(x - 32, layer, z - 32);
                    Block block = level.getBlockState(pos).getBlock();

                    // 根据方块类型获取索引
                    int blockIndex = BlockMapping.getBlockIndex(block);
                    if (blockIndex >= 0 && blockIndex < 64) {
                        // 将索引转换回 Base64 字符
                        char c = indexToBase64Char(blockIndex);
                        base64Data.append(c);
                    }
                }
            }
        }

        return base64Data.toString();
    }

    /**
     * 将 Base64 字符转换为 0-63 的索引
     */
    private static int base64CharToIndex(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';           // 0-25
        if (c >= 'a' && c <= 'z') return c - 'a' + 26;      // 26-51
        if (c >= '0' && c <= '9') return c - '0' + 52;      // 52-61
        if (c == '+') return 62;
        if (c == '/') return 63;
        return -1; // 无效字符
    }

    /**
     * 将 0-63 的索引转换为 Base64 字符
     */
    private static char indexToBase64Char(int index) {
        if (index >= 0 && index <= 25) return (char) ('A' + index);
        if (index >= 26 && index <= 51) return (char) ('a' + (index - 26));
        if (index >= 52 && index <= 61) return (char) ('0' + (index - 52));
        if (index == 62) return '+';
        if (index == 63) return '/';
        return '='; // 填充字符
    }

    /**
     * 在玩家当前位置前方生成方块
     *
     * @param base64Data Base64 编码的数据
     */
    public static void generateBlocksAtPlayer(String base64Data) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        // 在玩家脚下位置开始生成
        BlockPos playerPos = client.player.blockPosition();
        BlockPos startPos = playerPos.relative(client.player.getDirection(), 5);

        generateBlocks(startPos, base64Data);
    }

    /**
     * 计算需要的层数
     */
    public static int calculateLayers(String base64Data) {
        int totalBlocks = base64Data.length();
        return (int) Math.ceil((double) totalBlocks / (64.0 * 64.0));
    }
}