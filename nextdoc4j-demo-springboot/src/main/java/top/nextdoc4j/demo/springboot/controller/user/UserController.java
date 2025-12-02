package top.nextdoc4j.demo.springboot.controller.user;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.nextdoc4j.demo.common.enums.ResultCode;
import top.nextdoc4j.demo.common.enums.StatusType;
import top.nextdoc4j.demo.common.model.base.PageResult;
import top.nextdoc4j.demo.common.model.base.R;
import top.nextdoc4j.demo.common.model.query.UserQuery;
import top.nextdoc4j.demo.common.model.req.RoleReq;
import top.nextdoc4j.demo.common.model.req.UserBatchReq;
import top.nextdoc4j.demo.common.model.req.UserReq;
import top.nextdoc4j.demo.common.model.req.UserUpdateReq;
import top.nextdoc4j.demo.common.model.resp.UserResp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 用户控制器
 *
 * @author echo
 * @date 2025/09/16
 */
@Tag(name = "用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {


    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    @SaCheckPermission(value = {"user-add", "user-all", "user-delete"}, mode = SaMode.OR)
    @Operation(summary = "新增用户", description = "创建一个新的用户账户")
    @PostMapping
    public R<UserResp> createUser(@RequestBody UserReq userReq) {
        UserResp userResp = BeanUtil.copyProperties(userReq, UserResp.class);
        userResp.setId(IdUtil.getSnowflakeNextId());
        userResp.setStatus(StatusType.ACTIVE);
        userResp.setCreateTime(LocalDateTime.now());
        userResp.setUpdateTime(LocalDateTime.now());
        return R.ok(userResp);
    }


    @SaCheckPermission(value = "user.add", orRole = "admin")
    @Operation(summary = "新增用户和角色", description = "")
    @PostMapping("/userAndRole")
    public R<UserResp> createUserAndRole(@RequestBody UserReq userReq, @RequestBody RoleReq roleReq) {
        UserResp userResp = BeanUtil.copyProperties(userReq, UserResp.class);
        userResp.setId(IdUtil.getSnowflakeNextId());
        userResp.setStatus(StatusType.ACTIVE);
        userResp.setCreateTime(LocalDateTime.now());
        userResp.setUpdateTime(LocalDateTime.now());
        return R.ok(userResp);
    }

    /**
     * 批量新增用户（对象包装方式）
     * <p>
     * 示例请求体：
     * {
     * "users": [
     * { "username": "Alice", "email": "alice@test.com" },
     * { "username": "Bob", "email": "bob@test.com" }
     * ]
     * }
     */
    @Operation(
            summary = "批量新增用户（对象包装）",
            description = """
                    批量创建多个用户账户。
                    入参使用对象封装格式（UserBatchReq），包含一个 users 列表字段。
                    适合在请求中需要附带额外元数据时使用（如操作人、来源等）。
                    """
    )
    @PostMapping("/batch-object")
    public R<List<UserResp>> createUsersBatchByObject(@RequestBody UserBatchReq batchReq) {
        List<UserResp> users = batchReq.getUsers().stream()
                .map(userReq -> {
                    UserResp userResp = BeanUtil.copyProperties(userReq, UserResp.class);
                    userResp.setId(IdUtil.getSnowflakeNextId());
                    userResp.setStatus(StatusType.ACTIVE);
                    userResp.setCreateTime(LocalDateTime.now());
                    userResp.setUpdateTime(LocalDateTime.now());
                    return userResp;
                })
                .toList();
        return R.ok(users);
    }

    /**
     * 批量新增用户（直接数组方式）
     * <p>
     * 示例请求体：
     * [
     * { "username": "Alice", "email": "alice@test.com" },
     * { "username": "Bob", "email": "bob@test.com" }
     * ]
     */
    @Operation(
            summary = "批量新增用户（直接数组）",
            description = """
                    批量创建多个用户账户。
                    入参为用户对象数组，适用于简洁的前端批量创建请求。
                    与 /batch-object 功能一致，仅请求结构不同。
                    """
    )
    @PostMapping("/batch-array")
    public R<List<UserResp>> createUsersBatchByArray(@RequestBody List<UserReq> batchReq) {
        List<UserResp> users = batchReq.stream()
                .map(userReq -> {
                    UserResp userResp = BeanUtil.copyProperties(userReq, UserResp.class);
                    userResp.setId(IdUtil.getSnowflakeNextId());
                    userResp.setStatus(StatusType.ACTIVE);
                    userResp.setCreateTime(LocalDateTime.now());
                    userResp.setUpdateTime(LocalDateTime.now());
                    return userResp;
                })
                .toList();
        return R.ok(users);
    }


    @Operation(summary = "根据ID查询用户", description = "通过用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<R<UserResp>> getUserById(@PathVariable @Max(value = 999L,message = "id 不能大于 999") Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(R.fail(ResultCode.BAD_REQUEST));
        }

        if (id == 999L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(R.fail(ResultCode.NOT_FOUND));
        }

        UserResp user = new UserResp();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setEmail("user" + id + "@nextdoc4j.top");
        user.setAge(25);
        user.setPhone("138" + String.format("%08d", id));
        user.setStatus(StatusType.ACTIVE);
        user.setCreateTime(LocalDateTime.now().minusDays(RandomUtil.randomInt(1, 100)));
        user.setUpdateTime(LocalDateTime.now());

        return ResponseEntity.ok(R.ok(user));
    }

    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表，支持用户名和邮箱模糊查询")
    @GetMapping("/page")
    public R<PageResult<UserResp>> queryUsers(UserQuery query) {
        List<UserResp> users = CollUtil.newArrayList();
        for (int i = 1; i <= query.getPageSize(); i++) {
            UserResp user = new UserResp();
            long userId = (long) (query.getPageNum() - 1) * query.getPageSize() + i;
            user.setId(userId);
            user.setUsername("user_" + userId);
            user.setEmail("user" + userId + "@nextdoc4j.top");
            user.setAge(RandomUtil.randomInt(18, 65));
            user.setPhone("138" + String.format("%08d", userId));
            user.setStatus(RandomUtil.randomEle(List.of(StatusType.ACTIVE, StatusType.INACTIVE, StatusType.PENDING)));
            user.setCreateTime(LocalDateTime.now().minusDays(RandomUtil.randomInt(1, 365)));
            user.setUpdateTime(LocalDateTime.now());
            users.add(user);
        }

        PageResult<UserResp> pageResult = new PageResult<>();
        pageResult.setRecords(users);
        pageResult.setTotal(1000L);
        pageResult.setCurrent(query.getPageNum());
        pageResult.setSize(query.getPageSize());
        pageResult.setPages((long) Math.ceil(1000.0 / query.getPageSize()));

        return R.ok(pageResult);
    }

    @Operation(summary = "更新用户", description = "根据用户ID更新用户信息")
    @PutMapping("/{id}")
    public ResponseEntity<R<UserResp>> updateUser(@PathVariable Long id, @RequestBody UserUpdateReq updateReq) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(R.fail(ResultCode.BAD_REQUEST));
        }

        if (id == 999L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(R.fail(ResultCode.NOT_FOUND));
        }

        UserResp user = new UserResp();
        user.setId(id);
        user.setUsername(updateReq.getUsername());
        user.setEmail(updateReq.getEmail());
        user.setAge(updateReq.getAge());
        user.setPhone(updateReq.getPhone());
        user.setStatus(updateReq.getStatus());
        user.setCreateTime(LocalDateTime.now().minusDays(30));
        user.setUpdateTime(LocalDateTime.now());

        return ResponseEntity.ok(R.ok(user));
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @DeleteMapping("/{id}")
    public ResponseEntity<R<Void>> deleteUser(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(R.fail(ResultCode.BAD_REQUEST));
        }

        if (id == 999L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(R.fail(ResultCode.NOT_FOUND));
        }

        return ResponseEntity.ok(R.ok(null));
    }

    @Operation(summary = "批量删除用户", description = "根据用户ID列表批量删除用户")
    @DeleteMapping("/batch")
    public R<Void> deleteUsersBatch(@RequestBody List<Long> ids) {
        // 模拟批量删除逻辑
        return R.ok(null);
    }

    @Operation(summary = "用户数据 SSE 推送", description = "实时推送用户数据变更，使用 Server-Sent Events")
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUsers() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        executor.schedule(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    UserResp user = new UserResp();
                    user.setId((long) i);
                    user.setUsername("sse_user_" + i);
                    user.setEmail("sse_user" + i + "@nextdoc4j.top");
                    user.setAge(RandomUtil.randomInt(18, 65));
                    user.setStatus(StatusType.ACTIVE);
                    user.setCreateTime(LocalDateTime.now());
                    user.setUpdateTime(LocalDateTime.now());

                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(i))
                            .name("user-data")
                            .data(user));

                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        }, 0, TimeUnit.SECONDS);

        return emitter;
    }

    @Operation(summary = "导出用户数据", description = "导出用户数据为 CSV 文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers(UserQuery query) {
        String csvContent = "ID,用户名,邮箱,年龄,手机号,状态,创建时间\n" +
                "1,user_1,user1@nextdoc4j.top,25,13800000001,ACTIVE,2024-01-01 12:00:00\n" +
                "2,user_2,user2@nextdoc4j.top,30,13800000002,ACTIVE,2024-01-02 12:00:00\n";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"users.csv\"")
                .header("Content-Type", "text/csv; charset=utf-8")
                .body(csvContent.getBytes());
    }

    @Operation(summary = "启用/禁用用户", description = "切换用户状态")
    @PatchMapping("/{id}/toggle-status")

    public ResponseEntity<R<UserResp>> toggleUserStatus(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().body(R.fail(ResultCode.BAD_REQUEST));
        }

        if (id == 999L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(R.fail(ResultCode.NOT_FOUND));
        }

        UserResp user = new UserResp();
        user.setId(id);
        user.setUsername("user_" + id);
        user.setEmail("user" + id + "@nextdoc4j.top");
        user.setAge(25);
        user.setPhone("138" + String.format("%08d", id));
        user.setStatus(StatusType.ACTIVE); // 切换状态
        user.setCreateTime(LocalDateTime.now().minusDays(30));
        user.setUpdateTime(LocalDateTime.now());

        return ResponseEntity.ok(R.ok(user));
    }
}
