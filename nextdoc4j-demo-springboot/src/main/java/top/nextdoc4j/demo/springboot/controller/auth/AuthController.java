package top.nextdoc4j.demo.springboot.controller.auth;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.nextdoc4j.demo.common.model.base.R;
import top.nextdoc4j.demo.common.model.req.auth.LoginReq;
import top.nextdoc4j.demo.common.model.resp.LoginResp;

import java.util.UUID;

/**
 * 认证管理
 *
 * @author echo
 * @since 2025/10/17
 */
@Slf4j
@Tag(name = "认证管理", description = "登录登出")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * @param req
     * @param request
     * @return 登录响应参数
     */
    @SaIgnore
    @Operation(summary = "登录", description = "用户登录")
    @PostMapping("/login")
    public R<LoginResp> login(@Validated @RequestBody LoginReq req, HttpServletRequest request) {   // 登录参数基类
        // 模拟生成 token（你可以换成 Sa-Token / JWT）
        String token = UUID.randomUUID().toString().replace("-", "");
        return R.ok(LoginResp.builder().token(token).build());
    }
}
