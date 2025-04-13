package com.yupi.yuojcodesandbox.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

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

    /**
     * 发生错误的id
     */
    private Long errorId;


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
        TIMEOUT(1, "超时"),
        MEMORY_LIMIT_EXCEEDED(2, "内存超限"),
        COMPILE_ERROR(3, "编译错误"),
        RUNTIME_ERROR(4, "运行错误"),
        UNSUPPORTED_LANGUAGE(5, "不支持的语言");

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
