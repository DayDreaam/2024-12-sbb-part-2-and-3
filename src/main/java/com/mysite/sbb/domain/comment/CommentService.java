package com.mysite.sbb.domain.comment;

import com.mysite.sbb.domain.answer.Answer;
import com.mysite.sbb.domain.question.Question;
import com.mysite.sbb.domain.user.SiteUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment makeAnswerComment(SiteUser user, Answer answer, String content){
        Comment comment = Comment.builder()
                .author(user)
                .answer(answer)
                .question(answer.getQuestion())
                .content(content)
                .build();
        commentRepository.save(comment);
        return comment;
    }

    public Comment makeQuestionComment(SiteUser user, Question question, String content){
        Comment comment = Comment.builder()
                .author(user)
                .question(question)
                .content(content)
                .build();
        commentRepository.save(comment);
        return comment;
    }

    public void commentDelete(Comment comment){
        commentRepository.delete(comment);
    }

    public Comment getComment(Integer id) {
        return commentRepository.findById(id).get();
    }

    public Comment commentModify(Comment comment, String content) {
        comment.updateContent(content);
        return commentRepository.save(comment);
    }

    public List<Comment> recentComment() {
        return commentRepository.findTop10ByOrderByCreateDateDesc();
    }
}
