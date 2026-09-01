package com.college.library.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.ServletContext;

public final class ThymeleafConfig {

    private static TemplateEngine templateEngine;

    private ThymeleafConfig() {}

    public static synchronized TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if (templateEngine == null) {
            IWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
            WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(application);
            resolver.setPrefix("/WEB-INF/templates/");
            resolver.setSuffix(".html");
            resolver.setTemplateMode(TemplateMode.HTML);
            resolver.setCharacterEncoding("UTF-8");
            resolver.setCacheable(false);

            templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(resolver);
        }
        return templateEngine;
    }
}
