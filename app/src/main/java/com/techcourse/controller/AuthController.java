package com.techcourse.controller;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @RequestMapping(value = "/login/view", method = RequestMethod.GET)
    public String loginView(final HttpServletRequest req, final HttpServletResponse res) {
        return UserSession.getUserFrom(req.getSession())
                .map(user -> {
                    log.info("logged in {}", user.getAccount());
                    return "redirect:/index.jsp";
                })
                .orElse("/login.jsp");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(final HttpServletRequest req, final HttpServletResponse res) {
        if (UserSession.isLoggedIn(req.getSession())) {
            return "redirect:/index.jsp";
        }

        final var account = req.getParameter("account");
        if (Objects.isNull(account)) {
            return "redirect:/401.jsp";
        }

        return InMemoryUserRepository.findByAccount(account)
                .map(user -> {
                    log.info("User : {}", user);
                    return login(req, user);
                })
                .orElse("redirect:/401.jsp");
    }

    private String login(final HttpServletRequest request, final User user) {
        if (user.checkPassword(request.getParameter("password"))) {
            final var session = request.getSession();
            session.setAttribute(UserSession.SESSION_KEY, user);
            return "redirect:/index.jsp";
        } else {
            return "redirect:/401.jsp";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(final HttpServletRequest req, final HttpServletResponse res) {
        final var session = req.getSession();
        session.removeAttribute(UserSession.SESSION_KEY);
        return "redirect:/";
    }
}
