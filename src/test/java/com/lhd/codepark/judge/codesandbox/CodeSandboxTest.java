package com.lhd.codepark.judge.codesandbox;

import com.lhd.codepark.judge.codesandbox.impl.ExampleCodeSandbox;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeRequest;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeResponse;
import com.lhd.codepark.model.enums.QuestionSubmitLanguageEnum;
import com.lhd.codepark.model.enums.QuestionSubmitStatusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CodeSandboxTest {
    @Value("${codesandbox.type:remote}")
    String type;
    @Test
    public void test() {
        CodeSandbox codeSandbox = new ExampleCodeSandbox();
        String code = "int main() {}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", " 3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .inputList(inputList)
                .code(code)
                .language(language)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String type = scanner.next();
            String language = QuestionSubmitLanguageEnum.JAVA.getValue();
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            String code = "psvm";
            List<String> inputList = Arrays.asList("1 2", " 3 4");
            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                    .inputList(inputList)
                    .code(code)
                    .language(language)
                    .build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        }
    }
    @Test
    void executeCodeByValue() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main() { }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
    @Test
    void executeCodeByProxy() {
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果:\" + (a + b));\n" +
                "    }\n" +
                "}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
}