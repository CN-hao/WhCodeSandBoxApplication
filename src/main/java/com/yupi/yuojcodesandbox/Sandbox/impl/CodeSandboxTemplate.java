package com.yupi.yuojcodesandbox.Sandbox.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.Sandbox.CodeSandbox;
import com.yupi.yuojcodesandbox.Sandbox.config.SandboxLanguageConfig;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.yupi.yuojcodesandbox.Sandbox.config.SandboxLanguageConfig.GLOBAL_CODE_DIR_NAME;

/**
 * Java 代码沙箱模板方法的实现
 */
@Slf4j
@Getter
public abstract class CodeSandboxTemplate implements CodeSandbox {


    protected final SandboxLanguageConfig sandboxLanguageConfig;

    protected String globalCodePathName;
    protected String userCodeParentPath;
    protected String userCodePath;

    public CodeSandboxTemplate(String language) {
        this.sandboxLanguageConfig = SandboxLanguageConfig.getSandboxLanguageConfig(language);
        globalCodePathName = SandboxLanguageConfig.HOST_WORK_DIR + File.separator + GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        if(!FileUtil.exist(userCodeParentPath)) {
            FileUtil.mkdir(userCodeParentPath);
        }
        userCodePath = userCodeParentPath + File.separator + sandboxLanguageConfig.getSourceFileName();
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行代码请求
     * @return 执行代码响应
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        ExecuteCodeResponse outputResponse = null;
        try{
            // 1. 把用户的代码保存为文件
            File userCodeFile = saveCodeToFile(code);

            try{
                // 2. 编译代码，得到 class 文件
                compileFile(userCodeFile);
            }catch (Exception e){
                outputResponse = new ExecuteCodeResponse();
                outputResponse.setStatus(ExecuteCodeResponse.Status.COMPILE_ERROR.getCode());
                outputResponse.setMessage(ExecuteCodeResponse.Status.COMPILE_ERROR.getMessage());
                return outputResponse;
            }

            // 3. 执行代码，得到输出结果
            List<ExecuteMessage> executeMessageList = runFile(userCodeFile, executeCodeRequest);

            // 4. 收集整理输出结果
            outputResponse = getOutputResponse(executeMessageList);

        }catch(Exception e){
            log.error("执行代码未处理的异常", e);
        }finally {
            // 5. 文件清理
            clean();
        }

        return outputResponse;
    }

    /**
     * 保存代码到文件
     *
     * @param code 用户代码
     * @return 用户代码文件
     */
    protected File saveCodeToFile(String code) {
        return FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
    }

    /**
     * 编译文件
     *
     * @param userCodeFile 用户代码文件
     */
    protected  void compileFile(File userCodeFile){
        if(sandboxLanguageConfig.isInterpreted()) {
            return;
        }

        String compileCmd = String.format(sandboxLanguageConfig.getCompileCmdFormat(),
                userCodeFile.getParentFile().getAbsolutePath()+File.separator+sandboxLanguageConfig.getSourceFileName(),
                userCodeFile.getParentFile().getAbsolutePath()+File.separator+sandboxLanguageConfig.getOutputFileName());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if (executeMessage.getExitValue() != 0) {
                System.out.println("编译错误：" + executeMessage.getErrorMessage());
                throw new RuntimeException("编译错误");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行文件
     *
     * @param userCodeFile 用户代码文件
     * @param executeCodeRequest 执行代码请求
     * @return 执行结果列表
     */
    protected abstract List<ExecuteMessage> runFile(File userCodeFile, ExecuteCodeRequest executeCodeRequest);

    /**
     * 获取输出结果
     *
     * @param executeMessageList 执行结果列表
     * @return 执行代码响应
     */
    protected ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        JudgeInfo resJudgeInfo = new JudgeInfo();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        long maxMemory = 0;
        for (int i = 0; i < executeMessageList.size(); i++) {
            outputList.add(executeMessageList.get(i).getOutput());
            maxTime = Math.max(maxTime, executeMessageList.get(i).getTime());
            maxMemory= Math.max(maxMemory, executeMessageList.get(i).getMemory());
            if(executeMessageList.get(i).getStatus().getCode() != ExecuteMessage.Status.SUCCESS.getCode()) {
                switch (executeMessageList.get(i).getStatus()) {
                    case TIMEOUT -> {
                        executeCodeResponse.setStatus(ExecuteCodeResponse.Status.TIMEOUT.getCode());
                        executeCodeResponse.setMessage(ExecuteMessage.Status.TIMEOUT.getMessage());
                    }
                    case MEMORY_LIMIT_EXCEEDED -> {
                        executeCodeResponse.setStatus(ExecuteCodeResponse.Status.MEMORY_LIMIT_EXCEEDED.getCode());
                        executeCodeResponse.setMessage(ExecuteCodeResponse.Status.MEMORY_LIMIT_EXCEEDED.getMessage());
                    }
                    case RUNTIME_ERROR -> {
                        executeCodeResponse.setStatus(ExecuteCodeResponse.Status.RUNTIME_ERROR.getCode());
                        executeCodeResponse.setMessage(ExecuteCodeResponse.Status.RUNTIME_ERROR.getMessage());
                    }
                }
                resJudgeInfo.setMessage(executeMessageList.get(i).getErrorMessage());
                executeCodeResponse.setErrorId((long) i);
                break;
            }
        }
        if (executeCodeResponse.getStatus() == null) {
            executeCodeResponse.setStatus(ExecuteCodeResponse.Status.SUCCESS.getCode());
            executeCodeResponse.setMessage(ExecuteMessage.Status.SUCCESS.getMessage());
        }
        executeCodeResponse.setOutputList(outputList);
        resJudgeInfo.setTime(maxTime);
        resJudgeInfo.setMemory(maxMemory);
        executeCodeResponse.setJudgeInfo(resJudgeInfo);
        return executeCodeResponse;
    }

    /**
     * 删除文件
     *
     */
    @Override
    public void clean() {
            FileUtil.del(userCodeParentPath);
    }
}
