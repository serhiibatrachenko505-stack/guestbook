package sumdu.edu.ua.config;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class EmailFreemarkerConfig {

    @Bean(name = "emailFreemarkerConfiguration")
    public Configuration emailFreemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        // Templates are loaded from src/main/resources/mail-templates/
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "mail-templates");
        cfg.setDefaultEncoding("UTF-8");

        // Escape ${...} values as HTML
        cfg.setOutputFormat(HTMLOutputFormat.INSTANCE);

        // Fail fast on template errors
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);

        return cfg;
    }
}
