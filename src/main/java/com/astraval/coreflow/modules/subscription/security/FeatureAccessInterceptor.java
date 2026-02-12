package com.astraval.coreflow.modules.subscription.security;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.astraval.coreflow.common.exception.SubscriptionAccessDeniedException;
import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.modules.subscription.service.SubscriptionGuardService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FeatureAccessInterceptor implements HandlerInterceptor {

    private final SubscriptionGuardService subscriptionGuardService;
    private final SecurityUtil securityUtil;

    public FeatureAccessInterceptor(SubscriptionGuardService subscriptionGuardService, SecurityUtil securityUtil) {
        this.subscriptionGuardService = subscriptionGuardService;
        this.securityUtil = securityUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresFeature requiresFeature = handlerMethod.getMethodAnnotation(RequiresFeature.class);
        if (requiresFeature == null) {
            requiresFeature = handlerMethod.getBeanType().getAnnotation(RequiresFeature.class);
        }

        RequiresActiveSubscription requiresActive = handlerMethod.getMethodAnnotation(RequiresActiveSubscription.class);
        if (requiresActive == null) {
            requiresActive = handlerMethod.getBeanType().getAnnotation(RequiresActiveSubscription.class);
        }

        if (requiresFeature == null && requiresActive == null) {
            return true;
        }

        Long companyId = resolveCompanyId(request);
        if (companyId == null) {
            companyId = securityUtil.getCurrentCompanyIdAsLong();
        }

        if (companyId == null) {
            throw new SubscriptionAccessDeniedException(
                    "Unable to resolve company context for subscription validation",
                    "SUBSCRIPTION_CONTEXT_MISSING",
                    requiresFeature != null ? requiresFeature.value() : null,
                    null,
                    null,
                    "/pricing");
        }

        if (requiresFeature != null) {
            subscriptionGuardService.assertFeatureAccess(companyId, requiresFeature.value());
            return true;
        }

        subscriptionGuardService.assertActiveSubscription(companyId);
        return true;
    }

    @SuppressWarnings("unchecked")
    private Long resolveCompanyId(HttpServletRequest request) {
        Object uriTemplateVarsObj = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(uriTemplateVarsObj instanceof Map<?, ?> uriTemplateVars)) {
            return null;
        }

        Object companyIdObj = uriTemplateVars.get("companyId");
        if (companyIdObj == null) {
            return null;
        }

        try {
            return Long.parseLong(companyIdObj.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
