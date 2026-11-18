package com.back.domain.post.postComment.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.post.postComment.entity.PostComment;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("댓글 다건조회 : 특정 글의 댓글을 최신순(id 큰 순)으로 반환한다.")
    void t1() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                )
                .andDo(print());

        Post post = postService.findById(postId).get();
        List<PostComment> comments = post.getComments().reversed();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글의 댓글 목록을 조회하였습니다.".formatted(postId)))
                .andExpect(jsonPath("$.data.length()").value(3));

        for (int i = 0; i < comments.size(); i++) {
            PostComment comment = comments.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(comment.getId()))
                    .andExpect(jsonPath("$.data[%d].createDate".formatted(i)).value(Matchers.startsWith(comment.getCreateDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].modifyDate".formatted(i)).value(Matchers.startsWith(comment.getModifyDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].postId".formatted(i)).value(postId))
                    .andExpect(jsonPath("$.data[%d].content".formatted(i)).value(comment.getContent()));
        }

        resultActions
                .andExpect(jsonPath("$.data[0].content").value("댓글 1-3"));
    }

    @Test
    @DisplayName("댓글 다건조회 : 댓글이 없는 글이면 빈 배열")
    void t2() throws Exception {
        int postId = 3;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("댓글 다건조회 : 존재하지 않는 글이면 404")
    void t3() throws Exception {
        int postId = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments".formatted(postId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("댓글 단건조회 : 글 id 와 댓글 id 로 댓글 1건을 반환한다.")
    void t4() throws Exception {
        int postId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                )
                .andDo(print());

        PostComment comment = postService.findById(postId).get().findCommentById(id).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글을 조회하였습니다.".formatted(id)))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(comment.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(comment.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.content").value("댓글 1-1"));
    }

    @Test
    @DisplayName("댓글 단건조회 : 존재하지 않는 댓글이면 404")
    void t5() throws Exception {
        int postId = 1;
        int id = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }

    @Test
    @DisplayName("댓글 단건조회 : 다른 글에 달린 댓글을 요청하면 404")
    void t6() throws Exception {
        int postId = 1;
        int id = 4; // 4번 댓글(댓글 2-1)은 2번 글의 댓글

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/%d/comments/%d".formatted(postId, id))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }

    @Test
    @DisplayName("댓글 작성 : 글에 새 댓글을 달고 201 을 반환한다.")
    void t7() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 new"
                                        }
                                        """)
                )
                .andDo(print());

        Post post = postService.findById(postId).get();
        PostComment comment = post.getComments().getLast();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 댓글이 작성되었습니다.".formatted(comment.getId())))
                .andExpect(jsonPath("$.data.id").value(comment.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(comment.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(comment.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.content").value("댓글 new"));

        assertThat(post.getComments()).hasSize(4);
    }

    @Test
    @DisplayName("댓글 작성 : 내용이 비어 있으면 400")
    void t8() throws Exception {
        int postId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value(Matchers.containsString("content-NotBlank")));
    }

    @Test
    @DisplayName("댓글 작성 : 존재하지 않는 글이면 404")
    void t9() throws Exception {
        int postId = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/%d/comments".formatted(postId))
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "댓글 new"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }
}
