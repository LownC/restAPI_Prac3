package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * 앱 시작 시 샘플 데이터(글 3건, 댓글 5건)를 만든다.
 * 이미 글이 있으면 다시 만들지 않는다.
 */
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    // @Transactional 이 프록시를 통해 적용되도록 자기 자신을 프록시로 주입받는다.
    @Autowired
    @Lazy
    private BaseInitData self;

    private final PostService postService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.work1();
    }

    @Transactional
    public void work1() {
        if (postService.count() > 0) return;

        Post post1 = postService.write("제목 1", "내용 1");
        Post post2 = postService.write("제목 2", "내용 2");
        postService.write("제목 3", "내용 3");

        post1.addComment("댓글 1-1");
        post1.addComment("댓글 1-2");
        post1.addComment("댓글 1-3");

        post2.addComment("댓글 2-1");
        post2.addComment("댓글 2-2");
    }
}
