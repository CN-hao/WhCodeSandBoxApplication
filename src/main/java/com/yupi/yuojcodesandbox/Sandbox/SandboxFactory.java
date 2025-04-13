package com.yupi.yuojcodesandbox.Sandbox;

import com.yupi.yuojcodesandbox.Sandbox.impl.CodeSandboxTemplate;
import com.yupi.yuojcodesandbox.Sandbox.impl.DockerCodeSandbox;
import org.springframework.stereotype.Component;

@Component
public class SandboxFactory {
    public static DockerCodeSandbox getSandbox(String language) {
        return new DockerCodeSandbox(language);
    }
}
