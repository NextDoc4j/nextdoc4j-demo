package top.nextdoc4j.demo.springboot.controller.robot;

import org.springframework.web.bind.annotation.*;
import top.nextdoc4j.demo.common.model.base.PageResult;
import top.nextdoc4j.demo.common.model.base.R;
import top.nextdoc4j.demo.common.model.req.Robot;

/**
 * 机器人管理接口
 * <p>
 * 提供机器人基础的增删改查操作。
 */
@RestController
@RequestMapping("/robot")
public class RobotController {

    /**
     * 获取机器人列表（分页）
     *
     * @param robot 机器人查询信息
     * @return 分页结果
     */
    @GetMapping("/list")
    public R<PageResult<Robot>> listRobots(Robot  robot) {
        // TODO: 调用服务获取分页结果
        PageResult<Robot> page = new PageResult<>();
        return R.ok(page);
    }

    /**
     * 获取指定 ID 的机器人
     *
     * @param id 机器人 ID
     * @return 机器人信息
     */
    @GetMapping("/{id}")
    public R<Robot> getRobot(@PathVariable Long id) {
        // TODO: 查询指定机器人
        Robot robot = null;
        return R.ok(robot);
    }

    /**
     * 创建机器人
     *
     * @param robot 机器人对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public R<Robot> createRobot(@RequestBody Robot robot) {
        // TODO: 创建机器人逻辑
        return R.ok(robot);
    }

    /**
     * 更新机器人信息
     *
     * @param robot 机器人对象
     * @return 更新结果
     */
    @PutMapping("/update")
    public R<Robot> updateRobot(@RequestBody Robot robot) {
        // TODO: 更新机器人逻辑
        return R.ok(robot);
    }

    /**
     * 删除机器人
     *
     * @param id 机器人ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> deleteRobot(@PathVariable Long id) {
        // TODO: 删除机器人逻辑
        return R.ok(null);
    }
}