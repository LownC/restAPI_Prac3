package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public long count() {
        return postRepository.count();
    }

    public Post write(String title, String content) {
        Post post = new Post(title, content);

        return postRepository.save(post);
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public List<Post> findAllByOrderByIdDesc() {
        return postRepository.findAllByOrderByIdDesc();
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public Optional<Post> findLatest() {
        return postRepository.findFirstByOrderByIdDesc();
    }

    // 영속 상태의 엔티티 값만 바꾸면 트랜잭션 커밋 시 더티체킹으로 UPDATE 된다.
    public void modify(Post post, String title, String content) {
        post.modify(title, content);
    }

    // cascade = REMOVE 설정으로 글의 댓글도 함께 삭제된다.
    public void delete(Post post) {
        postRepository.delete(post);
    }

    // 댓글은 Post 애그리거트를 통해 추가한다. (cascade PERSIST 로 저장)
    public PostComment writeComment(Post post, String content) {
        return post.addComment(content);
    }

    // 지연된 INSERT 를 즉시 실행해 새 댓글의 id 를 확정할 때 사용한다.
    public void flush() {
        postRepository.flush();
    }
}
