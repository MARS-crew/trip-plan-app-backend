package mars.tripplanappbackend.mypage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mars.tripplanappbackend.mypage.service.MyPageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 엔드포인트")
public class MyPageController {

    private final MyPageService myPageService;
}
