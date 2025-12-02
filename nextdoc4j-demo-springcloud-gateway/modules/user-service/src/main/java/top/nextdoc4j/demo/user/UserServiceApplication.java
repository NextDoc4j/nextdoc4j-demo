package top.nextdoc4j.demo.user;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.nextdoc4j.demo.common.configuration.properties.ProjectProperties;
import top.nextdoc4j.demo.common.model.base.R;

@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class UserServiceApplication {

    private final ProjectProperties projectProperties;


    @Hidden
    @GetMapping("/")
    public R index() {
        return R.ok(projectProperties);
    }

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
