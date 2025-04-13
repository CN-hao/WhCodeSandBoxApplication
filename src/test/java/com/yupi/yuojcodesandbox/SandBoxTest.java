package com.yupi.yuojcodesandbox;

import com.yupi.yuojcodesandbox.Sandbox.impl.DockerCodeSandbox;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class SandBoxTest {

    @Test
    void testJava() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("java");
        String code = """
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        System.out.println(123);
                    }
                }
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("java")
                .memoryLimit(128)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }
    //test go
    @Test
    void testGo() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("go");
        String code = """
                package main
                import "fmt"
                func main() {
                    fmt.Println(123)
                }
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("go")
                .memoryLimit(128*1024*1024)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }
    //test input
    @Test
    void testInput() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("java");
        String code = """
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        System.out.println(a+b);
                    }
                }
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("\n1\n2"))
                .code(code)
                .language("java")
                .memoryLimit(128*1024*1024)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }

    //test 编译错误
    @Test
    void testCpp() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("cpp");
        String code = """
                p
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("cpp")
                .memoryLimit(128*1024*1024)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }

    //test run error
    @Test
    void testRunError() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("python");
        String code = """
                print(1\\0)
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("python")
                .memoryLimit(128*1024*1024)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }
}
