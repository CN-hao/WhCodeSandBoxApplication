package com.yupi.yuojcodesandbox.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    /**
     * status 枚举类
     * 0：成功
     * 1：失败
     * 2：超时
     * 3：内存超限
     * 4：编译错误
     * 5：运行错误
     */

    @Getter
    public  enum Status {
        SUCCESS(0, "成功"),
        ERROR(1, "失败"),
        TIMEOUT(2, "超时"),
        MEMORY_LIMIT_EXCEEDED(3, "内存超限"),
        COMPILE_ERROR(4, "编译错误"),
        RUNTIME_ERROR(5, "运行错误");

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
    private List<String> outputList;

    /**
     * 接口信息
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
}
