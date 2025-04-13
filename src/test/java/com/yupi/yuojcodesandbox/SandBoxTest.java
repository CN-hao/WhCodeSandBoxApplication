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
                .memoryLimit(128*1024*1024)
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
                .inputList(List.of("\n1\n\n6"))
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
                #include <iostream>
                using namespace std;
                int main() {
                    cout << "Hello World" << endl;
                    return 0;
                }
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
                print(1/0)
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
    //test Memory Limit
    @Test
    void testMemoryLimit() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("cpp");
        String code = """
                #include <iostream>
                 #include <cstdlib>
                 #include <cstring>
                 int main() {
                     size_t size = 300 * 1024 * 1024; // 分配 300MB
                     char* buffer = (char*)malloc(size);
                     if (!buffer) {
                         std::cerr << "分配失败！" << std::endl;
                         return 1;
                     }
                     for (size_t i = 0; i < size; i += 4096) {
                         buffer[i] = 1;
                     }
                     free(buffer);
                     return 0;
                 }
                
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("cpp")
                .memoryLimit(10)
                .timeLimit(1000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }

    //test timeout
    @Test
    void testTimeout() throws IOException {
        DockerCodeSandbox dockerCodeSandbox = new DockerCodeSandbox("cpp");
        String code = """
                #include <iostream>
                #include <unistd.h>
                using namespace std;
                using namespace std;
                int main() {do{sleep;}while(1);}
                """;
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(List.of("1", "2"))
                .code(code)
                .language("cpp")
                .memoryLimit(128*1024*1024)
                .timeLimit(5000)
                .build();
        ExecuteCodeResponse executeCodeResponse = dockerCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        dockerCodeSandbox.close();
    }
}
