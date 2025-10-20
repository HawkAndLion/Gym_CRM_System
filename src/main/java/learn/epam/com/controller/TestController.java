package learn.epam.com.controller;

import learn.epam.com.dto.TraineeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/echo")
    public ResponseEntity<TraineeDto> echo(@RequestBody TraineeDto dto) {
        System.out.println("Received: " + dto);
        return ResponseEntity.ok(dto);
    }
}


