package com.yupi.yuojcodesandbox;

import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;

import java.io.File;
import java.util.List;

/**
 * Java 代码沙箱模板方法的实现
 */
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2. 编译代码，得到 class 文件
        compileFile(userCodeFile);

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        // 4. 收集整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

        // 5. 文件清理
        deleteFile(userCodeFile);

        return outputResponse;
    }

    protected abstract File saveCodeToFile(String code);

    protected abstract void compileFile(File userCodeFile);

    protected abstract List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList);

    protected abstract ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList);

    protected abstract void deleteFile(File userCodeFile);
}
