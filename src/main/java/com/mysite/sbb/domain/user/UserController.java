package com.mysite.sbb.domain.user;

import com.mysite.sbb.domain.user.Form.UserCreateForm;
import com.mysite.sbb.domain.user.Form.UserPasswordFindForm;
import com.mysite.sbb.domain.user.Form.UserPasswordResetForm;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm){
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm,
                         BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "signup_form";
        }
        if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())){
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }
        try{
            userService.create(userCreateForm.getUsername(),
                    userCreateForm.getEmail(),
                    userCreateForm.getPassword1());
        }catch(DataIntegrityViolationException e){
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        }catch(Exception e){
            e.printStackTrace();
            bindingResult.reject("signupFailed",e.getMessage());
            return "signup_form";
        }
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(){
        return "login_form";
    }

    @GetMapping("/profile")
    public String profile(Principal principal,
            Model model){
        SiteUser user = userService.getUser(principal.getName());
        UserDto userDto = new UserDto(user);
        model.addAttribute(userDto);
        return "user_profile";
    }

    @GetMapping("/password")
    public String findPassword(UserPasswordFindForm userPasswordFindForm){
        return "password_find_form";
    }

    @PostMapping("/password")
    public String findPassword(@Valid UserPasswordFindForm userPasswordFindForm,
            BindingResult bindingResult,
            Model model) throws MessagingException {
        if(bindingResult.hasErrors()){
            return "password_find_form";
        }
        SiteUser user = userService.findByUsername(userPasswordFindForm.getUsername())
                .orElseThrow(()->new RuntimeException("가입된 아이디가 아닙니다."));

        if(!user.getEmail().equals(userPasswordFindForm.getEmail())){
            throw new RuntimeException("해당 아이디에 맞는 이메일이 아닙니다.");
        }

        userService.sendPasswordFindEmail(user);
        return "mail_sent";
    }

    @GetMapping("{id}/password/{token}")
    public String resetPassword(@PathVariable("id") Integer id,
            @PathVariable("token") String token,
            Model model){
        SiteUser user = userService.findByUserId(id).orElseThrow(
                ()-> new RuntimeException("잘못된 사용자입니다."));
        userService.matchToken(user,token);
        UserPasswordResetForm form = new UserPasswordResetForm();
        model.addAttribute("userPasswordResetForm", form);
        model.addAttribute("user", user);
        model.addAttribute("token",token);
        return "password_reset";
    }

    @PostMapping("{id}/password/{token}")
    public String resetPassword(@PathVariable("id") Integer id,
            @PathVariable("token") String token,
            @Valid UserPasswordResetForm userPasswordResetForm,
            BindingResult bindingResult){
        SiteUser user = userService.findByUserId(id).orElseThrow(
                ()-> new RuntimeException("잘못된 사용자입니다."));
        userService.matchToken(user,token);
        if (!userPasswordResetForm.getPassword1().equals(userPasswordResetForm.getPassword2())) {
            bindingResult.rejectValue("password2", "error.password2", "비밀번호가 일치하지 않습니다.");
            return "password_reset";
        }
        userService.resetPassword(user,userPasswordResetForm.getPassword1());
        return "password_rested";
    }
}
