package com.yupi.yuojcodesandbox.Sandbox.impl;
import cn.hutool.core.io.FileUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yupi.yuojcodesandbox.Sandbox.config.SandboxLanguageConfig;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.utils.DockerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Slf4j
public  class DockerCodeSandbox extends  CodeSandboxTemplate {

    protected DockerClient dockerClient= DockerClientBuilder.getInstance().build();

    private String containerId;

    public DockerCodeSandbox(String language) {
        super(language);
        DockerUtils.pullImageIfNotExists(dockerClient, sandboxLanguageConfig.getImageName());
    }

    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, ExecuteCodeRequest executeCodeRequest) {


        List<String> inputList = executeCodeRequest.getInputList();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        initContainer();


        for (String inputArgs : inputList) {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setMemory(0L);

            long time =-1;
            StopWatch stopWatch = new StopWatch();

            String runCmd = String.format(sandboxLanguageConfig.getRunCmdFormat(),
                    sandboxLanguageConfig.getOutputFileName());
            runCmd= "echo \""+inputArgs+"\" | "+ runCmd;
            String[] cmdArray = {"sh","-c",runCmd};

            // 创建执行命令 准备后续开始执行
            String execId = dockerCreateExecCmd(cmdArray);

            // 获取内存占用等使用状态
            ResultCallback<Statistics> statisticsCallback = new ResultCallback.Adapter<>(){
                @Override
                public void onNext(Statistics statistics) {
                    executeMessage.setMemory( Math.max(
                            Optional.ofNullable(statistics.getMemoryStats().getUsage())
                                    .orElse(0L)
                            , executeMessage.getMemory()));
                    if (executeMessage.getMemory() > executeCodeRequest.getMemoryLimit()) {
                        executeMessage.setErrorMessage("内存超限");
                        executeMessage.setStatus(ExecuteMessage.Status.MEMORY_LIMIT_EXCEEDED.getCode());
                        executeMessage.setMemory(0L);
                    }
                }
            };

            dockerClient.statsCmd(containerId)
                    .exec(statisticsCallback);

            try {
                stopWatch.start();

                dockerClient.execStartCmd(execId)
                        .exec(new ExecStartResultCallback(stdout,stderr))
                        .awaitCompletion(
                                    Math.min(executeCodeRequest.getTimeLimit(),
                                            SandboxLanguageConfig.TIME_OUT)
                                , TimeUnit.MILLISECONDS);

                stopWatch.stop();

                time = stopWatch.getLastTaskTimeMillis();

            } catch (InterruptedException e) {
                executeMessage.setErrorMessage("执行超时");
                executeMessage.setStatus(ExecuteMessage.Status.TIMEOUT.getCode());
            }

            if(executeMessage.getStatus() == null) {//超时or 内存超限
                executeMessage.setOutput(stdout.toString(StandardCharsets.UTF_8).trim());
                executeMessage.setErrorMessage(stderr.toString(StandardCharsets.UTF_8).trim());
                executeMessage.setTime(time);
                executeMessage.setExitValue(0);
                executeMessage.setStatus(ExecuteMessage.Status.SUCCESS.getCode());
            }

            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }


    private String dockerCreateExecCmd(String[] cmdArray) {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdArray)
                .withWorkingDir(SandboxLanguageConfig.WORK_DIR)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        return execId;
    }


    private void initContainer() {
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(sandboxLanguageConfig.getImageName());
        HostConfig hostConfig = DockerUtils.createRestrictedHostConfig(); // 使用提取的配置方法

        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume(SandboxLanguageConfig.WORK_DIR)));

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();
    }


    @Override
    public void close() throws IOException {
        FileUtil.del(userCodeParentPath);
        if(containerId != null) {
            DockerUtils.stopContainer(dockerClient, containerId);
        }
    }

    @Override
    public void clean() {
        FileUtil.clean(userCodeParentPath);
    }
}
