package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.MemberRole;
import roomescape.global.exception.auth.AuthenticationRequiredException;
import roomescape.global.exception.auth.AuthorizationFailedException;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        LoginRequired loginRequired = findLoginRequired(handlerMethod);
        if (loginRequired == null) {
            return true;
        }
        LoginMember loginMember = sessionManager.findLoginMember(request)
                .orElseThrow(() -> new AuthenticationRequiredException("인증이 필요합니다."));
        if (requiresManagerRole(request, loginRequired, loginMember)) {
            throw new AuthorizationFailedException("관리자 권한이 필요합니다.");
        }
        request.setAttribute(SessionManager.LOGIN_MEMBER_ATTRIBUTE, loginMember);
        return true;
    }

    private LoginRequired findLoginRequired(HandlerMethod handlerMethod) {
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
    }

    private boolean requiresManagerRole(HttpServletRequest request, LoginRequired loginRequired, LoginMember loginMember) {
        return (request.getRequestURI().startsWith("/admin/") || loginRequired.managerOnly())
                && loginMember.role() != MemberRole.MANAGER;
    }
}
