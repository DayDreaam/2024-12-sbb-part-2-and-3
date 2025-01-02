package com.mysite.sbb.domain.question;

import com.mysite.sbb.domain.answer.Answer;
import com.mysite.sbb.domain.answer.AnswerForm;
import com.mysite.sbb.domain.answer.AnswerService;
import com.mysite.sbb.domain.category.Category;
import com.mysite.sbb.domain.category.CategoryService;
import com.mysite.sbb.domain.comment.Comment;
import com.mysite.sbb.domain.comment.CommentService;
import com.mysite.sbb.domain.user.SiteUser;
import com.mysite.sbb.domain.user.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@RequestMapping("/question")
@Controller
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final CategoryService categoryService;
    private final CommentService commentService;

    @GetMapping("/list")
    public String list(Model model,
            @RequestParam(value="page", defaultValue = "0") int page,
            @RequestParam(value="kw", defaultValue = "") String kw,
            @RequestParam(value="category",defaultValue = "0") int categoryId){
        Page<Question> paging;
        if(categoryId!=0){
            paging = questionService.getCategoryList(page,categoryId);
        } else{
            paging = questionService.getList(page,kw);
        }
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("paging",paging);
        model.addAttribute(kw);
        model.addAttribute("categoryId", categoryId);
        List<Answer> recentAnswers = answerService.recentAnswer();
        model.addAttribute("recentAnswers",recentAnswers);
        List<Comment> recentComments = commentService.recentComment();
        model.addAttribute("recentComments",recentComments);
        return "question_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model,
                         @PathVariable("id") Integer id,
                         @RequestParam(defaultValue = "0") int page,
                         AnswerForm answerForm){
        Question question = questionService.getQuestion(id);
        questionService.visited(question);
        Page<Answer> answerPage = answerService.answerPage(question,page);
        model.addAttribute("question",question);
        model.addAttribute("answerPage",answerPage);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String create(QuestionForm questionForm,
            Model model){
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories",categories);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String create(@Valid QuestionForm questionForm,
                         BindingResult bindingResult,
                         Principal principal){
        if(bindingResult.hasErrors()){
            return "question_form";
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.create(questionForm.getSubject(),questionForm.getContent(),siteUser,categoryService.findById(questionForm.getCategory()));
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm,
                                 @PathVariable("id") Integer id,
                                 Principal principal){
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정 권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm,
                                 BindingResult bindingResult,
                                 @PathVariable("id") Integer id,
                                 Principal principal){
        if(bindingResult.hasErrors()){
            return "question_form";
        }
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정 권한이 없습니다.");
        }
        questionService.modify(question,questionForm.getSubject(),questionForm.getContent());
        return String.format("redirect:/question/detail/%s",id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(@PathVariable("id") Integer id,
                                 Principal principal){
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id){
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.vote(question,siteUser);
        return String.format("redirect:/question/detail/%s",id);
    }
}
