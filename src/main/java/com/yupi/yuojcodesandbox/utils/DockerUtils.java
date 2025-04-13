package com.yupi.yuojcodesandbox.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Ulimit;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class DockerUtils {

    /**
     * 判断 Docker 镜像是否存在
     *
     * @param imageName 镜像名称，格式如 "openjdk:8"
     * @return 镜像存在返回 true，否则返回 false
     */
    public static boolean imageExists(DockerClient dockerClient, String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return false;
        }
        
        try {
            ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
            
            // 拆分镜像名称和标签
            String[] parts = imageName.split(":");
            String name = parts[0];
            String tag = parts.length > 1 ? parts[1] : "latest";
            
            List<Image> images = listImagesCmd.withImageNameFilter(name).exec();
            
            // 检查是否有匹配的镜像和标签
            for (Image image : images) {
                if (image.getRepoTags() != null) {
                    for (String repoTag : image.getRepoTags()) {
                        if (repoTag.equals(imageName) || 
                            repoTag.equals(name + ":" + tag)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            // 出现异常则视为镜像不存在
            throw new RuntimeException("Error checking image existence: " + e.getMessage(), e);
        }
    }

    /**
     * 拉取 Docker 镜像
     *
     * @param dockerClient Docker 客户端
     * @param imageName    镜像名称
     */
    public static void pullImage(DockerClient dockerClient, String imageName) {
        try {
            log.info("Pulling image: {}", imageName);
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
            log.info("Image {} pulled successfully.", imageName);
        } catch (Exception e) {
            throw new RuntimeException("Error pulling image: " + imageName, e);
        }
    }

    /**
     * 检查镜像是否已拉取，如果未拉取则执行拉取操作
     *
     * @param dockerClient Docker 客户端
     * @param imageName    镜像名称，格式如 "openjdk:8"
     */
    public static void pullImageIfNotExists(DockerClient dockerClient, String imageName) {
        if (!imageExists(dockerClient, imageName)) {
            log.warn("Image {} not found, pulling from Docker Hub...", imageName);
            pullImage(dockerClient, imageName);
        }
    }

    /**
     * 停止并删除容器
     *
     * @param dockerClient Docker 客户端
     * @param containerId  容器 ID
     */
    public static void stopContainer(DockerClient dockerClient, String containerId) {
        try {
            log.info("start Stopping container: {}", containerId);
            dockerClient.stopContainerCmd(containerId).exec();
            log.info("Stopped container: {}", containerId);
        } catch (Exception e) {
            log.error("Failed to stop  container: {}", containerId, e);
            throw new RuntimeException("Error stopping or removing container: " + containerId, e);
        }
    }

    /**
     * 创建受限的 HostConfig 配置
     *
     * @return 配置好的 HostConfig 对象
     */
    public static HostConfig createRestrictedHostConfig() {
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(128 * 1024 * 1024L); // 128MB
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.withNetworkMode("none"); // 禁用网络，以隔离容器的网络访问
        hostConfig.withReadonlyRootfs(true); // 将根文件系统设为只读
        hostConfig.withSecurityOpts(Collections.singletonList("seccomp="+ ResourceUtil.readUtf8Str("security/seccomp.json"))); // 使用 Seccomp 配置来限制系统调用
        hostConfig.withPrivileged(false); // 禁用特权模式
        hostConfig.withUlimits(Collections.singletonList(new Ulimit("nofile", 1024, 1024))); // 限制文件描述符数量
        hostConfig.withAutoRemove(true); // 自动删除容器
        hostConfig.withLogConfig(new LogConfig().setType(LogConfig.LoggingType.JSON_FILE));
        return hostConfig;
    }
}
