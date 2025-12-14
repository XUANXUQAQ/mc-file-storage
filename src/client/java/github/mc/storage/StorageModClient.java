package github.mc.storage;

import github.mc.storage.utils.BlockGenerator;
import github.mc.storage.utils.DataProcessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageModClient implements ClientModInitializer {

    public static final String MOD_ID = "mc-storage";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // 注册 savetoblock 指令
            dispatcher.register(ClientCommandManager.literal("savetoblock")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        if (client.player == null) {
                            return 0;
                        }

                        try {
                            // 获取 MC 安装目录下的 storage_files 文件夹
                            Path minecraftDir = client.gameDirectory.toPath();
                            Path storageFilesDir = minecraftDir.resolve("storage_files");
                            File storageFolder = storageFilesDir.toFile();

                            if (!storageFolder.exists()) {
                                context.getSource().sendFeedback(Component.literal("§c错误: storage_files 文件夹不存在!"));
                                context.getSource().sendFeedback(Component.literal("§e请在 MC 安装目录下创建 storage_files 文件夹"));
                                return 0;
                            }

                            // 获取文件夹中的所有文件
                            File[] files = storageFolder.listFiles();
                            if (files == null || files.length == 0) {
                                context.getSource().sendFeedback(Component.literal("§c错误: storage_files 文件夹为空!"));
                                return 0;
                            }

                            List<File> fileList = new ArrayList<>(Arrays.asList(files));
                            context.getSource().sendFeedback(Component.literal("§a正在处理 " + fileList.size() + " 个文件..."));

                            // 压缩文件
                            var zippedDataOpt = DataProcessor.zipFiles(fileList);
                            if (zippedDataOpt.isEmpty()) {
                                context.getSource().sendFeedback(Component.literal("§c错误: 文件压缩失败!"));
                                return 0;
                            }

                            // 转换为 Base64
                            String base64Data = DataProcessor.encodeToBase64(zippedDataOpt.get());

                            context.getSource().sendFeedback(Component.literal("§a文件处理成功! Base64 长度: " + base64Data.length()));

                            // 计算需要的层数
                            int layers = BlockGenerator.calculateLayers(base64Data);
                            context.getSource().sendFeedback(Component.literal("§a将生成 " + layers + " 层方块"));

                            // 使用异步方法生成方块，显示进度
                            BlockGenerator.generateBlocksAtPlayerAsync(base64Data, message -> context.getSource().sendFeedback(Component.literal(message)));

                        } catch (IOException e) {
                            context.getSource().sendFeedback(Component.literal("§c错误: " + e.getMessage()));
                            LOGGER.error("error {}", e.getMessage(), e);
                            return 0;
                        }

                        return 1;
                    }));

            // 注册 restorefiles 指令
            dispatcher.register(ClientCommandManager.literal("loadfromblock")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        if (client.player == null) {
                            return 0;
                        }

                        try {
                            context.getSource().sendFeedback(Component.literal("§a正在自动检测并还原方块数据..."));

                            // 从玩家附近的方块还原数据，自动检测方向标记和层数
                            BlockPos playerPos = client.player.blockPosition();
                            BlockPos searchPos = playerPos.relative(client.player.getDirection(), 2);

                            String base64Data = BlockGenerator.restoreFromBlocks(searchPos);

                            if (base64Data.isEmpty()) {
                                context.getSource().sendFeedback(Component.literal("§c错误: 未找到方向标记或无法还原数据!"));
                                return 0;
                            }

                            context.getSource().sendFeedback(Component.literal("§a数据还原成功! Base64 长度: " + base64Data.length()));

                            // 解码 Base64 并解压到 restored_files 文件夹
                            Path minecraftDir = client.gameDirectory.toPath();
                            Path restoredDir = minecraftDir.resolve("restored_files");

                            DataProcessor.restoreFilesFromBase64(base64Data, restoredDir);

                            context.getSource().sendFeedback(Component.literal("§a文件已还原到: " + restoredDir));

                        } catch (Exception e) {
                            context.getSource().sendFeedback(Component.literal("§c错误: " + e.getMessage()));
                            LOGGER.error("error {}", e.getMessage(), e);
                            return 0;
                        }

                        return 1;
                    }));
        });
    }
}