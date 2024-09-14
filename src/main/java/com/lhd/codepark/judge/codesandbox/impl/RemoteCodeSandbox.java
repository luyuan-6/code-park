package com.lhd.codepark.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.lhd.codepark.common.ErrorCode;
import com.lhd.codepark.exception.BusinessException;
import com.lhd.codepark.judge.codesandbox.CodeSandbox;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeRequest;
import com.lhd.codepark.judge.codesandbox.model.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;


/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
public class RemoteCodeSandbox implements CodeSandbox {
    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_SECRET = "secretKey";

    /**
     * 执行代码片段。
     * 通过向远程代码沙箱服务发送请求，执行传入的代码请求，并返回执行结果。
     *
     * @param executeCodeRequest 执行代码的请求对象，包含需要执行的代码片段和其他相关信息。
     * @return ExecuteCodeResponse 远程执行代码后的响应对象，包含执行结果等信息。
     * @throws BusinessException 如果远程请求失败或响应为空，则抛出业务异常。
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 输出提示信息，表示进入代码执行环节
        System.out.println("远程代码沙箱");

        // 设置远程服务的URL，用于后续发送执行代码的请求
        String url = "http://localhost:8090/executeCode";

        // 将执行代码的请求对象转换为JSON字符串，作为请求体发送给远程服务
        String json = JSONUtil.toJsonStr(executeCodeRequest);

        // 发送POST请求到远程代码沙箱服务，并获取响应内容
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();

        // 检查远程服务的响应内容是否为空，如果为空则抛出业务异常
        if (StringUtils.isBlank(responseStr)){
            throw  new BusinessException(ErrorCode.API_REQUEST_ERROR, "远程代码沙箱调用失败" + responseStr);
        }
        System.out.println(responseStr);
        // 将远程服务的响应内容转换为ExecuteCodeResponse对象，返回给调用方
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }

}
