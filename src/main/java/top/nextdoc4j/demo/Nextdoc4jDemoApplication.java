package top.nextdoc4j.demo;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.nextdoc4j.demo.configuration.properties.ProjectProperties;
import top.nextdoc4j.demo.model.base.R;

@RequiredArgsConstructor
@SpringBootApplication
@RestController
public class Nextdoc4jDemoApplication {

    private final ProjectProperties projectProperties;


    @Hidden
    @GetMapping("/")
    public R index() {
        return R.ok(projectProperties);
    }

    public static void main(String[] args) {
        SpringApplication.run(Nextdoc4jDemoApplication.class, args);
    }

}
