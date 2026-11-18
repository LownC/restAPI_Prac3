package com.back.domain.post.post.controller;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("글 다건조회 : 최신글(id 큰 순)부터 모든 글을 반환한다.")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts")
                )
                .andDo(print());

        List<Post> posts = postService.findAllByOrderByIdDesc();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("글 목록을 조회하였습니다."))
                .andExpect(jsonPath("$.data.length()").value(posts.size()));

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);

            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(post.getId()))
                    .andExpect(jsonPath("$.data[%d].createDate".formatted(i)).value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].modifyDate".formatted(i)).value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                    .andExpect(jsonPath("$.data[%d].title".formatted(i)).value(post.getTitle()))
                    .andExpect(jsonPath("$.data[%d].content".formatted(i)).value(post.getContent()));
        }

        // 가장 최근에 작성된 글이 맨 앞에 와야 한다.
        resultActions
                .andExpect(jsonPath("$.data[0].title").value("제목 3"));
    }

    @Test
    @DisplayName("글 단건조회 : id 로 글 1건을 반환한다.")
    void t2() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/" + id)
                )
                .andDo(print());

        Post post = postService.findById(id).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(id)))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.title").value("제목 1"))
                .andExpect(jsonPath("$.data.content").value("내용 1"));
    }

    @Test
    @DisplayName("글 단건조회 : 존재하지 않는 글이면 404")
    void t3() throws Exception {
        int id = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/" + id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("글 작성 : 제목/내용을 받아 새 글을 만들고 201 을 반환한다.")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 new",
                                            "content": "내용 new"
                                        }
                                        """)
                )
                .andDo(print());

        Post post = postService.findLatest().get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다.".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0, 16))))
                .andExpect(jsonPath("$.data.title").value("제목 new"))
                .andExpect(jsonPath("$.data.content").value("내용 new"));

        assertThat(postService.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("글 작성 : 제목이 비어 있으면 400")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "",
                                            "content": "내용 new"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value(Matchers.containsString("title-NotBlank")));
    }

    @Test
    @DisplayName("글 작성 : JSON 형식이 잘못되면 400")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 new",
                                            content": 내용 new"
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("요청 본문이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("글 수정 : 제목/내용을 바꾸고 200 을 반환한다.")
    void t7() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/" + id)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 수정",
                                            "content": "내용 수정"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 수정되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.title").value("제목 수정"))
                .andExpect(jsonPath("$.data.content").value("내용 수정"));

        Post post = postService.findById(id).get();

        assertThat(post.getTitle()).isEqualTo("제목 수정");
        assertThat(post.getContent()).isEqualTo("내용 수정");
    }

    @Test
    @DisplayName("글 수정 : 존재하지 않는 글이면 404")
    void t8() throws Exception {
        int id = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/" + id)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 수정",
                                            "content": "내용 수정"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"));
    }

    @Test
    @DisplayName("글 수정 : 내용이 비어 있으면 400")
    void t9() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/" + id)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                            "title": "제목 수정",
                                            "content": ""
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value(Matchers.containsString("content-NotBlank")));
    }

    @Test
    @DisplayName("글 삭제 : 글과 그 글의 댓글까지 삭제하고 200 을 반환한다.")
    void t10() throws Exception {
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/" + id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        assertThat(postService.findById(id)).isEmpty();
        assertThat(postService.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("글 삭제 : 존재하지 않는 글이면 404")
    void t11() throws Exception {
        int id = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/" + id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }
}
