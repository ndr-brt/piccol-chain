package srl.paros.piccolchain.node;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

public class StaticContentHandler implements Handler<RoutingContext> {

    private final TemplateEngine engine;
    private final String templateDirectory;
    private final String templateFileName;

    public StaticContentHandler(String templateDirectory, String templateFileName) {
        this.engine = ThymeleafTemplateEngine.create();
        this.templateDirectory = templateDirectory;
        this.templateFileName = templateFileName;
    }

    @Override
    public void handle(RoutingContext context) {
        engine.render(context, templateDirectory, templateFileName, res -> {
            if (res.succeeded()) {
                context.response().end(res.result());
            } else {
                context.fail(res.cause());
            }
        });
    }
}
