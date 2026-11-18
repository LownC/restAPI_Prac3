package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.dto.PostCommentDto;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 댓글은 항상 특정 글에 속하므로 URL 도 /posts/{postId}/comments 형태로 둔다.
 */
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
    private final PostService postService;

    @GetMapping
    @Transactional(readOnly = true)
    public RsData<List<PostCommentDto>> getItems(@PathVariable int postId) {
        Post post = postService.findById(postId).get();

        List<PostCommentDto> items = post.getComments()
                .reversed()
                .stream()
                .map(PostCommentDto::new)
                .toList();

        return new RsData<>(
                "200-1",
                "%d번 글의 댓글 목록을 조회하였습니다.".formatted(postId),
                items
        );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RsData<PostCommentDto> getItem(@PathVariable int postId, @PathVariable int id) {
        Post post = postService.findById(postId).get();
        PostComment postComment = post.findCommentById(id).get();

        return new RsData<>(
                "200-1",
                "%d번 댓글을 조회하였습니다.".formatted(id),
                new PostCommentDto(postComment)
        );
    }

    record PostCommentWriteReqBody(
            @NotBlank
            @Size(min = 2, max = 1000)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public RsData<PostCommentDto> write(@PathVariable int postId, @Valid @RequestBody PostCommentWriteReqBody reqBody) {
        Post post = postService.findById(postId).get();

        PostComment postComment = postService.writeComment(post, reqBody.content());

        // 응답에 새 댓글 id 를 담기 위해 INSERT 를 먼저 실행
        postService.flush();

        return new RsData<>(
                "201-1",
                "%d번 댓글이 작성되었습니다.".formatted(postComment.getId()),
                new PostCommentDto(postComment)
        );
    }
}
