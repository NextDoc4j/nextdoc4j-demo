package top.nextdoc4j.demo.system;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.nextdoc4j.demo.web.configuration.properties.ProjectProperties;
import top.nextdoc4j.demo.core.model.base.R;

@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class SystemServiceApplication {

    private final ProjectProperties projectProperties;

    public static void main(String[] args) {
        SpringApplication.run(SystemServiceApplication.class, args);
    }

    @Hidden
    @GetMapping("/")
    public R index() {
        return R.ok(projectProperties);
    }

}
