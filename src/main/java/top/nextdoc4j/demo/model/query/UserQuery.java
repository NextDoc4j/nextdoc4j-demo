package top.nextdoc4j.demo.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询参数")
public class UserQuery {

    @Schema(description = "用户名，支持模糊查询", example = "echo")
    private String username;

    @Schema(description = "邮箱，支持模糊查询", example = "nextdoc4j@163.com")
    private String email;

    @Schema(description = "当前页码，默认1", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页记录数，默认10", example = "10")
    private Integer pageSize = 10;
}