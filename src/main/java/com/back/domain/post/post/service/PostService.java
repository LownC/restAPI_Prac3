package com.back.domain.post.post.service;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
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
}
