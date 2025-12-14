package github.mc.storage.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Optional;
import java.util.function.Consumer;

public class BlockGenerator {

    private static final int MAX_LAYERS = 256;

    /**
     * 异步生成方块，逐层显示进度
     *
     * @param startPos         起始位置
     * @param base64Data       Base64数据
     * @param direction        方向
     * @param progressCallback 进度回调函数
     */
    public static void generateBlocksAsync(BlockPos startPos, String base64Data, Direction direction, Consumer<String> progressCallback) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        Level level = client.level;

        // 计算需要的层数
        int totalLayers = calculateLayers(base64Data);

        // 检查是否超过高度限制
        if (totalLayers > MAX_LAYERS) {
            progressCallback.accept("§c错误: 数据需要 " + totalLayers + " 层，超过了 " + MAX_LAYERS + " 层的限制!");
            progressCallback.accept("§e建议: 请减小文件大小或分批处理");
            return;
        }

        // 首先生成方向标记
        generateDirectionMarkers(level, startPos, direction);

        // 异步逐层生成
        Thread.ofVirtual().start(() -> {
            try {
                int charIndex = 0;
                int currentLayer = 1; // 从第1层开始，第0层用于方向标记

                for (char c : base64Data.toCharArray()) {
                    // 将 Base64 字符转换为 0-63 的索引
                    int blockTypeIndex = base64CharToIndex(c);
                    if (blockTypeIndex < 0) {
                        charIndex++;
                        continue; // 跳过无效字符
                    }

                    // 计算在当前层中的位置
                    int posInLayer = charIndex % (64 * 64); // 每层最多 64*64 = 4096 个方块
                    int localX = posInLayer % 64;
                    int localZ = (posInLayer / 64) % 64;

                    // 检测是否需要切换到下一层
                    int newLayer = 1 + (charIndex / (64 * 64));
                    if (newLayer != currentLayer) {
                        currentLayer = newLayer;
                        // 更新进度
                        int finalCurrentLayer = currentLayer;
                        client.execute(() -> progressCallback.accept("§a正在生成第 " + finalCurrentLayer + "/" + totalLayers + " 层..."));
                        // 添加延迟，让玩家能看到生成动画
                        Thread.sleep(50);
                    }

                    Optional<Block> blockOpt = BlockMapping.getBlock(blockTypeIndex);
                    if (blockOpt.isEmpty()) {
                        charIndex++;
                        continue;
                    }

                    Block block = blockOpt.get();

                    BlockPos pos = switch (direction) {
                        case NORTH -> startPos.offset(localX - 32, currentLayer, -localZ);
                        case SOUTH -> startPos.offset(localX - 32, currentLayer, localZ);
                        case WEST -> startPos.offset(-localZ, currentLayer, localX - 32);
                        case EAST -> startPos.offset(localZ, currentLayer, localX - 32);
                        default -> startPos.offset(localX - 32, currentLayer, localZ);
                    };

                    // 在主线程中放置方块
                    client.execute(() -> level.setBlock(pos, block.defaultBlockState(), 3));

                    charIndex++;
                }

                // 生成完成
                client.execute(() -> progressCallback.accept("§a方块生成完成! 共 " + totalLayers + " 层"));

            } catch (InterruptedException e) {
                client.execute(() -> progressCallback.accept("§c生成被中断: " + e.getMessage()));
            }
        });
    }

    /**
     * 生成方向标记
     * 在最底层(y=0)放置L形红石块标记
     */
    private static void generateDirectionMarkers(Level level, BlockPos startPos, Direction direction) {
        // L形标记的相对位置
        int[][] markerPositions = switch (direction) {
            case NORTH -> new int[][]{{0, 0}, {1, 0}, {0, 1}}; // L开口朝北
            case SOUTH -> new int[][]{{0, 0}, {-1, 0}, {0, -1}}; // L开口朝南
            case EAST -> new int[][]{{0, 0}, {0, 1}, {-1, 0}}; // L开口朝东
            case WEST -> new int[][]{{0, 0}, {0, -1}, {1, 0}}; // L开口朝西
            default -> new int[][]{{0, 0}, {1, 0}, {0, 1}};
        };

        // 在起始位置放置L形红石块标记
        for (int[] pos : markerPositions) {
            BlockPos markerPos = startPos.offset(pos[0], 0, pos[1]);
            level.setBlock(markerPos, BlockMapping.MARKER_BLOCK.defaultBlockState(), 3);
        }
    }

    /**
     * 从方块结构还原 Base64 数据
     * 自动检测方向标记和层数，读取直到遇到空层
     *
     * @param searchStartPos 搜索起始位置
     * @return Base64 编码的字符串
     */
    public static String restoreFromBlocks(BlockPos searchStartPos) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return "";
        }

        Level level = client.level;

        // 搜索方向标记并确定实际起始位置和方向
        DirectionInfo directionInfo = findDirectionMarkers(level, searchStartPos);
        if (directionInfo == null) {
            return ""; // 未找到方向标记
        }

        BlockPos startPos = directionInfo.startPos;
        Direction direction = directionInfo.direction;

        StringBuilder base64Data = new StringBuilder();

        // 从第1层开始读取（第0层是方向标记），一直读取直到遇到空层
        int layer = 1;

        while (layer <= MAX_LAYERS) {
            boolean hasValidBlocks = false;

            for (int localZ = 0; localZ < 64; localZ++) {
                for (int localX = 0; localX < 64; localX++) {
                    // 根据方向计算实际位置
                    BlockPos pos = switch (direction) {
                        case NORTH -> startPos.offset(localX - 32, layer, -localZ);
                        case SOUTH -> startPos.offset(localX - 32, layer, localZ);
                        case WEST -> startPos.offset(-localZ, layer, localX - 32);
                        case EAST -> startPos.offset(localZ, layer, localX - 32);
                        default -> startPos.offset(localX - 32, layer, localZ);
                    };

                    Block block = level.getBlockState(pos).getBlock();

                    // 根据方块类型获取索引
                    int blockIndex = BlockMapping.getBlockIndex(block);
                    if (blockIndex >= 0 && blockIndex < 64) {
                        // 将索引转换回 Base64 字符
                        char c = indexToBase64Char(blockIndex);
                        base64Data.append(c);
                        hasValidBlocks = true;
                    }
                }
            }

            // 如果这一层没有任何有效方块，说明读取完成
            if (!hasValidBlocks) {
                break;
            }

            layer++;
        }

        return base64Data.toString();
    }

    /**
     * 搜索方向标记并返回方向信息
     */
    private static DirectionInfo findDirectionMarkers(Level level, BlockPos searchCenter) {
        // 在搜索中心周围寻找L形红石块标记
        int searchRadius = 10;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = -5; y <= 5; y++) { // 在垂直方向也搜索一定范围
                    BlockPos testPos = searchCenter.offset(x, y, z);

                    // 检查是否为红石块
                    if (level.getBlockState(testPos).getBlock() == BlockMapping.MARKER_BLOCK) {
                        // 尝试识别L形标记的方向
                        Direction direction = detectMarkerDirection(level, testPos);
                        if (direction != null) {
                            return new DirectionInfo(testPos, direction);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 检测L形标记的方向
     */
    private static Direction detectMarkerDirection(Level level, BlockPos centerPos) {
        // 检查各个方向的L形模式
        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        for (Direction direction : directions) {
            int[][] markerPositions = switch (direction) {
                case NORTH -> new int[][]{{0, 0}, {1, 0}, {0, 1}};
                case SOUTH -> new int[][]{{0, 0}, {-1, 0}, {0, -1}};
                case EAST -> new int[][]{{0, 0}, {0, 1}, {-1, 0}};
                case WEST -> new int[][]{{0, 0}, {0, -1}, {1, 0}};
                default -> new int[][]{{0, 0}, {1, 0}, {0, 1}};
            };

            boolean allMatch = true;
            for (int[] pos : markerPositions) {
                BlockPos checkPos = centerPos.offset(pos[0], 0, pos[1]);
                if (level.getBlockState(checkPos).getBlock() != BlockMapping.MARKER_BLOCK) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                return direction;
            }
        }

        return null;
    }

    /**
     * 方向信息类
     */
    private record DirectionInfo(BlockPos startPos, Direction direction) {
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
     * 在玩家当前位置前方异步生成方块
     *
     * @param base64Data       Base64 编码的数据
     * @param progressCallback 进度回调
     */
    public static void generateBlocksAtPlayerAsync(String base64Data, Consumer<String> progressCallback) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        // 从玩家前方2格处开始生成（避免卡在玩家身上）
        BlockPos playerPos = client.player.blockPosition();
        Direction direction = client.player.getDirection();
        BlockPos startPos = playerPos.relative(direction, 2);

        generateBlocksAsync(startPos, base64Data, direction, progressCallback);
    }

    /**
     * 计算需要的层数
     */
    public static int calculateLayers(String base64Data) {
        int totalBlocks = base64Data.length();
        return (int) Math.ceil((double) totalBlocks / (64.0 * 64.0)) + 1; // +1 for marker layer
    }
}