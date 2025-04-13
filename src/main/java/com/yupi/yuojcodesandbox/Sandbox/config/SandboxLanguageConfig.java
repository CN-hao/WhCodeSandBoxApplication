package com.yupi.yuojcodesandbox.Sandbox.config;

import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

//todo 运行参数 和 编译参数的格式化
@Data
@Getter
public class SandboxLanguageConfig {

    public static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    public static final String HOST_WORK_DIR = System.getProperty("user.dir");
    public static final String WORK_DIR = "/opt/workspace";
    public static final Long TIME_OUT = 5000L;//5s
    private static final Map<String, SandboxLanguageConfig> LANGUAGE_CONFIG_MAP = new HashMap<>(){
        {
            put("java", new SandboxLanguageConfig("Main.java", "Main",
                    "openjdk:8-alpine",
                    "javac -source 1.8 -target 1.8 -encoding utf-8 %s",
                    "java -Xmx256m -Dfile.encoding=UTF-8 %s"));
        }
        {
            put("cpp", new SandboxLanguageConfig("Main.cpp", "Main.out",
                    "gcc:latest",
                    "g++ -std=c++11  %s -o %s",
                    "./%s"));
        }
        {
            put("python", new SandboxLanguageConfig("Main.py", "Main.py",
                    "python:latest",
                    "python3 ./%s",
                    true));
        }
        {
            put("go", new SandboxLanguageConfig("Main.go", "Main",
                    "busybox:glibc",
                    "go build -o %2$s %1$s",
                    "./%s"));
        }

    };

    /**
     * 获取语言配置
     *
     * @param language 语言类型
     * @return 语言配置
     */
    public static SandboxLanguageConfig getSandboxLanguageConfig(String language) {
        SandboxLanguageConfig sandboxLanguageConfig = LANGUAGE_CONFIG_MAP.get(language);
        if (sandboxLanguageConfig == null) {
            throw new RuntimeException("不支持的语言类型");
        }
        return sandboxLanguageConfig;
    }

    private String sourceFileName;//第一参数
    private String outputFileName;//第二个参数
    private String imageName;
    private String compileCmdFormat;
    private String runCmdFormat;
    //如果是解释型语言，返回true
    private boolean isInterpreted = false;

    public SandboxLanguageConfig(String sourceFileName, String outputFileName, String imageName, String compileCmdFormat, String runCmdFormat) {
        this.sourceFileName = sourceFileName;
        this.outputFileName = outputFileName;
        this.imageName = imageName;
        this.compileCmdFormat = compileCmdFormat;
        this.runCmdFormat = runCmdFormat;
    }

    public SandboxLanguageConfig(String sourceFileName, String outputFileName, String imageName , String runCmdFormat, boolean isInterpreted) {
        this.sourceFileName = sourceFileName;
        this.outputFileName = outputFileName;
        this.imageName = imageName;
        this.runCmdFormat = runCmdFormat;
        this.isInterpreted = isInterpreted;
    }
}
