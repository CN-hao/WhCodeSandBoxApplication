package com.yupi.yuojcodesandbox.controller;

import com.yupi.yuojcodesandbox.Sandbox.impl.DockerCodeSandbox;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";



    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        if (executeCodeRequest.getLanguage() == null||
                executeCodeRequest.getTimeLimit() == null ||
                executeCodeRequest.getMemoryLimit() == null||
                executeCodeRequest.getCode() == null||
                executeCodeRequest.getInputList() == null) {
            return ExecuteCodeResponse.builder()
                    .status(ExecuteCodeResponse.Status.BAD_REQUEST.getCode())
                    .message(ExecuteCodeResponse.Status.BAD_REQUEST.getMessage())
                    .build();
        }

        try (DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox(executeCodeRequest.getLanguage())) {
            return dockerCodeSandbox.executeCode(executeCodeRequest);
        }catch (Exception e){
            return ExecuteCodeResponse.builder()
                    .status(ExecuteCodeResponse.Status.UNSUPPORTED_LANGUAGE.getCode())
                    .message(ExecuteCodeResponse.Status.UNSUPPORTED_LANGUAGE.getMessage())
                    .build();
        }

    }
}
