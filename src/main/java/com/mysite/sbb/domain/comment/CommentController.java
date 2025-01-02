package com.mysite.sbb.domain.comment;

import com.mysite.sbb.domain.answer.Answer;
import com.mysite.sbb.domain.answer.AnswerService;
import com.mysite.sbb.domain.question.Question;
import com.mysite.sbb.domain.question.QuestionService;
import com.mysite.sbb.domain.user.SiteUser;
import com.mysite.sbb.domain.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated")
    @PostMapping("/question/{id}/comment")
    public String createQuestionComment(@PathVariable Integer id,
                                        @Valid CommentForm commentForm,
                                        BindingResult bindingResult,
                                        Principal principal){
        Question question = questionService.getQuestion(id);
        if(bindingResult.hasErrors()){
            // 프론트단에서 알아서 빈값 못들어오게함..
            return "redirect:/question/detail/%s".formatted(question.getId());
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        commentService.makeQuestionComment(siteUser,question,commentForm.getContent());
        return "redirect:/question/detail/{id}";
    }

    @PreAuthorize("isAuthenticated")
    @PostMapping("/answer/{id}/comment")
    public String createAnswerComment(@PathVariable Integer id,
                                        @Valid CommentForm commentForm,
                                        BindingResult bindingResult,
                                        Principal principal){
        Answer answer = answerService.getAnswer(id);
        Integer qid = answer.getQuestion().getId();
        if(bindingResult.hasErrors()){
            return "redirect:/question/detail/%s".formatted(qid);
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        commentService.makeAnswerComment(siteUser,answer,commentForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s",qid,answer.getId());
    }

    private Integer findQuestionId(Comment comment){
        if(comment.getQuestion() == null){
            return comment.getAnswer().getQuestion().getId();
        }
        return comment.getQuestion().getId();
    }

    @PreAuthorize("isAuthenticated")
    @GetMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Integer id,
                                Principal principal){
        Comment comment = commentService.getComment(id);
        Integer qid = findQuestionId(comment);
        if(!principal.getName().equals(comment.getAuthor().getUsername())){
            return "redirect:/question/detail/%s".formatted(qid);
        }
        commentService.commentDelete(comment);
        return "redirect:/question/detail/%s".formatted(qid);
    }

    @PreAuthorize("isAuthenticated")
    @PostMapping("/comment/modify/{id}")
    public String modifyComment(@PathVariable Integer id,
                                Principal principal,
                                @Valid CommentForm commentForm,
                                BindingResult bindingResult){
        Comment comment = commentService.getComment(id);
        Integer qid = findQuestionId(comment);
        if(bindingResult.hasErrors()){
            return "redirect:/question/detail/%s".formatted(qid);
        }
        if(!principal.getName().equals(comment.getAuthor().getUsername())){
            return "redirect:/question/detail/%s".formatted(qid);
        }
        commentService.commentModify(comment,commentForm.getContent());
        return "redirect:/question/detail/%s".formatted(qid);
    }
}
