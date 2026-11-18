package com.back.domain.post.post.controller;

import com.back.domain.post.post.dto.PostDto;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {
    private final PostService postService;

    @GetMapping
    @Transactional(readOnly = true)
    public RsData<List<PostDto>> getItems() {
        List<PostDto> items = postService.findAllByOrderByIdDesc()
                .stream()
                .map(PostDto::new)
                .toList();

        return new RsData<>(
                "200-1",
                "글 목록을 조회하였습니다.",
                items
        );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RsData<PostDto> getItem(@PathVariable int id) {
        // 글이 없으면 NoSuchElementException → GlobalExceptionHandler 가 404 로 응답
        Post post = postService.findById(id).get();

        return new RsData<>(
                "200-1",
                "%d번 글을 조회하였습니다.".formatted(id),
                new PostDto(post)
        );
    }

    record PostWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,
            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public RsData<PostDto> write(@Valid @RequestBody PostWriteReqBody reqBody) {
        Post post = postService.write(reqBody.title(), reqBody.content());

        return new RsData<>(
                "201-1",
                "%d번 글이 작성되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    record PostModifyReqBody(
            @NotBlank
            @Size(min = 2, max = 100)
            String title,
            @NotBlank
            @Size(min = 2, max = 5000)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostDto> modify(@PathVariable int id, @Valid @RequestBody PostModifyReqBody reqBody) {
        Post post = postService.findById(id).get();

        postService.modify(post, reqBody.title(), reqBody.content());

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(id),
                new PostDto(post)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> delete(@PathVariable int id) {
        Post post = postService.findById(id).get();

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(id)
        );
    }
}
