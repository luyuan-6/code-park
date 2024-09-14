package com.lhd.codepark.judge.codesandbox;

import com.lhd.codepark.judge.codesandbox.impl.ExampleCodeSandbox;
import com.lhd.codepark.judge.codesandbox.impl.RemoteCodeSandbox;
import com.lhd.codepark.judge.codesandbox.impl.ThirdPartyCodeSandbox;

public class CodeSandboxFactory {

    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
