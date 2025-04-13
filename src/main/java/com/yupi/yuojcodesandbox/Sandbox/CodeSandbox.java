package com.yupi.yuojcodesandbox.Sandbox;


import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;

import java.io.Closeable;
import java.io.IOException;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox extends Closeable {

    /**
     * 执行代码
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);


    /**
     * 清理沙箱内的文件
     */
    void clean();

    /**
     * 关闭沙箱
     */
    @Override
    void close() throws IOException;

}
