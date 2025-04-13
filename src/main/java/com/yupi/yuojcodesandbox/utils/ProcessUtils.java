package com.yupi.yuojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 */
@Slf4j
public class ProcessUtils {
    /**
     * 从输入流中逐行读取内容
     *
     * @param inputStream 输入流
     * @return 读取的内容列表
     */
    private static List<String> readLinesFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> outputStrList = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            outputStrList.add(line);
        }
        return outputStrList;
    }

    /**
     * 执行进程并获取信息
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);

            if (exitValue == 0) {
                log.info("{}成功", opName);
                executeMessage.setOutput(StringUtils.join(readLinesFromInputStream(runProcess.getInputStream()), "\n"));
            } else {
                log.error("{}失败，错误码： {}", opName, exitValue);
                executeMessage.setOutput(StringUtils.join(readLinesFromInputStream(runProcess.getInputStream()), "\n"));
                executeMessage.setErrorMessage(StringUtils.join(readLinesFromInputStream(runProcess.getErrorStream()), "\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            log.error("执行进程时发生异常", e);
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String join = StrUtil.join("\n", (Object) args.split(" ")) + "\n";
            outputStreamWriter.write(join);
            outputStreamWriter.flush();

            executeMessage.setOutput(StringUtils.join(readLinesFromInputStream(runProcess.getInputStream()), ""));
            outputStreamWriter.close();
            outputStream.close();
            runProcess.getInputStream().close();
            runProcess.destroy();
        } catch (Exception e) {
            log.error("执行交互式进程时发生异常", e);
        }
        return executeMessage;
    }
}
