package com.yupi.yuojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {

    private Integer status;

    private Integer exitValue;

    private String output;

    private String errorMessage;

    private Long time;

    private Long memory;

    public enum Status {
        SUCCESS(0, "成功"),
        TIMEOUT(1, "超时"),
        MEMORY_LIMIT_EXCEEDED(2, "内存超限"),
        RUNTIME_ERROR(3, "运行错误");

        private final int code;
        private final String message;

        Status(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
